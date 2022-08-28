////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.z;

/**
 * An iterator over a single integer
 */

public class IntSingletonIterator implements IntIterator {

    private final int value;
    boolean gone = false;

    public IntSingletonIterator(int value) {
        this.value = value;
    }

    @Override
    public boolean hasNext() {
        return !gone;
    }

    @Override
    public int next() {
        gone = true;
        return value;
    }

}
