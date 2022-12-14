////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.arrays.SquareArrayConstructor;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.TupleType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.transpile.CSharpInnerClass;
import net.sf.saxon.transpile.CSharpSuppressWarnings;
import net.sf.saxon.type.*;
import net.sf.saxon.value.Cardinality;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A lookup expression is an expression of the form A?*, where A must be a map or an array
 */

public class LookupAllExpression extends UnaryExpression {

    /**
     * Constructor
     *
     * @param lhs The left hand operand (which must always select a sequence of maps or arrays).
     */

    public LookupAllExpression(Expression lhs) {
        super(lhs);
    }

    @Override
    protected OperandRole getOperandRole() {
        return OperandRole.INSPECT;
    }


    /**
     * Determine the data type of the items returned by this expression
     *
     * @return the type of the step
     */

    /*@NotNull*/
    @Override
    public final ItemType getItemType() {
        ItemType lhs = getBaseExpression().getItemType();
        if (lhs instanceof MapType) {
            return ((MapType)lhs).getValueType().getPrimaryType();
        } else if (lhs instanceof ArrayItemType) {
            return ((ArrayItemType) lhs).getMemberType().getPrimaryType();
        } else {
            return AnyItemType.getInstance();
        }
    }


    /**
     * Get the static type of the expression as a UType, following precisely the type
     * inference rules defined in the XSLT 3.0 specification.
     *
     * @return the static item type of the expression according to the XSLT 3.0 defined rules
     * @param contextItemType The type of the context item (not used)
     */
    @Override
    public UType getStaticUType(UType contextItemType) {
        return getItemType().getUType();
    }

    /**
     * Type-check the expression
     */

    /*@NotNull*/
    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {

        Configuration config = visitor.getConfiguration();
        TypeHierarchy th = config.getTypeHierarchy();

        // Check the first operand
        getOperand().typeCheck(visitor, contextInfo);

        ItemType containerType = getBaseExpression().getItemType();
        boolean isArrayLookup = containerType instanceof ArrayItemType;
        boolean isMapLookup = containerType instanceof MapType || containerType instanceof TupleType;

        if (!isArrayLookup && !isMapLookup) {
            if (th.relationship(containerType, MapType.ANY_MAP_TYPE) == Affinity.DISJOINT &&
                    th.relationship(containerType, ArrayItemType.getInstance()) == Affinity.DISJOINT) {
                if (Cardinality.allowsZero(getBaseExpression().getCardinality())) {
                    visitor.issueWarning("The left-hand operand of '?' must be a map or an array; the expression can succeed only if the operand is an empty sequence " + containerType, getLocation());
                } else {
                    XPathException err = new XPathException("The left-hand operand of '?' must be a map or an array; the supplied expression is of type " + containerType, "XPTY0004");
                    err.setLocation(getLocation());
                    err.setIsTypeError(true);
                    err.setFailingExpression(this);
                    throw err;
                }
            }
        }

        if (getBaseExpression() instanceof Literal) {
            return new Literal(SequenceTool.toGroundedValue(iterate(visitor.makeDynamicContext())));
        }

        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        getOperand().optimize(visitor, contextItemType);

        if (getBaseExpression() instanceof Literal) {
            return new Literal(SequenceTool.toGroundedValue(iterate(visitor.makeDynamicContext())));
        }

        // See W3C bug 30228. In the interests of keeping certain tests streamable, we do a rewrite of [A,B,C]?*
        // to (A, B, C).
        if (getBaseExpression() instanceof SquareArrayConstructor) {
            List<Expression> children = new ArrayList<>();
            for (Operand o : getBaseExpression().operands()) {
                children.add(o.getChildExpression().copy(new RebindingMap()));
            }
            Expression[] childExpressions = children.toArray(new Expression[0]);
            Block block = new Block(childExpressions);
            ExpressionTool.copyLocationInfo(this, block);
            return block;
        }
        return this;
    }


    /**
     * Return the estimated cost of evaluating an expression. This is a very crude measure based
     * on the syntactic form of the expression (we have no knowledge of data values). We take
     * the cost of evaluating a simple scalar comparison or arithmetic expression as 1 (one),
     * and we assume that a sequence has length 5. The resulting estimates may be used, for
     * example, to reorder the predicates in a filter expression so cheaper predicates are
     * evaluated first.
     * @return the cost estimate
     */
    @Override
    public double getCost() {
        return getBaseExpression().getCost() + 1;
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
        return ITERATE_METHOD;
    }

