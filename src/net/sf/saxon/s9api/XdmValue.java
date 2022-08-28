////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.s9api;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceCopier;
import net.sf.saxon.expr.sort.DocumentOrderIterator;
import net.sf.saxon.expr.sort.GlobalOrderComparer;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.*;
import net.sf.saxon.s9api.streams.Step;
import net.sf.saxon.s9api.streams.Steps;
import net.sf.saxon.s9api.streams.XdmStream;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.transpile.CSharpModifiers;
import net.sf.saxon.value.AnyExternalObject;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceExtent;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An value in the XDM data model. A value is a sequence of zero or more items,
 * each item being either an atomic value or a node.
 * <p>An XdmValue is immutable.</p>
 * <p>A sequence consisting of a single item may be represented as an instance of {@link XdmItem},
 * which is a subtype of XdmValue. However, there is no guarantee that a sequence of length one
 * will always be an instance of XdmItem.</p>
 * <p>Similarly, a zero-length sequence may be represented as an instance of {@link XdmEmptySequence},
 * but there is no guarantee that every sequence of length zero will always be an instance of
 * XdmEmptySequence.</p>
 *
 * @since 9.0
 */

@CSharpModifiers(code = {"internal"})
public class XdmValue implements Iterable<XdmItem> {

    private final GroundedValue value;

    /**
     * Create an XdmValue that wraps a supplied <code>GroundedValue</code>. This
     * method is primarily for internal use, though it is also available to applications
     * that manipulate data using lower-level Saxon interfaces.
     * <p>Note that this constructor necessarily produces an <code>XdmValue</code> regardless
     * of the supplied value. To construct instances of subclasses of <code>XdmValue</code>,
     * such as <code>XdmAtomicValue</code> or <code>XdmNode</code>, use the {@link #wrap} method,
     * or use the overriding constructor on the required subclass.</p>
     * @param value the value to be wrapped.
     */

    protected XdmValue(GroundedValue value) {
        this.value = value;
    }

    /**
     * Create an XdmValue as a sequence of XdmItem objects
     *
     * @param items a sequence of XdmItem objects. Note that if this is supplied as a list or similar
     *              collection, subsequent changes to the list/collection will have no effect on the XdmValue.
     * @since 9.0.0.4
     */

    public XdmValue(Iterable<? extends XdmItem> items) {
        List<Item> values = new ArrayList<>();
        for (XdmItem item : items) {
            values.add(item.getUnderlyingValue());
        }
        value = new SequenceExtent.Of<>(values);
    }

    /**
     * Create an XdmValue containing the items returned by an {@link Iterator}.
     *
     * @param iterator the iterator that supplies the values
     * @throws SaxonApiException if an error occurs reading values from the supplied iterator
     * @since 9.6. Extended in 9.9 to take any Iterator, not just an {@link XdmSequenceIterator}.
     */

    public XdmValue(Iterator<? extends XdmItem> iterator) throws SaxonApiException {
        try {
            List<Item> values = new ArrayList<>();
            while (iterator.hasNext()) {
                values.add(iterator.next().getUnderlyingValue());
            }
            value = new SequenceExtent.Of<>(values);
        } catch (SaxonApiUncheckedException e) {
            throw new SaxonApiException(e.getCause());
        }
    }

    /**
     * Create an XdmValue containing the results of reading a Stream
     * @param stream the stream to be read
     * @throws SaxonApiException if an error occurs reading values from the supplied stream
     */

    public XdmValue(Stream<? extends XdmItem> stream) throws SaxonApiException {
        this(stream.iterator());
    }

    /**
     * Create an XdmValue that wraps an existing Saxon Sequence
     *
     * @param value the supplied Sequence (which may be a singleton Item),
     * @return an XdmValue corresponding to the supplied Sequence. If the
     *         supplied value is null, an empty sequence is returned. If the supplied
     *         value is an atomic value, the result will be an instance of XdmAtomicValue.
     *         <ul>
     *         <li>If the supplied value is a node, the result will be an instance of XdmNode.</li>
     *         <li>If the supplied value is a map, the result will be an instance of XdmMap.</li>
     *         <li>If the supplied value is an array, the result will be an instance of XdmArray.</li>
     *         <li>If the supplied value is a function item, the result will be an instance of
     *         XdmFunctionItem.</li>
     *         </ul>
     * @throws SaxonApiUncheckedException if the supplied Sequence is not yet fully evaluated, and evaluation
     *                                    of the underlying expression fails with a dynamic error.
     * @since 9.5 (previously a protected method)
     */

