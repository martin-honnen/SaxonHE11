////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SequenceIteratorOverJavaIterator;
import net.sf.saxon.value.ObjectValue;

import java.util.Iterator;

/**
 * This class implements the changes to the tuple stream effected by a "for" clause in a FLWOR expression
 */
public class ForMemberClausePull extends ForClausePull {

    ArrayItem arrayItem;

    public ForMemberClausePull(TuplePull base, ForMemberClause forClause) {
        super(base, forClause);
    }

    @Override
    protected SequenceIterator getIterator(XPathContext context) throws XPathException {
        Expression sequence = forClause.getSequence();
        arrayItem = (ArrayItem)sequence.evaluateItem(context);
        Iterator<GroundedValue> members = arrayItem.members().iterator();
        //noinspection Convert2MethodRef
        return new SequenceIteratorOverJavaIterator<GroundedValue>(members,
                                                                   mem -> new ObjectValue<GroundedValue>(mem));
    }

    @Override
    protected GroundedValue variableValue(Item item) {
        return ((ObjectValue<GroundedValue>)item).getObject();
    }


}

