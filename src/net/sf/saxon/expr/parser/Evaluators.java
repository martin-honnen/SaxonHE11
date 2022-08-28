////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.parser;

/**
 * Constants for different mechanisms of expression evaluation
 */

public class Evaluators {

    // These numeric constants must be stable as they are held in the SEF file

    public static final int UNDECIDED = -1;
    public static final int EVALUATE_LITERAL=0;
    public static final int EVALUATE_VARIABLE=1;
    public static final int MAKE_CLOSURE = 3;
    public static final int MAKE_MEMO_CLOSURE=4;
    public static final int RETURN_EMPTY_SEQUENCE = 5;
    public static final int EVALUATE_AND_MATERIALIZE_VARIABLE = 6;
    public static final int CALL_EVALUATE_OPTIONAL_ITEM = 7;
    public static final int ITERATE_AND_MATERIALIZE = 8;
    public static final int PROCESS = 9;
    public static final int LAZY_TAIL_EXPRESSION = 10;
    public static final int SHARED_APPEND_EXPRESSION = 11;
    public static final int MAKE_INDEXED_VARIABLE = 12;
    public static final int MAKE_SINGLETON_CLOSURE = 13;
    public static final int EVALUATE_SUPPLIED_PARAMETER = 14;
    public static final int STREAMING_ARGUMENT = 15;
    public static final int CALL_EVALUATE_SINGLE_ITEM = 16;

    /**
     * Get an evaluator for a given evaluation mechanism
     * @param code one of the constants identifying an evaluation mechanism
     * @return the corresponding {@link Evaluator}
     */
    public static Evaluator getEvaluator(int code) {
        switch(code) {
            case UNDECIDED:
                return Evaluator.EagerSequence.INSTANCE;
            case EVALUATE_LITERAL:
                return Evaluator.Literal.INSTANCE;
            case EVALUATE_VARIABLE:
                return Evaluator.Variable.INSTANCE;
            case MAKE_CLOSURE:
                return Evaluator.LazySequence.INSTANCE;
            case MAKE_MEMO_CLOSURE:
                return Evaluator.MemoClosureEvaluator.INSTANCE;
            case RETURN_EMPTY_SEQUENCE:
                return Evaluator.EmptySequenceEvaluator.INSTANCE;
            case EVALUATE_AND_MATERIALIZE_VARIABLE:
                return Evaluator.Variable.INSTANCE;
            case CALL_EVALUATE_OPTIONAL_ITEM:
                return Evaluator.OptionalItem.INSTANCE;
            case ITERATE_AND_MATERIALIZE:
                return Evaluator.EagerSequence.INSTANCE;
            case PROCESS:
                return Evaluator.Process.INSTANCE;
            case LAZY_TAIL_EXPRESSION:
                return Evaluator.LazyTail.INSTANCE;
            case SHARED_APPEND_EXPRESSION:
                return Evaluator.SharedAppend.INSTANCE;
            case MAKE_INDEXED_VARIABLE:
                return Evaluator.MakeIndexedVariable.INSTANCE;
            case MAKE_SINGLETON_CLOSURE:
                return Evaluator.SingletonClosure.INSTANCE;
            case EVALUATE_SUPPLIED_PARAMETER:
                return Evaluator.SuppliedParameter.INSTANCE;
            case STREAMING_ARGUMENT:
                return Evaluator.StreamingArgument.INSTANCE;
            case CALL_EVALUATE_SINGLE_ITEM:
                return Evaluator.SingleItem.INSTANCE;
            default:
                return Evaluator.EagerSequence.INSTANCE;
        }
    }

}

