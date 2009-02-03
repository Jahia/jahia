/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

 package org.jahia.utils.xml;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * Makes writing XML much much easier. 
 * Improved from 
 * <a href="http://builder.com.com/article.jhtml?id=u00220020318yan01.htm&page=1&vf=tt">article</a>
 *
 * @author <a href="mailto:bayard@apache.org">Henri Yandell</a>
 * @author <a href="mailto:pete@fingertipsoft.com">Peter Cassetta</a>
 * @version 1.0
 */
public class XmlWriter {

    private Writer writer;      // underlying writer
    private Stack stack;        // of xml entity names
    private StringBuffer attrs; // current attribute string
    private boolean empty;      // is the current node empty
    private boolean closed;     // is the current node closed...

    private String namespace;   // the current default namespace

    private boolean pretty;    // is pretty printing enabled?
    private boolean wroteText; // was text the last thing output?
    private String indent;     // output this to indent one level when pretty printing
    private String newline;    // output this to end a line when pretty printing

    /**
     * Create an XmlWriter on top of an existing java.io.Writer.
     */
    public XmlWriter(Writer writer) {
        this.writer = writer;
        this.closed = true;
        this.stack = new Stack();
        this.pretty = true;
        this.wroteText = false;
        this.newline = "\n";
        this.indent = "  ";
    }

    /**
     * Turn pretty printing on or off. 
     * Pretty printing is enabled by default, but it can be turned off
     * to generate more compact XML.  
     *
     * @param boolean true to enable, false to disable pretty printing.
     */
    public void enablePrettyPrint(boolean enable) {
        this.pretty = enable;
    }

    /**
     * Specify the string to prepend to a line for each level of indent. 
     * It is 2 spaces ("  ") by default. Some may prefer a single tab ("\t")
     * or a different number of spaces. Specifying an empty string will turn
     * off indentation when pretty printing.
     *
     * @param String representing one level of indentation while pretty printing.
     */
    public void setIndent(String indent) {
        this.indent = indent;
    }

    /**
     * Specify the string used to terminate each line when pretty printing. 
     * It is a single newline ("\n") by default. Users who need to read
     * generated XML documents in Windows editors like Notepad may wish to
     * set this to a carriage return/newline sequence ("\r\n"). Specifying
     * an empty string will turn off generation of line breaks when pretty
     * printing.  
     *
     * @param String representing the newline sequence when pretty printing.
     */
    public void setNewline(String newline) {
        this.newline = newline;
    }

    /**
     * The default namespace. Once this is turned on, any new entities 
     * will have this namespace, regardless of scope.
     *
     * @param String nname of the namespace
     */
    public void setDefaultNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * A helper method. It writes out an entity which contains only text.
     *
     * @param name String name of tag
     * @param text String of text to go inside the tag
     */
    public XmlWriter writeEntityWithText(String name, String text) throws IOException {
        writeEntity(name);
        writeText(text);
        return endEntity();
    }

    /**
     * A helper method. It writes out empty entities.
     *
     * @param name String name of tag
     */
    public XmlWriter writeEmptyEntity(String name) throws IOException {
        writeEntity(name);
        return endEntity();
    }

    /**
     * Begin to write out an entity. Unlike the helper tags, this tag 
     * will need to be ended with the endEntity method.
     *
     * @param name String name of tag
     */
    public XmlWriter writeEntity(String name) throws IOException {
        if(this.namespace == null) {
            return openEntity(name);
        } else {
            return openEntity(this.namespace+":"+name);
        }
    }

    /**
     * Begin to output an entity. 
     *
     * @param String name of entity.
     */
    private XmlWriter openEntity(String name) throws IOException {
        boolean wasClosed = this.closed;
        closeOpeningTag();
        this.closed = false;
        if (this.pretty) {
            //   ! wasClosed separates adjacent opening tags by a newline.
            // this.wroteText makes sure an entity embedded within the text of
            // its parent entity begins on a new line, indented to the proper
            // level. This solves only part of the problem of pretty printing
            // entities which contain both text and child entities.
            if (! wasClosed || this.wroteText) {
                this.writer.write(newline);
            }
            for (int i = 0; i < this.stack.size(); i++) {
                this.writer.write(indent); // Indent opening tag to proper level
            }
        }
        this.writer.write("<");
        this.writer.write(name);
        stack.add(name);
        this.empty = true;
        this.wroteText = false;
        return this;
    }

