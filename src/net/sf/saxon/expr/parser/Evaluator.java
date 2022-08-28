////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.parser;

import net.sf.saxon.Controller;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.SequenceCollector;
import net.sf.saxon.expr.*;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.ma.zeno.ZenoSequence;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerRange;
import net.sf.saxon.value.MemoClosure;

/**
 * An Evaluator evaluates an expression to return a sequence
 */
public abstract class Evaluator {

    //private int code;

    /**
     * Set the integer code for an Evaluator. Internal use
     */

//    void setCode(int code) {
//        this.code = code;
//        Evaluators.index(this, code);
//    }

    /**
     * Get the integer code for the evaluator
     *
     * @return the integer code for the evaluator
     */

    public abstract int getCode();
//    {
//        return code;
//    }

    /**
     * Evaluate an expression to return a sequence
     * @param expr the expression to be evaluated
     * @param context the dynamic context for evaluation
     * @return the result of the evaluation
     * @throws XPathException if any dynamic error occurs during the evaluation
     */

    public abstract Sequence evaluate(Expression expr, XPathContext context) throws XPathException;

    /**
     * An evaluator that always returns the empty sequence
     */

    public final static class EmptySequenceEvaluator extends Evaluator {
        public final static EmptySequenceEvaluator INSTANCE = new EmptySequenceEvaluator();

        private EmptySequenceEvaluator() {
        }

