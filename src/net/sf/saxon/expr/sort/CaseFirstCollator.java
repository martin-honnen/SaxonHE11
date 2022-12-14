////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.sort;

import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.str.UnicodeString;
import net.sf.saxon.regex.charclass.Categories;
import net.sf.saxon.trans.XPathException;


/**
 * A StringCollator that sorts lowercase before uppercase, or vice versa.
 * <p>Case is irrelevant, unless the strings are equal ignoring
 * case, in which case lowercase comes first.</p>
 */

public class CaseFirstCollator implements StringCollator {

    private final StringCollator baseCollator;
    private final boolean upperFirst;
    private final String uri;

    /**
     * Create a CaseFirstCollator
     *  @param base       the base collator, which determines how characters are sorted irrespective of case
     * @param upperFirst true if uppercase precedes lowercase, false otherwise
     * @param collationURI the URI of the collation
     */

    public CaseFirstCollator(StringCollator base, boolean upperFirst, String collationURI) {
        this.baseCollator = base;
        this.upperFirst = upperFirst;
        this.uri = collationURI;
    }

    public static StringCollator makeCaseOrderedCollator(String uri, StringCollator stringCollator, String caseOrder) throws XPathException {
        switch (caseOrder) {
            case "lower-first":
                stringCollator = new CaseFirstCollator(stringCollator, false, uri);
                break;
            case "upper-first":
                stringCollator = new CaseFirstCollator(stringCollator, true, uri);
                break;
            default:
                throw new XPathException("case-order must be lower-first, upper-first, or #default");
        }
        return stringCollator;
    }

    /**
     * Get the collation URI. It must be possible to use this collation URI to reconstitute the collation
     *
     * @return a collation URI that can be used to reconstruct the collation when an XSLT package is reloaded.
     */
    @Override
    public String getCollationURI() {
        return uri;
    }

    /**
     * Compare two string objects: case is irrelevant, unless the strings are equal ignoring
     * case, in which case lowercase comes first.
     *
     * @return &lt;0 if a&lt;b, 0 if a=b, &gt;0 if a&gt;b
     * @throws ClassCastException if the objects are of the wrong type for this Comparer
     * @param a the first string
     * @param b the second string
     */

    @Override
    public int compareStrings(UnicodeString a, UnicodeString b) {
        a = a.tidy();
        b = b.tidy();
        Categories.Category letters = Categories.getCategory("L");
        Categories.Category upperCase = Categories.getCategory("Lu");
        Categories.Category lowerCase = Categories.getCategory("Ll");
        int diff = baseCollator.compareStrings(a, b);
        if (diff != 0) {
            return diff;
        }

        // This is doing a character-by-character comparison, which isn't really right.
        // There might be a sequence of letters constituting a single collation unit.

        long i = 0;
        long j = 0;
        while (true) {
            // Skip characters that are equal in the two strings
            while (i < a.length() && j < b.length() && a.codePointAt(i) == b.codePointAt(j)) {
                i++;
                j++;
            }
            // Skip non-letters in the first string
            while (i < a.length() && !letters.test(a.codePointAt(i))) {
                i++;
            }
            // Skip non-letters in the second string
            while (j < b.length() && !letters.test(b.codePointAt(j))) {
                j++;
            }
            // If we've got to the end of either string, treat the strings as equal
            if (i >= a.length()) {
                return 0;
            }
            if (j >= b.length()) {
                return 0;
            }
            // If one of the characters is upper/lower case and the other isn't, the issue is decided
            boolean aFirst = upperFirst ? upperCase.test(a.codePointAt(i++)) : lowerCase.test(a.codePointAt(i++));
            boolean bFirst = upperFirst ? upperCase.test(b.codePointAt(j++)) : lowerCase.test(b.codePointAt(j++));
            if (aFirst && !bFirst) {
                return -1;
            }
            if (bFirst && !aFirst) {
                return +1;
            }
        }
    }

    /**
     * Compare two strings for equality. This may be more efficient than using compareStrings and
     * testing whether the result is zero, but it must give the same result
     *
     * @param s1 the first string
     * @param s2 the second string
     * @return true if and only if the strings are considered equal,
     */

    @Override
    public boolean comparesEqual(UnicodeString s1, /*@NotNull*/ UnicodeString s2) {
        return compareStrings(s1, s2) == 0;
    }

    /**
     * Get a collation key for a String. The essential property of collation keys
     * is that if (and only if) two strings are equal under the collation, then
     * comparing the collation keys using the equals() method must return true.
     * @param s the string whose collation key is required
     */

    @Override
    public AtomicMatchKey getCollationKey(UnicodeString s) {
        return baseCollator.getCollationKey(s);
    }

}

