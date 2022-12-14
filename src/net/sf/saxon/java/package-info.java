////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * <p>This package contains Saxon code that is specific to the Java platform, as distinct from .NET</p>
 * <p>The areas where Saxon has different implementations for the two platforms are primarily
 * URI handling, interfaces to XML parsers, regular expression handling, and use of collations.</p>
 * <p>Access to these classes is generally via the <code>Platform</code> object, of which the
 * implementation for the Java platform is named <code>JavaPlatform</code>. This is obtained in turn
 * via the static method <code>Version.platform</code></p>
 */
package net.sf.saxon.java;
