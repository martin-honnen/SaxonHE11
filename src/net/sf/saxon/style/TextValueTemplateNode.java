////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.str.UnicodeString;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.TextImpl;
import net.sf.saxon.value.Whitespace;

/**
 * A text node in an XSLT 3.0 stylesheet that may or may not contain a text value template
 */
public class TextValueTemplateNode extends TextImpl {

    private Expression contentExp;
    private TextValueTemplateContext staticContext;

    public TextValueTemplateNode(UnicodeString value) {
        super(value);
    }

    public Expression getContentExpression() {
        return contentExp;
    }

    public TextValueTemplateContext getStaticContext() {
        if (staticContext == null) {
            staticContext = new TextValueTemplateContext((StyleElement) getParent(), this);
        }
        return staticContext;
    }

    /**
     * Parse any XPath expressions contained in the content of the text value template
     * @throws XPathException if parsing of an XPath expression fails
     */
    public void parse() throws XPathException {
        boolean disable = false;
        NodeInfo parent = getParent();
        if (parent instanceof XSLText && StyleElement.isYes(Whitespace.trim(parent.getAttributeValue("", "disable-output-escaping")))) {
            disable = true;
        }
        try {
            contentExp = AttributeValueTemplate.make(getUnicodeStringValue().toString(), getStaticContext());
        } catch (XPathException e) {
            e.setLocation(this);
            assert getParent() instanceof StyleElement;
            ((StyleElement) getParent()).compileError(e);
            contentExp = new StringLiteral(getStringValue());
        }
        contentExp = new ValueOf(contentExp, disable, false);
        assert getParent() != null;
        contentExp.setRetainedStaticContext(((StyleElement) getParent()).makeRetainedStaticContext());
    }

    /**
     * Validate the text node; specifically, perform type checking of any contained expressions
     * @throws XPathException if type checking finds any problems
     */
    public void validate() throws XPathException {
        assert getParent() != null;
        contentExp = ((StyleElement)getParent()).typeCheck("tvt", contentExp);
    }
}