    /**
     * Copy an expression. This makes a deep copy.
     *
     * @return the copy of the original expression
     * @param rebindings variables that need to be re-bound.
     */

    /*@NotNull*/
    @Override
    public LookupAllExpression copy(RebindingMap rebindings) {
        return new LookupAllExpression(getBaseExpression().copy(rebindings));
    }

    /**
     * Determine the static cardinality of the expression
     */

    @Override
    protected int computeCardinality() {
        return StaticProperty.ALLOWS_ZERO_OR_MORE;
    }

    /**
     * Is this expression the same as another expression?
     */

    public boolean equals(Object other) {
        if (!(other instanceof LookupAllExpression)) {
            return false;
        }
        LookupAllExpression p = (LookupAllExpression) other;
        return getBaseExpression().isEqual(p.getBaseExpression());
    }

    /**
     * get HashCode for comparing two expressions
     */

    @Override
    protected int computeHashCode() {
        return "LookupAll".hashCode() ^ getBaseExpression().hashCode();
    }

    /**
     * Iterate the lookup-expression in a given context
     *
     * @param context the evaluation context
     */

    /*@NotNull*/
    @Override
    @CSharpInnerClass(outer=true, extra={"Saxon.Hej.expr.XPathContext context"})
    @CSharpSuppressWarnings("UnsafeIteratorConversion")
    public SequenceIterator iterate(final XPathContext context) throws XPathException {

        return new SequenceIterator() {

            final SequenceIterator level0 = getBaseExpression().iterate(context);
            Iterator<GroundedValue> level1forArrays = null;
            Iterator<KeyValuePair> level1forMaps = null;
                // delivers GroundedValue in the case of an array, or KeyValuePair in the case of a map
            SequenceIterator level2 = null;

            @Override
            public Item next() {
                if (level2 == null) {
                    if (level1forArrays == null && level1forMaps == null) {
                        Item lhs = level0.next();
                        if (lhs == null) {
                            return null;
                        } else if (lhs instanceof ArrayItem) {
                            level1forArrays = ((ArrayItem)lhs).members().iterator();
                            return next();
                        } else if (lhs instanceof MapItem) {
                            level1forMaps = ((MapItem)lhs).keyValuePairs().iterator();
                            return next();
                        } else {
                            try {
                                LookupExpression.mustBeArrayOrMap(LookupAllExpression.this, lhs);
                            } catch (XPathException e) {
                                throw new UncheckedXPathException(e);
                            }
                            return null;
                        }
                    } else if (level1forArrays != null && level1forArrays.hasNext()) {
                        GroundedValue nextEntry = level1forArrays.next();
                        level2 = nextEntry.iterate();
                    } else if (level1forMaps != null && level1forMaps.hasNext()) {
                        KeyValuePair nextEntry = level1forMaps.next();
                        GroundedValue value = nextEntry.value;
                        level2 = value.iterate();
                    } else {
                        level1forMaps = null;
                        level1forArrays = null;
                    }
                    return next();
                } else {
                    Item nextItem = level2.next();
                    if (nextItem == null) {
                        level2 = null;
                        return next();
                    } else {
                        return nextItem;
                    }
                }
            }

            @Override
            public void close() {
                if (level0 != null) {
                    level0.close();
                }
                if (level2 != null) {
                    level2.close();
                }
            }

            @Override
            public void discharge() {
                if (level0 != null) {
                    level0.discharge();
                }
                if (level2 != null) {
                    level2.discharge();
                }
            }

        };

    }

    /**
     * Diagnostic print of expression structure. The abstract expression tree
     * is written to the supplied output destination.
     */

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("lookupAll", this);
        getBaseExpression().export(destination);
        destination.endElement();
    }

    /**
     * The toString() method for an expression attempts to give a representation of the expression
     * in an XPath-like form, but there is no guarantee that the syntax will actually be true XPath.
     * In the case of XSLT instructions, the toString() method gives an abstracted view of the syntax
     *
     * @return a representation of the expression as a string
     */

    public String toString() {
        return ExpressionTool.parenthesize(getBaseExpression()) + "?*";
    }

    @Override
    public String toShortString() {
        return getBaseExpression().toShortString() + "?*";
    }

}

