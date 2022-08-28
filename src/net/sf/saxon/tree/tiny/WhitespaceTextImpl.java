////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.tree.tiny;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.str.CompressedWhitespace;
import net.sf.saxon.str.UnicodeString;
import net.sf.saxon.str.UnicodeBuilder;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.StringValue;

/**
 * A node in the XML parse tree representing a text node with compressed whitespace content
 */

public final class WhitespaceTextImpl extends TinyNodeImpl {

    /**
     * Create a compressed whitespace text node
     *
     * @param tree   the tree to contain the node
     * @param nodeNr the internal node number
     */

    public WhitespaceTextImpl(TinyTree tree, int nodeNr) {
        this.tree = tree;
        this.nodeNr = nodeNr;
    }

    /**
     * Get the string value of the item. For a WhitespaceTextImpl node, it avoids the
     * cost of decompressing the whitespace
     * @return the string value of the node
     */

    @Override
    public UnicodeString getUnicodeStringValue() {
        long value = ((long) tree.alpha[nodeNr] << 32) | ((long) tree.beta[nodeNr] & 0xffffffffL);
        return new CompressedWhitespace(value);
    }

    /**
     * Static method to get the string value of a text node without first constructing the node object
     *
     * @param tree   the tree
     * @param nodeNr the node number of the text node
     * @return the string value of the text node
     */

    public static UnicodeString getStringValue(TinyTree tree, int nodeNr) {
        long value = ((long) tree.alpha[nodeNr] << 32) | ((long) tree.beta[nodeNr] & 0xffffffffL);
        return new CompressedWhitespace(value);
    }

    /**
     * Static method to get the string value of a text node and append it to a supplied buffer
     * without first constructing the node object
     *
     * @param tree   the tree
     * @param nodeNr the node number of the text node
     * @param buffer a buffer to which the string value will be appended
     */

    public static void appendStringValue(TinyTree tree, int nodeNr, UnicodeBuilder buffer) {
        long value = ((long) tree.alpha[nodeNr] << 32) | ((long) tree.beta[nodeNr] & 0xffffffffL);
        buffer.append(CompressedWhitespace.uncompress(value));
    }

    /**
     * Get the typed value.
     *
     * @return the typed value. This will either be a single AtomicValue or a Value whose items are
     *         atomic values.
     * @since 8.5
     */

    @Override
    public StringValue atomize() {
        return StringValue.makeUntypedAtomic(getUnicodeStringValue());
    }

    /**
     * Static method to get the "long" value representing the content of a whitespace text node
     *
     * @param tree   the TinyTree
     * @param nodeNr the internal node number
     * @return a value representing the compressed whitespace content
     * @see CompressedWhitespace
     */

    public static long getLongValue(TinyTree tree, int nodeNr) {
        return ((long) tree.alpha[nodeNr] << 32) | ((long) tree.beta[nodeNr] & 0xffffffffL);
    }

    /**
     * Return the type of node.
     *
     * @return Type.TEXT
     */

    @Override
    public final int getNodeKind() {
        return Type.TEXT;
    }

    /**
     * Copy this node to a given outputter
     */

    @Override
    public void copy(/*@NotNull*/ Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.characters(getUnicodeStringValue(), locationId, ReceiverOption.NONE);
    }


}

