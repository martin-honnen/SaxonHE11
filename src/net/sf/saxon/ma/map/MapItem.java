////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.ma.map;

import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.functions.DeepEqual;
import net.sf.saxon.om.*;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.str.UnicodeString;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.type.*;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

/**
 * Interface supported by different implementations of an XDM map item
 */
public abstract class MapItem implements Function {

    /**
     * Get an entry from the Map
     * @param key     the value of the key
     * @return the value associated with the given key, or null if the key is not present in the map.
     */

    abstract public GroundedValue get(AtomicValue key);

    /**
     * Get the size of the map
     *
     * @return the number of keys/entries present in this map
     */

    abstract public int size();

    /**
     * Ask whether the map is empty
     *
     * @return true if and only if the size of the map is zero
     */

    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Get the set of all key values in the map.
     *
     * @return a set containing all the key values present in the map, in unpredictable order
     */

    abstract public AtomicIterator keys();

    /**
     * Get the set of all key-value pairs in the map
     * @return an iterable containing all the key-value pairs
     */

    abstract public Iterable<KeyValuePair> keyValuePairs();

    /**
     * Create a new map containing the existing entries in the map plus an additional entry,
     * without modifying the original. If there is already an entry with the specified key,
     * this entry is replaced by the new entry.
     *
     * @param key   the key of the new entry
     * @param value the value associated with the new entry
     * @return the new map containing the additional entry
     */

    abstract public MapItem addEntry(AtomicValue key, GroundedValue value);

    /**
     * Remove an entry from the map
     *
     *
     * @param key     the key of the entry to be removed
     * @return a new map in which the requested entry has been removed; or this map
     *         unchanged if the specified key was not present
     */

    abstract public MapItem remove(AtomicValue key);

    /**
     * Ask whether the map conforms to a given map type
     * @param keyType the required keyType
     * @param valueType the required valueType
     * @param th the type hierarchy cache for the configuration
     * @return true if the map conforms to the required type
     */
    abstract public boolean conforms(AtomicType keyType, SequenceType valueType, TypeHierarchy th);

    /**
     * Get the type of the map. This method is used largely for diagnostics, to report
     * the type of a map when it differs from the required type.
     * @param th the type hierarchy cache
     * @return the type of this map
     */

    abstract public ItemType getItemType(TypeHierarchy th);

    /**
     * Get the lowest common item type of the keys in the map
     *
     * @return the most specific type to which all the keys belong. If the map is
     *         empty, return UType.VOID
     */

    abstract public UType getKeyUType();

