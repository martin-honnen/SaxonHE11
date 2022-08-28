////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.SubstringMatcher;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.str.EmptyUnicodeString;
import net.sf.saxon.str.UnicodeString;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

/**
 * Implements the fn:substring-after() function with the collation already known
 */
public class SubstringAfter extends CollatingFunctionFixed {

    @Override
    public boolean isSubstringMatchingFunction() {
        return true;
    }

    /**
     * Evaluate the expression
     *
     * @param context   the dynamic evaluation context
     * @param arguments the values of the arguments, supplied as Sequences
     * @return the result of the evaluation, in the form of a Sequence
     * @throws net.sf.saxon.trans.XPathException
     *          if a dynamic error occurs during the evaluation of the expression
     */
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        UnicodeString s0 = getUniStringArg(arguments[0]);
        UnicodeString s1 = getUniStringArg(arguments[1]);

        return new StringValue(substringAfter(s0, s1, (SubstringMatcher)getStringCollator()));
    }

    private static UnicodeString substringAfter(UnicodeString arg1, UnicodeString arg2, SubstringMatcher collator) {
        if (arg1 == null) {
            arg1 = EmptyUnicodeString.getInstance();
        }
        if (arg2 == null) {
            arg2 = EmptyUnicodeString.getInstance();
        }
        if (arg2.isEmpty()) {
            return arg1;
        }
        if (arg1.isEmpty()) {
            return EmptyUnicodeString.getInstance();
        }

        return collator.substringAfter(arg1, arg2);
    }

    @Override
    public String getCompilerName() {
        return "SubstringAfterCompiler";
    }

}
