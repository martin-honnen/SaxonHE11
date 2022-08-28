////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.serialize;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.charcode.XMLCharacterData;
import net.sf.saxon.str.StringView;
import net.sf.saxon.str.UnicodeString;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.z.IntIterator;

/**
 * This class is used on the serialization pipeline to check that the document conforms
 * to XML 1.0 rules. It is placed on the pipeline only when the configuration permits
 * XML 1.1 constructs, but the particular output document is being serialized as XML 1.0
 *
 * <p>Simplified in Saxon 9.6 because the rules for XML Names in 1.0 are now the same as
 * the rules in 1.1; only the rules for valid characters are different.</p>
 */

public class XML10ContentChecker extends ProxyReceiver {


    public XML10ContentChecker(Receiver next) {
        super(next);
    }

    /**
     * Notify the start of an element
     *
     * @param elemName   the name of the element.
     * @param type       the type annotation of the element.
     * @param attributes the attributes of this element
     * @param namespaces the in-scope namespaces of this element: generally this is all the in-scope
     *                   namespaces, without relying on inheriting namespaces from parent elements
     * @param location   an object providing information about the module, line, and column where the node originated
     * @param properties bit-significant properties of the element node. If there are no relevant
     *                   properties, zero is supplied. The definitions of the bits are in class {@link ReceiverOption}
     * @throws XPathException if an error occurs
     */
    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        for (AttributeInfo att : attributes) {
            checkString(StringView.of(att.getValue()), att.getLocation());
        }
        nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }

//    /**
//     * Notify an attribute. Attributes are notified after the startElement event, and before any
//     * children. Namespaces and attributes may be intermingled.
//     *
//     * @param attName    The name of the attribute
//     * @param typeCode   The type of the attribute
//     * @param locationId the location of the node in the source, or of the instruction that created it
//     * @param properties Bit significant value. The following bits are defined:
//     *                   <dl>
//     *                   <dd>DISABLE_ESCAPING</dd>    <dt>Disable escaping for this attribute</dt>
//     *                   <dd>NO_SPECIAL_CHARACTERS</dd>      <dt>Attribute value contains no special characters</dt>
//     *                   </dl>
//     * @throws IllegalStateException: attempt to output an attribute when there is no open element
//     *                                start tag
//     */
//
//    public void attribute(NodeName attName, SimpleType typeCode, CharSequence value, Location locationId, int properties) throws XPathException {
//        checkString(value, locationId);
//        nextReceiver.attribute(attName, typeCode, value, locationId, properties);
//    }

    /**
     * Character data
     */

    @Override
    public void characters(UnicodeString chars, Location locationId, int properties) throws XPathException {
        checkString(chars, locationId);
        nextReceiver.characters(chars, locationId, properties);
    }

    /**
     * Output a comment
     */

    @Override
    public void comment(UnicodeString chars, Location locationId, int properties) throws XPathException {
        checkString(chars, locationId);
        nextReceiver.comment(chars, locationId, properties);
    }

    /**
     * Processing Instruction
     */

    @Override
    public void processingInstruction(String target, /*@NotNull*/ UnicodeString data, Location locationId, int properties) throws XPathException {
        checkString(data, locationId);
        nextReceiver.processingInstruction(target, data, locationId, properties);
    }

    /**
     * Check that a string consists of valid XML 1.0 characters (UTF-16 encoded)
     *
     * @param in         the string to be checked
     * @param locationId the location of the string
     */

    private void checkString(UnicodeString in, Location locationId) throws XPathException {
        IntIterator iter = in.codePoints();
        while (iter.hasNext()) {
            int ch32 = iter.next();
            if (!XMLCharacterData.isValid10(ch32)) {
                XPathException err = new XPathException("The result tree contains a character not allowed by XML 1.0 (hex " +
                        Integer.toHexString(ch32) + ')');
                err.setErrorCode("SERE0006");
                err.setLocator(locationId);
                throw err;
            }
        }
    }

}

