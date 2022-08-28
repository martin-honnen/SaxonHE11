////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.parser;

import net.sf.saxon.expr.*;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.hof.FunctionLiteral;
import net.sf.saxon.functions.hof.PartialApply;
import net.sf.saxon.functions.hof.UnresolvedXQueryFunctionItem;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.functions.registry.XPath31FunctionSet;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryParser;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.*;
import net.sf.saxon.value.*;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Dummy Parser extension for syntax in XPath that is accepted only in particular product variants.
 * Originally, this meant XPath 3.0 syntax associated with higher-order functions. It now covers
 * Saxon syntax extensions and XQuery Update.
 */
public class ParserExtension {

    // TODO: methods concerned with higher-order function syntax can be integrated back into XPathParser


    public ParserExtension() {
    }



    private void needExtension(XPathParser p, String what) throws XPathException {
        p.grumble(what + " require support for Saxon extensions, available in Saxon-PE or higher");
    }

    private void needUpdate(XPathParser p, String what) throws XPathException {
        p.grumble(what + " requires support for XQuery Update, available in Saxon-EE or higher");
    }


    /**
     * Parse a literal function item (function#arity). On entry, the function name and
     * the '#' token have already been read
     *
     * @param p the parser
     * @return an expression representing the function value
     * @throws XPathException in the event of a syntax error
     */

    public Expression parseNamedFunctionReference(XPathParser p) throws XPathException {

        Tokenizer t = p.getTokenizer();
        String fname = t.currentTokenValue;
        int offset = t.currentTokenStartOffset;

        StaticContext env = p.getStaticContext();

        // the "#" has already been read by the Tokenizer: now parse the arity

        p.nextToken();
        p.expect(Token.NUMBER);
        NumericValue number = NumericValue.parseNumber(t.currentTokenValue);
        if (!(number instanceof IntegerValue)) {
            p.grumble("Number following '#' must be an integer");
        }
        if (number.compareTo(0) < 0 || number.compareTo(Integer.MAX_VALUE) > 0) {
            p.grumble("Number following '#' is out of range", "FOAR0002");
        }
        int arity = (int) number.longValue();
        p.nextToken();
        StructuredQName functionName = null;

        try {
            functionName = p.getQNameParser().parse(fname, env.getDefaultFunctionNamespace());
            if (functionName.getPrefix().equals("")) {
                if (XPathParser.isReservedFunctionName31(functionName.getLocalPart())) {
                    p.grumble("The unprefixed function name '" + functionName.getLocalPart() + "' is reserved in XPath 3.1");
                }
            }
        } catch (XPathException e) {
            p.grumble(e.getMessage(), e.getErrorCodeLocalPart());
            assert functionName != null;
        }

        Function fcf = null;
        try {
            FunctionLibrary lib = env.getFunctionLibrary();
            SymbolicName.F sn = new SymbolicName.F(functionName, arity);
            fcf = lib.getFunctionItem(sn, env);
            if (fcf == null) {
                p.grumble("Function " + functionName.getEQName() + "#" + arity + " not found", "XPST0017", offset);
            }
        } catch (XPathException e) {
            p.grumble(e.getMessage(), "XPST0017", offset);
        }

        // Special treatment of functions in the system function library that depend on dynamic context; turn these
        // into calls on function-lookup()

        if (functionName.hasURI(NamespaceConstant.FN) && fcf instanceof SystemFunction) {
            final BuiltInFunctionSet.Entry details = ((SystemFunction) fcf).getDetails();
            if (details != null &&
                    (details.properties & (BuiltInFunctionSet.FOCUS | BuiltInFunctionSet.DEPENDS_ON_STATIC_CONTEXT)) != 0) {
                // For a context-dependent function, return a call on function-lookup(), which saves the context
                SystemFunction lookup = XPath31FunctionSet.getInstance().makeFunction("function-lookup", 2);
                lookup.setRetainedStaticContext(env.makeRetainedStaticContext());
                return lookup.makeFunctionCall(Literal.makeLiteral(new QNameValue(functionName, BuiltInAtomicType.QNAME)),
                                               Literal.makeLiteral(Int64Value.makeIntegerValue(arity)));
            }
        }

        Expression ref = makeNamedFunctionReference(functionName, fcf);
        p.setLocation(ref, offset);
        return ref;
    }

