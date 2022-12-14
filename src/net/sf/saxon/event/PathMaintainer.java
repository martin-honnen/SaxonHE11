////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.event;

import net.sf.saxon.om.*;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;

import net.sf.saxon.transpile.CSharpReplaceBody;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Type;

import java.util.HashMap;
import java.util.Stack;


/**
 * This class sits in a receiver (push) pipeline and maintains the current path.
 */


public class PathMaintainer extends ProxyReceiver {

    private final Stack<AbsolutePath.PathElement> path = new Stack<>();
    private final Stack<HashMap<NodeName, Integer>> siblingCounters = new Stack<>();

    public PathMaintainer(/*@NotNull*/ Receiver next) {
        super(next);
        siblingCounters.push(new HashMap<>());
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type,
                             AttributeMap attributes, NamespaceMap namespaces,
                             Location location, int properties)
            throws XPathException {
        // System.err.println("startElement " + nameCode);
        nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
        HashMap<NodeName, Integer> counters = siblingCounters.peek();

        int preceding = counters.getOrDefault(elemName, 0);
        int index = preceding + 1;
        counters.put(elemName, index);
        path.push(new AbsolutePath.PathElement(Type.ELEMENT, elemName, index));
        siblingCounters.push(new HashMap<>());
    }

    /**
     * Handle an end-of-element event
     */

    @Override
    public void endElement() throws XPathException {
        nextReceiver.endElement();
        siblingCounters.pop();
        path.pop();
    }

    /**
     * Get the path to the current location in the stream
     *
     * @param useURIs set to true if namespace URIs are to appear in the path;
     *                false if prefixes are to be used instead. The prefix will be the one
     *                that is used in the source document, and is potentially ambiguous.
     * @return the path to the current location, as a string.
     */

    public String getPath(boolean useURIs) {
        StringBuilder fsb = new StringBuilder(256);
        for (AbsolutePath.PathElement pe : path) {
            fsb.append('/');
            if (useURIs) {
                String uri = pe.getName().getURI();
                if (!uri.isEmpty()) {
                    fsb.append('"');
                    fsb.append(uri);
                    fsb.append('"');
                }
            } else {
                String prefix = pe.getName().getPrefix();
                if (!prefix.isEmpty()) {
                    fsb.append(prefix);
                    fsb.append(':');
                }
            }
            fsb.append(pe.getName().getLocalPart());
            fsb.append('[');
            fsb.append(pe.getIndex() + "");
            fsb.append(']');
        }
        return fsb.toString();
    }

    @CSharpReplaceBody(code="return new Saxon.Hej.om.AbsolutePath(new List<Saxon.Hej.om.AbsolutePath.PathElement>(path.ToArray()));")
    // Custom code needed for C# because a Stack is not a List.
    public AbsolutePath getAbsolutePath() {
        return new AbsolutePath(path);
    }


}