    public static XdmValue wrap(Sequence value) {
        if (value == null) {
            return XdmEmptySequence.getInstance();
        }
        GroundedValue gv;
        try {
            gv = value.materialize();
        } catch (UncheckedXPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
        if (gv.getLength() == 0) {
            return XdmEmptySequence.getInstance();
        } else if (gv.getLength() == 1) {
            Item first = gv.head();
            if (first instanceof NodeInfo) {
                return new XdmNode((NodeInfo) first);
            } else if (first instanceof AtomicValue) {
                return new XdmAtomicValue((AtomicValue) first);
            } else if (first instanceof MapItem) {
                return new XdmMap((MapItem)first);
            } else if (first instanceof ArrayItem) {
                return new XdmArray((ArrayItem)first);
            } else if (first instanceof Function) {
                return new XdmFunctionItem((Function) first);
            } else if (first instanceof AnyExternalObject) {
                return new XdmExternalObject(first);
            } else {
                throw new IllegalArgumentException("Unknown item type " + first.getClass());
            }
        } else {
            return new XdmValue(gv);
        }
    }

    public static XdmValue wrap(AtomicSequence value) {
        switch (value.getLength()) {
            case 0:
                return XdmEmptySequence.getInstance();
            case 1:
                return new XdmAtomicValue(value.head());
            default:
                return new XdmValue(value);
        }
    }


    /**
     * Create a new XdmValue by concatenating the contents of this XdmValue and another
     * XdmValue into a single sequence. The two input XdmValue objects are unaffected by this operation.
     * <p>Note: creating a sequence of N values by successive calls on this method
     * takes time proportional to N-squared.</p>
     *
     * @param otherValue the value to be appended
     * @return a new XdmValue containing the concatenation of the two input XdmValue objects
     * @since 9.2
     */

    public XdmValue append(XdmValue otherValue) {
        List<Item> values = new ArrayList<>();
        for (XdmItem item : this) {
            values.add(item.getUnderlyingValue());
        }
        for (XdmItem item : otherValue) {
            values.add(item.getUnderlyingValue());
        }
        GroundedValue gv = SequenceExtent.makeSequenceExtent(values);
        return new XdmValue(gv);
    }

    /**
     * Get the number of items in the sequence
     *
     * @return the number of items in the value, considered as a sequence. Note that for arrays
     * and maps, the answer will be 1 (one) since arrays and maps are items.
     */

    public int size() {
        return value.getLength();
    }

    /**
     * Ask whether the sequence is empty
     * @return true if the value is an empty sequence
     * @since 10.1
     */

    public boolean isEmpty() {
        return value.head() == null;
    }

    /**
     * Get the n'th item in the value, counting from zero.
     *
     * @param n the item that is required, counting the first item in the sequence as item zero
     * @return the n'th item in the sequence making up the value, counting from zero
     * @throws IndexOutOfBoundsException  if n is less than zero or greater than or equal to the number
     *                                    of items in the value
     * @throws SaxonApiUncheckedException if the value is lazily evaluated and the delayed
     *                                    evaluation fails with a dynamic error.
     */

    public XdmItem itemAt(int n) throws IndexOutOfBoundsException, SaxonApiUncheckedException {
        if (n < 0 || n >= size()) {
            throw new IndexOutOfBoundsException("" + n);
        }
        try {
            Item item = SequenceTool.itemAt(value, n);
            return (XdmItem)XdmItem.wrap(item);
        } catch (UncheckedXPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }

    /**
     * Get a subsequence of the value
     *
     * @param start  the index of the first item to be included in the result, counting from zero.
     *               A negative value is taken as zero. If the value is beyond the end of the sequence, an empty
     *               sequence is returned
     * @param length the number of items to be included in the result. Specify Integer.MAX_VALUE to
     *               get the subsequence up to the end of the base sequence. If the value is negative, an empty sequence
     *               is returned. If the length goes off the end of the sequence, the result returns items up to the end
     *               of the sequence
     * @return the required subsequence.
     * @since 11
     */

    public XdmValue subsequence(int start, int length) {
        return new XdmValue(value.subsequence(start, length));
    }

    /**
     * Get an iterator over the items in this value.
     *
     * @return an Iterator over the items in this value.
     * @throws SaxonApiUncheckedException if the value is lazily evaluated and the delayed
     *                                    evaluation fails with a dynamic error.
     */
    @Override
    public XdmSequenceIterator<XdmItem> iterator() throws SaxonApiUncheckedException {
        try {
            Sequence v = getUnderlyingValue();
            return new XdmSequenceIterator<>(v.iterate());
        } catch (UncheckedXPathException e) {
            throw new SaxonApiUncheckedException(e);
        }
    }



    /**
     * Get the underlying implementation object representing the value. This method allows
     * access to lower-level Saxon functionality, including classes and methods that offer
     * no guarantee of stability across releases.
     *
     * @return the underlying implementation object representing the value
     */

    public GroundedValue getUnderlyingValue() {
        return value;
    }

    /**
     * Create a string representation of the value. The is the result of serializing
     * the value using the adaptive serialization method.
     * @return a string representation of the value
     */

    public String toString() {
        try {
            Configuration config = null;
            SequenceIterator iter = value.iterate();
            Item item;
            while ((item = iter.next()) != null) {
                if (item instanceof NodeInfo) {
                    config = ((NodeInfo)item).getConfiguration();
                    break;
                }
            }
            if (config == null) {
                config = Configuration.newConfiguration();
            }
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            SerializationProperties properties = new SerializationProperties();
            properties.setProperty(OutputKeys.METHOD, "adaptive");
            properties.setProperty(OutputKeys.INDENT, "true");
            properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "true");
            Receiver r = config.getSerializerFactory().getReceiver(result, properties);
            SequenceCopier.copySequence(value.iterate(), r);
            return writer.toString();
        } catch (XPathException e) {
            return super.toString();
        }

    }

    /**
     * Make an XDM sequence from a Java {@link Iterable}. Each value delivered by the iterable
     * is first converted to an XDM value using the {@link #makeValue(Object)} method;
     * if the result is anything other than a single XDM item, it is then wrapped in an
     * {@link XdmArray}.
     * @param list the Java iterable
     * @return the result of the conversion if successful
     * @throws IllegalArgumentException if conversion is not possible
     */

    public static XdmValue makeSequence(Iterable<?> list) throws IllegalArgumentException {
        List<Item> result = new ArrayList<>();
        for (Object o : list) {
            XdmValue v = XdmValue.makeValue(o);
            if (v instanceof XdmItem) {
                result.add((Item)v.getUnderlyingValue());
            } else {
                result.add(new XdmArray(v).getUnderlyingValue());
            }
        }
        return XdmValue.wrap(SequenceExtent.makeSequenceExtent(result));
    }

    /**
     * Make an XDM value from a Java object. The supplied object may be any of the following:
     * <ul>
     * <li>An instance of {@link XdmValue} (for example, an
     * {@link XdmNode} or {@link XdmAtomicValue} or {@link XdmArray} or {@link XdmMap}),
     * which is returned unchanged</li>
     * <li>An instance of {@link net.sf.saxon.om.Sequence} (for example, a
     * {@link net.sf.saxon.om.NodeInfo} or {@link net.sf.saxon.value.AtomicValue}),
     * which is wrapped as an {@code XdmValue}</li>
     * <li>An instance of {@link java.util.Map}, which is converted to an XDM Map using
     * the {@link XdmMap#makeMap} method</li>
     * <li>A Java array, of any type, which is converted to an XDM Array using the
     * {@link XdmArray#makeArray} method</li>
     * <li>A Java collection, more specifically {@link java.lang.Iterable}, which is converted to an XDM sequence
     * using the {@link XdmValue#makeSequence(Iterable)} method</li>
     * <li>Any object that is convertible to an XDM atomic value using the
     * method {@link XdmAtomicValue#makeAtomicValue(Object)}</li>
     * </ul>
     *
     * @param o the Java object
     * @return the result of the conversion if successful
     * @throws IllegalArgumentException if conversion is not possible
     */

    public static XdmValue makeValue(Object o) throws IllegalArgumentException {
        if (o instanceof Sequence) {
            return XdmValue.wrap((Sequence) o);
        } else if (o instanceof XdmValue) {
            return (XdmValue) o;
        } else if (o instanceof Map) {
            return XdmMap.makeMap((Map<?,?>) o);
        } else if (o instanceof Object[]) {
            return XdmArray.makeArray((Object[]) o);
        } else if (o instanceof Iterable) {
            return XdmValue.makeSequence((Iterable<?>) o);
        } else {
            return XdmAtomicValue.makeAtomicValue(o);
        }
    }

    /**
     * Return a new XdmValue containing the nodes present in this XdmValue,
     * with duplicates eliminated, and sorted into document order
     * @return the same nodes, sorted into document order, with duplicates eliminated
     * @throws SaxonApiException if anything goes wrong (typically during delayed evaluation of the
     * input sequence)
     * @throws ClassCastException if the sequence contains items that are not nodes
     * @since 9.9
     */

    public XdmValue documentOrder() throws SaxonApiException {
        try {
            SequenceIterator iter = value.iterate();
            SequenceIterator sorted = new DocumentOrderIterator(iter, GlobalOrderComparer.getInstance());
            return new XdmValue(SequenceTool.toGroundedValue(sorted));
        } catch (UncheckedXPathException e) {
            throw new SaxonApiException(e);
        }
    }

    /**
     * Get a stream comprising the items in this value
     *
     * @return a Stream over the items in this value
     * @since 9.9
     */
    public XdmStream<? extends XdmItem> stream() {
        return new XdmStream<>(StreamSupport.stream(spliterator(), false));
    }

    /**
     * Get a stream of items by applying a {@link Step} to the items in this value. This operation
     * is analogous to the {@code Stream.flatMap} operation in Java, or to the "!" operator
     * in XPath.
     *
     * <p>The following examples assume a static import declaration of the standard Steps:
     * <code>import static net.sf.saxon.s9api.streams.Steps.*;</code></p>
     * <ul>
     *     <li><code>select(child())</code> returns a stream containing all children</li>
     *     <li><code>select(child("*"))</code> returns a stream containing all element children</li>
     *     <li><code>select(child("author"))</code> returns a stream containing all element children with
     *     local name "author" (regardless of namespace)</li>
     *     <li><code>select(child("http://my.ns/", "author"))</code> returns a stream containing all element children with
     *     local name "author" and namespace "http://my.ns/"</li>
     *     <li><code>select(descendant("author"))</code> returns a stream containing all element descendants with
     *     local name "author" (regardless of namespace)</li>
     *     <li><code>select(descendant("author").then(attribute("name"))</code> returns a stream containing the "name"
     *     attributes of "author" descendants</li>
     *     <li><code>select(path("//", "author", "@name"))</code> returns a stream containing the "name"
     *     attributes of "author" descendants</li>
     *     <li><code>select(child("author").where(attributeEq("firstName", "Jane"))</code> returns a stream
     *     containing all the "author" children having a "firstName" attribute whose value is "Jane"</li>
     *     <li><code>select(child("author").first())</code> returns a stream containing the first "author" child
     *     (if there is one)</li>
     *     <li><code>select(child("author").where(attributeEq("firstName", "Jane").last())</code> returns a stream
     *     containing the last "author" child having a "firstName" attribute whose value is "Jane" (if there is one)</li>
     * </ul>
     *
     * <p>In each of the above examples, the selected nodes are returned as an {@link XdmStream}&lt;{@link XdmNode}&gt;; this
     * class extends {@link java.util.stream.Stream}&lt;{@link XdmNode}&gt;, so all the standard operations on streams are
     * available, together with some additional operations specific to node streams.</p>
     *
     * <p>The <code>select</code> method thus has comparable power to simple XPath expressions; but unlike
     * XPath expressions, there is no run-time overhead in parsing the XPath expression and developing an
     * execution plan.</p>
     *
     * @param step the <code>Step</code> to be applied to the items in this value. This will often be one of the standard
     *             steps returned by the static methods of the {@link Steps} class, such as {@link Steps#child},
     *             or by instance methods (such as {@link Step#where}, {@link Step#cat}, or {@link Step#first})
     *             but it is also possible to create user-defined steps.
     * @param <T> the type of items to be returned by the step (often {@link XdmNode})
     * @return a Stream of items obtained by replacing each item X in this value by the items obtained
     * by applying the Step function to X.
     * @since 9.9
     */

    public <T extends XdmItem> XdmStream<T> select(Step<T> step) {
        return stream().flatMapToXdm(step);
    }

}