    private static Expression makeNamedFunctionReference(StructuredQName functionName, Function fcf) {
        if (fcf instanceof UserFunction && !functionName.hasURI(NamespaceConstant.XSLT)) {
            // This case is treated specially because a UserFunctionReference in XSLT can be redirected
            // at link time to an overriding function. However, this doesn't apply to xsl:original
            return new UserFunctionReference((UserFunction) fcf);
        } else if (fcf instanceof UnresolvedXQueryFunctionItem) {
            return ((UnresolvedXQueryFunctionItem) fcf).getFunctionReference();
        } else {
            return new FunctionLiteral(fcf);
        }
    }

    /**
     * Parse the item type used for function items (XQuery 3.0 higher order functions)
     * Syntax (changed by WG decision on 2009-09-22):
     * function '(' '*' ') |
     * function '(' (SeqType (',' SeqType)*)? ')' 'as' SeqType
     * For backwards compatibility with Saxon 9.2 we allow the "*" to be omitted for the time being
     * The "function(" has already been read
     *
     * @param p           the XPath parser
     * @param annotations the list of annotation assertions for this function item type
     * @return the ItemType after parsing
     * @throws XPathException if a static error is found
     */

    public ItemType parseFunctionItemType(XPathParser p, AnnotationList annotations) throws XPathException {
        Tokenizer t = p.getTokenizer();
        p.nextToken();
        List<SequenceType> argTypes = new ArrayList<>(3);
        SequenceType resultType;

        if (t.currentToken == Token.STAR || t.currentToken == Token.MULT) {
            // Allow both to be safe
            p.nextToken();
            p.expect(Token.RPAR);
            p.nextToken();
            if (annotations.isEmpty()) {
                return AnyFunctionType.getInstance();
            } else {
                return new AnyFunctionTypeWithAssertions(annotations, p.getStaticContext().getConfiguration());
            }
        } else {
            while (t.currentToken != Token.RPAR) {
                SequenceType arg = p.parseSequenceType();
                argTypes.add(arg);
                if (t.currentToken == Token.RPAR) {
                    break;
                } else if (t.currentToken == Token.COMMA) {
                    p.nextToken();
                } else {
                    p.grumble("Expected ',' or ')' after function argument type, found '" +
                                      Token.tokens[t.currentToken] + '\'');
                }
            }
            p.nextToken();
            if (t.currentToken == Token.AS) {
                p.nextToken();
                resultType = p.parseSequenceType();
                SequenceType[] argArray = new SequenceType[argTypes.size()];
                argArray = argTypes.toArray(argArray);
                return new SpecificFunctionType(argArray, resultType, annotations);
            } else if (!argTypes.isEmpty()) {
                p.grumble("Result type must be given if an argument type is given: expected 'as (type)'");
                return null;
            } else {
                p.grumble("function() is no longer allowed for a general function type: must be function(*)");
                return null;
                // in the new syntax adopted on 2009-09-22, this case is an error
                // return AnyFunctionType.getInstance();
            }
        }
    }

    /**
     * Parse an ItemType within a SequenceType
     *
     * @param p the XPath parser
     * @return the ItemType after parsing
     * @throws XPathException if a static error is found
     */

    public ItemType parseExtendedItemType(XPathParser p) throws XPathException {
        return null;
    }


    /**
     * Parse an extended XSLT pattern in the form ~itemType (predicate)*
     *
     * @param p the XPath parser
     * @return the equivalent expression in the form .[. instance of type] (predicate)*
     * @throws XPathException if a static error is found
     */

    public Expression parseTypePattern(XPathParser p) throws XPathException {
        needExtension(p, "type-based patterns");
        return null;
    }


    public static class TemporaryXSLTVariableBinding implements LocalBinding {
        SourceBinding declaration;

        public TemporaryXSLTVariableBinding(SourceBinding decl) {
            this.declaration = decl;
        }

        @Override
        public SequenceType getRequiredType() {
            return declaration.getInferredType(true);
        }

        @Override
        public Sequence evaluateVariable(XPathContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isGlobal() {
            return false;
        }


        @Override
        public boolean isAssignable() {
            return false;
        }

        @Override
        public int getLocalSlotNumber() {
            return 0;
        }

        @Override
        public StructuredQName getVariableQName() {
            return declaration.getVariableQName();
        }

        @Override
        public void addReference(VariableReference ref, boolean isLoopingReference) {

        }

        @Override
        public IntegerValue[] getIntegerBoundsForVariable() {
            return null;
        }

        @Override
        public void setIndexedVariable() {
        }

        @Override
        public boolean isIndexedVariable() {
            return false;
        }
    }

