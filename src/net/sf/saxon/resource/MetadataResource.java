////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.resource;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.CallableDelegate;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.CallableFunction;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import java.util.Map;

/**
 * Represents information about a resource, as well as a pointer to the resource itself
 */
public class MetadataResource implements Resource {

    private final Map<String, GroundedValue> properties;
    private final String resourceURI;
    private final Resource content;
    private final XPathContext context;

    public MetadataResource(String resourceURI, Resource content, Map<String, GroundedValue> properties, XPathContext context) {
        this.resourceURI = resourceURI;
        this.content = content;
        this.properties = properties;
        this.context = context;
    }

    @Override
    public String getContentType() {
        return content.getContentType();
    }

    @Override
    public String getResourceURI() {
        return resourceURI;
    }

    @Override
    public Item getItem()  {

        // Create a map for the result
        DictionaryMap map = new DictionaryMap();

        // Add the custom properties of the resource
        for (Map.Entry<String, GroundedValue> entry : properties.entrySet()) {
             map.initialPut(entry.getKey(), entry.getValue());
        }

        // Add the resourceURI of the resource as the "name" property
        map.initialPut("name", StringValue.makeStringValue(resourceURI));

        // Add a fetch() function, which can be used to fetch the resource
        Callable fetcher = new CallableDelegate((context1, arguments) -> content.getItem());

        FunctionItemType fetcherType = new SpecificFunctionType(new SequenceType[0], SequenceType.SINGLE_ITEM);
        CallableFunction fetcherFunction = new CallableFunction(0, fetcher, fetcherType);

        map.initialPut("fetch", fetcherFunction);
        return map;
    }
}

