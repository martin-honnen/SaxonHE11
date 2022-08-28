////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.*;
import net.sf.saxon.expr.parser.*;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

import java.util.List;

import static net.sf.saxon.expr.flwor.Clause.ClauseName.FOR_MEMBER;

/**
 * A "for member" clause in a FLWOR expression  (XQuery 4.0: iterates over members of an array)
 */
public class ForMemberClause extends ForClause {

    public ForMemberClause() {
    }

    @Override
    public ClauseName getClauseKey() {
        return FOR_MEMBER;
    }

    @Override
    public ForMemberClause copy(FLWORExpression flwor, RebindingMap rebindings) {
        ForMemberClause f2 = new ForMemberClause();
        f2.setLocation(getLocation());
        f2.setPackageData(getPackageData());
        f2.rangeVariable = rangeVariable.copy();
        if (positionVariable != null) {
            f2.positionVariable = positionVariable.copy();
        }
        f2.initSequence(flwor, getSequence().copy(rebindings));
        f2.allowsEmpty = allowsEmpty;
        return f2;
    }

    /**
     * Type-check the expression
     */
    @Override
    public void typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        SequenceType decl = rangeVariable.getRequiredType();
        if (allowsEmpty && !Cardinality.allowsZero(decl.getCardinality())) {
            RoleDiagnostic emptyRole = new RoleDiagnostic(RoleDiagnostic.VARIABLE, rangeVariable.getVariableQName().getDisplayName(), 0);
            Expression checker =
                    CardinalityChecker.makeCardinalityChecker(
                            getSequence(), StaticProperty.ALLOWS_ONE_OR_MORE, emptyRole);
            setSequence(checker);
        }
        SequenceType sequenceType = SequenceType.makeSequenceType(new ArrayItemType(decl), StaticProperty.EXACTLY_ONE);
        RoleDiagnostic role = new RoleDiagnostic(RoleDiagnostic.FOR_MEMBER, rangeVariable.getVariableQName().getDisplayName(), 0);
        setSequence(
                TypeChecker.strictTypeCheck(
                        getSequence(), sequenceType, role, visitor.getStaticContext()));
    }

    /**
     * Get a tuple stream that implements the functionality of this clause, taking its
     * input from another tuple stream which this clause modifies
     *
     * @param base    the input tuple stream
     * @param context the XPath dynamic context
     * @return the output tuple stream
     */
    @Override
    public TuplePull getPullStream(TuplePull base, XPathContext context) {
        return new ForMemberClausePull(base, this);
    }

    /**
     * Get a push-mode tuple stream that implements the functionality of this clause, supplying its
     * output to another tuple stream
     *
     * @param destination the output tuple stream
     * @param output the destination for the result
     * @param context     the dynamic evaluation context
     * @return the push tuple stream that implements the functionality of this clause of the FLWOR
     *         expression
     */
    @Override
    public TuplePush getPushStream(TuplePush destination, Outputter output, XPathContext context) {
        return new ForMemberClausePush(output, destination, this);
    }

    /**
     * Convert where clause to a predicate.
     *
     * @param flwor           the FLWOR expression (sans the relevant part of the where clause)
     * @param visitor         the expression visitor
     * @param contextItemType the item type of the context item
     * @param condition       the predicate to be added. This will always be a single term (never a composite condition
     *                        using "and"), as the where clause is split into separate terms before calling this method
     * @return true if the expression has been changed, that is, if the where clause has been converted
     * @throws XPathException if an error is encountered
     */

    public boolean addPredicate(FLWORExpression flwor, ExpressionVisitor visitor, ContextItemStaticInfo contextItemType, Expression condition) throws XPathException {
        return false;
    }

    @Override
    public void refineVariableType(ExpressionVisitor visitor, List<VariableReference> references, Expression returnExpr) {
        ItemType actualItemType = getSequence().getItemType();
        if (actualItemType instanceof ArrayItemType) {
            SequenceType memberType = ((ArrayItemType) actualItemType).getMemberType();
            for (VariableReference ref : references) {
                ref.refineVariableType(memberType.getPrimaryType(), memberType.getCardinality(),
                                       null, getSequence().getSpecialProperties());
            }
        }
    }

    @Override
    public void addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet varPath = getSequence().addToPathMap(pathMap, pathMapNodeSet);
        pathMap.registerPathForVariable(rangeVariable, varPath);
    }

}

