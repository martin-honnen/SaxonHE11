////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.serialize.HTMLURIEscaper;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

/**
 * This class supports the function fn:escape-html-uri()
 */

public class EscapeHtmlUri extends ScalarSystemFunction {

    @Override
    public AtomicValue evaluate(Item arg, XPathContext context) throws XPathException {
        final String s = arg.getStringValue();
        return new StringValue(HTMLURIEscaper.escapeURL(s, false, getRetainedStaticContext().getConfiguration()));
    }

    @Override
    public Sequence resultWhenEmpty() {
        return StringValue.EMPTY_STRING;
    }

}