    /**
     * Provide a short string showing the contents of the item, suitable
     * for use in error messages
     *
     * @return a depiction of the item suitable for use in error messages
     */
    @Override
    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        sb.append("map{");
        int count = size();
        if (count == 0) {
            sb.append("}");
        } else if (count <= 5) {
            int pos = 0;
            for (KeyValuePair pair : keyValuePairs()) {
                if (pos++ > 0) {
                    sb.append(",");
                }
                sb.append(Err.depict(pair.key))
                        .append(":")
                        .append(Err.depictSequence(pair.value));
            }
            sb.append("}");
        } else {
            sb.append("(:size ").append(count).append(":)}");
        }
        return sb.toString();
    }

    /**
     * Get the genre of this item
     *
     * @return the genre: specifically, Map.
     */
    @Override
    public Genre getGenre() {
        return Genre.MAP;
    }

    /**
     * Ask whether this function item is an array
     *
     * @return false (it is not an array)
     */
    @Override
    public boolean isArray() {
        return false;
    }

    /**
     * Ask whether this function item is a map
     *
     * @return true (it is a map)
     */
    @Override
    public boolean isMap() {
        return true;
    }

    /**
     * Get the function annotations (as defined in XQuery). Returns an empty
     * list if there are no function annotations.
     *
     * @return the function annotations
     */

    @Override
    public AnnotationList getAnnotations() {
        return AnnotationList.EMPTY;
    }

    /**
     * Atomize the item.
     *
     * @return the result of atomization
     * @throws XPathException if atomization is not allowed for this kind of item
     */
    @Override
    public AtomicSequence atomize() throws XPathException {
        throw new XPathException("Cannot atomize a map (" + toShortString() + ")", "FOTY0013");
    }

    /**
     * Ask whether all the items in a sequence are known to conform to a given item type
     *
     * @param value    the sequence
     * @param itemType the given item type
     * @return true if all the items conform; false if not, or if the information cannot
     * be efficiently determined
     */

    public static boolean isKnownToConform(Sequence value, ItemType itemType) {
        // Problem is we don't have access to a TypeHierarchy object...
        if (itemType == AnyItemType.getInstance()) {
            return true;
        }
        try {
            SequenceIterator iter = value.iterate();
            Item item;
            while ((item = iter.next()) != null) {
                if (item instanceof AtomicValue) {
                    if (itemType instanceof AtomicType) {
                        if (!Type.isSubType(((AtomicValue) item).getItemType(), (AtomicType) itemType)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else if (item instanceof NodeInfo) {
                    if (itemType instanceof NodeTest) {
                        if (!((NodeTest) itemType).test((NodeInfo) item)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    // functions, maps, arrays: give up (this is only an optimization)
                    return false;
                }
            }
            return true;
        } catch (UncheckedXPathException e) {
            return false;
        }
    }

    /**
     * Get an item type to which all the values in a sequence are known to conform
     *
     * @param val the sequence
     * @return the type of the first item in the sequence, provided that all subsequent
     * values in the sequence are known to conform to this type; otherwise item().
     */

    public static ItemType getItemTypeOfSequence(Sequence val) {
        try {
            Item first = val.head();
            if (first == null) {
                return AnyItemType.getInstance();
            } else {
                ItemType type;
                if (first instanceof AtomicValue) {
                    type = ((AtomicValue) first).getItemType();
                } else if (first instanceof NodeInfo) {
                    type = NodeKindTest.makeNodeKindTest(((NodeInfo) first).getNodeKind());
                } else {
                    type = AnyFunctionType.getInstance();
                }
                if (isKnownToConform(val, type)) {
                    return type;
                } else {
                    return AnyItemType.getInstance();
                }
            }
        } catch (XPathException e) {
            return AnyItemType.getInstance();
        }
    }

    /**
     * Get the roles of the arguments, for the purposes of streaming
     *
     * @return an array of OperandRole objects, one for each argument
     */
    @Override
    public OperandRole[] getOperandRoles() {
        return new OperandRole[]{OperandRole.SINGLE_ATOMIC};
    }


    /**
     * Get the item type of this item as a function item. Note that this returns the generic function
     * type for maps, not a type related to this specific map.
     *
     * @return the function item's type
     */
    @Override
    public FunctionItemType getFunctionItemType(/*@Nullable*/) {
        return MapType.ANY_MAP_TYPE;
    }

    /**
     * Get the name of the function, or null if it is anonymous
     *
     * @return the function name, or null for an anonymous inline function
     */
    @Override
    public StructuredQName getFunctionName() {
        return null;
    }

    /**
     * Get a description of this function for use in error messages. For named functions, the description
     * is the function name (as a lexical QName). For others, it might be, for example, "inline function",
     * or "partially-applied ends-with function".
     *
     * @return a description of the function for use in error messages
     */
    @Override
    public String getDescription() {
        return "map";
    }

    /**
     * Get the arity of the function
     *
     * @return the number of arguments in the function signature
     */
    @Override
    public int getArity() {
        return 1;
    }

    /**
     * Prepare an XPathContext object for evaluating the function
     *
     * @param callingContext the XPathContext of the function calling expression
     * @param originator not used
     * @return a suitable context for evaluating the function (which may or may
     * not be the same as the caller's context)
     */
    @Override
    public XPathContext makeNewContext(XPathContext callingContext, ContextOriginator originator) {
        return callingContext;
    }

    /**
     * Invoke the function
     *
     * @param context the XPath dynamic evaluation context
     * @param args    the actual arguments to be supplied
     * @return the result of invoking the function
     * @throws XPathException if an error occurs evaluating
     *                        the supplied argument
     */
    @Override
    public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        AtomicValue key = (AtomicValue) args[0].head();
        Sequence value = get(key);
        if (value == null) {
            return EmptySequence.getInstance();
        } else {
            return value;
        }
    }

    /**
     * Get the value of the item as a string. For nodes, this is the string value of the
     * node as defined in the XPath 2.0 data model, except that all nodes are treated as being
     * untyped: it is not an error to get the string value of a node with a complex type.
     * For atomic values, the method returns the result of casting the atomic value to a string.
     *
     * @return the string value of the item
     * @throws UnsupportedOperationException if the item is a function item (an unchecked exception
     *                                       is used here to avoid introducing exception handling to a large number of paths where it is not
     *                                       needed)
     * @since 8.4
     */
    @Override
    public UnicodeString getUnicodeStringValue() {
        throw new UnsupportedOperationException("A map has no string value");
    }

    /**
     * Get the typed value of the item.
     * <p>For a node, this is the typed value as defined in the XPath 2.0 data model. Since a node
     * may have a list-valued data type, the typed value is in general a sequence, and it is returned
     * in the form of a SequenceIterator.</p>
     * <p>If the node has not been validated against a schema, the typed value
     * will be the same as the string value, either as an instance of xs:string or as an instance
     * of xs:untypedAtomic, depending on the node kind.</p>
     * <p>For an atomic value, this method returns an iterator over a singleton sequence containing
     * the atomic value itself.</p>
     *
     * @return an iterator over the items in the typed value of the node or atomic value. The
     * items returned by this iterator will always be atomic values.
     * @throws XPathException where no typed value is available, for example in the case of
     *                        an element with complex content
     * @since 8.4
     */
    public SequenceIterator getTypedValue() throws XPathException {
        throw new XPathException("A map has no typed value");
    }

    /**
     * Test whether this FunctionItem is deep-equal to another function item,
     * under the rules of the deep-equal function
     *
     * @param other the other function item
     */
    @Override
    public boolean deepEquals(Function other, XPathContext context, AtomicComparer comparer, int flags) throws XPathException {
        if (other instanceof MapItem &&
                ((MapItem) other).size() == size()) {
            AtomicIterator keyIter = keys();
            AtomicValue key;
            while ((key = keyIter.next()) != null) {
                Sequence thisValue = get(key);
                assert thisValue != null;
                Sequence otherValue = ((MapItem) other).get(key);
                if (otherValue == null) {
                    return false;
                }
                if (!DeepEqual.deepEqual(otherValue.iterate(),
                                         thisValue.iterate(), comparer, context, flags)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /*@Nullable*/
    @Override
    public MapItem itemAt(int n) {
        return n == 0 ? this : null;
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        throw new XPathException("A map item has no effective boolean value");
    }

    /**
     * Returns a string representation of the map.
     * @param map the input map
     * @return a string representation of the input map (using syntax resembling that of an XPath 3.1 map constructor).
     */

    public static String mapToString(MapItem map) {
        StringBuilder buffer = new StringBuilder(256);
        buffer.append("map{");
        for (KeyValuePair pair : map.keyValuePairs()) {
            if (buffer.length() > 4) {
                buffer.append(",");
            }
            buffer.append(pair.key.toString());
            buffer.append(":");
            buffer.append(pair.value.toString());
        }
        buffer.append("}");
        return buffer.toString();
    }

    /**
     * Export information about this function item to the export() or explain() output
     */
    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("map");
        out.emitAttribute("size", "" + size());
        for (KeyValuePair kvp : keyValuePairs()) {
            Literal.exportAtomicValue(kvp.key, out);
            Literal.exportValue(kvp.value, out);
        }
        out.endElement();
    }

    @Override
    public boolean isTrustedResultType() {
        return true;
    }


}

// Copyright (c) 2011-2022 Saxonica Limited
