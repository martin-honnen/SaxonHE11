////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.parser.*;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.IntegerValue;

/**
 * A ForMemberExpression maps an expression over the members of an array. This is a Saxon
 * extension introduced in Saxon 10.0. The range variable is bound to each member of the
 * array in turn (which may itself be any sequence), and the action expression is evaluated
 * with that variable in scope; the results of the evaluations of the action expression
 * are sequence-concatenated.
 */


public class ForMemberExpression extends Assignation {

    /**
     * Create a "for member" expression (for member $x at $p in ARRAY return ACTION)
     */

    public ForMemberExpression() {
    }

    /**
     * Get a name identifying the kind of expression, in terms meaningful to a user.
     *
     * @return a name identifying the kind of expression, in terms meaningful to a user.
     * The name will always be in the form of a lexical XML QName, and should match the name used
     * in explain() output displaying the expression.
     */

    @Override
    public String getExpressionName() {
        return "forMember";
    }


    /**
     * Type-check the expression
     */

    /*@NotNull*/
    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {

        // The order of events is critical here. First we ensure that the type of the
        // sequence expression is established. This is used to establish the type of the variable,
        // which in turn is required when type-checking the action part.

        getSequenceOp().typeCheck(visitor, contextInfo);
        RoleDiagnostic role = new RoleDiagnostic(RoleDiagnostic.MISC, "'in' clause of for-member", 0);
        Expression operand = visitor.getConfiguration().getTypeChecker(false).staticTypeCheck(
                getSequence(), ArrayItemType.SINGLE_ARRAY,
                role, visitor);
        setSequence(operand);

        if (Literal.isEmptySequence(getAction())) {
            return getAction();
        }
        getActionOp().typeCheck(visitor, contextInfo);
        return this;
    }

    /**
     * For an expression that returns an integer or a sequence of integers, get
     * a lower and upper bound on the values of the integers that may be returned, from
     * static analysis. The default implementation returns null, meaning "unknown" or
     * "not applicable". Other implementations return an array of two IntegerValue objects,
     * representing the lower and upper bounds respectively. The values
     * UNBOUNDED_LOWER and UNBOUNDED_UPPER are used by convention to indicate that
     * the value may be arbitrarily large. The values MAX_STRING_LENGTH and MAX_SEQUENCE_LENGTH
     * are used to indicate values limited by the size of a string or the size of a sequence.
     *
     * @return the lower and upper bounds of integer values in the result, or null to indicate
     * unknown or not applicable.
     */
    /*@Nullable*/
    @Override
    public IntegerValue[] getIntegerBounds() {
        return getAction().getIntegerBounds();
    }


    /**
     * Copy an expression. This makes a deep copy.
     *
     * @param rebindings variables that need to be re-bound
     * @return the copy of the original expression
     */

    /*@NotNull*/
    @Override
    public Expression copy(RebindingMap rebindings) {
        ForMemberExpression forExp = new ForMemberExpression();
        ExpressionTool.copyLocationInfo(this, forExp);
        forExp.setRequiredType(requiredType);
        forExp.setVariableQName(variableName);
        forExp.setSequence(getSequence().copy(rebindings));
        //rebindings.put(this, forExp);
        Expression newAction = getAction().copy(rebindings);
        forExp.setAction(newAction);
        forExp.variableName = variableName;
        forExp.slotNumber = slotNumber;
        ExpressionTool.rebindVariableReferences(newAction, this, forExp);
        return forExp;
    }

    /**
     * Determine whether this is a vacuous expression as defined in the XQuery update specification
     *
     * @return true if this expression is vacuous
     */

    @Override
    public boolean isVacuousExpression() {
        return getAction().isVacuousExpression();
    }

    /**
     * An implementation of Expression must provide at least one of the methods evaluateItem(), iterate(), or process().
     * This method indicates which of these methods is provided. This implementation provides both iterate() and
     * process() methods natively.
     */

    @Override
    public int getImplementationMethod() {
        return ITERATE_METHOD | PROCESS_METHOD;
    }