        @Override
        public int getCode() {
            return Evaluators.RETURN_EMPTY_SEQUENCE;
        }

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) {
            return EmptySequence.getInstance();
        }

    };

    /**
     * An evaluator for arguments supplied as a literal
     */

    public final static class Literal extends Evaluator {

        public static Literal INSTANCE = new Literal();

        private Literal() {
        }

        @Override
        public int getCode() {
            return Evaluators.EVALUATE_LITERAL;
        }

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) {
            return ((net.sf.saxon.expr.Literal)expr).getGroundedValue();
        }

    };

    /**
     * An evaluator for arguments supplied as a variable reference
     */

    public final static class Variable extends Evaluator {
        public static Variable INSTANCE = new Variable();

        private Variable() {
        }

        @Override
        public int getCode() {
            return Evaluators.EVALUATE_VARIABLE;
        }
        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            try {
                return ((VariableReference) expr).evaluateVariable(context);
            } catch (ClassCastException e) {
                // should not happen
                assert false;
                // but if it does...
                return LazySequence.INSTANCE.evaluate(expr, context);
            }
        }

    };

    /**
     * An evaluator for a reference to an external parameter value
     */

    public final static class SuppliedParameter extends Evaluator {
        public final static SuppliedParameter INSTANCE = new SuppliedParameter();

        private SuppliedParameter() {
        }

        @Override
        public int getCode() {
            return Evaluators.EVALUATE_SUPPLIED_PARAMETER;
        }
        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            try {
                return ((SuppliedParameterReference) expr).evaluateVariable(context);
            } catch (ClassCastException e) {
                // should not happen
                assert false;
                // but if it does...
                return LazySequence.INSTANCE.evaluate(expr, context);
            }
        }

    };

    /**
     * A (default) evaluator for arguments supplied as an expression that will always return a
     * singleton item
     */

    public final static class SingleItem extends Evaluator {
        public final static SingleItem INSTANCE = new SingleItem();

        private SingleItem() {
        }

        @Override
        public int getCode() {
            return Evaluators.CALL_EVALUATE_SINGLE_ITEM;
        }
        @Override
        public Item evaluate(Expression expr, XPathContext context) throws XPathException {
            return expr.evaluateItem(context);
        }

    };

    /**
     * A (default) evaluator for arguments supplied as an expression that will return either a
     * singleton item, or an empty sequence
     */

    public final static class OptionalItem extends Evaluator {
        public final static OptionalItem INSTANCE = new OptionalItem();

        public OptionalItem() {
        }

        @Override
        public int getCode() {
            return Evaluators.CALL_EVALUATE_OPTIONAL_ITEM;
        }
        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            Item item = expr.evaluateItem(context);
            if (item == null) {
                return EmptySequence.getInstance();
            } else {
                return item;
            }
        }

    };

    /**
     * An evaluator for arguments that in general return a sequence, where the sequence is evaluated
     * lazily on first use. This is appropriate when calling a function which might not use the value, or
     * might not use all of it. It returns a {@code LazySequence}, which can only be read once, so
     * this is only suitable for use when calling a function that can be trusted to read the argument
     * once only.
     */

    public final static class LazySequence extends Evaluator {
        public final static LazySequence INSTANCE = new LazySequence();

        public LazySequence() {
        }

        @Override
        public int getCode() {
            return Evaluators.MAKE_CLOSURE;
        }
        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            SequenceIterator iter = expr.iterate(context);
            return new net.sf.saxon.om.LazySequence(iter);
        }

    };

    /**
     * An evaluator for arguments that in general return a sequence, where the sequence is evaluated
     * lazily on first use, and where the value might be needed more than once.
     */

    public final static class MemoClosureEvaluator extends Evaluator {
        public final static MemoClosureEvaluator INSTANCE = new MemoClosureEvaluator();

        public MemoClosureEvaluator() {
        }

        @Override
        public int getCode() {
            return Evaluators.MAKE_MEMO_CLOSURE;
        }

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            return new MemoClosure(expr, context);
        }

    };

    /**
     * An evaluator for arguments that in general return a sequence, where the sequence is evaluated
     * lazily on first use, and where the value might be needed more than once.
     */

    public final static class SingletonClosure extends Evaluator {
        public final static SingletonClosure INSTANCE = new SingletonClosure();

        public SingletonClosure() {
        }

        @Override
        public int getCode() {
            return Evaluators.MAKE_SINGLETON_CLOSURE;
        }

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            return new net.sf.saxon.value.SingletonClosure(expr, context);
        }

    };

    /**
     * An evaluator for arguments that in general return a sequence, where the sequence is evaluated
     * eagerly. This is appropriate when it is known that the function will always use the entire value,
     * or when it will use it more than once.
     */

    public final static class EagerSequence extends Evaluator {

        public static EagerSequence INSTANCE = new EagerSequence();

        public EagerSequence() {
        }

        @Override
        public int getCode() {
            return Evaluators.ITERATE_AND_MATERIALIZE;
        }

        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            try {
                SequenceIterator iter = expr.iterate(context);
                return SequenceTool.toGroundedValue(iter);
            } catch (UncheckedXPathException e) {
                throw e.getXPathException();
            }
        }

    };

    /**
     * An evaluator for "shared append" expressions: used when the argument to a function
     * is a block potentially containing a recursive call.
     */

    public final static class SharedAppend extends Evaluator {
        public final static SharedAppend INSTANCE = new SharedAppend();

        public SharedAppend() {
        }

        @Override
        public int getCode() {
            return Evaluators.SHARED_APPEND_EXPRESSION;
        }
        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            try {
                if (expr instanceof Block) {
                    Block block = (Block) expr;
                    Operand[] children = block.getOperanda();
                    ZenoSequence chain = new ZenoSequence();
                    for (Operand o : children) {
                        Expression child = o.getChildExpression();
                        if (child instanceof VariableReference) {
                            GroundedValue seq = ((VariableReference)child).evaluateVariable(context).materialize();
                            chain = chain.appendSequence(seq);
                        } else {
                            SequenceIterator iter = child.iterate(context);
                            Item item;
                            while ((item = iter.next()) != null) {
                                chain = chain.append(item);
                            }
                        }
                    }
                    return chain;
                } else {
                    // it's not a Block: it must have been rewritten after deciding to use this evaluation mode
                    return SequenceTool.toGroundedValue(expr.iterate(context));
                }
            } catch (UncheckedXPathException e) {
                throw e.getXPathException();
            }
        }

    };

    /**
     * An evaluator for the first (streamed) argument of a streamable function call.
     */

    public final static class StreamingArgument extends Evaluator {
        public final static StreamingArgument INSTANCE = new StreamingArgument();

        public StreamingArgument() {
        }

        @Override
        public int getCode() {
            return Evaluators.STREAMING_ARGUMENT;
        }
        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            return context.getConfiguration().obtainOptimizer().evaluateStreamingArgument(expr, context);
        }

    };

    /**
     * An evaluator for an expression that makes use of an indexed variable
     */

    public final static class MakeIndexedVariable extends Evaluator {
        public final static MakeIndexedVariable INSTANCE = new MakeIndexedVariable();

        public MakeIndexedVariable() {
        }

        @Override
        public int getCode() {
            return Evaluators.MAKE_INDEXED_VARIABLE;
        }
        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            return context.getConfiguration().obtainOptimizer().makeIndexedValue(expr.iterate(context));
        }

    };

    /**
     * A push-mode evaluator for an expression
     */

    public final static class Process extends Evaluator {
        public final static Process INSTANCE = new Process();

        public Process() {
        }

        @Override
        public int getCode() {
            return Evaluators.PROCESS;
        }
        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            Controller controller = context.getController();
            SequenceCollector seq = controller.allocateSequenceOutputter();
            ComplexContentOutputter out = new ComplexContentOutputter(seq);
            out.open();
            expr.process(out, context);
            out.close();
            Sequence val = seq.getSequence();
            seq.reset();
            return val;
        }

    };

    /**
     * An evaluator for arguments that in general return a sequence, where the sequence is evaluated
     * lazily on first use. This is appropriate when calling a function which might not use the value, or
     * might not use all of it. It returns a {@code LazySequence}, which can only be read once, so
     * this is only suitable for use when calling a function that can be trusted to read the argument
     * once only.
     */

    public final static class LazyTail extends Evaluator {
        public final static LazyTail INSTANCE = new LazyTail();

        public LazyTail() {
        }

        @Override
        public int getCode() {
            return Evaluators.LAZY_TAIL_EXPRESSION;
        }
        @Override
        public Sequence evaluate(Expression expr, XPathContext context) throws XPathException {
            TailExpression tail = (TailExpression) expr;
            VariableReference vr = (VariableReference) tail.getBaseExpression();
            Sequence base = Variable.INSTANCE.evaluate(vr, context);
            if (base instanceof net.sf.saxon.value.MemoClosure) {
                SequenceIterator it = base.iterate();
                base = SequenceTool.toGroundedValue(it);
            }
            if (base instanceof IntegerRange) {
                long step = ((IntegerRange) base).getStep();
                long start = ((IntegerRange) base).getStart() + (tail.getStart() - 1) * step;
                long end = ((IntegerRange) base).getEnd();
                if (start == end) {
                    return Int64Value.makeIntegerValue(end);
                } else if (start > end) {
                    return EmptySequence.getInstance();
                } else {
                    return new IntegerRange(start, step, end);
                }
            }
            if (base instanceof GroundedValue) {
                GroundedValue baseSeq = (GroundedValue)base;
                return baseSeq.subsequence(tail.getStart() - 1, baseSeq.getLength() - tail.getStart() + 1);
            }

            return new net.sf.saxon.value.MemoClosure(tail, context);
        }

    };


}



