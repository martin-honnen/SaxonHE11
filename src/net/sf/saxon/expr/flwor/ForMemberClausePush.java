////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;

/**
 * This class implements the changes to the tuple stream effected by a "for" clause in a FLWOR expression
 */
public class ForMemberClausePush extends TuplePush {

    protected TuplePush destination;
    protected ForClause forClause;

    public ForMemberClausePush(Outputter outputter, TuplePush destination, ForClause forClause) {
        super(outputter);
        this.destination = destination;
        this.forClause = forClause;
    }

    /*
     * Process the next tuple.
     */
    @Override
    public void processTuple(XPathContext context) throws XPathException {
        Expression sequence = forClause.getSequence();
        ArrayItem arrayItem = (ArrayItem) sequence.evaluateItem(context);
        int pos = 0;
        for (GroundedValue member : arrayItem.members()) {
            context.setLocalVariable(forClause.getRangeVariable().getLocalSlotNumber(), member);
            if (forClause.getPositionVariable() != null) {
                context.setLocalVariable(
                        forClause.getPositionVariable().getLocalSlotNumber(),
                        new Int64Value(++pos));
            }
            destination.processTuple(context);
        }
    }

    /*
     * Close the tuple stream
     */
    @Override
    public void close() throws XPathException {
        destination.close();
    }
}

