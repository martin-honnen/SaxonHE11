////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2020 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


package net.sf.saxon.str;

import net.sf.saxon.trans.XPathException;

/**
 * Interface that accepts a string in the form of a sequence of CharSequences,
 * which are conceptually concatenated (though in some implementations, the final
 * string may never be materialized in memory)
 */

public interface UniStringConsumer {

    /**
     * Initial processing. This should be called before any calls on append.
     * Default implementation does nothing.
     * @throws XPathException if processing fails for any reason
     */

    default void open() throws XPathException {
        // no action
    }

    /**
     * Process a supplied character sequence
     * @param chars the characters to be processed
     * @return this CharSequenceConsumer (to allow method chaining)
     * @throws XPathException if processing fails for any reason
     */

    UniStringConsumer accept(UnicodeString chars) throws XPathException;


    /**
     * Complete the writing of characters to the result. The default implementation
     * does nothing.
     * @throws XPathException if processing fails for any reason
     */

    default void close() throws XPathException {}


}

