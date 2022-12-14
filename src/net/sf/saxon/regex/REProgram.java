////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Originally part of Apache's Jakarta project (downloaded January 2012),
 * this file has been extensively modified for integration into Saxon by
 * Michael Kay, Saxonica.
 */

package net.sf.saxon.regex;

import net.sf.saxon.str.UnicodeString;
import net.sf.saxon.z.IntPredicateProxy;

import java.util.List;

/**
 * A class that holds compiled regular expressions.
 */

public class REProgram {
    protected static final int OPT_HASBACKREFS = 1;
    protected static final int OPT_HASBOL = 2;

    protected Operation operation;
    protected REFlags flags;
    protected UnicodeString prefix;              // Prefix string optimization
    protected IntPredicateProxy initialCharClass;
    protected List<RegexPrecondition> preconditions = new java.util.ArrayList<RegexPrecondition>();
    protected int minimumLength = 0;
    protected int fixedLength = -1;
    protected int optimizationFlags;      // Optimization flags (REProgram.OPT_*)
    protected int maxParens = -1;
    protected int backtrackingLimit = -1;

    /**
     * Constructs a program object from a character array
     * @param operation Array with RE opcode instructions in it. The "next"
     * @param parens       Count of parens in the program
     *                     pointers within the operations must already have been converted to absolute
     *                     offsets.
     * @param flags the regular expression flags
     */
    public REProgram(Operation operation, int parens, REFlags flags) {
        this.flags = flags;
        setOperation(operation);
        this.maxParens = parens;
    }

    /**
     * Sets a new regular expression program to run.  It is this method which
     * performs any special compile-time search optimizations.  Currently only
     * two optimizations are in place - one which checks for backreferences
     * (so that they can be lazily allocated) and another which attempts to
     * find an prefix anchor string so that substantial amounts of input can
     * potentially be skipped without running the actual program.
     *
     * @param operation Program instruction buffer
     */
    private void setOperation(Operation operation) {
        // Save reference to instruction array
        this.operation = operation;

        // Initialize other program-related variables
        this.optimizationFlags = 0;
        this.prefix = null;

        this.operation = operation.optimize(this, flags);

        // Try various compile-time optimizations

        if (operation instanceof OpSequence) {
            Operation first = ((OpSequence)operation).getOperations().get(0);
            if (first instanceof OpBOL) {
                optimizationFlags |= REProgram.OPT_HASBOL;
            } else if (first instanceof OpAtom) {
                prefix = ((OpAtom)first).getAtom();
            } else if (first instanceof OpCharClass) {
                initialCharClass = ((OpCharClass)first).getPredicate();
            }
            addPrecondition(operation, -1, 0);
        }

        minimumLength = operation.getMinimumMatchLength();
        fixedLength = operation.getMatchLength();

    }

    public void setBacktrackingLimit(int limit) {
        this.backtrackingLimit = limit;
    }

    public int getBacktrackingLimit() {
        return backtrackingLimit;
    }

    private void addPrecondition(Operation op, int fixedPosition, int minPosition) {
        if (op instanceof OpAtom || op instanceof OpCharClass) {
            preconditions.add(new RegexPrecondition(op, fixedPosition, minPosition));
        } else if (op instanceof OpRepeat && ((OpRepeat) op).min >= 1) {
            OpRepeat parent = (OpRepeat) op;
            Operation child = parent.op;
            if (child instanceof OpAtom || child instanceof OpCharClass) {
                if (parent.min == 1) {
                    preconditions.add(new RegexPrecondition(parent, fixedPosition, minPosition));
                } else {
                    OpRepeat parent2 = new OpRepeat(child, parent.min, parent.min, true);
                    preconditions.add(new RegexPrecondition(parent2, fixedPosition, minPosition));
                }
            } else {
                addPrecondition(child, fixedPosition, minPosition);
            }
        } else if (op instanceof OpCapture) {
            addPrecondition(((OpCapture)op).childOp, fixedPosition, minPosition);
        } else if (op instanceof OpSequence) {
            int fp = fixedPosition;
            int mp = minPosition;
            for (Operation o : ((OpSequence)op).getOperations()) {
                if (o instanceof OpBOL) {
                    fp = 0;
                }
                addPrecondition(o, fp, mp);
                if (fp != -1 && o.getMatchLength() != -1) {
                    fp += o.getMatchLength();
                } else {
                    fp = -1;
                }
                mp += o.getMinimumMatchLength();
            }
        }
    }

    /**
     * Ask whether the regular expression is known statically to match a zero length string
     *
     * @return true if the regex is known statically to match a zero length string. If
     * the result is true, then there is definitely a match; if the result is false, then
     * it is not known statically whether there is a match (this arises when the expression
     * contains back-references).
     */

    public boolean isNullable() {
        int m = operation.matchesEmptyString();
        return (m & Operation.MATCHES_ZLS_ANYWHERE) != 0;
    }

    /**
     * Returns a copy of the prefix of current regular expression program
     * in a character array.  If there is no prefix, or there is no program
     * compiled yet, <code>getPrefix</code> will return null.
     *
     * @return A copy of the prefix of current compiled RE program
     */
    public UnicodeString getPrefix() {
        return prefix;
    }


}
