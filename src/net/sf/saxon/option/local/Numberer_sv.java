////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.option.local;

import net.sf.saxon.expr.number.AbstractNumberer;

/**
 * Numberer class for the Swedish language.
 *
 * @author Karel Goossens
 *         BTR-Services Belgium.
 * @see <a href="http://en.wikipedia.org/wiki/Swedish_grammar">http://en.wikipedia.org/wiki/Swedish_grammar</a>
 * @see <a href="http://www2.hhs.se/isa/swedish/chap4.htm">http://www2.hhs.se/isa/swedish/chap4.htm</a>
 */
public class Numberer_sv extends AbstractNumberer {

    private static final long serialVersionUID = 1L;

    private static final String[] swedishOrdinalUnits = {
            "", "f\u00f6rsta", "andra", "tredje", "fj\u00e4rde", "femte", "sj\u00e4tte", "sjunde", "\u00e5ttonde", "nionde",
            "tionde", "elfte", "tolfte", "trettonde", "fjortonde", "femtonde", "sextonde",
            "sjuttonde", "artonde", "n\u00edttonde"};

    private static final String[] swedishOrdinalTens = {
            "", "tionde", "tjugonde", "trettionde", "fyrtionde", "femtionde",
            "sextionde", "sjuttionde", "\u00e5ttionde", "n\u00edttionde"};

    private static final String[] swedishUnits = {
            "", "ett", "tv\u00e5", "tre", "fyra", "fem", "sex", "sju", "\u00e5tta", "nio",
            "tio", "elva", "tolv", "tretton", "fjorton", "femton", "sexton",
            "sjutton", "arton", "nitton"};

    private static final String[] swedishTens = {
            "", "tio", "tjugo", "trettio", "fyrtio", "femtio",
            "sextio", "sjuttio", "\u00e5ttio", "nittio"};

    /**
     * Show an ordinal number as swedish words in a requested case (for example, Twentyfirst)
     *
     * @param ordinalParam not used.
     * @param number       the number to be converted to a word.
     * @param wordCase     UPPER_CASE or LOWER_CASE.
     * @return String representing the number in words.
     */

    @Override
    public String toOrdinalWords(String ordinalParam, long number, int wordCase) {

        String s;
        if (number == 1000000000) {
            s = "miljardte";
        } else if (number == 1000000) {
            s = "miljonte";
        } else if (number == 1000) {
            s = "tusende";
        } else if (number == 100) {
            s = "hundrade";
        } else if (number >= 1000000000) {
            long rem = number % 1000000000;
            s = (number / 1000000000 == 1 ? "en" : toWords(number / 1000000000)) + " miljard " +
                    toOrdinalWords(ordinalParam, rem, wordCase);
        } else if (number >= 1000000) {
            long rem = number % 1000000;
            s = (number / 1000000 == 1 ? "en" : toWords(number / 1000000)) + " miljon " +
                    toOrdinalWords(ordinalParam, rem, wordCase);
        } else if (number >= 1000) {
            long rem = number % 1000;
            s = (number / 1000 == 1 ? "et" : toWords(number / 1000)) + "tusen" + " " +
                    toOrdinalWords(ordinalParam, rem, wordCase);
        } else if (number >= 100) {
            long rem = number % 100;
            s = (number / 100 == 1 ? "" : toWords(number / 100)) + "hundra" +
                    (rem == 0 || rem > 19 ? "" : "en") +
                    toOrdinalWords(ordinalParam, rem, wordCase);
        } else {
            if (number < 20) {
                s = swedishOrdinalUnits[(int) number];
            } else {
                int rem = (int) (number % 10);
                if (rem == 0) {
                    s = swedishOrdinalTens[(int) number / 10];
                } else {
                    s = swedishTens[(int) number / 10] + swedishOrdinalUnits[rem];
                }
            }
        }

        if (wordCase == UPPER_CASE) {
            return s.toUpperCase();
        } else if (wordCase == LOWER_CASE) {
            return s.toLowerCase();
        } else {
            return s;
        }
    }

    @Override
    public String toWords(long number) {
        if (number >= 1000000000) {
            long rem = number % 1000000000;
            return (number / 1000000000 == 1 ? "en " : toWords(number / 1000000000)) + "miljard" +
                    (rem == 0 ? "" : " ") + toWords(rem);
        } else if (number >= 1000000) {
            long rem = number % 1000000;
            return (number / 1000000 == 1 ? "en " : toWords(number / 1000000)) + "miljon" +
                    (rem == 0 ? "" : " ") + toWords(rem);
        } else if (number >= 1000) {
            long rem = number % 1000;
            return (number / 1000 == 1 ? "et" : toWords(number / 1000)) + "tusen" +
                    (rem == 0 ? "" : (rem < 100 ? "en" : " ") + toWords(rem));
        } else if (number >= 100) {
            long rem = number % 100;
            return (number / 100 == 1 ? "" : toWords(number / 100)) + "hundra" + toWords(rem);
        } else {
            if (number < 20) return swedishUnits[(int) number];
            int rem = (int) (number % 10);
            return swedishTens[(int) number / 10] + swedishUnits[rem];
        }
    }

    @Override
    public String toWords(long number, int wordCase) {
        String s;
        if (number == 0) {
            s = "noll";
        } else {
            s = toWords(number);
        }
        if (wordCase == UPPER_CASE) {
            return s.toUpperCase();
        } else if (wordCase == LOWER_CASE) {
            return s.toLowerCase();
        } else {
            return s;
        }
    }


    private static final String[] swedishMonths = {
            "januari", "februari", "mars", "april", "maj", "juni",
            "juli", "augusti", "september", "oktober", "november", "december"
    };

    /**
     * Get a month name or abbreviation
     *
     * @param month    The month number (1=January, 12=December)
     * @param minWidth The minimum number of characters
     * @param maxWidth The maximum number of characters
     */

    //@Override
    @Override
    public String monthName(int month, int minWidth, int maxWidth) {
        String name = swedishMonths[month - 1];
        if (maxWidth < 3) {
            maxWidth = 3;
        }
        if (name.length() > maxWidth) {
            name = name.substring(0, maxWidth);
        }
        while (name.length() < minWidth) {
            name = name + ' ';
        }
        return name;
    }

    /**
     * Get a day name or abbreviation
     *
     * @param day      The day of the week (1=Monday, 7=Sunday)
     * @param minWidth The minimum number of characters
     * @param maxWidth The maximum number of characters
     */

    @Override
    public String dayName(int day, int minWidth, int maxWidth) {
        String name = swedishDays[day - 1];
        if (maxWidth < 2) {
            maxWidth = 2;
        }
        if (name.length() > maxWidth) {
            name = swedishDayAbbreviations[day - 1];
            if (name.length() > maxWidth) {
                name = name.substring(0, maxWidth);
            }
        }
        while (name.length() < minWidth) {
            name = name + ' ';
        }
        if (minWidth == 1 && maxWidth == 2) {
            // special case
            name = name.substring(0, minUniqueDayLength[day - 1]);
        }
        return name;
    }

    private static final String[] swedishDays = {
            "m\u00e5ndag", "tisdag", "onsdag", "torsdag", "fredag", "l\u00f6rdag", "s\u00f6ndag"
    };

    private static final String[] swedishDayAbbreviations = {
            "m\u00e5", "ti", "on", "to", "fr", "l\u00f6", "s\u00f6"
    };

    /*@NotNull*/ private static final int[] minUniqueDayLength = {
            1, 2, 1, 2, 1, 2, 2
    };


}