    // close off the opening tag
    private void closeOpeningTag() throws IOException {
        if (!this.closed) {
            writeAttributes();
            this.closed = true;
            this.writer.write(">");
        }
    }

    // write out all current attributes
    private void writeAttributes() throws IOException {
        if (this.attrs != null) {
            this.writer.write(this.attrs.toString());
            this.attrs.setLength(0);
            this.empty = false;
        }
    }

    /**
     * Write an attribute out for the current entity. 
     * Any xml characters in the value are escaped.
     * Currently it does not actually throw the exception, but 
     * the api is set that way for future changes.
     *
     * @param String name of attribute.
     * @param String value of attribute.
     */
    public XmlWriter writeAttribute(String attr, String value) throws IOException {
        return writeAttribute(attr, value, true);
    }

    /**
     * Write an attribute out for the current entity. 
     * Any xml characters in the value are escaped.
     * Currently it does not actually throw the exception, but 
     * the api is set that way for future changes.
     *
     * @param String name of attribute.
     * @param String value of attribute.
     * @param escapeXml should we escape the value as XML?
     */
    public XmlWriter writeAttribute(String attr, String value, boolean escapeXml) throws IOException {

        // maintain api
        if (false) throw new IOException();

        if (this.attrs == null) {
            this.attrs = new StringBuffer();
        }
        this.attrs.append(" ");
        this.attrs.append(attr);
        this.attrs.append("=\"");
        this.attrs.append(escapeXml ? XmlUtils.escapeXml(value) : value);
        this.attrs.append("\"");
        return this;
    }

    /**
     * End the current entity. This will throw an exception 
     * if it is called when there is not a currently open 
     * entity.
     */
    public XmlWriter endEntity() throws IOException {
        if(this.stack.empty()) {
            throw new IOException("Called endEntity too many times. ");
        }
        String name = (String)this.stack.pop();
        if (name != null) {
            if (this.empty) {
                writeAttributes();
                this.writer.write("/>");
            } else {
            if (this.pretty && ! this.wroteText) {
                for (int i = 0; i < this.stack.size(); i++) {
                    this.writer.write(indent); // Indent closing tag to proper level
                }
            }
            this.writer.write("</");
            this.writer.write(name);
            this.writer.write(">");
        }
        if (this.pretty)
            this.writer.write(newline); // Add a newline after the closing tag
            this.empty = false;
            this.closed = true;
            this.wroteText = false;
        }
        return this;
    }

    /**
     * Close this writer. It does not close the underlying 
     * writer, but does throw an exception if there are 
     * as yet unclosed tags.
     */
    public void close() throws IOException {
        if(!this.stack.empty()) {
            throw new IOException("Tags are not all closed. "+
                "Possibly, "+this.stack.pop()+" is unclosed. ");
        }
    }

    /**
     * Output body text. Any xml characters are escaped. 
     */
    public XmlWriter writeText(String text) throws IOException {
        closeOpeningTag();
        this.empty = false;
        this.wroteText = true;
        this.writer.write(XmlUtils.escapeXml(text));
        return this;
    }

    /**
     * Write out a chunk of CDATA. This helper method surrounds the 
     * passed in data with the CDATA tag.
     *
     * @param String of CDATA text.
     */
    public XmlWriter writeCData(String cdata) throws IOException {
        writeChunk("<![CDATA[ "+cdata+" ]]>");
        return this;
    }

    /**
     * Write out a chunk of comment. This helper method surrounds the 
     * passed in data with the xml comment tag.
     *
     * @param String of text to comment.
     */
    public XmlWriter writeComment(String comment) throws IOException {
        writeChunk("<!-- "+comment+" -->");
        return this;
    }
    private void writeChunk(String data) throws IOException {
        closeOpeningTag();
        this.empty = false;
        if (this.pretty && ! this.wroteText) {
            for (int i = 0; i < this.stack.size(); i++) {
                this.writer.write(indent); 
            }
        }
        this.writer.write(data);
        if (this.pretty) {
            this.writer.write(newline); 
        }
    }

}
