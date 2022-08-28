////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.functions;


import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.map.RecordTest;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.transpile.CSharpReplaceBody;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

/**
 * Proposed XPath 4.0 function that returns the components of an atomic value as a map (for example, the components
 * of a dateTime, a duration, or a URI).
 */
public class Parts extends SystemFunction implements Callable {

    /**
     * Get the return type, given knowledge of the actual arguments
     *
     * @param args the actual arguments supplied
     * @return the best available item type that the function will return
     */

    public ItemType getResultItemType(Expression[] args) {
        AtomicType argType = args[0].getStaticType().getPrimaryType().getAtomizedItemType().getPrimitiveItemType();
        if (argType == BuiltInAtomicType.ANY_ATOMIC) {
            return super.getResultItemType(args);
        }
        while (argType.getBaseType() != BuiltInAtomicType.ANY_ATOMIC) {
            // Handle xs:integer, xs:dayTimeDuration, xs:yearMonthDuration
            argType = (AtomicType)argType.getBaseType();
        }
        RecordTest resultType = resultTypeMap.get(argType);
        if (resultType == null) {
            return super.getResultItemType(args);
        } else {
            return resultType;
        }
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        DictionaryMap result = new DictionaryMap();
        AtomicValue subject = (AtomicValue)args[0].head();
        AtomicType type = subject.getPrimitiveType();
        if (type.getBaseType() == BuiltInAtomicType.DURATION) {
            type = BuiltInAtomicType.DURATION;
        }
        Map<String, ComponentDetails> details = componentMap.get(type);
        if (details == null) {
            details = new HashMap<>();
        }
        for (Map.Entry<String, ComponentDetails> component : details.entrySet()) {
            result.initialPut(component.getKey(), SequenceTool.toGroundedValue(component.getValue().supplier.apply(subject)));
        }
        result.initialPut("value", subject);
        return result;
    }

    private static class ComponentDetails {
        SequenceType componentType;
        Function<AtomicValue, SequenceIterator> supplier;
    }

    private final static Map<AtomicType, Map<String, ComponentDetails>> componentMap = new HashMap<>();

    private static void register(BuiltInAtomicType type, String fieldName, SequenceType componentType,
                                 Function<AtomicValue, SequenceIterator> supplier) {
        //noinspection Convert2Diamond
        Map<String, ComponentDetails> typeData =
                componentMap.computeIfAbsent(type, k -> new HashMap<String, ComponentDetails>());
        ComponentDetails details = new ComponentDetails();
        details.componentType = componentType;
        details.supplier = supplier;
        typeData.put(fieldName, details);
    }

    static SequenceIterator oneInt(long x) {
        return SingletonIterator.makeIterator(Int64Value.makeIntegerValue(x));
    }

    static SequenceIterator oneDec(BigDecimal x) {
        return SingletonIterator.makeIterator(new BigDecimalValue(x));
    }

    static SequenceIterator timezone(CalendarValue a) {
        if (a.hasTimezone()) {
            try {
                return SingletonIterator.makeIterator(
                        DayTimeDurationValue.fromMilliseconds(60000L * a.getTimezoneInMinutes()));
            } catch (ValidationException e) {
                return EmptyIterator.ofAtomic();
            }
        } else {
            return EmptyIterator.ofAtomic();
        }
    }


