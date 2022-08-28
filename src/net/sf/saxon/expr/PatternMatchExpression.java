////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr;

import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;


/**
 * An expression corresponding to the XSLT 4.0 xsl:match instruction: returns true
 * if every item in a supplied sequence matches a supplied pattern
 */

public final class PatternMatchExpression extends Expression {

    private final Operand selectOp;
    private final Operand patternOp;

    /**
     * Construct a pattern match expression
     * @param select expression selecting the items to be tested against the pattern
     * @param pattern the pattern to test these items against
     */

    public PatternMatchExpression(Expression select, Pattern pattern) {
        selectOp = new Operand(this, select, new OperandRole(0, OperandUsage.NAVIGATION, SequenceType.ANY_SEQUENCE));
        patternOp = new Operand(this, pattern, OperandRole.INSPECT);
    }

    public Expression getSelectExpression() {
        return selectOp.getChildExpression();
    }

    public Pattern getPattern() {
        return (Pattern)patternOp.getChildExpression();
    }

    @Override
    public Iterable<Operand> operands() {
        return operandList(selectOp, patternOp);
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext c) throws XPathException {
        Pattern pattern = getPattern();
        SequenceIterator iter = getSelectExpression().iterate(c);
        Item it;
        while ((it = iter.next()) != null) {
            if (!pattern.matches(it, c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        return BooleanValue.get(effectiveBooleanValue(context));
    }

    /**
     * Determine the data type of the expression
     *
     * @return Type.BOOLEAN
     */

    /*@NotNull*/
    @Override
    public ItemType getItemType() {
        return BuiltInAtomicType.BOOLEAN;
    }

    /**
     * Determine the static cardinality
     */

    @Override
    protected int computeCardinality() {
        return StaticProperty.EXACTLY_ONE;
    }

    /**
     * Copy an expression. This makes a deep copy.
     *
     * @return the copy of the original expression
     * @param rebindings variables that need to be re-bound
     */

    /*@NotNull*/
    @Override
    public Expression copy(RebindingMap rebindings) {
        PatternMatchExpression exp = new PatternMatchExpression(
                selectOp.getChildExpression(), (Pattern)patternOp.getChildExpression());
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    /**
     * An implementation of Expression must provide at least one of the methods evaluateItem(), iterate(), or process().
     * This method indicates which of these methods is provided directly. The other methods will always be available
     * indirectly, using an implementation that relies on one of the other methods.
     *
     * @return the implementation method, for example {@link #ITERATE_METHOD} or {@link #EVALUATE_METHOD} or
     * {@link #PROCESS_METHOD}
     */
    @Override
    public int getImplementationMethod() {
        return EVALUATE_METHOD;
    }

    /**
     * Is this expression the same as another expression?
     *
     * @param other the expression to be compared with this one
     * @return true if the two expressions are statically equivalent
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof PatternMatchExpression
                && ((PatternMatchExpression)other).getSelectExpression().equals(getSelectExpression())
                && ((PatternMatchExpression)other).getPattern().equals(getSelectExpression());
    }

    /**
     * Hashcode supporting equals()
     */

    @Override
    protected int computeHashCode() {
        return getSelectExpression().computeHashCode() << 5 ^ getPattern().computeHashCode();
    }

    /**
     * Get a name identifying the kind of expression, in terms meaningful to a user.
     *
     * @return a name identifying the kind of expression, in terms meaningful to a user.
     * The name will always be in the form of a lexical XML QName, and should match the name used
     * in export() output displaying the expression.
     */
    @Override
    public String getExpressionName() {
        return "match";
    }

    /**
     * Diagnostic print of expression structure. The abstract expression tree
     * is written to the supplied output destination.
     */

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("patMatch", this);
        getSelectExpression().export(destination);
        getPattern().export(destination);
        destination.endElement();
    }

    /**
     * <p>The toString() method for an expression attempts to give a representation of the expression
     * in an XPath-like form.</p>
     * <p>For subclasses of Expression that represent XPath expressions, the result should always be a string that
     * parses as an XPath 3.0 expression.</p>
     *
     * @return a representation of the expression as a string
     */
    @Override
    public String toString() {
        return "patMatch(" + getSelectExpression().toString() + "," + getPattern().toString() + ")";
    }

    @Override
    public String toShortString() {
        return "patMatch(" + getSelectExpression().toShortString() + "," + getPattern().toShortString() + ")";

    }
}

