////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.om;

import net.sf.saxon.transpile.CSharpModifiers;

/**
 * A QNameException represents an error condition whereby a QName (for example a variable
 * name or template name) is malformed
 */

public class QNameException extends Exception {

    String message;

    public QNameException(String message) {
        this.message = message;
    }

    @Override
    @CSharpModifiers(code={"public", "override"})
    public String getMessage() {
        return message;
    }

}
