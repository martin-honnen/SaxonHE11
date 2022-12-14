////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.flwor;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;

/**
 * This class implements the changes to the tuple stream effected by a "for" clause in a FLWOR expression
 * where "allowing empty" is specified
 */
public class ForClauseOuterPush extends TuplePush {

    protected TuplePush destination;
    protected ForClause forClause;

    public ForClauseOuterPush(Outputter outputter, TuplePush destination, ForClause forClause) {
        super(outputter);
        this.destination = destination;
        this.forClause = forClause;
    }

    /*
     * Process the next tuple.
     */
    @Override
    public void processTuple(XPathContext context) throws XPathException {
        SequenceIterator iter = forClause.getSequence().iterate(context);
        int pos = 0;
        Item next = iter.next();
        if (next == null) {
            context.setLocalVariable(forClause.getRangeVariable().getLocalSlotNumber(), EmptySequence.getInstance());
            if (forClause.getPositionVariable() != null) {
                context.setLocalVariable(forClause.getPositionVariable().getLocalSlotNumber(), Int64Value.ZERO);
            }
            destination.processTuple(context);
        } else {
            while (true) {
                context.setLocalVariable(forClause.getRangeVariable().getLocalSlotNumber(), next);
                if (forClause.getPositionVariable() != null) {
                    context.setLocalVariable(
                            forClause.getPositionVariable().getLocalSlotNumber(),
                            new Int64Value(++pos));
                }
                destination.processTuple(context);
                next = iter.next();
                if (next == null) {
                    break;
                }
            }
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

