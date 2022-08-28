////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon;

import net.sf.saxon.jaxp.SaxonTransformerFactory;


/**
 * A TransformerFactory instance can be used to create Transformer and Template
 * objects.
 * <p>This implementation of the JAXP TransformerFactory interface always
 * creates a Saxon configuration with Saxon-HE capability, even if Saxon-PE or
 * Saxon-EE are loaded from the classpath; no attempt is made to look for a license
 * file. This is therefore the recommended factory to use if you want to be sure
 * that an application will run successfully under Saxon-HE, and will fail if it attempts to
 * use Saxon-PE or Saxon-EE facilities.</p>
 * <p>The system property that determines which Factory implementation
 * to create is named "javax.xml.transform.TransformerFactory". This
 * property names a concrete subclass of the TransformerFactory abstract
 * class. If the property is not defined, a platform default is be used.</p>
 * <p>This implementation class implements the abstract methods on both the
 * javax.xml.transform.TransformerFactory and javax.xml.transform.sax.SAXTransformerFactory
 * classes.</p>
 * <p>This class is the "public" implementation of the TransformerFactory
 * interface for Saxon-HE. It is a trivial subclass of the internal class
 * {@link SaxonTransformerFactory}, which is in a separate package
 * along with the implementation classes to which it has protected access.</p>
 * @since 9.8.0.5: see bug 3410
 */

public class BasicTransformerFactory extends TransformerFactoryImpl {

    public BasicTransformerFactory() {
        Configuration config = new Configuration();
        config.setProcessor(this);
        setConfiguration(config);
    }

    public BasicTransformerFactory(Configuration config) {
        super(config);
    }

}

