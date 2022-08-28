////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.tree.jiter;

import java.util.Iterator;

/**
 * An iterator over items, that concatenates the items returned by two supplied iterators.
 */

public class ConcatenatingIterable<E> implements Iterable<E> {

    Iterable<? extends E> first;
    Iterable<? extends E> second;

    /**
     * Create an iterable that concatenates the results of two supplied iterables.
     * @param first the first iterable
     * @param second the second iterable
     */

    public ConcatenatingIterable(Iterable<? extends E> first, Iterable<? extends E> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public Iterator<E> iterator() {
        return new ConcatenatingIterator<>(first.iterator(), () -> second.iterator());
    }
}

