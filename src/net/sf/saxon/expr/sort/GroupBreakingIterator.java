////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceExtent;

import java.util.ArrayList;
import java.util.List;

/**
 * A GroupBreakingIterator iterates over a sequence of groups defined by
 * xsl:for-each-group break-when="x". The groups are returned in
 * order of first appearance.
 */

public class GroupBreakingIterator implements LookaheadIterator, GroupIterator {

    private final Expression select;
    private final FocusIterator population;
    private final Function breakWhen;
    private final XPathContext baseContext;
    private final XPathContext runningContext;
    private List<Item> currentMembers;
    private Item nextItem;
    private Item current = null;
    private int position = 0;


    public GroupBreakingIterator(Expression select, Function breakWhen,
                                 XPathContext baseContext)
            throws XPathException {
        this.select = select;
        this.breakWhen = breakWhen;
        this.baseContext = baseContext;
        this.runningContext = baseContext.newMinorContext();
        this.population = runningContext.trackFocus(select.iterate(baseContext));
        nextItem = population.next();
    }

    private void advance() throws XPathException {
        currentMembers = new ArrayList<>(20);
        currentMembers.add(current);
        while (true) {
            Item nextCandidate = population.next();
            if (nextCandidate == null) {
                break;
            }
            BooleanValue result = (BooleanValue)breakWhen.call(runningContext,
                                                               new Sequence[]{
                                                                       SequenceExtent.makeSequenceExtent(currentMembers),
                                                               nextCandidate});
            try {
                if (!result.getBooleanValue()) {
                    currentMembers.add(nextCandidate);
                } else {
                    nextItem = nextCandidate;
                    return;
                }
            } catch (ClassCastException e) {
                String message = "Grouping key values are of non-comparable types";
                XPathException err = new XPathException(message);
                err.setIsTypeError(true);
                err.setXPathContext(runningContext);
                throw err;
            }
        }
        nextItem = null;
    }

    @Override
    public AtomicSequence getCurrentGroupingKey() {
        return null;
    }

    @Override
    public SequenceIterator iterateCurrentGroup() {
        return new ListIterator.Of<>(currentMembers);
    }

    @Override
    public boolean supportsHasNext() {
        return true;
    }

    @Override
    public boolean hasNext() {
        return nextItem != null;
    }

    @Override
    public Item next() {
        try {
            if (nextItem == null) {
                current = null;
                position = -1;
                return null;
            }
            current = nextItem;
            position++;
            advance();
            return current;
        } catch (XPathException e) {
            throw new UncheckedXPathException(e);
        }
    }

    @Override
    public void close() {
        population.close();
    }

    @Override
    public void discharge() {
        population.discharge();
    }



}

