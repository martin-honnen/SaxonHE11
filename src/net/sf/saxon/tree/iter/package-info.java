////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * <p>This package defines implementations and subinterfaces of the interface SequenceIterator, which is
 * used to iterate over an XDM sequence.</p>
 * <p>The subinterfaces mostly represent iterators with additional capability: for example a LastPositionFinder
 * can determine the number of items in the sequence without reading to the end; a GroundedIterator can deliver
 * the original sequence as a list in memory; a LookaheadIterator is capable of one-item look-ahead. Note
 * that just because a class implements such an interface does not mean it necessarily has this capability;
 * it is necessary to check the properties of the specific iterator before assuming this.</p>
 */
package net.sf.saxon.tree.iter;
