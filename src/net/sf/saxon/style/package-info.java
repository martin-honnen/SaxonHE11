////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * <p>This package provides classes used during the compilation of an XSLT stylesheet.
 * The instances of these classes are discarded once compilation is complete, and they play no role
 * in executing the transformation at run-time, except when tracing and debugging are invoked.</p>
 * <p>The class <b>StyleElement</b> represents an element node on the stylesheet tree. Subclasses
 * of StyleElement represent individual stylesheet elements, and are generally named according to
 * the XSLT element name, for example XSLApplyTemplates, XSLChoose. The class <b>XSLStylesheet</b>
 * is used for the <code>xsl:stylesheet</code> element in each stylesheet module, and in particular for the
 * <code>xsl:stylesheet</code> element in the principal stylesheet module.</p>
 * <p>During construction of the stylesheet tree, the class <b>StyleNodeFactory</b> is nominated to
 * the <b>Builder</b> as the factory class responsible for creating element nodes on the tree. It is
 * this class that decides which subclass of StyleElement to use for each element appearing in the
 * stylesheet. For extension elements, the decision is delegated to a user-created
 * <b>ExtensionElementFactory</b>.</p>
 * <p>Each class provides a number of methods supporting the various phases of processing. The sequence
 * of events sometimes varies slightly, but in general the first phase is done by <code>prepareAttributes</code>,
 * which performs local validation of the attributes of each instruction. The second phase is represented
 * by the <code>validate</code> method, which does global validation, fixup of references, and type checking.
 * The third phase is done by the <code>compile</code> method, which generates <code>Instruction</code> and
 * <code>Expression</code> objects. Further processing (local and global optimization) is then done on these
 * Instruction objects, and is no longer the responsibility of this package.</p>
 */
package net.sf.saxon.style;
