////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.tree.linked;

import net.sf.saxon.pattern.NodeTest;

final class ChildEnumeration extends TreeEnumeration {

    public ChildEnumeration(NodeImpl node, NodeTest nodeTest) {
        super(node, nodeTest);
        nextNode = node.getFirstChild();
        while (!conforms(nextNode)) {
            step();
        }
    }

    @Override
    protected void step() {
        nextNode = nextNode.getNextSibling();
    }

}

