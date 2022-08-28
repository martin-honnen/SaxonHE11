////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.sort;

/**
 * This interface sometimes represents an XPath atomic value where the Java comparison semantics (using the
 * {@code compareTo} method) match the XPath equality and ordering semantics; and sometimes it represents
 * a surrogate for an XPath atomic value, chosen so that the Java comparison semantics match the XPath rules.
 */
public interface XPathComparable extends Comparable<XPathComparable> {
}

