////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.lib;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.InputStreamMarker;
import net.sf.saxon.resource.EncodingDetector;
import net.sf.saxon.resource.ResourceLoader;
import net.sf.saxon.resource.TypedStreamSource;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.transpile.CSharpReplaceBody;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.*;

/**
 * Default implementation of the UnparsedTextURIResolver, used if no other implementation
 * is nominated to the Configuration. This implementation
 * * handles anything that the java URL class will handle, plus the <code>classpath</code>
 * * URI scheme defined in the Spring framework, and the <code>data</code> URI scheme defined in
 * * RFC 2397.
 */

public class StandardUnparsedTextResolver
        implements UnparsedTextURIResolver {

    private boolean debug = false;

    /**
     * Set debugging on or off. In debugging mode, information is written to System.err
     * to trace the process of deducing an encoding.
     *
     * @param debug set to true to enable debugging
     */

    public void setDebugging(boolean debug) {
        this.debug = debug;
    }

    /**
     * Resolve the URI passed to the XSLT unparsed-text() function, after resolving
     * against the base URI.
     *
     * @param absoluteURI the absolute URI obtained by resolving the supplied
     *                    URI against the base URI
     * @param encoding    the encoding requested in the call of unparsed-text(), if any. Otherwise null.
     * @param config      The configuration. Provided in case the URI resolver
     *                    needs it.
     * @return a Reader, which Saxon will use to read the unparsed text. After the text has been read,
     * the close() method of the Reader will be called.
     * @throws net.sf.saxon.trans.XPathException if any failure occurs
     * @since 8.9
     */

    @Override
    public Reader resolve(URI absoluteURI, String encoding, Configuration config) throws XPathException {

        Logger err = config.getLogger();
        if (debug) {
            err.info("unparsed-text(): processing " + absoluteURI);
            err.info("unparsed-text(): requested encoding = " + encoding);
        }
        if (!absoluteURI.isAbsolute()) {
            throw new XPathException("Resolved URI supplied to unparsed-text() is not absolute: " + absoluteURI.toString(),
                                     "FOUT1170");
        }


        ResourceRequest rr = new ResourceRequest();
        rr.uri = absoluteURI.toString();
        rr.nature = ResourceRequest.TEXT_NATURE;
        rr.purpose = "fn:unparsed-text";
        Source resolved = rr.resolve(config.getResourceResolver(), new DirectResourceResolver(config));
        if (resolved == null) {
            throw new XPathException("unparsed-text(): failed to resolve URI " + absoluteURI);
        }
        if (resolved instanceof StreamSource) {
            return getReaderFromStreamSource((StreamSource)resolved, encoding, config, debug);
        } else {
            throw new XPathException("Resolver for unparsed-text() returned unrecognized Source ("
                                             + resolved.getClass().getName() + ")");
        }
    }

    public static Reader getReaderFromStreamSource(StreamSource source, String encoding, Configuration config,
                                                   boolean debug) throws XPathException {
        Logger err = config.getLogger();
        InputStream inputStream = source.getInputStream();

        if (inputStream == null) {
            if (source.getReader() != null) {
                return source.getReader();
            } else {
                String systemId = source.getSystemId();
                if (systemId != null) {
                    try {
                        if (systemId.startsWith("classpath:")) {
                            inputStream = openClasspathResource(config, systemId);
                        } else {
                            URL url = new URL(systemId);
                            inputStream = ResourceLoader.urlStream(url);
                        }
                    } catch (IOException e) {
                        throw new XPathException("unparsed-text(): cannot retrieve " + systemId, e);
                    }
                } else {
                    throw new XPathException("unparsed-text(): resolver returned empty StreamSource");
                }
            }
        }
        // The encoding of the external resource is determined as follows:
        //
        // * external encoding information is used if available, otherwise
        // * if the media type of the resource is text/xml or application/xml (see [RFC 2376]),
        //   or if it matches the conventions text/*+xml or application/*+xml (see [RFC 7303]
        //   and/or its successors), then the encoding is recognized as specified in
        //   [Extensible Markup Language (XML) 1.0 (Fifth Edition)], otherwise
        // * the value of the $encoding argument is used if present, otherwise
        // * the processor may use ·implementation-defined· heuristics to determine the likely encoding, otherwise
        // * UTF-8 is assumed.

        // Use the contentType from the HTTP header if available
        String contentType = null;
        if (source instanceof TypedStreamSource) {
            contentType = ((TypedStreamSource) source).getContentType();
        }
        if (debug) {
            err.info("unparsed-text(): content type = " + contentType);
        }
        boolean isXmlMediaType = false;
        if (contentType != null) {
            String mediaType;
            int pos = contentType.indexOf(';');
            if (pos >= 0) {
                mediaType = contentType.substring(0, pos);
            } else {
                mediaType = contentType;
            }
            mediaType = mediaType.trim();
            if (debug) {
                err.info("unparsed-text(): media type = " + mediaType);
            }
            isXmlMediaType = (mediaType.startsWith("application/") || mediaType.startsWith("text/")) &&
                    (mediaType.endsWith("/xml") || mediaType.endsWith("+xml"));

            String charset = "";
            pos = contentType.toLowerCase().indexOf("charset");
            if (pos >= 0) {
                pos = contentType.indexOf('=', pos + 7);
                if (pos >= 0) {
                    charset = contentType.substring(pos + 1);
                }
                if ((pos = charset.indexOf(';')) > 0) {
                    charset = charset.substring(0, pos);
                }

                // attributes can have comment fields (RFC 822)
                if ((pos = charset.indexOf('(')) > 0) {
                    charset = charset.substring(0, pos);
                }
                // ... and values may be quoted
                if ((pos = charset.indexOf('"')) > 0) {
                    charset = charset.substring(pos + 1,
                                                charset.indexOf('"', pos + 2));
                }
                if (debug) {
                    err.info("unparsed-text(): charset = " + charset.trim());
                }
                encoding = charset.trim();
            }
        }

        try {
            if (encoding == null || isXmlMediaType) {
                inputStream = InputStreamMarker.ensureMarkSupported(inputStream);
                encoding = EncodingDetector.inferStreamEncoding(inputStream, debug ? err : null);
                if (debug) {
                    err.info("unparsed-text(): inferred encoding = " + encoding);
                }
            }
        } catch (IOException e) {
            encoding = "UTF-8";
        }
        return makeReaderFromStream(inputStream, encoding);
    }

    @CSharpReplaceBody(code="throw new Saxon.Hej.trans.XPathException(\"classpath: URI scheme is not supported on .NET\");")
    private static InputStream openClasspathResource(Configuration config, String systemId) throws XPathException {
        return config.getClass().getClassLoader().getResourceAsStream(systemId.substring(10));
    }


    @CSharpReplaceBody(code = "return new System.IO.StreamReader(stream, System.Text.Encoding.GetEncoding(encoding));")
    private static Reader makeReaderFromStream(InputStream stream, String encoding) throws XPathException {
        // The following is necessary to ensure that encoding errors are not recovered.
        try {
            Charset charset2 = Charset.forName(encoding);
            CharsetDecoder decoder = charset2.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return new BufferedReader(new InputStreamReader(stream, decoder));
        } catch (IllegalCharsetNameException | UnsupportedCharsetException icne) {
            throw new XPathException("Invalid encoding name: " + encoding, "FOUT1190");
        }
    }


}

