////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.*;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;

import java.util.List;

/**
 * A GroupMatchingIterator contains code shared between GroupStartingIterator and GroupEndingIterator
 */

public abstract class GroupMatchingIterator implements LookaheadIterator, LastPositionFinder, GroupIterator {

    protected Expression select;
    protected FocusIterator population;
    protected Pattern pattern;
    protected XPathContext baseContext;
    protected XPathContext runningContext;
    protected List<Item> currentMembers;
    /*@Nullable*/ protected Item nextItem;
    protected Item current = null;
    protected int position = 0;


    protected abstract void advance() throws XPathException;

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
            if (nextItem != null) {
                current = nextItem;
                position++;
                advance();
                return current;
            } else {
                current = null;
                position = -1;
                return null;
            }
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

