////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.sort;

import net.sf.saxon.value.AtomicValue;

/**
 * Marker interface to identify an object that acts as a surrogate for an atomic value, with the property
 * that if two atomic values are equal under the XPath 'eq' operator, then their corresponding surrogates
 * are equal under the Java equals() comparison (and by implication, they have equal hash codes).
 * <p>Match keys representing atomic values
 * of an ordered type, however, must also implement Comparable&lt;AtomicMatchKey&gt;, and their compareTo() method must
 * reflect the ordering semantics. In the case of strings this means the keys must reflect the
 * semantics of the relevant collation.</p>
 */


public interface AtomicMatchKey {

    /**
     * Get an atomic value that encapsulates this match key. Needed to support the collation-key() function.
     * @return an atomic value that encapsulates this match key. NB: this is NOT (necessarily) the atomic value
     * from which the {@code AtomicMatchKey} was derived.
     */

    AtomicValue asAtomic();

}
