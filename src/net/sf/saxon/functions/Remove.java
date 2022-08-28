////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.functions;

import net.sf.saxon.expr.*;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NumericValue;

/**
 * The XPath 2.0 remove() function
 */

public class Remove extends SystemFunction {

    @Override
    public Expression makeFunctionCall(Expression[] arguments) {

        if (Literal.isAtomic(arguments[1])) {
            Sequence index = ((Literal) arguments[1]).getGroundedValue();
            if (index instanceof IntegerValue) {
                try {
                    long value = ((IntegerValue) index).longValue();
                    if (value <= 0) {
                        return arguments[0];
                    } else if (value == 1) {
                        return new TailExpression(arguments[0], 2);
                    }
                } catch (XPathException err) {
                    //
                }
            }
        }

        return super.makeFunctionCall(arguments);
    }

    /**
     * Evaluate the expression as a general function call
     *
     * @param context   the dynamic evaluation context
     * @param arguments the values of the arguments, supplied as SequenceIterators
     * @return the result of the evaluation, in the form of a SequenceIterator
     * @throws net.sf.saxon.trans.XPathException if a dynamic error occurs during the evaluation of the expression
     */
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        NumericValue n = (NumericValue) arguments[1].head();
        int pos = (int) n.longValue();
        if (pos < 1) {
            return arguments[0];
        }
        return SequenceTool.toLazySequence(new RemoveIterator(arguments[0].iterate(), pos));
    }

    /**
     * An implementation of SequenceIterator that returns all items except the one
     * at a specified position.
     */

    public static class RemoveIterator implements SequenceIterator, LastPositionFinder {

        SequenceIterator base;
        int removePosition;
        int basePosition = 0;
        Item current = null;

        public RemoveIterator(SequenceIterator base, int removePosition) {
            this.base = base;
            this.removePosition = removePosition;
        }

        @Override
        public Item next() {
            current = base.next();
            basePosition++;
            if (current != null && basePosition == removePosition) {
                current = base.next();
                basePosition++;
            }
            return current;
        }

        @Override
        public void close() {
            base.close();
        }

        @Override
        public void discharge() {
            base.discharge();
        }

        /**
         * Ask whether this iterator supports use of the {@link #getLength()} method. This
         * method should always be called before calling {@link #getLength()}, because an iterator
         * that implements this interface may support use of {@link #getLength()} in some situations
         * and not in others
         *
         * @return true if the {@link #getLength()} method can be called to determine the length
         * of the underlying sequence.
         */
        @Override
        public boolean supportsGetLength() {
            return SequenceTool.supportsGetLength(base);
        }

        /**
         * Get the last position (that is, the number of items in the sequence). This method is
         * non-destructive: it does not change the state of the iterator.
         * The result is undefined if the next() method of the iterator has already returned null.
         */

        @Override
        public int getLength() {
            int x = SequenceTool.getLength(base);
            if (removePosition >= 1 && removePosition <= x) {
                return x - 1;
            } else {
                return x;
            }
        }

    }

    @Override
    public String getStreamerName() {
        return "Remove";
    }

}

