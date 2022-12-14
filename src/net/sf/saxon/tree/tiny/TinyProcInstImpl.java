////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.tree.tiny;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.str.EmptyUnicodeString;
import net.sf.saxon.str.UnicodeString;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.StringValue;

/**
 * TinyProcInstImpl is a node in the TinyTree representing a processing instruction
 */


final class TinyProcInstImpl extends TinyNodeImpl {

    public TinyProcInstImpl(TinyTree tree, int nodeNr) {
        this.tree = tree;
        this.nodeNr = nodeNr;
    }

    @Override
    public UnicodeString getUnicodeStringValue() {
        int start = tree.alpha[nodeNr];
        int len = tree.beta[nodeNr];
        if (len == 0) {
            return EmptyUnicodeString.getInstance();    // need to special-case this for the Microsoft JVM
        }
        return tree.commentBuffer.substring(start, start + len);
    }

    /**
     * Get the typed value of this node.
     * Returns the string value, as an instance of xs:string
     */

    @Override
    public AtomicSequence atomize() {
        return new StringValue(getUnicodeStringValue());
    }

    @Override
    public final int getNodeKind() {
        return Type.PROCESSING_INSTRUCTION;
    }

    /**
     * Get the base URI of this processing instruction node.
     */

    @Override
    public String getBaseURI() {
        return Navigator.getBaseURI(this);
    }

    /**
     * Copy this node to a given outputter
     */

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.processingInstruction(getDisplayName(), getUnicodeStringValue(), locationId, ReceiverOption.NONE);
    }

    // DOM methods

    /**
     * The target of this processing instruction. XML defines this as being
     * the first token following the markup that begins the processing
     * instruction.
     *
     * @return the "target", or in XDM terms, the name of the processing instruction
     */

    public String getTarget() {
        return getDisplayName();
    }

    /**
     * The content of this processing instruction. This is from the first non
     * white space character after the target to the character immediately
     * preceding the <code>?&gt;</code> .
     *
     * @return the content of the processing instruction (in XDM this is the
     *         same as its string value)
     */

    /*@NotNull*/
    public UnicodeString getData() {
        return getUnicodeStringValue();
    }

}

