////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.value;

import net.sf.saxon.str.*;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ValidationFailure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of the xs:gDay data type
 */

public class GDayValue extends GDateValue {

    private static final Pattern regex =
            Pattern.compile("---([0-9][0-9])(Z|[+-][0-9][0-9]:[0-9][0-9])?");

    private GDayValue() {
    }

    public static ConversionResult makeGDayValue(UnicodeString value) {
        final UnicodeString trimmed = Whitespace.trim(value);
        Matcher m = regex.matcher(trimmed.toString());
        if (!m.matches()) {
            return new ValidationFailure("Cannot convert '" + value + "' to a gDay");
        }
        GDayValue g = new GDayValue();
        String base = m.group(1);
        String tz = m.group(2);
        String date = "2000-01-" + base + (tz == null ? "" : tz);
        g.typeLabel = BuiltInAtomicType.G_DAY;
        return setLexicalValue(g, BMPString.of(date), true);
    }

    public GDayValue(byte day, int tz) {
        this(day, tz, BuiltInAtomicType.G_DAY);
    }

    public GDayValue(byte day, int tz, AtomicType type) {
        this.year = 2000;
        this.month = 1;
        this.day = day;
        setTimezoneInMinutes(tz);
        this.typeLabel = type;
    }

    /**
     * Make a copy of this date, time, or dateTime value
     *
     * @param typeLabel the type label of the new copy. The caller is responsible for checking that
     *                  the value actually conforms to this type.
     * @return the copied value
     */

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        GDayValue v = new GDayValue(day, getTimezoneInMinutes());
        v.typeLabel = typeLabel;
        return v;
    }

    /**
     * Determine the primitive type of the value. This delivers the same answer as
     * getItemType().getPrimitiveItemType(). The primitive types are
     * the 19 primitive types of XML Schema, plus xs:integer, xs:dayTimeDuration and xs:yearMonthDuration,
     * and xs:untypedAtomic. For external objects, the result is AnyAtomicType.
     */

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.G_DAY;
    }

    /*@NotNull*/
    @Override
    public UnicodeString getPrimitiveStringValue() {

        UnicodeBuilder sb = new UnicodeBuilder(16);

        sb.appendLatin("---");
        appendTwoDigits(sb, day);

        if (hasTimezone()) {
            appendTimezone(sb);
        }

        return sb.toUnicodeString();

    }

    /**
     * Add a duration to this date/time value
     *
     * @param duration the duration to be added (which might be negative)
     * @return a new date/time value representing the result of adding the duration. The original
     *         object is not modified.
     * @throws net.sf.saxon.trans.XPathException if an error is detected
     *
     */

    @Override
    public CalendarValue add(DurationValue duration) throws XPathException {
        XPathException err = new XPathException("Cannot add a duration to an xs:gDay");
        err.setErrorCode("XPTY0004");
        throw err;
    }

    /**
     * Return a new date, time, or dateTime with the same normalized value, but
     * in a different timezone
     *
     * @param tz the new timezone, in minutes
     * @return the date/time in the new timezone
     */

    @Override
    public CalendarValue adjustTimezone(int tz) {
        DateTimeValue dt = (DateTimeValue) toDateTime().adjustTimezone(tz);
        return new GDayValue(dt.getDay(), dt.getTimezoneInMinutes());
    }
}

