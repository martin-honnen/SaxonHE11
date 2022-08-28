////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.str;

/**
 * This abstract class represents a couple of different implementations of strings
 * containing whitespace only.
 */

public abstract class WhitespaceString extends UnicodeString {


    /**
     * Uncompress the whitespace to a (normal) UnicodeString
     * @return the uncompressed value
     */

    public abstract UnicodeString uncompress();

    @Override
    public int getWidth() {
        return 7;
    }



    /**
     * Returns a new <code>UnicodeString</code> that is a subsequence of this sequence.
     * The subsequence starts with the codepoint value at the specified index and
     * ends with the codepoint value at index <tt>end - 1</tt>.  The length
     * (in codepoints) of thereturned sequence is <tt>end - start</tt>, so if <tt>start == end</tt>
     * then an empty sequence is returned.
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @return the specified subsequence
     * @throws IndexOutOfBoundsException if <tt>start</tt> or <tt>end</tt> are negative,
     *                                   if <tt>end</tt> is greater than <tt>length()</tt>,
     *                                   or if <tt>start</tt> is greater than <tt>end</tt>
     */
    @Override
    public UnicodeString substring(long start, long end) {
        return uncompress().substring(start, end);
    }

    @Override
    public long indexOf(int codePoint, long from) {
        // Faster implementations are possible, but not needed
        return uncompress().indexOf(codePoint, from);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return uncompress().toString();
    }

    /**
     * Write the value to a UnicodeWriter
     *
     * @param writer the writer to write to
     * @throws java.io.IOException if an error occurs downstream
     */

    public abstract void write(/*@NotNull*/ UnicodeWriter writer) throws java.io.IOException;

    void copy8bit(byte[] target, int offset) {
        uncompress().copy8bit(target, offset);
    }

    void copy16bit(char[] target, int offset) {
        uncompress().copy16bit(target, offset);
    }

    void copy24bit(byte[] target, int offset) {
        uncompress().copy24bit(target, offset);
    }


    /**
     * Write the value to a Writer with escaping of special characters
     *
     * @param specialChars identifies which characters are considered special
     * @param writer       the writer to write to
     * @throws java.io.IOException if an error occurs downstream
     */

    public abstract void writeEscape(boolean[] specialChars, UnicodeWriter writer) throws java.io.IOException;
}