    static {
        register(BuiltInAtomicType.DATE_TIME, "year", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((DateTimeValue) a).getYear())
        );
        register(BuiltInAtomicType.DATE_TIME, "month", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((DateTimeValue) a).getMonth())
        );
        register(BuiltInAtomicType.DATE_TIME, "day", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((DateTimeValue) a).getDay())
        );
        register(BuiltInAtomicType.DATE_TIME, "hours", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((DateTimeValue) a).getHour())
        );
        register(BuiltInAtomicType.DATE_TIME, "minutes", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((DateTimeValue) a).getMinute())
        );
        register(BuiltInAtomicType.DATE_TIME, "seconds", SequenceType.SINGLE_INTEGER,
                 a -> {
                     BigDecimal d = BigDecimal.valueOf(((DateTimeValue)a).getNanosecond());
                     d = d.divide(BigDecimalValue.BIG_DECIMAL_ONE_BILLION, 6, RoundingMode.HALF_UP);
                     d = d.add(BigDecimal.valueOf(((DateTimeValue) a).getSecond()));
                     return oneDec(d);
                 }
        );
        register(BuiltInAtomicType.DATE_TIME, "timezone", SequenceType.OPTIONAL_DAY_TIME_DURATION,
                 a -> timezone((DateTimeValue) a)
        );

        register(BuiltInAtomicType.DATE, "year", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((DateValue) a).getYear())
        );
        register(BuiltInAtomicType.DATE, "month", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((DateValue) a).getMonth())
        );
        register(BuiltInAtomicType.DATE, "day", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((DateValue) a).getDay())
        );
        register(BuiltInAtomicType.DATE, "timezone", SequenceType.OPTIONAL_DAY_TIME_DURATION,
                 a -> timezone((DateValue) a)
        );

        register(BuiltInAtomicType.TIME, "hours", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((TimeValue) a).getHour())
        );
        register(BuiltInAtomicType.TIME, "minutes", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((TimeValue) a).getMinute())
        );
        register(BuiltInAtomicType.TIME, "seconds", SequenceType.SINGLE_INTEGER,
                 a -> {
                     BigDecimal d = BigDecimal.valueOf(((TimeValue) a).getNanosecond());
                     d = d.divide(BigDecimalValue.BIG_DECIMAL_ONE_BILLION, 6, RoundingMode.HALF_UP);
                     d = d.add(BigDecimal.valueOf(((TimeValue) a).getSecond()));
                     return oneDec(d);
                 }
        );
        register(BuiltInAtomicType.TIME, "timezone", SequenceType.OPTIONAL_DAY_TIME_DURATION,
                 a -> timezone((TimeValue) a)
        );

        register(BuiltInAtomicType.G_YEAR, "year", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((GDateValue) a).getYear())
        );
        register(BuiltInAtomicType.G_YEAR, "timezone", SequenceType.OPTIONAL_DAY_TIME_DURATION,
                 a -> timezone((GDateValue) a)
        );

        register(BuiltInAtomicType.G_YEAR_MONTH, "year", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((GDateValue) a).getYear())
        );
        register(BuiltInAtomicType.G_YEAR_MONTH, "month", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((GDateValue) a).getMonth())
        );
        register(BuiltInAtomicType.G_YEAR_MONTH, "timezone", SequenceType.OPTIONAL_DAY_TIME_DURATION,
                 a -> timezone((GDateValue) a)
        );

        register(BuiltInAtomicType.G_MONTH, "month", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((GDateValue) a).getMonth())
        );
        register(BuiltInAtomicType.G_MONTH, "timezone", SequenceType.OPTIONAL_DAY_TIME_DURATION,
                 a -> timezone((GDateValue) a)
        );

        register(BuiltInAtomicType.G_MONTH_DAY, "month", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((GDateValue) a).getMonth())
        );
        register(BuiltInAtomicType.G_MONTH_DAY, "day", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((GDateValue) a).getDay())
        );
        register(BuiltInAtomicType.G_MONTH_DAY, "timezone", SequenceType.OPTIONAL_DAY_TIME_DURATION,
                 a -> timezone((GDateValue) a)
        );

        register(BuiltInAtomicType.G_DAY, "day", SequenceType.SINGLE_INTEGER,
                 a -> oneInt(((GDateValue) a).getDay())
        );
        register(BuiltInAtomicType.G_DAY, "timezone", SequenceType.OPTIONAL_DAY_TIME_DURATION,
                 a -> timezone((GDateValue) a)
        );

        register(BuiltInAtomicType.DURATION, "years", SequenceType.SINGLE_INTEGER,
                 a -> oneInt((long) ((DurationValue) a).getYears() * ((DurationValue) a).signum())
        );
        register(BuiltInAtomicType.DURATION, "months", SequenceType.SINGLE_INTEGER,
                 a -> oneInt((long) ((DurationValue) a).getMonths() * ((DurationValue) a).signum())
        );
        register(BuiltInAtomicType.DURATION, "days", SequenceType.SINGLE_INTEGER,
                 a -> oneInt((long) ((DurationValue) a).getDays() * ((DurationValue) a).signum())
        );
        register(BuiltInAtomicType.DURATION, "hours", SequenceType.SINGLE_INTEGER,
                 a -> oneInt((long) ((DurationValue) a).getHours() * ((DurationValue) a).signum())
        );
        register(BuiltInAtomicType.DURATION, "minutes", SequenceType.SINGLE_INTEGER,
                 a -> oneInt((long) ((DurationValue) a).getMinutes() * ((DurationValue) a).signum())
        );
        register(BuiltInAtomicType.DURATION, "seconds", SequenceType.SINGLE_INTEGER,
                 a -> SingletonIterator.makeIterator(((DurationValue) a).getComponent(AccessorFn.Component.SECONDS))
        );

        register(BuiltInAtomicType.HEX_BINARY, "octets", SequenceType.makeSequenceType(BuiltInAtomicType.UNSIGNED_BYTE, StaticProperty.ALLOWS_ZERO_OR_MORE),
                 a -> {
                     List<AtomicValue> result = new ArrayList<>();
                     for (byte b : ((HexBinaryValue)a).getBinaryValue()) {
                         result.add(new Int64Value(b & 0xFF, BuiltInAtomicType.UNSIGNED_BYTE));
                     }
                     return new ListIterator.Of<>(result);
                 }
        );

        register(BuiltInAtomicType.BASE64_BINARY, "octets", SequenceType.makeSequenceType(BuiltInAtomicType.UNSIGNED_BYTE, StaticProperty.ALLOWS_ZERO_OR_MORE),
                 a -> {
                     List<AtomicValue> result = new ArrayList<>();
                     for (byte b : ((Base64BinaryValue) a).getBinaryValue()) {
                         result.add(new Int64Value(b & 0xFF, BuiltInAtomicType.UNSIGNED_BYTE));
                     }
                     return new ListIterator.Of<>(result);
                 }
        );

        register(BuiltInAtomicType.QNAME, "prefix", SequenceType.OPTIONAL_STRING,
                 a -> {
                    String prefix = ((QNameValue) a).getStructuredQName().getPrefix();
                    return prefix.isEmpty()
                            ? EmptyIterator.ofAtomic()
                            : SingletonIterator.makeIterator(new StringValue(prefix, BuiltInAtomicType.NCNAME));
                 }
        );
        register(BuiltInAtomicType.QNAME, "namespace-uri", SequenceType.OPTIONAL_STRING,
                 a -> {
                     String uri = ((QNameValue) a).getStructuredQName().getURI();
                     return uri.isEmpty()
                             ? EmptyIterator.ofAtomic()
                             : SingletonIterator.makeIterator(new StringValue(uri, BuiltInAtomicType.ANY_URI));
                 }
        );
        register(BuiltInAtomicType.QNAME, "local-name", SequenceType.SINGLE_STRING,
                 a -> {
                     String local = ((QNameValue) a).getStructuredQName().getLocalPart();
                     return SingletonIterator.makeIterator(new StringValue(local, BuiltInAtomicType.NCNAME));
                 }
        );
        register(BuiltInAtomicType.ANY_URI, "scheme", SequenceType.OPTIONAL_STRING,
                 a -> {
                     MapItem map = uriComponents(a.getStringValue());
                     return SingletonIterator.makeIterator((StringValue)map.get(new StringValue("scheme")));
                 }
        );
        register(BuiltInAtomicType.ANY_URI, "authority", SequenceType.OPTIONAL_STRING,
                 a -> {
                     MapItem map = uriComponents(a.getStringValue());
                     return SingletonIterator.makeIterator((StringValue) map.get(new StringValue("authority")));
                 }
        );
        register(BuiltInAtomicType.ANY_URI, "path", SequenceType.OPTIONAL_STRING,
                 a -> {
                     MapItem map = uriComponents(a.getStringValue());
                     return SingletonIterator.makeIterator((StringValue) map.get(new StringValue("path")));
                 }
        );
        register(BuiltInAtomicType.ANY_URI, "query", SequenceType.OPTIONAL_STRING,
                 a -> {
                     MapItem map = uriComponents(a.getStringValue());
                     return SingletonIterator.makeIterator((StringValue) map.get(new StringValue("query")));
                 }
        );
        register(BuiltInAtomicType.ANY_URI, "fragment", SequenceType.OPTIONAL_STRING,
                 a -> {
                     MapItem map = uriComponents(a.getStringValue());
                     return SingletonIterator.makeIterator((StringValue) map.get(new StringValue("fragment")));
                 }
        );
        register(BuiltInAtomicType.ANY_URI, "query-parameters", MapType.SINGLE_MAP_ITEM,
                 a -> {
                     MapItem map = uriComponents(a.getStringValue());
                     return SingletonIterator.makeIterator((MapItem) map.get(new StringValue("queryParams")));
                 }
        );

        buildRecordTypes();

    }

    private static Map<AtomicType, RecordTest> resultTypeMap;

    private static void buildRecordTypes() {
        resultTypeMap = new HashMap<>();
        for (Map.Entry<AtomicType, Map<String, ComponentDetails>> entry : componentMap.entrySet()) {
             //RecordTest(List<String> names, List<SequenceType> types, List<String> optionalFieldNames, boolean extensible)
            List<String> names = new ArrayList<>();
            List<SequenceType> types = new ArrayList<>();
            List<String> optionalFields = new ArrayList<>();
            for (Map.Entry<String, ComponentDetails> details : entry.getValue().entrySet()) {
                names.add(details.getKey());
                types.add(details.getValue().componentType);
                if (Cardinality.allowsZero(details.getValue().componentType.getCardinality())) {
                    optionalFields.add(details.getKey());
                }
            }
            names.add("value");
            types.add(SequenceType.makeSequenceType(entry.getKey(), StaticProperty.EXACTLY_ONE));
            resultTypeMap.put(entry.getKey(), new RecordTest(names, types, optionalFields, false));
        }
    }

    public static SequenceType getComponentType(BuiltInAtomicType type, String componentName) {
        if (type == BuiltInAtomicType.DAY_TIME_DURATION || type == BuiltInAtomicType.YEAR_MONTH_DURATION) {
            type = BuiltInAtomicType.DURATION;
        }
        Map<String, ComponentDetails> typeData = componentMap.get(type);
        if (typeData == null) {
            return null;
        } else {
            ComponentDetails details = typeData.get(componentName);
            return details == null ? null : details.componentType;
        }
    }

    public static SequenceIterator getComponentValue(AtomicValue value, String componentName) {
        AtomicType type = value.getPrimitiveType();
        if (type == BuiltInAtomicType.DAY_TIME_DURATION || type == BuiltInAtomicType.YEAR_MONTH_DURATION) {
            type = BuiltInAtomicType.DURATION;
        }
        Map<String, ComponentDetails> typeData = componentMap.get(type);
        if (typeData == null) {
            return EmptyIterator.ofAtomic();
        }
        Function<AtomicValue, SequenceIterator> supplier = typeData.get(componentName).supplier;
        return supplier.apply(value);
    }

    public static DictionaryMap uriComponents(String arg) {
        DictionaryMap dict = new DictionaryMap();
        try {
            URI uri = new URI(arg);
            dict.initialPut("isValid", BooleanValue.TRUE);
            boolean absolute = uri.isAbsolute();
            dict.initialPut("isAbsolute", BooleanValue.get(absolute));
            dict.initialPut("isOpaque", BooleanValue.get(uri.isOpaque()));
            URI surrogateURI = absolute ? uri : getSurrogateURI(uri);
            String val = absolute ? uri.getAuthority() : null;
            if (val != null) {
                dict.initialPut("authority", new StringValue(val));
            }
            val = absolute ? uri.getRawAuthority() : null;
            if (val != null) {
                dict.initialPut("rawAuthority", new StringValue(val));
            }
            val = absolute ? uri.getUserInfo() : null;
            if (val != null) {
                dict.initialPut("userInfo", new StringValue(val));
            }
            val = absolute ? uri.getRawUserInfo() : null;
            if (val != null) {
                dict.initialPut("rawUserInfo", new StringValue(val));
            }
            val = absolute ? uri.getScheme() : null;
            if (val != null) {
                dict.initialPut("scheme", new StringValue(val));
            }
            val = absolute ? uri.getHost() : null;
            if (val != null) {
                dict.initialPut("host", new StringValue(val));
            }
            val = surrogateURI.getSchemeSpecificPart();
            if (val != null) {
                dict.initialPut("schemeSpecificPart", new StringValue(val));
            }
            val = surrogateURI.getRawSchemeSpecificPart();
            if (val != null) {
                dict.initialPut("rawSchemeSpecificPart", new StringValue(val));
            }
            if (absolute) {
                int port = uri.getPort();
                if (port >= 0) {
                    dict.initialPut("port", new Int64Value(port));
                }
            }
            val = surrogateURI.getPath();
            if (val != null) {
                if (val.equals("/") && !arg.endsWith("/")) {
                    // Relevant for .NET, where the absolute path of a URI such as "http://www.example.com" is wrongly
                    // given as "/" rather than "". This doesn't catch all cases, for example if there is a query part.
                    val = "";
                }
                if (!absolute && val.startsWith("/") && !arg.startsWith("/")) {
                    // For a relative URI such as "books.xml", return "books.xml" rather than "/books.xml"
                    val = val.substring(1);
                }
                dict.initialPut("path", new StringValue(val));
            }
            val = surrogateURI.getRawPath();
            if (val != null) {
                dict.initialPut("rawPath", new StringValue(val));
            }
            val = surrogateURI.getRawQuery();
            if (val != null) {
                dict.initialPut("rawQuery", new StringValue(val));
            }
            val = surrogateURI.getQuery();
            if (val != null) {
                dict.initialPut("query", new StringValue(val));
                DictionaryMap params = new DictionaryMap();
                StringTokenizer t = new StringTokenizer(val, ";&");
                while (t.hasMoreTokens()) {
                    String tok = t.nextToken();
                    int eq = tok.indexOf('=');
                    if (eq >= 0) {
                        String keyword = tok.substring(0, eq);
                        String value = tok.substring(eq + 1);
                        params.initialAppend(keyword, new StringValue(value));
                    } else {
                        params.initialAppend(tok, EmptySequence.getInstance());
                    }
                }
                dict.initialPut("queryParams", params);
            }
            val = surrogateURI.getRawFragment();
            if (val != null) {
                dict.initialPut("rawFragment", new StringValue(val));
            }
            val = surrogateURI.getFragment();
            if (val != null) {
                dict.initialPut("fragment", new StringValue(val));
            }
            val = uri.toASCIIString();
            if (val != null) {
                dict.initialPut("asciiString", new StringValue(val));
            }
            return dict;
        } catch (URISyntaxException e) {
            dict.initialPut("isValid", BooleanValue.FALSE);
            dict.initialPut("error", new StringValue(e.getMessage()));
            return dict;
        }
    }

    /**
     * For a relative URI, get a corresponding URI whose components are accessible. No action on Java;
     * on C#, resolve the relative URI against an arbitrary base URI
     */

    @CSharpReplaceBody(code="return new System.Uri(new System.Uri(\"http://dummy.uri/\"), relative);")
    private static URI getSurrogateURI(URI relative) {
        return relative;
    }
}

