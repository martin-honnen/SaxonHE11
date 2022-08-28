////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.ma.arrays;

import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.om.*;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.z.IntSet;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple implementation of XDM array items, in which the array is backed by a Java List.
 */
public class SimpleArrayItem extends AbstractArrayItem {

    /**
     * Static constant value representing an empty array
     */

    public static final SimpleArrayItem EMPTY_ARRAY =
            new SimpleArrayItem(new ArrayList<>());

    private final List<GroundedValue> _members;
    private boolean knownToBeGrounded = false;

    /**
     * Construct an array whose members are arbitrary sequences
     * @param members the list of values (in general, each value is a sequence) to form the members of the array.
     *                The values must be repeatable sequences (not LazySequences); this is not checked.
     */

    public SimpleArrayItem(List<GroundedValue> members) {
        this._members = members;
    }

    /**
     * Construct an array whose members are single items
     * @param input an iterator over the items to make up the array
     * @return an array in which each member is a single item, taken from the input sequence
     * @throws XPathException if evaluating the SequenceIterator fails
     */

    public static SimpleArrayItem makeSimpleArrayItem(SequenceIterator input) throws XPathException {
        List<GroundedValue> members = new ArrayList<>();
        SequenceTool.supply(input, (ItemConsumer<? super Item>) item -> {
            if (item.getClass().getName().equals("com.saxonica.functions.extfn.ArrayMemberValue")) {
                members.add((GroundedValue) ((ObjectValue<GroundedValue>) item).getObject());
            } else {
                members.add(item);
            }
        });
        SimpleArrayItem result = new SimpleArrayItem(members);
        result.knownToBeGrounded = true;
        return result;
    }

    /**
     * Get the roles of the arguments, for the purposes of streaming
     *
     * @return an array of OperandRole objects, one for each argument
     */
    @Override
    public OperandRole[] getOperandRoles() {
        return new OperandRole[]{OperandRole.SINGLE_ATOMIC};
    }

    /**
     * Ensure that all the members are grounded. The idea is that a member may
     * initially be a reference to a lazily-evaluated sequence, but once computed, the
     * reference will be replaced with the actual value
     *
     * @throws XPathException if an error is detected
     */

    public void makeGrounded() throws XPathException {
        if (!knownToBeGrounded) {
            synchronized(this) {
                for (int i = 0; i< _members.size(); i++) {
                    _members.set(i, ((Sequence) _members.get(i)).materialize());
                }
                knownToBeGrounded = true;
            }
        }
    }

    /**
     * Get the function annotations (as defined in XQuery). Returns an empty
     * list if there are no function annotations.
     *
     * @return the function annotations
     */

    @Override
    public AnnotationList getAnnotations() {
        return AnnotationList.EMPTY;
    }

    /**
     * Get a member of the array
     *
     * @param index the position of the member to retrieve (zero-based)
     * @return the value at the given position.
     * @throws IndexOutOfBoundsException if the index is out of range
     */


    @Override
    public GroundedValue get(int index) {
        return _members.get(index);
    }

    /**
     * Replace a member of the array
     *
     * @param index    the position of the member to replace (zero-based)
     * @param newValue the replacement value
     * @return the value at the given position.
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public ArrayItem put(int index, GroundedValue newValue) {
        ImmutableArrayItem a2 = new ImmutableArrayItem(this);
        return a2.put(index, newValue);
    }

    /**
     * Get the size of the array
     *
     * @return the number of members in this array
     */

    @Override
    public int arrayLength() {
        return _members.size();
    }

    /**
     * Ask whether the array is empty
     *
     * @return true if and only if the size of the array is zero
     */

    @Override
    public boolean isEmpty() {
        return _members.isEmpty();
    }

    /**
     * Get the list of all members of the array
     *
     * @return an iterator over the members of the array
     */

    @Override
    public Iterable<GroundedValue> members() {
        return _members;
    }

    /**
     * Remove zero or more members from the array
     *
     * @param positions the positions of the members to be removed (zero-based).
     *                  A value that is out of range is ignored.
     * @return a new array in which the requested member has been removed
     */

    @Override
    public ArrayItem removeSeveral(IntSet positions) {
        ImmutableArrayItem a2 = new ImmutableArrayItem(this);
        return a2.removeSeveral(positions);
    }

    /**
     * Remove a member from the array
     *
     * @param pos the position of the member to be removed (zero-based). A value
     *            that is out of range results in an IndexOutOfBoundsException
     * @return a new array in which the requested member has been removed
     */

    @Override
    public ArrayItem remove(int pos) {
        ImmutableArrayItem a2 = new ImmutableArrayItem(this);
        return a2.remove(pos);
    }

    /**
     * Get a subarray given a start and end position
     *
     * @param start the start position (zero based)
     * @param end   the end position (the position of the first item not to be returned)
     *              (zero based)
     * @throws IndexOutOfBoundsException if start, or start+end, is out of range
     */
    @Override
    public ArrayItem subArray(int start, int end) {
        return new SimpleArrayItem(_members.subList(start, end));
    }

    /**
     * Insert a new member into an array
     *
     * @param position the 0-based position that the new item will assume
     * @param member   the new member to be inserted
     * @return a new array item with the new member inserted
     * @throws IndexOutOfBoundsException if position is out of range
     */
    @Override
    public ArrayItem insert(int position, GroundedValue member) {
        ImmutableArrayItem a2 = new ImmutableArrayItem(this);
        return a2.insert(position, member);
    }

    /**
     * Concatenate this array with another
     *
     * @param other the second array
     * @return the concatenation of the two arrays; that is, an array
     *         containing first the members of this array, and then the members of the other array
     */

    @Override
    public ArrayItem concat(ArrayItem other) {
        ImmutableArrayItem a2 = new ImmutableArrayItem(this);
        return a2.concat(other);
    }


    /**
     * Get a list of the members of the array
     *
     * @return the list of members. Note that this returns the actual contained member array, and this is
     * mutable. Changes to this array are permitted only if the caller knows what they are doing, for example
     * during initial construction of an array that will not be presented to the user until construction
     * has finished.
     */

    public List<GroundedValue> getMembers() {
        return _members;
    }

    /**
     * Provide a short string showing the contents of the item, suitable
     * for use in error messages
     *
     * @return a depiction of the item suitable for use in error messages
     */
    @Override
    public String toShortString() {
        int size = getLength();
        if (size == 0) {
            return "[]";
        } else if (size > 5) {
            return "[(:size " + size + ":)]";
        } else {
            StringBuilder buff = new StringBuilder(256);
            buff.append("[");
            for (GroundedValue entry : members()) {
                buff.append(Err.depictSequence(entry).toString().trim());
                buff.append(", ");
            }
            if (size == 1) {
                buff.append("]");
            } else {
                buff.setCharAt(buff.length() - 2, ']');
            }
            return buff.toString().trim();
        }
    }
}

