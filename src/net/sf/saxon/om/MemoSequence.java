////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.om;

import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.transpile.CSharpSimpleEnum;
import net.sf.saxon.tree.iter.*;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;

import java.util.Arrays;

/**
 * A Sequence implementation that represents a lazy evaluation of a supplied iterator. Items
 * are read from the base iterator only when they are first required, and are then remembered
 * within the local storage of the MemoSequence, which eventually (if the sequence is read
 * to the end) contains the entire value.
 */

public class MemoSequence implements Sequence {

    private final SequenceIterator inputIterator;

    private Item[] reservoir = null;
    private int used;

    @CSharpSimpleEnum
    private enum State {
        // State in which no items have yet been read
        UNREAD,
        // State in which zero or more items are in the reservoir and it is not known
        // whether more items exist
        MAYBE_MORE,
        // State in which all the items are in the reservoir
        ALL_READ,
        // State in which we are getting the base iterator. If the closure is called in this state,
        // it indicates a recursive entry, which is only possible on an error path
        BUSY,
        // State in which we know that the value is an empty sequence
        EMPTY}

    private State state = State.UNREAD;

    public MemoSequence(SequenceIterator iterator) {
        this.inputIterator = iterator;
    }

    @Override
    public Item head() throws XPathException {
        return iterate().next();
    }


    @Override
    public synchronized SequenceIterator iterate() {

        switch (state) {
            case UNREAD:
                state = State.BUSY;
                if (inputIterator instanceof EmptyIterator) {
                    state = State.EMPTY;
                    return inputIterator;
                }
                reservoir = new Item[50];
                used = 0;
                state = State.MAYBE_MORE;
                return new ProgressiveIterator(this);

            case MAYBE_MORE:
                return new ProgressiveIterator(this);

            case ALL_READ:
                switch (used) {
                    case 0:
                        state = State.EMPTY;
                        return EmptyIterator.emptyIterator();
                    case 1:
                        assert reservoir != null;
                        return SingletonIterator.makeIterator(reservoir[0]);
                    default:
                        return new ArrayIterator.Of<>(reservoir, 0, used);
                }

            case BUSY:
                // recursive entry: can happen if there is a circularity involving variable and function definitions
                // Can also happen if variable evaluation is attempted in a debugger, hence the cautious message
                XPathException de = new XPathException("Attempt to access a variable while it is being evaluated");
                de.setErrorCode("XTDE0640");
                //de.setXPathContext(context);
                throw new UncheckedXPathException(de);

            case EMPTY:
                return EmptyIterator.emptyIterator();

            default:
                throw new IllegalStateException("Unknown iterator state");

        }
    }

    /**
     * Get the Nth item in the sequence (0-based), reading new items into the internal reservoir if necessary
     * @param n the index of the required item
     * @return the Nth item if it exists, or null otherwise
     * @throws XPathException if the input sequence cannot be read
     */

    public synchronized Item itemAt(int n) throws XPathException {
        if (n < 0) {
            return null;
        }
        if (reservoir != null && n < used) {
            return reservoir[n];
        }
        if (state == State.ALL_READ || state == State.EMPTY) {
            return null;
        }
        if (state == State.UNREAD) {
            Item item = inputIterator.next();
            if (item == null) {
                state = State.EMPTY;
                return null;
            } else {
                state = State.MAYBE_MORE;
                reservoir = new Item[50];
                append(item);
                if (n == 0) {
                    return item;
                }
            }
        }
        // We have read some items from the input sequence but not enough. Read as many more as are needed.
        int diff = n - used + 1;
        while (diff-- > 0) {
            Item i = inputIterator.next();
            if (i == null) {
                state = State.ALL_READ;
                condense();
                return null;
            }
            append(i);
            state = State.MAYBE_MORE;
        }
        //noinspection ConstantConditions
        return reservoir[n];

    }


    /**
     * Append an item to the reservoir
     *
     * @param item the item to be added
     */

    private void append(Item item) {
        assert reservoir != null;
        if (used >= reservoir.length) {
            reservoir = Arrays.copyOf(reservoir, used * 2);
        }
        reservoir[used++] = item;
    }

