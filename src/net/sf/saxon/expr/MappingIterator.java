////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.transpile.CSharpInjectMembers;

/**
 * MappingIterator merges a sequence of sequences into a single flat
 * sequence. It takes as inputs an iteration, and a mapping function to be
 * applied to each Item returned by that iteration. The mapping function itself
 * returns another iteration. The result is an iteration of the concatenation of all
 * the iterations returned by the mapping function.
 * <p>This is a powerful class. It is used, with different mapping functions,
 * in a great variety of ways. It underpins the way that "for" expressions and
 * path expressions are evaluated, as well as sequence expressions. It is also
 * used in the implementation of the document(), key(), and id() functions.</p>
 * <p>In functional programming theory, this iterator implements a "flatMap"
 * operation.</p>
 */

@CSharpInjectMembers(code = {
        "public MappingIterator(Saxon.Hej.om.SequenceIterator first, Saxon.Hej.expr.SequenceMapper.Lambda action) : this(first, Saxon.Hej.expr.SequenceMapper.of(action)) {}",
})
public class MappingIterator implements SequenceIterator {

    private final SequenceIterator base;
    private final MappingFunction action;
    private SequenceIterator results = null;
    private boolean discharged;

    /**
     * Construct a MappingIterator that will apply a specified MappingFunction to
     * each Item returned by the base iterator.
     *
     * @param base   the base iterator
     * @param action the mapping function to be applied: a function from items to SequenceIterators.
     */

    public MappingIterator(SequenceIterator base, MappingFunction action) {
        this.base = base;
        this.action = action;
    }

    /**
     * Static factory method designed to handle the case where the mapping function is supplied
     * as a lambda expression. (Although a lambda expression can be used directly as the mapping
     * function in Java, this is not possible in C#).
     * @param base   the base iterator
     * @param mappingExpression the mapping function to be applied: a function from items to SequenceIterators.
     * @return a flattened iterator
     */

    public static MappingIterator map(SequenceIterator base, SequenceMapper.Lambda mappingExpression) {
        return new MappingIterator(base, SequenceMapper.of(mappingExpression));
    }

    @Override
    public Item next() {
        try {
            Item nextItem;
            while (true) {
                if (results != null) {
                    nextItem = results.next();
                    if (nextItem != null) {
                        break;
                    } else {
                        results = null;
                    }
                }
                Item nextSource = base.next();
                if (nextSource != null) {
                    // Call the supplied mapping function
                    SequenceIterator obj = action.map(nextSource);
                    if (discharged) {
                        obj.discharge();
                    }

                    // The result may be null (representing an empty sequence),
                    //  or a SequenceIterator (any sequence)

                    if (obj != null) {
                        results = obj;
                        nextItem = results.next();
                        if (nextItem == null) {
                            results = null;
                        } else {
                            break;
                        }
                    }
                    // now go round the loop to get the next item from the base sequence
                } else {
                    results = null;
                    return null;
                }
            }

            return nextItem;
        } catch (XPathException e) {
            throw new UncheckedXPathException(e);
        }
    }

    @Override
    public void close() {
        if (results != null) {
            results.close();
        }
        base.close();
    }

    @Override
    public void discharge() {
        if (results != null) {
            results.discharge();
        }
        base.discharge();
        discharged = true;
    }

}

