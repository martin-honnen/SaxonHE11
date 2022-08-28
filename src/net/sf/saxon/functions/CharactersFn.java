////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.functions;

import net.sf.saxon.expr.ItemMapper;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.str.UnicodeChar;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.StringValue;

/**
 * Implements the proposed fn:characters() function. This splits a supplied string into a sequence of
 * single-character strings
 */

public class CharactersFn extends SystemFunction {

    /**
     * Evaluate this function call
     *
     * @param context   The XPath dynamic evaluation context
     * @param arguments The values of the arguments to the function call. Each argument value (which is in general
     *                  a sequence) is supplied in the form of a sequence.
     * @return the results of the function.
     * @throws XPathException if a dynamic error occurs during evaluation of the function. The Saxon run-time
     *                                           code will add information about the error location.
     */

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue s = (StringValue)arguments[0].head();
        SequenceIterator codepoints = s.iterateCharacters();
        ItemMappingIterator mapper = new ItemMappingIterator(codepoints, ItemMapper.of(cp ->
            new StringValue(new UnicodeChar((int) ((IntegerValue) cp).longValue()))
        ));
        return new LazySequence(mapper);
    }


}

// Copyright (c) 2020 Saxonica Limited

