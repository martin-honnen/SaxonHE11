////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.registry.XPath40FunctionSet;
import net.sf.saxon.trans.XPathException;

/**
 * xsl:array-member element in stylesheet - XSLT 4.0 proposal.
 * The xsl:array-member element has the same content model as xsl:sequence. It evaluates the expression
 * in its select attribute or its contained sequence constructor, and wraps the resulting sequence
 * into an external object. This external object is recognized by the saxon:array instruction, and is
 * converted into a single member of the array.
 * The instruction also allows xsl:extension-element-prefixes etc.
 */

public class XSLArrayMember extends XSLSequence {


    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (getSelectExpression() == null) {
            setSelectExpression(compileSequenceConstructor(exec, decl, false));
        }
        SystemFunction arrayMem = XPath40FunctionSet.getInstance().makeFunction("parcel", 1);
        return arrayMem.makeFunctionCall(getSelectExpression());
    }

}