    public Expression parseDotFunction(XPathParser p) throws XPathException {
        needExtension(p, "Dot functions");
        return null;
    }

    public Expression parseUnderscoreFunction(XPathParser p) throws XPathException {
        needExtension(p, "Underscore functions");
        return null;
    }

    public Expression bindNumericParameterReference(XPathParser p) throws XPathException {
        needExtension(p, "Underscore functions");
        return null;
    }

    /**
     * Process a function call in which one or more of the argument positions are
     * represented as "?" placemarkers (indicating partial application or currying)
     *
     * @param parser       the XPath parser
     * @param offset       offset in the query source of the start of the expression
     * @param name         the function call (as if there were no currying)
     * @param args         the arguments (with EmptySequence in the placemarker positions)
     * @param placeMarkers the positions of the placemarkers    @return the curried function
     * @return the curried function
     * @throws XPathException if a dynamic error occurs
     */

    public Expression makeCurriedFunction(
            XPathParser parser, int offset,
            StructuredQName name, Expression[] args, IntSet placeMarkers) throws XPathException {
        StaticContext env = parser.getStaticContext();
        FunctionLibrary lib = env.getFunctionLibrary();
        SymbolicName.F sn = new SymbolicName.F(name, args.length);
        Function target = lib.getFunctionItem(sn, env);
        if (target == null) {
            // This will not happen in XQuery; instead, a dummy function will be created in the
            // UnboundFunctionLibrary in case it's a forward reference to a function not yet compiled
            List<String> reasons = new ArrayList<>();
            return parser.reportMissingFunction(offset, name, args, reasons);
        }
        Expression targetExp = makeNamedFunctionReference(name, target);
        parser.setLocation(targetExp, offset);
        return curryFunction(targetExp, args, placeMarkers);
    }

    /**
     * Process a function expression in which one or more of the argument positions are
     * represented as "?" placemarkers (indicating partial application or currying)
     *
     * @param functionExp  an expression that returns the function to be curried
     * @param args         the arguments (with EmptySequence in the placemarker positions)
     * @param placeMarkers the positions of the placemarkers
     * @return the curried function
     */

    public static Expression curryFunction(Expression functionExp, Expression[] args, IntSet placeMarkers) {
        IntIterator ii = placeMarkers.iterator();
        while (ii.hasNext()) {
            args[ii.next()] = null;
        }
        return new PartialApply(functionExp, args);
    }



    public Expression createDynamicCurriedFunction(
            XPathParser p, Expression functionItem, ArrayList<Expression> args, IntSet placeMarkers) {
        Expression[] arguments = new Expression[args.size()];
        arguments = args.toArray(arguments);
        Expression result = curryFunction(functionItem, arguments, placeMarkers);
        p.setLocation(result, p.getTokenizer().currentTokenStartOffset);
        return result;
    }

    public void handleExternalFunctionDeclaration(XQueryParser p, XQueryFunction func) throws XPathException {
        needExtension(p, "External function declarations");
    }

    /**
     * Parse a type alias declaration. Allowed only in Saxon-PE and higher
     *
     * @param p the XPath parser
     * @throws XPathException if parsing fails
     */

    public void parseTypeAliasDeclaration(XQueryParser p) throws XPathException {
        needExtension(p, "Type alias declarations");
    }

    /**
     * Parse the "declare revalidation" declaration.
     * Syntax: not allowed unless XQuery update is in use
     *
     * @param p the XPath parser
     * @throws XPathException if the syntax is incorrect, or is not allowed in this XQuery processor
     */

    public void parseRevalidationDeclaration(XQueryParser p) throws XPathException {
        needUpdate(p, "A revalidation declaration");
    }

    /**
     * Parse an updating function declaration (allowed in XQuery Update only)
     *
     * @param p the XPath parser
     * @throws XPathException if parsing fails PathMapor if updating functions are not allowed
     */

    public void parseUpdatingFunctionDeclaration(XQueryParser p) throws XPathException {
        needUpdate(p, "An updating function");
    }

    protected Expression parseExtendedExprSingle(XPathParser p) throws XPathException {
        return null;
    }

    /**
     * Parse a for-member expression (Saxon extension):
     * for member $x in expr return expr
     *
     * @param p the XPath parser
     * @return the resulting subexpression
     * @throws XPathException if any error is encountered
     */

    protected Expression parseForMemberExpression(XPathParser p) throws XPathException {
        return null;
    }
}
