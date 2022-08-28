////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.ma;

import net.sf.saxon.ma.map.RecordTest;
import net.sf.saxon.ma.map.SingleEntryMap;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.str.Twine8;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Parcel is a way of wrapping an arbitrary sequence as a single item. It is implemented
 * as a single-entry map, the single key being the string "value", and the corresponding
 * value being the wrapped value.
 */
public class Parcel extends SingleEntryMap {

    /**
     * Construct a list of length 1
     * @param value the value to go in the list
     * @param <T> the type of items in the list
     * @return the singleton list
     */
    private static <T> List<T> singletonList(T value) {
        List<T> result = new ArrayList<>(1);
        result.add(value);
        return result;
    }

    /**
     * The key of the single entry, that is the string "value"
     */
    public static final StringValue parcelKey = new StringValue(new Twine8("value"));

    /**
     * The type of the singleton map: a record type, effectively <code>record(value: item()*)</code>
     */
    public static RecordTest TYPE = new RecordTest(
            singletonList("value"),
            singletonList(SequenceType.ANY_SEQUENCE),
            Collections.emptyList(),
            false);

    /**
     * Create a parcel
     * @param content the value to be wrapped
     */
    public Parcel(GroundedValue content) {
        super(parcelKey, content);
    }

}

