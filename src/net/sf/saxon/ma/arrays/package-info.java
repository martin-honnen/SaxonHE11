////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * <p>This package implements arrays, as introduced in XPath/XQuery 3.1:
 * arrays provide a list-like data structure that (unlike sequences) allows nesting.</p>
 * <p>Arrays are immutable, so that adding a new entry to an array creates a new array.</p>
 * <p>The entries in a array are arbitrary XDM sequences.</p>
 * <p>There are functions (each supported by its own implementation class) to create a new array,
 * to add an entry to a array, to get an entry from an array, to get the size of an array,
 * and so on..</p>
 */
package net.sf.saxon.ma.arrays;
