////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.tree.iter;

import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.FocusIterator;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.UncheckedXPathException;

import java.util.function.Supplier;


/**
 * ManualIterator: a pseudo-iterator used while streaming. It has a current node and a current position
 * which are set manually. Calling last() is an error. Calling next() always returns null.
 */

public class ManualIterator implements FocusIterator, SequenceIterator,
        ReversibleIterator, LastPositionFinder, GroundedIterator, LookaheadIterator {

    private Item item;
    private int _position;
    private Supplier<Integer> lengthFinder;

    /**
     * Create an uninitialized ManualIterator: this is only usable after the context item, position, and size (if required)
     * have been initialized using setter methods.
     */

    public ManualIterator() {
        item = null;
        _position = 0;
    }

    /**
     * Create a ManualIterator initializing the context item and position.
     * The value of "last()" for such an iterator is unknown unless a LastPositionFinder is supplied.
     * @param value the context item. May be null if the value is to be initialized later.
     * @param position the context position
     */

    public ManualIterator(Item value, int position) {
        this.item = value;
        this._position = position;
    }

    /**
     * Create a ManualIterator supplying the context item, and setting the value of
     * both "position()" and "last()" implicitly to 1.
     * @param value the context item
     */

    public ManualIterator(Item value) {
        this.item = value;
        this._position = 1;
        this.lengthFinder = () -> 1;
    }

    public void setContextItem(Item value) {
        this.item = value;
    }

    public void setLengthFinder(Supplier<Integer> finder) {
        this.lengthFinder = finder;
    }

    public void incrementPosition() {
        _position++;
    }

    public void setPosition(int position) {
        this._position = position;
    }

    @Override
    public boolean supportsHasNext() {
        return true;
    }

    /**
     * Determine whether there are more items to come. Note that this operation
     * is stateless and it is not necessary (or usual) to call it before calling
     * next(). It is used only when there is an explicit need to tell if we
     * are at the last element.
     *
     * @return true if there are more items
     */

    @Override
    public boolean hasNext() {
        return position() != getLength();
    }

    @Override
    public Item next() {
        return null;
    }

    @Override
    public Item current() {
        return item;
    }

    /**
     * Return the current position in the sequence.
     *
     * @return 0 before the first call on next(); 1 before the second call on next(); -1 after the second
     *         call on next().
     */
    @Override
    public int position() {
        return _position;
    }

    /**
     * Ask whether this iterator supports use of the {@link #getLength()} method. This
     * method should always be called before calling {@link #getLength()}, because an iterator
     * that implements this interface may support use of {@link #getLength()} in some situations
     * and not in others
     *
     * @return true if the {@link #getLength()} method can be called to determine the length
     * of the underlying sequence.
     */
    @Override
    public boolean supportsGetLength() {
        return true;
    }

    @Override
    public int getLength() {
        if (lengthFinder == null) {
            throw new UncheckedXPathException("Saxon streaming restriction: last() cannot be used when consuming a sequence of streamed nodes, even if the items being processed are grounded");
        } else {
            int length = lengthFinder.get();
            //System.err.println("ManualIterator " + this + ".length = " + length);
            return length;
        }
    }

    public boolean isActuallyGrounded() {
        return true;
    }

    @Override
    public ManualIterator getReverseIterator() {
        return new ManualIterator(item);
    }

    /**
     * Return a Value containing all the items in the sequence returned by this
     * SequenceIterator
     *
     * @return the corresponding Value. If the value is a closure or a function call package, it will be
     *         evaluated and expanded.
     */

    /*@Nullable*/
    @Override
    public GroundedValue materialize() {
        return item;
    }

    @Override
    public GroundedValue getResidue() {
        return item;
    }

}

// Copyright (c) 2009-2022 Saxonica Limited
