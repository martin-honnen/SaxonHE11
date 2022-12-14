////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.value;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.transpile.CSharpModifiers;
import net.sf.saxon.tree.iter.SingletonIterator;

/**
 * A SingletonClosure represents a value that has not yet been evaluated: the value is represented
 * by an expression, together with saved values of all the context variables that the
 * expression depends on. The value of a SingletonClosure is always either a single item
 * or an empty sequence.
 * <p>The expression may depend on local variables and on the context item; these values
 * are held in the saved XPathContext object that is kept as part of the Closure, and they
 * will always be read from that object. The expression may also depend on global variables;
 * these are unchanging, so they can be read from the Bindery in the normal way. Expressions
 * that depend on other contextual information, for example the values of position(), last(),
 * current(), current-group(), should not be evaluated using this mechanism: they should
 * always be evaluated eagerly. This means that the Closure does not need to keep a copy
 * of these context variables.</p>
 */

public class SingletonClosure extends Closure implements Sequence {

    private boolean built = false;
    /*@Nullable*/ private Item value = null;

    /**
     * Constructor should not be called directly, instances should be made using the make() method.
     *
     * @param exp     the expression to be lazily evaluated
     * @param context the context in which the expression should be evaluated
     * @throws XPathException if an error occurs saving the dynamic context
     */

    public SingletonClosure(/*@NotNull*/ Expression exp, /*@NotNull*/ XPathContext context) throws XPathException {
        expression = exp;
        savedXPathContext = context.newContext();
        saveContext(exp, context);
        //System.err.println("Creating SingletonClosure");
    }

    /**
     * Evaluate the expression in a given context to return an iterator over a sequence
     */

    /*@NotNull*/
    @Override
    public SequenceIterator iterate() {
        try {
            return SingletonIterator.makeIterator(asItem());
        } catch (XPathException e) {
            throw new UncheckedXPathException(e);
        }
    }


    /**
     * Return the value in the form of an Item
     *
     * @return the value in the form of an Item
     * @throws XPathException if an error is detected
     */

    /*@Nullable*/
    public Item asItem() throws XPathException {
        if (!built) {
            value = expression.evaluateItem(savedXPathContext);
            built = true;
            savedXPathContext = null;   // release variables saved in the context to the garbage collector
        }
        return value;
    }

    /**
     * Get the n'th item in the sequence (starting from 0). This is defined for all
     * SequenceValues, but its real benefits come for a SequenceValue stored extensionally.
     *
     * @param n the index of the requested item
     * @return the n'th item in the sequence
     * @throws XPathException if an error is detected
     */

    /*@Nullable*/
    public Item itemAt(int n) throws XPathException {
        if (n != 0) {
            return null;
        }
        return asItem();
    }

    /**
     * Get the length of the sequence
     *
     * @return the length of the sequence
     * @throws XPathException if an error is detected
     */

    public int getLength() throws XPathException {
        return asItem() == null ? 0 : 1;
    }

    /**
     * Return a value containing all the items in the sequence returned by this
     * SequenceIterator
     *
     * @return the corresponding value
     */

    @Override
    @CSharpModifiers(code = {"public", "override"})
    public GroundedValue materialize() {
        try {
            return SequenceTool.itemOrEmpty(asItem());
        } catch (XPathException e) {
            throw new UncheckedXPathException(e);
        }
    }

    @Override
    public SingletonClosure makeRepeatable() {
        return this;
    }

}

