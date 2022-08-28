////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * <p>This package defines the implementation of the so-called "linked tree" structure. This
 * structure can be used to represent both the source document and the stylesheet.
 * It is no longer the default structure for source documents, but is always used for
 * stylesheets and for schema documents, because it allows each element to be represented
 * by a subclass of <code>Element</code> with application-specific functionality.</p>
 * <p>The classes represent the various kinds of node on the tree. Most of them
 * are not visible outside the package, with the notable exception of ElementImpl,
 * which can be subclassed to contain properties for a particular kind of element.
 * This capability is exploited especially in the stylesheet tree.</p>
 * <p>As well as classes representing nodes, there are classes representing
 * iterators over the various XPath axes, for example <code>ChildEnumeration</code>
 * and <code>PrecedingEnumeration</code>.</p>
 * <p>The <code>TreeBuilder</code> performs the work of constructing a tree, from a
 * sequence of SAX-like <code>Receiver</code> events.</p>
 * <p>The package also contains some helper classes such as <code>SystemIdMap</code>
 * and <code>LineNumberMap</code> that are used also by the TinyTree implementation.</p>
 */
package net.sf.saxon.tree.linked;
