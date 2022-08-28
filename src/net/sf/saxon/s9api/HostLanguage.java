////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.s9api;

import net.sf.saxon.Configuration;
import net.sf.saxon.transpile.CSharpSimpleEnum;

/**
 * Identifies a host language in which XPath expressions appear. Generally used when different error codes
 * need to be returned depending on the host language.
 * @since 10.0; previously an integer constant in class {@link Configuration} was used
 */

@CSharpSimpleEnum
public enum HostLanguage {
    UNKNOWN,
    XSLT,
    XQUERY,
    XML_SCHEMA,
    XPATH,
    XSLT_PATTERN
}


