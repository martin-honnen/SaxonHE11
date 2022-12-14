////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;

/**
 * This class implements the changes to the tuple stream effected by a "for" clause in a FLWOR expression
 */
public class ForClausePull extends TuplePull {

    protected TuplePull base;
    protected ForClause forClause;
    /*@Nullable*/ protected FocusIterator currentIteration;
    // We use a FocusIterator because it does the position counting for us

    public ForClausePull(TuplePull base, ForClause forClause) {
        this.base = base;
        this.forClause = forClause;
    }

    /**
     * Move on to the next tuple. Before returning, this method must set all the variables corresponding
     * to the "returned" tuple in the local stack frame associated with the context object
     *
     * @param context the dynamic evaluation context
     * @return true if another tuple has been generated; false if the tuple stream is exhausted. If the
     *         method returns false, the values of the local variables corresponding to this tuple stream
     *         are undefined.
     */
    @Override
    public boolean nextTuple(XPathContext context) throws XPathException {
        while (true) {
            if (currentIteration == null) {
                if (!base.nextTuple(context)) {
                    return false;
                }
                currentIteration = new FocusTrackingIterator(getIterator(context));
            }
            Item next = currentIteration.next();
            if (next != null) {
                context.setLocalVariable(
                        forClause.getRangeVariable().getLocalSlotNumber(),
                        variableValue(next));
                if (forClause.getPositionVariable() != null) {
                    context.setLocalVariable(
                            forClause.getPositionVariable().getLocalSlotNumber(),
                            new Int64Value(currentIteration.position()));
                }
                return true;
            } else {
                currentIteration = null;
            }
        }
    }

    /**
     * Get an iterator over the items or members in the target collection.
     * @param context dynamic evaluation context
     * @return for a simple "for $x" clause, the sequence of items to which $x will be bound.
     * For a "for member ??x" clause, the sequence of array members, each wrapped as an object value
     * @throws XPathException if evaluation fails
     */
    protected SequenceIterator getIterator(XPathContext context) throws XPathException {
        return forClause.getSequence().iterate(context);
    }

    /**
     * Get the value to be bound to the iteration variable
     * @param item the value returned by the sequence iterator
     * @return the value to be assigned to the iteration variable
     */
    protected GroundedValue variableValue(Item item) {
        return item;
    }

    /**
     * Close the tuple stream, indicating that although not all tuples have been read,
     * no further tuples are required and resources can be released
     */
    @Override
    public void close() {
        base.close();
        if (currentIteration != null) {
            currentIteration.close();
        }
    }
}

