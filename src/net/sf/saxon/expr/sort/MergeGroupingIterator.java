////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.sort;

import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ObjectValue;

import java.util.*;

/**
 * An iterator that groups the result of merging several xsl:merge input streams, identifying
 * groups of adjacent items having the same merge key value
 */

public class MergeGroupingIterator implements GroupIterator, LookaheadIterator, LastPositionFinder {

    private final SequenceIterator baseItr;
    private ObjectValue<ItemWithMergeKeys> currenti = null;
    private ObjectValue<ItemWithMergeKeys> nextItem;
    private List<Item> currentMembers;
    private Map<String, List<Item>> currentSourceMembers;
    private final Comparator<? super ObjectValue<ItemWithMergeKeys>> comparer;
    private int position = 0;
    List<AtomicValue> compositeMergeKey;
    private final LastPositionFinder lastPositionFinder;


    public MergeGroupingIterator(
            SequenceIterator p1,
            Comparator<? super ObjectValue<ItemWithMergeKeys>> comp, LastPositionFinder lpf) throws XPathException {
        this.baseItr = p1;
        nextItem = (ObjectValue<ItemWithMergeKeys>)p1.next();
        if (nextItem != null) {
            compositeMergeKey = nextItem.getObject().sortKeyValues;
        }
        this.comparer = comp;
        this.lastPositionFinder = lpf;
    }


    /**
     * The advance() method reads ahead a group of items having common merge key values. These items are
     * placed in the variable currentMembers. The variable next is left at the next item after this group,
     * or null if there are no more items
     * @throws XPathException if a failure occurs reading the input, or if merge keys are out of order, or
     * not comparable
     */
    private void advance() throws XPathException {
        currentMembers = new ArrayList<>(20);
        currentSourceMembers = new HashMap<>(20);
        Item currentItem = currenti.getObject().baseItem;
        String source = currenti.getObject().sourceName;
        currentMembers.add(currentItem);
        if (source != null) {
            List<Item> list = new ArrayList<>();
            list.add(currentItem);
            currentSourceMembers.put(source, list);
        }
        while (true) {
            ObjectValue<ItemWithMergeKeys> nextCandidate = (ObjectValue<ItemWithMergeKeys>)baseItr.next();
            if (nextCandidate == null) {
                nextItem = null;
                return;
            }

            try {
                int c = comparer.compare(currenti, nextCandidate);
                if (c == 0) {
                    currentItem = nextCandidate.getObject().baseItem;
                    source = nextCandidate.getObject().sourceName;
                    currentMembers.add(currentItem);
                    if (source != null) {
                        //noinspection Convert2Diamond
                        List<Item> list = currentSourceMembers.computeIfAbsent(source, k -> new ArrayList<Item>());
                        list.add(currentItem);
                    }
                } else if (c > 0) {
                    List<AtomicValue> keys = nextCandidate.getObject().sortKeyValues;
                    throw new XPathException(
                            "Merge input for source " + source + " is not ordered according to merge key, detected at key value: " +
                                    Arrays.toString(keys.toArray()), "XTDE2220");
                } else {
                    nextItem = nextCandidate;
                    return;
                }
            } catch (ClassCastException e) {
                XPathException err = new XPathException("Merge key values are of non-comparable types ("
                        + Type.displayTypeName(currentItem) + " and " + Type.displayTypeName(nextCandidate.getObject().baseItem) + ')', "XTTE2230");
                err.setIsTypeError(true);
                throw err;
            }

        }
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
                currenti = null;
                position = -1;
                return null;
            }
            currenti = nextItem;
            position++;
            compositeMergeKey = nextItem.getObject().sortKeyValues;
            advance();
            return currenti.getObject().baseItem;
        } catch (XPathException e) {
            throw new UncheckedXPathException(e);
        }
    }

    @Override
    public void close() {
        baseItr.close();
    }

    @Override
    public void discharge() {
        baseItr.discharge();
    }

    @Override
    public boolean supportsGetLength() {
        return true;
    }

    @Override
    public int getLength() {
        return lastPositionFinder.getLength();
    }

    @Override
    public AtomicSequence getCurrentGroupingKey() {
        return new AtomicArray(compositeMergeKey);
    }

    @Override
    public SequenceIterator iterateCurrentGroup() {
        return new ListIterator.Of<>(currentMembers);
    }

    public SequenceIterator iterateCurrentGroup(String source) {
        List<Item> sourceMembers = currentSourceMembers.get(source);
        if (sourceMembers == null) {
            return EmptyIterator.emptyIterator();
        } else {
            return new ListIterator.Of<>(sourceMembers);
        }
    }


}


