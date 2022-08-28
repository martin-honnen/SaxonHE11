////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.str.EmptyUnicodeString;
import net.sf.saxon.str.UnicodeString;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;


/**
 * This class implements the replace() function for replacing
 * substrings that match a regular expression
 */

public class Replace extends RegexFunction  {

    private boolean replacementChecked = false;

    @Override
    protected boolean allowRegexMatchingEmptyString() {
        return false;
    }

    /**
     * Make an expression that either calls this function, or that is equivalent to a call
     * on this function
     *
     * @param arguments the supplied arguments to the function call
     * @return either a function call on this function, or an expression that delivers
     * the same result
     */
    @Override
    public Expression makeFunctionCall(Expression... arguments) {
        boolean doEarlyReplacementCheck = true;
        if (arguments.length == 4) {
            if (arguments[3] instanceof StringLiteral) {
                String flags = ((StringLiteral) arguments[3]).stringify();
                if (flags.contains("q") || flags.contains(";")) {
                    doEarlyReplacementCheck = false;
                }
            } else {
                doEarlyReplacementCheck = false;
            }
        }
        if (arguments[2] instanceof StringLiteral && doEarlyReplacementCheck) {
            // Do early checking of the replacement expression if known statically
            UnicodeString rep = ((StringLiteral) arguments[2]).getString();
            if (checkReplacement(rep) == null) {
                replacementChecked = true;
            }
        }
        return super.makeFunctionCall(arguments);
    }

    /**
     * Evaluate the expression
     *
     * @param context   the dynamic evaluation context
     * @param arguments the values of the arguments, supplied as SequenceIterators
     * @return the result of the evaluation, in the form of a SequenceIterator
     * @throws net.sf.saxon.trans.XPathException
     *          if a dynamic error occurs during the evaluation of the expression
     */
    @Override
    public StringValue call(XPathContext context, Sequence[] arguments) throws XPathException {

        StringValue arg0 = (StringValue)arguments[0].head();
        UnicodeString input = arg0 == null ? EmptyUnicodeString.getInstance() : arg0.getUnicodeStringValue();

        UnicodeString replacement = arguments[2].head().getUnicodeStringValue();

        RegularExpression re = getRegularExpression(arguments);
        if (!re.getFlags().contains("q") && !re.isPlatformNative()) {
            if (!replacementChecked) {
                // if it is a string literal, the check was done at compile time
                String msg = checkReplacement(replacement);
                if (msg != null) {
                    throw new XPathException(msg, "FORX0004", context);
                }
            }
        }
        return new StringValue(re.replace(input, replacement));
    }

    /**
     * Check the contents of the replacement string
     *
     * @param rep the replacement string
     * @return null if the string is OK, or an error message if not
     */

    public static String checkReplacement(UnicodeString rep) {
        for (int i = 0; i < rep.length(); i++) {
            int c = rep.codePointAt(i);
            if (c == '$') {
                if (i + 1 < rep.length()) {
                    int index = ++i;
                    int next = rep.codePointAt(index);
                    if (next < '0' || next > '9') {
                        return "Invalid replacement string in replace(): $ sign must be followed by digit 0-9";
                    }
                } else {
                    return "Invalid replacement string in replace(): $ sign at end of string";
                }
            } else if (c == '\\') {
                if (i + 1 < rep.length()) {
                    int index = ++i;
                    int next = rep.codePointAt(index);
                    if (next != '\\' && next != '$') {
                        return "Invalid replacement string in replace(): \\ character must be followed by \\ or $";
                    }
                } else {
                    return "Invalid replacement string in replace(): \\ character at end of string";
                }
            }
        }
        return null;
    }

}

