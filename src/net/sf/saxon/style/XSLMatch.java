////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.style;

import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PatternMatchExpression;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.ItemTypePattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;


/**
 * An xsl:match element in the stylesheet (new in XSLT 4.0).
 */

public class XSLMatch extends StyleElement {

    private Expression select;
    private Pattern pattern;

    /**
     * Determine whether this node is an instruction.
     *
     * @return true - it is an instruction
     */

    @Override
    public boolean isInstruction() {
        return true;
    }

    /**
     * Determine whether this type of element is allowed to contain a sequence constructor
     *
     * @return false
     */

    @Override
    protected boolean mayContainSequenceConstructor() {
        return false;
    }

    /**
     * Determine whether this type of element is allowed to contain an xsl:fallback
     * instruction
     */

    @Override
    protected boolean mayContainFallback() {
        return true;
    }

    public Expression getSelectExpression() {
        return select;
    }

    public void setSelectExpression(Expression select) {
        this.select = select;
    }


    @Override
    protected void prepareAttributes() {

        for (AttributeInfo att : attributes()) {
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String f = attName.getDisplayName();
            if (f.equals("select")) {
                select = makeExpression(value, att);
            } else if (f.equals("pattern")) {
                pattern = makePattern(value, "pattern");
            } else {
                checkUnknownAttribute(attName);
            }
        }
        if (select == null) {
            select = new ContextItemExpression();
        }
        if (pattern == null) {
            reportAbsence("pattern");
            pattern = new ItemTypePattern(AnyItemType.getInstance()); // for error recovery
        }
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        for (NodeInfo child : children()) {
            if (!(child instanceof XSLFallback)) {
                compileError("Only xsl:fallback is allowed within xsl:match");
                break;
            }
        }
        select = typeCheck("select", select);
        pattern = typeCheck("pattern", pattern);
    }

    /*@Nullable*/
    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        return new PatternMatchExpression(select, pattern);
    }

}

