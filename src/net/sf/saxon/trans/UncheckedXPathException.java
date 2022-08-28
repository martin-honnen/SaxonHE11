////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.trans;

import net.sf.saxon.transpile.CSharpModifiers;

/**
 * When implementing certain interfaces Saxon is not able to throw a checked exception because
 * the interface definition does not allow it. In these circumstances the checked exception
 * is wrapped in an unchecked exception, which is thrown in its place. The intent is that
 * the unchecked exception will be caught and "unwrapped" before the calling application
 * gets to see it.
 *
 * <p>User-written callback functions (such as {@link net.sf.saxon.lib.ErrorReporter} may also
 * throw an {@code UncheckedXPathException}; this will generally cause the query or transformation
 * to be aborted.</p>
 */

public class UncheckedXPathException extends RuntimeException {

    public UncheckedXPathException(XPathException cause) {
        super(cause);
    }

    @CSharpModifiers(code = {"public", "override"})
    public UncheckedXPathException(String message) {
        super(new XPathException(message));
    }

    public UncheckedXPathException(Throwable cause) {
        super(new XPathException(cause));
    }

    public XPathException getXPathException() {
        return (XPathException)getCause();
    }

    @Override
    @CSharpModifiers(code = {"public", "override"})
    public String getMessage() {
        return getCause().getMessage();
    }
}
