////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.ma.Parcel;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

/**
 * This class implements the function fn:unparcel(), which is a proposed function in XPath 4.0
 * to unwrap a sequence that has been wrapped as a parcel item
 */

public class UnparcelFn extends SystemFunction {

    /**
     * Evaluate the expression
     *
     * @param context   the dynamic evaluation context
     * @param arguments the values of the arguments, supplied as Sequences
     * @return the result of the evaluation, in the form of a Sequence
     * @throws XPathException
     *          if a dynamic error occurs during the evaluation of the expression
     */
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        Item arg = arguments[0].head();
        return unparcel(arg, context);
    }

    public static GroundedValue unparcel(Item arg, XPathContext context) throws XPathException {
        if (arg instanceof Parcel) {
            return ((Parcel) arg).getValue();
        } else if (Parcel.TYPE.matches(arg, context.getConfiguration().getTypeHierarchy())) {
            return ((MapItem) arg).get(Parcel.parcelKey);
        } else {
            throw new XPathException("The first argument of fn:unparcel must be a parcel", "XPTY0004");
        }
    }
}
