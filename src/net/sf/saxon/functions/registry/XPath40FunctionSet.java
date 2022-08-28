////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.functions.registry;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.*;
import net.sf.saxon.ma.Parcel;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import java.net.URISyntaxException;

/**
 * Function signatures (and pointers to implementations) of the functions defined in XPath 3.1 without the
 * Higher-Order-Functions feature
 */

public class XPath40FunctionSet extends BuiltInFunctionSet {

    private static final XPath40FunctionSet THE_INSTANCE = new XPath40FunctionSet();

    public static XPath40FunctionSet getInstance() {
        return THE_INSTANCE;
    }

    private XPath40FunctionSet() {
        init();
    }

    private void init() {

        importFunctionSet(XPath31FunctionSet.getInstance());

        SpecificFunctionType predicate = new SpecificFunctionType(
                new SequenceType[]{SequenceType.SINGLE_ITEM},
                SequenceType.SINGLE_BOOLEAN);

        SpecificFunctionType ft = new SpecificFunctionType(
                new SequenceType[]{SequenceType.SINGLE_ITEM},
                SequenceType.ATOMIC_SEQUENCE);

        register("all", 2, SefFunction.class, BuiltInAtomicType.BOOLEAN, ONE, AS_ARG0 | LATE)
                .arg(0, AnyItemType.getInstance(), STAR, EMPTY)
                .arg(1, predicate, ONE, null);

        register("characters", 1, CharactersFn.class, BuiltInAtomicType.STRING, STAR, 0)
                .arg(0, BuiltInAtomicType.STRING, ONE, null);

        // codepoints-to-string becomes variadic in 4.0:
        register("codepoints-to-string", 1, CodepointsToString.class, BuiltInAtomicType.STRING, ONE, SEQV)
                .arg(0, BuiltInAtomicType.INTEGER, STAR, null);

        // Concat changes in 4.0:
        register("concat", 1, Concat.class, BuiltInAtomicType.STRING, ONE, SEQV)
                .arg(0, BuiltInAtomicType.ANY_ATOMIC, STAR, null);

        register("highest", 1, HighestOrLowest.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null);

        register("highest", 2, HighestOrLowest.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, BuiltInAtomicType.STRING, OPT, null);

        register("highest", 3, HighestOrLowest.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, BuiltInAtomicType.STRING, OPT, null)
                .arg(2, ft, ONE, null);

        register("identity", 1, SefFunction.class, AnyItemType.getInstance(), STAR, AS_ARG0 | LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null);

        register("index-where", 2, SefFunction.class, BuiltInAtomicType.INTEGER, STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, predicate, ONE, null);

        register("in-scope-namespaces", 1, SefFunction.class, MapType.ANY_MAP_TYPE, ONE, LATE)
                .arg(0, NodeKindTest.ELEMENT, STAR, null);

        register("is-NaN", 1, SefFunction.class, BuiltInAtomicType.BOOLEAN, ONE, LATE)
                .arg(0, BuiltInAtomicType.ANY_ATOMIC, ONE, null);

        register("items-before", 2, SefFunction.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, predicate, ONE, null);

        register("items-until", 2, SefFunction.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, predicate, ONE, null);

        register("items-from", 2, SefFunction.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, predicate, ONE, null);

        register("items-after", 2, SefFunction.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, predicate, ONE, null);

        register("lowest", 1, HighestOrLowest.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null);

        register("lowest", 2, HighestOrLowest.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, BuiltInAtomicType.STRING, OPT, null);

        register("lowest", 3, HighestOrLowest.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, BuiltInAtomicType.STRING, OPT, null)
                .arg(2, ft, ONE, null);

        register("parcel", 1, ParcelFn.class, Parcel.TYPE, ONE, 0)
                .arg(0, AnyItemType.getInstance(), STAR, null);

        register("parts", 1, Parts.class, new MapType(BuiltInAtomicType.STRING, SequenceType.ANY_SEQUENCE), ONE, 0)
                .arg(0, BuiltInAtomicType.ANY_ATOMIC, ONE, null);

        register("range", 2, SefFunction.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, predicate, OPT, null);

        register("range", 3, SefFunction.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, predicate, OPT, null)
                .arg(2, predicate, OPT, null);

        register("range", 4, SefFunction.class, AnyItemType.getInstance(), STAR, LATE)
                .arg(0, AnyItemType.getInstance(), STAR, null)
                .arg(1, predicate, OPT, null)
                .arg(2, predicate, OPT, null)
                .arg(3, MapType.ANY_MAP_TYPE,ONE, null);

        register("slice", 2, SefFunction.class, AnyItemType.getInstance(), STAR, AS_ARG0 | LATE)
                .arg(0, AnyItemType.getInstance(), STAR, EMPTY)
                .arg(1, MapType.ANY_MAP_TYPE, ONE, null);   // TODO: define a record type

        register("some", 2, SefFunction.class, BuiltInAtomicType.BOOLEAN, ONE, AS_ARG0 | LATE)
                .arg(0, AnyItemType.getInstance(), STAR, EMPTY)
                .arg(1, predicate, ONE, null);

        register("unparcel", 1, UnparcelFn.class, AnyItemType.getInstance(), STAR, 0)
                .arg(0, Parcel.TYPE, ONE, null);
    }

    public static class HighestOrLowest extends SefFunction {

        public HighestOrLowest(){
            super();
        };

        @Override
        public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
            Sequence input = arguments[0];
            String collationUri = null;
            if (arguments.length >= 2) {
                StringValue val = (StringValue)arguments[1].head();
                if (val != null) {
                    try {
                        collationUri = ResolveURI.makeAbsolute(val.getStringValue(), getStaticBaseUriString()).toString();
                    } catch (URISyntaxException e) {
                        throw new XPathException(e);
                    }
                }
            }
            if (collationUri == null) {
                collationUri = getRetainedStaticContext().getDefaultCollationName();
            }
            Function key;
            if (arguments.length >= 3) {
                key = (Function) arguments[2].head();
            } else {
                key = SystemFunction.makeFunction("data", getRetainedStaticContext(), 1);
            }
            SymbolicName.F delegate =
                    new SymbolicName.F(new StructuredQName("", "http://ns.saxonica.com/xpath-functions", "mhk_highest"), 4);

            Sequence[] args = new Sequence[4];
            args[0] = input.materialize();
            args[1] = new StringValue(collationUri);
            args[2] = key;
            args[3] = BooleanValue.get(getDetails().name.getLocalPart().equals("highest"));

            return callFunction(context, delegate, args);
        }
    }

}
