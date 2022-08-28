////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.lib;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.event.IDFilter;
import net.sf.saxon.resource.ResourceLoader;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * A <code>DirectResourceResolver</code> is a {@link ResourceResolver} that resolves requests
 * using the direct, native capabilities of the platform. For example a "file:" URI is resolved
 * by finding the file in filestore, and an "http:" URI is resolved by making an HTTP request.
 */

public class DirectResourceResolver implements ResourceResolver {

    private final Configuration config;

    public DirectResourceResolver(Configuration config) {
        this.config = config;
    }

    /**
     * Process a resource request to deliver a resource
     *
     * @param request the resource request
     * @return the returned Source; or null to delegate resolution to another resolver
     * @throws XPathException if the request is invalid in some way, or if the identified resource is unsuitable,
     *                        or if resolution is to fail rather than being delegated to another resolver.
     */
    @Override
    public Source resolve(ResourceRequest request) throws XPathException {

        if (request.uriIsNamespace) {
            return null;  // bug 5266
        }
        ProtocolRestrictor restrictor = config.getProtocolRestrictor();
        if (!"all".equals(restrictor.toString())) {
            try {
                URI u = new URI(request.uri);
                if (!restrictor.test(u)) {
                    throw new XPathException("URIs using protocol " + u.getScheme() + " are not permitted");
                }
            } catch (URISyntaxException err) {
                throw new XPathException("Unknown URI scheme requested " + request.uri);
            }
        }

        String relativeURI = request.relativeUri;
        if (relativeURI == null) {
            relativeURI = request.uri;
        }
        String id = null;

        // Extract any fragment identifier. Note, this code is no longer used to
        // resolve fragment identifiers in URI references passed to the document()
        // function: the code of the document() function handles these itself.

        int hash = relativeURI.indexOf('#');
        if (hash >= 0) {
            request = request.copy();
            request.relativeUri = relativeURI.substring(0, hash);
            id = relativeURI.substring(hash + 1);
            // System.err.println("StandardURIResolver, href=" + href + ", id=" + id);
        }

        Source ss;

        if (ResourceRequest.XSLT_NATURE.equals(request.nature)) {
            InputStream stream;
            try {
                if (request.uri.startsWith("classpath:")) {
                    String s = request.uri.substring(10);
                    if (s.startsWith("/")) {
                        s = s.substring(1);
                    }
                    stream = config.getDynamicLoader().getResourceAsStream(s);
                } else {
                    stream = ResourceLoader.urlStream(new URL(request.uri));
                }
            } catch (IOException e) {
                stream = null; // Carry on, the XML parser might know what to do with it.
            }
            InputSource is = new InputSource(request.uri);
            is.setByteStream(stream);
            ss = new SAXSource(config.getStyleParser(), is);
            if (stream != null) {
                // We created the stream, so we must close it after use
                ss = AugmentedSource.makeAugmentedSource(ss);
                ((AugmentedSource) ss).setPleaseCloseAfterUse(true);
            }
        } else if (ResourceRequest.XML_NATURE.equals(request.nature) || ResourceRequest.XSD_NATURE.equals(request.nature)) {
            InputStream stream;
            try {
                stream = ResourceLoader.urlStream(new URL(request.uri));
            } catch (IOException e) {
                stream = null; // Carry on, the XML parser might know what to do with it.
            }
            ss = new StreamSource(stream, request.uri);
            if (stream != null) {
                // We created the stream, so we must close it after use
                ss = AugmentedSource.makeAugmentedSource(ss);
                ((AugmentedSource) ss).setPleaseCloseAfterUse(true);
            }
        } else {
            ss = new StreamSource(request.uri);
        }


        if (id != null) {
            final String idFinal = id;
            FilterFactory factory = next -> new IDFilter(next, idFinal);
            ss = AugmentedSource.makeAugmentedSource(ss);
            ((AugmentedSource) ss).addFilter(factory);
        }

        return ss;

    }


}

