////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.str;

import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.transpile.CSharpInnerClass;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.z.IntIterator;

import java.util.Arrays;

public class StringTool {

    /**
     * Get the length of a string, as defined in XPath. This is not the same as the Java length,
     * as a Unicode surrogate pair counts as a single character.
     *
     * @param s The string whose length is required
     * @return the length of the string in Unicode code points
     */

    public static int getStringLength(/*@NotNull*/ CharSequence s) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i);
            if (c < 55296 || c > 56319) {
                n++;    // don't count high surrogates, i.e. D800 to DBFF
            }
        }
        return n;
    }

    /**
     * Expand a string into an array of 32-bit characters
     *
     * @param s the string to be expanded
     * @return an array of integers representing the Unicode code points
     */

    public static int[] expand(UnicodeString s) {
        int[] array = new int[s.length32()];
        IntIterator iter = s.codePoints();
        int i = 0;
        while (iter.hasNext()) {
            array[i++] = iter.next();
        }
        return array;
    }

    /**
     * Ask whether a string contains astral characters (represented as surrogate pairs)
     * @param str the string to be tested
     * @return true if the string contains surrogate characters
     */

    public static boolean containsSurrogates(String str) {
        for (int i=0; i<str.length(); i++) {
            if (UTF16CharacterSet.isSurrogate(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Contract an array of integers containing Unicode codepoints into a string
     *
     * @param codes an array of integers representing the Unicode code points
     * @param used  the number of items in the array that are actually used
     * @return the constructed string
     */

    /*@NotNull*/
    public static UnicodeString fromCodePoints(int[] codes, int used) {
        UnicodeBuilder sb = new UnicodeBuilder();
        for (int i = 0; i < used; i++) {
            sb.append(codes[i]);
        }
        return sb.toUnicodeString();
    }

    public static UnicodeString fromCharSequence(CharSequence chars) {
        UnicodeBuilder sb = new UnicodeBuilder();
        IntIterator iter = codePoints(chars);
        while (iter.hasNext()) {
            sb.append(iter.next());
        }
        return sb.toUnicodeString();
    }

    public static UnicodeString fromLatin1(String str) {
        byte[] bytes = new byte[str.length()];
        for (int i = 0; i<str.length(); i++) {
            bytes[i] = (byte)(str.charAt(i) & 0xff);
        }
        return new Twine8(bytes);
    }

    @CSharpInnerClass(outer=false, extra={"string value"})
    public static IntIterator codePoints(CharSequence value) {
        return new IntIterator() {
            int i = 0;
            boolean expectingLowSurrogate;

            @Override
            public boolean hasNext() {
                return i < value.length();
            }

            @Override
            public int next() {
                int c = value.charAt(i++);
                if (UTF16CharacterSet.isHighSurrogate(c)) {
                    try {
                        int d = hasNext() ? value.charAt(i++) : -1;
                        if (!UTF16CharacterSet.isLowSurrogate(d)) {
                            throw new IllegalStateException("Unmatched surrogate code value " + c + " at position " + i);
                        }
                        return UTF16CharacterSet.combinePair((char) c, (char) d);
                    } catch (StringIndexOutOfBoundsException e) {
                        throw new IllegalStateException("Invalid surrogate at end of string");
                    }
                } else {
                    return c;
                }
            }
        };
    }

    /**
     * Produce a diagnostic representation of the contents of the string
     *
     * @param s the string
     * @return a string in which non-Ascii-printable characters are replaced by \ uXXXX escapes
     */

    /*@NotNull*/
    public static String diagnosticDisplay(/*@NotNull*/ String s) {
        StringBuilder fsb = new StringBuilder(s.length());
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            if (c >= 0x20 && c <= 0x7e) {
                fsb.append(c);
            } else {
                fsb.append("\\u");
                for (int shift = 12; shift >= 0; shift -= 4) {
                    fsb.append("0123456789ABCDEF".charAt((c >> shift) & 0xF));
                }
            }
        }
        return fsb.toString();
    }

    /**
     * Insert a wide character (surrogate pair) at the start of a StringBuilder
     * @param builder the string builder
     * @param ch the codepoint of the character to be inserted
     */

    public static void prependWideChar(StringBuilder builder, int ch) {
        if (ch > 0xffff) {
            char[] pair = new char[]{UTF16CharacterSet.highSurrogate(ch), UTF16CharacterSet.lowSurrogate(ch)};
            builder.insert(0, pair);
        } else {
            builder.insert(0, (char) ch);
        }
    }

    /**
     * Insert repeated occurrences of a given character at the start of a StringBuilder
     * @param builder the string builder
     * @param ch the character to be inserted
     * @param count the number of repetitions
     */

    public static void prependRepeated(StringBuilder builder, char ch, int count) {
        char[] array = new char[count];
        Arrays.fill(array, ch);
        builder.insert(0, array);
    }

    /**
     * Insert repeated occurrences of a given character at the end of a StringBuilder
     *
     * @param builder the string builder
     * @param ch      the character to be inserted
     * @param count   the number of repetitions
     */

    public static void appendRepeated(StringBuilder builder, char ch, int count) {
        for (int i=0; i<count; i++) {
            builder.append(ch);
        }
    }

    /**
     * Get the last codepoint in a UnicodeString
     * @param str the input string
     * @return the integer value of the last character in the string
     * @throws IndexOutOfBoundsException if the string is empty
     */

    public static int lastCodePoint(UnicodeString str) {
        return str.codePointAt(str.length() - 1);
    }

    /**
     * Get the position of the last occurrence of a given codepoint within a string
     * @param str the input string
     * @param codePoint the sought codepoint
     * @return the zero-based position of the last occurrence of the codepoint within the input string,
     * or -1 if the codepoint does not appear within the string
     */

    public static long lastIndexOf(UnicodeString str, int codePoint) {
        for (long i=str.length()-1; i>=0; i--) {
            if (str.codePointAt(i) == codePoint) {
                return i;
            }
        }
        return -1L;
    }

    /**
     * Utility method for use where strings longer than 2^31 characters cannot yet be handled.
     * @param value the actual value of a character position within a string, or the length of
     *              a string
     * @return the value as an integer if it is within range
     * @throws UnsupportedOperationException if the supplied value exceeds {@link Integer#MAX_VALUE}
     */

    public static int requireInt(long value) {
        if (value > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("String exceeds 2^31 characters");
        }
        return (int) value;
    }

    /**
     * Attempt to compress a UnicodeString consisting entirely of whitespace. This is the first thing we
     * do to an incoming text node
     *
     * @param in the Unicode string to be compressed
     * @param offset the start position of the substring we are interested in
     * @param len the length of the substring we are interested in
     * @param compressWS set to true if whitespace compression is to be attempted
     * @return the compressed sequence if it can be compressed; or the uncompressed UnicodeString otherwise
     */

    /*@NotNull*/
    public static UnicodeString compress(char[] in, int offset, int len, boolean compressWS) {
        //final int inlen = in.length;
        if (len == 0) {
            return EmptyUnicodeString.getInstance();
        }
        int max = 255;
        int end = offset + len;
        boolean allWhite = compressWS;
        int surrogates = 0;
        // Find the maximum code value, and test whether all-white or surrogate
        for (int i=offset; i < end; i++) {
            int c = in[i];
            max |= c;
            if (allWhite && !Whitespace.isWhite(c)) {
                allWhite = false;
            }
            if (UTF16CharacterSet.isSurrogate(c)) {
                surrogates++;
            }
        }
        if (allWhite) {
            return CompressedWhitespace.compressWS(in, offset, end);
        }
        if (max < 256) {
            byte[] array = new byte[len];
            for (int i = offset, j=0; i < end;) {
                array[j++] = (byte)in[i++];
            }
            return new Twine8(array);
            //Following is slower:
            //byte[] array = new String(in, offset, len).getBytes(StandardCharsets.ISO_8859_1);
            //return new Twine8(array);
        }
        if (surrogates == 0) {
            char[] array = Arrays.copyOfRange(in, offset, offset + len);
            return new Twine16(array);
        } else {
            byte[] array = new byte[3 * (len - surrogates/2)];
            for (int i = offset, j = 0; i < end; ) {
                char c = in[i++];
                if (UTF16CharacterSet.isSurrogate(c)) {
                    int cp = UTF16CharacterSet.combinePair(c, in[i++]);
                    array[j++] = (byte) ((cp & 0xffffff) >> 16);
                    array[j++] = (byte) ((cp & 0xffff) >> 8);
                    array[j++] = (byte) (cp & 0xff);
                } else {
                    array[j++] = (byte) 0;
                    array[j++] = (byte) ((c & 0xffff) >> 8);
                    array[j++] = (byte) (c & 0xff);
                }
            }
            return new Twine24(array);
        }

    }

    /**
     * Copy from an array of 8-bit characters to an array holding 16-bit characters.
     * The caller is responsible for ensuring that the offsets are in range and that the
     * destination array is large enough.
     * @param source the source array
     * @param sourcePos the position in the source array where copying is to start
     * @param dest the destination array
     * @param destPos the position in the destination array where copying is to start
     * @param count the number of characters (codepoints) to copy
     */

    public static void copy8to16(byte[] source, int sourcePos, char[] dest, int destPos, int count) {
        int last = sourcePos + count;
        for (int i=sourcePos, j=destPos; i<last;) {
            dest[j++] = (char)(source[i++] & 0xff);
        }
    }

    /**
     * Copy from an array of 8-bit characters to an array holding 24-bit characters,
     * organised as three bytes per character
     * The caller is responsible for ensuring that the offsets are in range and that the
     * destination array is large enough.
     *
     * @param source    the source array
     * @param sourcePos the position in the source array where copying is to start
     * @param dest      the destination array, using three bytes per codepoint
     * @param destPos   the codepoint position (not byte position) in the destination array where
     *                  copying is to start
     * @param count     the number of characters (codepoints) to copy
     */

    public static void copy8to24(byte[] source, int sourcePos, byte[] dest, int destPos, int count) {
        int last = sourcePos + count;
        for (int i = sourcePos, j = destPos*3; i < last;) {
            dest[j++] = 0;
            dest[j++] = 0;
            dest[j++] = source[i++];
        }
    }

    /**
     * Copy from an array of 16-bit characters to an array holding 16-bit characters.
     * The caller is responsible for ensuring that the offsets are in range and that the
     * destination array is large enough.
     *
     * @param source    the source array. The caller is responsible for ensuring that this
     *                  contains no surrogates
     * @param sourcePos the position in the source array where copying is to start
     * @param dest      the destination array
     * @param destPos   the position in the destination array where copying is to start
     * @param count     the number of characters (codepoints) to copy
     */

    public static void copy16to24(char[] source, int sourcePos, byte[] dest, int destPos, int count) {
        int last = sourcePos + count;
        for (int i = sourcePos, j = destPos * 3; i < last; ) {
            char c = source[i++];
            dest[j++] = 0;
            dest[j++] = (byte) ((c >> 8) & 0xff);
            dest[j++] = (byte) (c & 0xff);
        }
    }
}

