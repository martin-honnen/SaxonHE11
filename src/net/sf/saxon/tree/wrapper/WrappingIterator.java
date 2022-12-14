////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.tree.wrapper;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.AxisIterator;

/**
 * A WrappingIterator delivers wrappers for the nodes delivered
 * by its underlying iterator. It is used when no whitespace stripping
 * is actually needed, e.g. for the attribute axis. But we still need to
 * create wrappers, so that further iteration remains in the virtual layer
 * rather than switching to the real nodes.
 */

public class WrappingIterator implements AxisIterator {

    AxisIterator base;
    VirtualNode parent;
    /*@Nullable*/ NodeInfo _current;
    boolean atomizing = false;
    WrappingFunction wrappingFunction;

    /**
     * Create a WrappingIterator
     *
     * @param base     The underlying iterator
     * @param function The wrapping function
     * @param parent   If all the nodes to be wrapped have the same parent,
     *                 it can be specified here. Otherwise specify null.
     */

    public WrappingIterator(AxisIterator base, WrappingFunction function, VirtualNode parent) {
        this.base = base;
        this.wrappingFunction = function;
        this.parent = parent;
    }


    /*@Nullable*/
    @Override
    public NodeInfo next() {
        NodeInfo n = base.next();
        if (n == null) {
            return _current = null;
        }
        if (atomizing) {
            _current = n;
        } else {
            _current = wrappingFunction.makeWrapper(n, parent);
        }
        return _current;
    }

    /*@Nullable*/
    public NodeInfo current() {
        return _current;
    }

    @Override
    public void close() {
        base.close();
    }

    @Override
    public void discharge() {
        base.discharge();
    }

}