    /**
     * Check that any elements and attributes constructed or returned by this expression are acceptable
     * in the content model of a given complex type. It's always OK to say yes, since the check will be
     * repeated at run-time. The process of checking element and attribute constructors against the content
     * model of a complex type also registers the type of content expected of those constructors, so the
     * static validation can continue recursively.
     */

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        getAction().checkPermittedContents(parentType, false);
    }

    /**
     * Iterate over the sequence of values
     */

    /*@NotNull*/
    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        int slot = getLocalSlotNumber();
        ArrayItem theArray = ((ArrayItem)getSequence().evaluateItem(context));
        int length = theArray.arrayLength();
        AscendingRangeIterator base = new AscendingRangeIterator(0, 1, length-1);
        return MappingIterator.map(base, index -> {
            Sequence member = theArray.get((int)((IntegerValue)index).longValue());
            context.setLocalVariable(slot, member);
            return getAction().iterate(context);
        });
    }

    /**
     * Process this expression as an instruction, writing results to the current
     * outputter
     */

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        int slot = getLocalSlotNumber();
        ArrayItem theArray = ((ArrayItem) getSequence().evaluateItem(context));
        for (GroundedValue member : theArray.members()) {
            context.setLocalVariable(slot, member);
            getAction().process(output, context);
        }
    }

    /**
     * Evaluate an updating expression, adding the results to a Pending Update List.
     * The default implementation of this method, which is used for non-updating expressions,
     * throws an UnsupportedOperationException
     *
     * @param context the XPath dynamic evaluation context
     * @param pul     the pending update list to which the results should be written
     */

    @Override
    public void evaluatePendingUpdates(XPathContext context, PendingUpdateList pul) throws XPathException {
        int slot = getLocalSlotNumber();
        ArrayItem theArray = ((ArrayItem) getSequence().evaluateItem(context));
        for (GroundedValue member : theArray.members()) {
            context.setLocalVariable(slot, member);
            getAction().evaluatePendingUpdates(context, pul);
        }
    }

    /**
     * Determine the data type of the items returned by the expression, if possible
     *
     * @return one of the values Type.STRING, Type.BOOLEAN, Type.NUMBER, Type.NODE,
     * or Type.ITEM (meaning not known in advance)
     */

    /*@NotNull*/
    @Override
    public ItemType getItemType() {
        return getAction().getItemType();
    }

    /**
     * Get the static type of the expression as a UType, following precisely the type
     * inference rules defined in the XSLT 3.0 specification.
     *
     * @param contextItemType the static type of the context item
     * @return the static item type of the expression according to the XSLT 3.0 defined rules
     */
    @Override
    public UType getStaticUType(UType contextItemType) {
        return getAction().getStaticUType(contextItemType);
    }

    /**
     * Determine the static cardinality of the expression
     */

    @Override
    protected int computeCardinality() {
        return StaticProperty.ALLOWS_ZERO_OR_MORE;
    }

    /**
     * The toString() method for an expression attempts to give a representation of the expression
     * in an XPath-like form, but there is no guarantee that the syntax will actually be true XPath.
     * In the case of XSLT instructions, the toString() method gives an abstracted view of the syntax
     *
     * @return a representation of the expression as a string
     */

    public String toString() {
        return "for member $" + getVariableEQName() +
                " in " + (getSequence() == null ? "(...)" : getSequence().toString()) +
                " return " + (getAction() == null ? "(...)" : ExpressionTool.parenthesize(getAction()));
    }

    @Override
    public String toShortString() {
        return "for member $" + getVariableQName().getDisplayName() +
                " in " + (getSequence() == null ? "(...)" : getSequence().toShortString()) +
                " return " + (getAction() == null ? "(...)" : getAction().toShortString());
    }

    /**
     * Diagnostic print of expression structure. The abstract expression tree
     * is written to the supplied output destination.
     */

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("forMember", this);
        out.emitAttribute("var", getVariableQName());
        out.emitAttribute("slot", "" + getLocalSlotNumber());
        out.setChildRole("in");
        getSequence().export(out);
        out.setChildRole("return");
        getAction().export(out);
        out.endElement();
    }



    @Override
    public String getStreamerName() {
        return "ForMemberExpression";
    }


}

// Copyright (c) 2018-2022 Saxonica Limited