    /**
     * Release unused space in the reservoir (provided the amount of unused space is worth reclaiming)
     */

    private void condense() {
        if (reservoir != null && reservoir.length - used > 30) {
            reservoir = Arrays.copyOf(reservoir, used);
        }
    }


    /**
     * A ProgressiveIterator starts by reading any items already held in the reservoir;
     * when the reservoir is exhausted, it reads further items from the inputIterator,
     * copying them into the reservoir as they are read.
     */

    public final static class ProgressiveIterator
            implements SequenceIterator, LastPositionFinder, GroundedIterator {

        private MemoSequence container;
        private int position = -1;  // zero-based position in the reservoir of the
        // item most recently read

        /**
         * Create a ProgressiveIterator
         * @param container the containing MemoSequence
         */

        public ProgressiveIterator(MemoSequence container) {
            this.container = container;
        }

        /**
         * Get the containing MemoSequence
         * @return the containing MemoSequence
         */

        public MemoSequence getMemoSequence() {
            return container;
        }

        /*@Nullable*/
        @Override
        public Item next() {
            synchronized (container) {
                // synchronized for the case where a multi-threaded xsl:for-each is reading the variable
                if (position == -2) {   // means we've already returned null once, keep doing so if called again.
                    return null;
                }
                if (++position < container.used) {
                    assert container.reservoir != null;
                    return container.reservoir[position];
                } else if (container.state == State.ALL_READ) {
                    // someone else has read the input to completion in the meantime
                    position = -2;
                    return null;
                } else {
                    assert container.inputIterator != null;
                    Item i = container.inputIterator.next();
                    if (i == null) {
                        container.state = State.ALL_READ;
                        container.condense();
                        position = -2;
                        return null;
                    }
                    position = container.used;
                    container.append(i);
                    container.state = State.MAYBE_MORE;
                    return i;
                }
            }
        }

        @Override
        public boolean supportsGetLength() {
            return true;
        }

        /**
         * Get the last position (that is, the number of items in the sequence)
         */

        @Override
        public int getLength() {
            if (container.state == State.ALL_READ) {
                return container.used;
            } else if (container.state == State.EMPTY) {
                return 0;
            } else {
                // save the current position
                int savePos = position;
                // fill the reservoir
                while (next() != null) {}
                // reset the current position
                position = savePos;
                // return the total number of items
                return container.used;
            }
        }

        public boolean isActuallyGrounded() {
            return true;
        }

        /**
         * Return a value containing all the items in the sequence returned by this
         * SequenceIterator
         *
         * @return the corresponding value
         */

        /*@Nullable*/
        @Override
        public GroundedValue materialize() {
            if (container.state == State.ALL_READ) {
                return makeExtent();
            } else if (container.state == State.EMPTY) {
                return EmptySequence.getInstance();
            } else {
                // save the current position
                int savePos = position;
                // fill the reservoir
                while (next() != null) {
                }
                // reset the current position
                position = savePos;
                // return all the items
                return makeExtent();
            }
        }

        private GroundedValue makeExtent() {
            if (container.used == container.reservoir.length) {
                if (container.used == 0) {
                    return EmptySequence.getInstance();
                } else if (container.used == 1) {
                    return container.reservoir[0];
                } else {
                    return new SequenceExtent.Of<Item>(container.reservoir);
                }
            } else {
                return SequenceExtent.makeSequenceExtent(
                        Arrays.asList(container.reservoir).subList(0, container.used));
            }
        }

        @Override
        public GroundedValue getResidue() {
            if (container.state == State.EMPTY || position >= container.used || position == -2) {
                return EmptySequence.getInstance();
            } else if (container.state == State.ALL_READ) {
                return SequenceExtent.makeSequenceExtent(
                        Arrays.asList(container.reservoir).subList(position + 1, container.used));
            } else {
                // save the current position
                int savePos = position;
                // fill the reservoir
                while (next() != null) {
                }
                // reset the current position
                position = savePos;
                // return all the items
                return SequenceExtent.makeSequenceExtent(
                        Arrays.asList(container.reservoir).subList(position + 1, container.used));
            }
        }


    }

}

