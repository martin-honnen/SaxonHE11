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
import net.sf.saxon.str.StringView;
import net.sf.saxon.str.UnicodeString;
import net.sf.saxon.str.WhitespaceString;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

import java.text.Normalizer;

/**
 * UnicodeNormalizer: This ProxyReceiver performs unicode normalization on the contents
 * of attribute and text nodes.
 */


public class UnicodeNormalizer extends ProxyReceiver {

    private final Normalizer.Form normForm;

    public UnicodeNormalizer(String form, Receiver next) throws XPathException {
        super(next);
        switch (form) {
            case "NFC":
                normForm = Normalizer.Form.NFC;
                break;
            case "NFD":
                normForm = Normalizer.Form.NFD;
                break;
            case "NFKC":
                normForm = Normalizer.Form.NFKC;
                break;
            case "NFKD":
                normForm = Normalizer.Form.NFKD;
                break;
            default:
                XPathException err = new XPathException("Unknown normalization form " + form);
                err.setErrorCode("SESU0011");
                throw err;
        }
    }

    /**
     * Get the underlying normalizer
     * @return the underlying Normalizer
     */

    public Normalizer.Form getNormalizationForm() {
        return normForm;
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
        AttributeMap am2 = attributes.apply(attInfo -> {
            String newValue = normalize(StringView.of(attInfo.getValue()),
                                        ReceiverOption.contains(attInfo.getProperties(), ReceiverOption.USE_NULL_MARKERS)).toString();

            return new AttributeInfo(
                        attInfo.getNodeName(),
                        attInfo.getType(),
                        newValue,
                        attInfo.getLocation(),
                        attInfo.getProperties());
        });
        nextReceiver.startElement(elemName, type, am2, namespaces, location, properties);
    }

    /**
     * Output character data
     */

    @Override
    public void characters(/*@NotNull*/ UnicodeString chars, Location locationId, int properties) throws XPathException {
        if (Whitespace.isAllWhite(chars)) {
            nextReceiver.characters(chars, locationId, properties);
        } else {
            nextReceiver.characters(normalize(chars, ReceiverOption.contains(properties, ReceiverOption.USE_NULL_MARKERS)),
                                    locationId, properties);
        }
    }

    public UnicodeString normalize(UnicodeString in, boolean containsNullMarkers) {
        if (in instanceof WhitespaceString) {
            return in;
        }
        UnicodeString t = in.tidy();
        if (containsNullMarkers) {
            StringBuilder out = new StringBuilder(t.length32());
            String s = in.toString();
            int start = 0;
            int nextNull = s.indexOf((char)0);
            while (nextNull >= 0) {
                out.append(Normalizer.normalize(s.substring(start, nextNull), normForm));
                out.append((char) 0);
                start = nextNull + 1;
                nextNull = s.indexOf((char) 0, start);
                out.append(s.substring(start, nextNull));
                out.append((char) 0);
                start = nextNull + 1;
                nextNull = s.indexOf((char) 0, start);
            }
            out.append(Normalizer.normalize(s.substring(start), normForm));
            return StringView.tidy(out.toString());
        } else {
            return StringView.tidy(Normalizer.normalize(in.toString(), normForm));
        }
    }

}

