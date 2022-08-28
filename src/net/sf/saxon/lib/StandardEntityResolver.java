////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.lib;

import net.sf.saxon.Configuration;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Objects;

/**
 * This class is an EntityResolver used to resolve references to common
 * DTDs and entity files, using local copies provided with the Saxon product.
 * It has become necessary to do this because W3C is no longer serving
 * these files from its server. Ideally the job of caching these files
 * would belong to the XML parser, but because many of the parsers were
 * issued years ago, they cannot be relied on to do it.
 */
public class StandardEntityResolver implements EntityResolver {

    public Configuration config;


    public StandardEntityResolver(Configuration config) {
        Objects.requireNonNull(config);
        this.config = config;
    }

    /**
     * Set configuration details. This is used to control tracing of accesses to files
     *
     * @param config the Saxon configuration
     */

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    /**
     * Allow the application to resolve external entities.
     * <p>The parser will call this method before opening any external
     * entity except the top-level document entity.  Such entities include
     * the external DTD subset and external parameter entities referenced
     * within the DTD (in either case, only if the parser reads external
     * parameter entities), and external general entities referenced
     * within the document element (if the parser reads external general
     * entities).  The application may request that the parser locate
     * the entity itself, that it use an alternative URI, or that it
     * use data provided by the application (as a character or byte
     * input stream).</p>
     * <p>Application writers can use this method to redirect external
     * system identifiers to secure and/or local URIs, to look up
     * public identifiers in a catalogue, or to read an entity from a
     * database or other input source (including, for example, a dialog
     * box).  Neither XML nor SAX specifies a preferred policy for using
     * public or system IDs to resolve resources.  However, SAX specifies
     * how to interpret any InputSource returned by this method, and that
     * if none is returned, then the system ID will be dereferenced as
     * a URL.  </p>
     * <p>If the system identifier is a URL, the SAX parser must
     * resolve it fully before reporting it to the application.</p>
     *
     * @param publicId The public identifier of the external entity
     *                 being referenced, or null if none was supplied.
     * @param systemId The system identifier of the external entity
     *                 being referenced.
     * @return An InputSource object describing the new input source,
     *         or null to request that the parser open a regular
     *         URI connection to the system identifier.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.InputSource
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        //return config.getResourceResolver().resolveEntity(publicId, systemId);
        return null; // TODO: fixme
    }

//    /**
//     * Get a resource from the classpath. This method is called if the URI uses the (Spring-defined)
//     * "classpath" URI scheme. It attempts to locate the resource on the
//     * classpath and returns an InputSource containing the relevant InputStream. If the
//     * inputstream cannot be located, it returns null. A subclass that does not want classpath
//     * URIs to be resolved in this way should override this method to return null unconditionally.
//     *
//     * @param resourceName    the resource to be fetched from the classpath
//     * @param config the Saxon Configuration object
//     * @return an InputSource representing the named resource (fetched from the classpath)
//     * or null if no resource is available.
//     */
//
//    protected InputSource getResource(String resourceName, Configuration config) {
//        InputStream inputStream = config.getDynamicLoader().getResourceAsStream(resourceName);
//        if (inputStream != null) {
//            InputSource inputSource = new InputSource(inputStream);
//            inputSource.setSystemId("classpath:" + resourceName);
//            return inputSource;
//        } else {
//            return null;
//        }
//    }
}

