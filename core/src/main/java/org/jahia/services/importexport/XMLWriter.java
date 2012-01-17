/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

// XMLWriter.java - serialize an XML document.
// Written by David Megginson, david@megginson.com
// NO WARRANTY!  This class is in the public domain.

// $Id$

package org.jahia.services.importexport;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Filter to write an XML document from a SAX event stream.
 *
 * <p>This class can be used by itself or as part of a SAX event
 * stream: it takes as input a series of SAX2 ContentHandler
 * events and uses the information in those events to write
 * an XML document.  Since this class is a filter, it can also
 * pass the events on down a filter chain for further processing
 * (you can use the XMLWriter to take a snapshot of the current
 * state at any point in a filter chain), and it can be
 * used directly as a ContentHandler for a SAX2 XMLReader.</p>
 *
 * <p>The client creates a document by invoking the methods for
 * standard SAX2 events, always beginning with the
 * {@link #startDocument startDocument} method and ending with
 * the {@link #endDocument endDocument} method.  There are convenience
 * methods provided so that clients to not have to create empty
 * attribute lists or provide empty strings as parameters; for
 * example, the method invocation</p>
 *
 * <pre>
 * w.startElement("foo");
 * </pre>
 *
 * <p>is equivalent to the regular SAX2 ContentHandler method</p>
 *
 * <pre>
 * w.startElement("", "foo", "", new AttributesImpl());
 * </pre>
 *
 * <p>Except that it is more efficient because it does not allocate
 * a new empty attribute list each time.  The following code will send
 * a simple XML document to standard output:</p>
 *
 * <pre>
 * XMLWriter w = new XMLWriter();
 *
 * w.startDocument();
 * w.startElement("greeting");
 * w.characters("Hello, world!");
 * w.endElement("greeting");
 * w.endDocument();
 * </pre>
 *
 * <p>The resulting document will look like this:</p>
 *
 * <pre>
 * &lt;?xml version="1.0" standalone="yes"?>
 *
 * &lt;greeting>Hello, world!&lt;/greeting>
 * </pre>
 *
 * <p>In fact, there is an even simpler convenience method,
 * <var>dataElement</var>, designed for writing elements that
 * contain only character data, so the code to generate the
 * document could be shortened to</p>
 *
 * <pre>
 * XMLWriter w = new XMLWriter();
 *
 * w.startDocument();
 * w.dataElement("greeting", "Hello, world!");
 * w.endDocument();
 * </pre>
 *
 * <h2>Whitespace</h2>
 *
 * <p>According to the XML Recommendation, <em>all</em> whitespace
 * in an XML document is potentially significant to an application,
 * so this class never adds newlines or indentation.  If you
 * insert three elements in a row, as in</p>
 *
 * <pre>
 * w.dataElement("item", "1");
 * w.dataElement("item", "2");
 * w.dataElement("item", "3");
 * </pre>
 *
 * <p>you will end up with</p>
 *
 * <pre>
 * &lt;item>1&lt;/item>&lt;item>3&lt;/item>&lt;item>3&lt;/item>
 * </pre>
 *
 * <p>You need to invoke one of the <var>characters</var> methods
 * explicitly to add newlines or indentation.  Alternatively, you
 * can use {@link DataWriter DataWriter}, which
 * is derived from this class -- it is optimized for writing
 * purely data-oriented (or field-oriented) XML, and does automatic
 * linebreaks and indentation (but does not support mixed content
 * properly).</p>
 *
 *
 * <h2>Namespace Support</h2>
 *
 * <p>The writer contains extensive support for XML Namespaces, so that
 * a client application does not have to keep track of prefixes and
 * supply <var>xmlns</var> attributes.  By default, the XML writer will
 * generate Namespace declarations in the form _NS1, _NS2, etc., wherever
 * they are needed, as in the following example:</p>
 *
 * <pre>
 * w.startDocument();
 * w.emptyElement("http://www.foo.com/ns/", "foo");
 * w.endDocument();
 * </pre>
 *
 * <p>The resulting document will look like this:</p>
 *
 * <pre>
 * &lt;?xml version="1.0" standalone="yes"?>
 *
 * &lt;_NS1:foo xmlns:_NS1="http://www.foo.com/ns/"/>
 * </pre>
 *
 * <p>In many cases, document authors will prefer to choose their
 * own prefixes rather than using the (ugly) default names.  The
 * XML writer allows two methods for selecting prefixes:</p>
 *
 * <ol>
 * <li>the qualified name</li>
 * <li>the {@link #setPrefix setPrefix} method.</li>
 * </ol>
 *
 * <p>Whenever the XML writer finds a new Namespace URI, it checks
 * to see if a qualified (prefixed) name is also available; if so
 * it attempts to use the name's prefix (as long as the prefix is
 * not already in use for another Namespace URI).</p>
 *
 * <p>Before writing a document, the client can also pre-map a prefix
 * to a Namespace URI with the setPrefix method:</p>
 *
 * <pre>
 * w.setPrefix("http://www.foo.com/ns/", "foo");
 * w.startDocument();
 * w.emptyElement("http://www.foo.com/ns/", "foo");
 * w.endDocument();
 * </pre>
 *
 * <p>The resulting document will look like this:</p>
 *
 * <pre>
 * &lt;?xml version="1.0" standalone="yes"?>
 *
 * &lt;foo:foo xmlns:foo="http://www.foo.com/ns/"/>
 * </pre>
 *
 * <p>The default Namespace simply uses an empty string as the prefix:</p>
 *
 * <pre>
 * w.setPrefix("http://www.foo.com/ns/", "");
 * w.startDocument();
 * w.emptyElement("http://www.foo.com/ns/", "foo");
 * w.endDocument();
 * </pre>
 *
 * <p>The resulting document will look like this:</p>
 *
 * <pre>
 * &lt;?xml version="1.0" standalone="yes"?>
 *
 * &lt;foo xmlns="http://www.foo.com/ns/"/>
 * </pre>
 *
 * <p>By default, the XML writer will not declare a Namespace until
 * it is actually used.  Sometimes, this approach will create
 * a large number of Namespace declarations, as in the following
 * example:</p>
 *
 * <pre>
 * &lt;xml version="1.0" standalone="yes"?>
 *
 * &lt;rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
 *  &lt;rdf:Description about="http://www.foo.com/ids/books/12345">
 *   &lt;dc:title xmlns:dc="http://www.purl.org/dc/">A Dark Night&lt;/dc:title>
 *   &lt;dc:creator xmlns:dc="http://www.purl.org/dc/">Jane Smith&lt;/dc:title>
 *   &lt;dc:date xmlns:dc="http://www.purl.org/dc/">2000-09-09&lt;/dc:title>
 *  &lt;/rdf:Description>
 * &lt;/rdf:RDF>
 * </pre>
 *
 * <p>The "rdf" prefix is declared only once, because the RDF Namespace
 * is used by the root element and can be inherited by all of its
 * descendants; the "dc" prefix, on the other hand, is declared three
 * times, because no higher element uses the Namespace.  To solve this
 * problem, you can instruct the XML writer to predeclare Namespaces
 * on the root element even if they are not used there:</p>
 *
 * <pre>
 * w.forceNSDecl("http://www.purl.org/dc/");
 * </pre>
 *
 * <p>Now, the "dc" prefix will be declared on the root element even
 * though it's not needed there, and can be inherited by its
 * descendants:</p>
 *
 * <pre>
 * &lt;xml version="1.0" standalone="yes"?>
 *
 * &lt;rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 *             xmlns:dc="http://www.purl.org/dc/">
 *  &lt;rdf:Description about="http://www.foo.com/ids/books/12345">
 *   &lt;dc:title>A Dark Night&lt;/dc:title>
 *   &lt;dc:creator>Jane Smith&lt;/dc:title>
 *   &lt;dc:date>2000-09-09&lt;/dc:title>
 *  &lt;/rdf:Description>
 * &lt;/rdf:RDF>
 * </pre>
 *
 * <p>This approach is also useful for declaring Namespace prefixes
 * that be used by qualified names appearing in attribute values or
 * character data.</p>
 *
 * @author David Megginson, david@megginson.com
 * @version 0.2
 * @see org.xml.sax.XMLFilter
 * @see org.xml.sax.ContentHandler
 */
public class XMLWriter extends XMLFilterImpl
{



    ////////////////////////////////////////////////////////////////////
    // Constructors.
    ////////////////////////////////////////////////////////////////////


    /**
     * Create a new XML writer.
     *
     * <p>Write to standard output.</p>
     */
    public XMLWriter ()
    {
        init(null);
    }


    /**
     * Create a new XML writer.
     *
     * <p>Write to the writer provided.</p>
     *
     * @param writer The output destination, or null to use standard
     *        output.
     */
    public XMLWriter (Writer writer)
    {
        init(writer);
    }


    /**
     * Create a new XML writer.
     *
     * <p>Use the specified XML reader as the parent.</p>
     *
     * @param xmlreader The parent in the filter chain, or null
     *        for no parent.
     */
    public XMLWriter (XMLReader xmlreader)
    {
        super(xmlreader);
        init(null);
    }


    /**
     * Create a new XML writer.
     *
     * <p>Use the specified XML reader as the parent, and write
     * to the specified writer.</p>
     *
     * @param xmlreader The parent in the filter chain, or null
     *        for no parent.
     * @param writer The output destination, or null to use standard
     *        output.
     */
    public XMLWriter (XMLReader xmlreader, Writer writer)
    {
        super(xmlreader);
        init(writer);
    }


    /**
     * Internal initialization method.
     *
     * <p>All of the public constructors invoke this method.
     *
     * @param writer The output destination, or null to use
     *        standard output.
     */
    private void init (Writer writer)
    {
        setOutput(writer);
        nsSupport = new NamespaceSupport();
        prefixTable = new HashMap();
        forcedDeclTable = new HashMap();
        doneDeclTable = new HashMap();
    }




    ////////////////////////////////////////////////////////////////////
    // Public methods.
    ////////////////////////////////////////////////////////////////////


    /**
     * Reset the writer.
     *
     * <p>This method is especially useful if the writer throws an
     * exception before it is finished, and you want to reuse the
     * writer for a new document.  It is usually a good idea to
     * invoke {@link #flush flush} before resetting the writer,
     * to make sure that no output is lost.</p>
     *
     * <p>This method is invoked automatically by the
     * {@link #startDocument startDocument} method before writing
     * a new document.</p>
     *
     * <p><strong>Note:</strong> this method will <em>not</em>
     * clear the prefix or URI information in the writer or
     * the selected output writer.</p>
     *
     * @see #flush
     */
    public void reset ()
    {
        elementLevel = 0;
        prefixCounter = 0;
        nsSupport.reset();
    }


    /**
     * Flush the output.
     *
     * <p>This method flushes the output stream.  It is especially useful
     * when you need to make certain that the entire document has
     * been written to output but do not want to close the output
     * stream.</p>
     *
     * <p>This method is invoked automatically by the
     * {@link #endDocument endDocument} method after writing a
     * document.</p>
     *
     * @see #reset
     */
    public void flush ()
        throws IOException
    {
        output.flush();
    }


    /**
     * Set a new output destination for the document.
     *
     * @param writer The output destination, or null to use
     *        standard output.
     * @return The current output writer.
     * @see #flush
     */
    public void setOutput (Writer writer)
    {
        if (writer == null) {
            output = new OutputStreamWriter(System.out);
        } else {
            output = writer;
        }
    }


    /**
     * Specify a preferred prefix for a Namespace URI.
     *
     * <p>Note that this method does not actually force the Namespace
     * to be declared; to do that, use the {@link
     * #forceNSDecl(java.lang.String) forceNSDecl} method as well.</p>
     *
     * @param uri The Namespace URI.
     * @param prefix The preferred prefix, or "" to select
     *        the default Namespace.
     * @see #getPrefix
     * @see #forceNSDecl(java.lang.String)
     * @see #forceNSDecl(java.lang.String,java.lang.String)
     */
    public void setPrefix (String uri, String prefix)
    {
        prefixTable.put(uri, prefix);
    }


    /**
     * Get the current or preferred prefix for a Namespace URI.
     *
     * @param uri The Namespace URI.
     * @return The preferred prefix, or "" for the default Namespace.
     * @see #setPrefix
     */
    public String getPrefix (String uri)
    {
        return (String)prefixTable.get(uri);
    }


    /**
     * Force a Namespace to be declared on the root element.
     *
     * <p>By default, the XMLWriter will declare only the Namespaces
     * needed for an element; as a result, a Namespace may be
     * declared many places in a document if it is not used on the
     * root element.</p>
     *
     * <p>This method forces a Namespace to be declared on the root
     * element even if it is not used there, and reduces the number
     * of xmlns attributes in the document.</p>
     *
     * @param uri The Namespace URI to declare.
     * @see #forceNSDecl(java.lang.String,java.lang.String)
     * @see #setPrefix
     */
    public void forceNSDecl (String uri)
    {
        forcedDeclTable.put(uri, Boolean.TRUE);
    }


    /**
     * Force a Namespace declaration with a preferred prefix.
     *
     * <p>This is a convenience method that invokes {@link
     * #setPrefix setPrefix} then {@link #forceNSDecl(java.lang.String)
     * forceNSDecl}.</p>
     *
     * @param uri The Namespace URI to declare on the root element.
     * @param prefix The preferred prefix for the Namespace, or ""
     *        for the default Namespace.
     * @see #setPrefix
     * @see #forceNSDecl(java.lang.String)
     */
    public void forceNSDecl (String uri, String prefix)
    {
        setPrefix(uri, prefix);
        forceNSDecl(uri);
    }




    ////////////////////////////////////////////////////////////////////
    // Methods from org.xml.sax.ContentHandler.
    ////////////////////////////////////////////////////////////////////


    /**
     * Write the XML declaration at the beginning of the document.
     *
     * Pass the event on down the filter chain for further processing.
     *
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the XML declaration, or if a handler further down
     *            the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#startDocument
     */
    public void startDocument ()
        throws SAXException
    {
        reset();
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        super.startDocument();
    }


    /**
     * Write a newline at the end of the document.
     *
     * Pass the event on down the filter chain for further processing.
     *
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the newline, or if a handler further down
     *            the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#endDocument
     */
    public void endDocument ()
        throws SAXException
    {
        write('\n');
        super.endDocument();
        try {
            flush();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }


    /**
     * Write a start tag.
     *
     * Pass the event on down the filter chain for further processing.
     *
     * @param uri The Namespace URI, or the empty string if none
     *        is available.
     * @param localName The element's local (unprefixed) name (required).
     * @param qName The element's qualified (prefixed) name, or the
     *        empty string is none is available.  This method will
     *        use the qName as a template for generating a prefix
     *        if necessary, but it is not guaranteed to use the
     *        same qName.
     * @param atts The element's attribute list (must not be null).
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the start tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement (String uri, String localName,
                              String qName, Attributes atts)
        throws SAXException
    {
        elementLevel++;
        nsSupport.pushContext();
        write('<');
        writeName(uri, localName, qName, true);
        writeAttributes(atts);
        if (elementLevel == 1) {
            forceNSDecls();
        }
//        writeNSDecls();
        write('>');
        super.startElement(uri, localName, qName, atts);
    }


    /**
     * Write an end tag.
     *
     * Pass the event on down the filter chain for further processing.
     *
     * @param uri The Namespace URI, or the empty string if none
     *        is available.
     * @param localName The element's local (unprefixed) name (required).
     * @param qName The element's qualified (prefixed) name, or the
     *        empty string is none is available.  This method will
     *        use the qName as a template for generating a prefix
     *        if necessary, but it is not guaranteed to use the
     *        same qName.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the end tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    public void endElement (String uri, String localName, String qName)
        throws SAXException
    {
        write("</");
        writeName(uri, localName, qName, true);
        write('>');
        if (elementLevel == 1) {
            write('\n');
        }
        super.endElement(uri, localName, qName);
        nsSupport.popContext();
        elementLevel--;
    }


    /**
     * Write character data.
     *
     * Pass the event on down the filter chain for further processing.
     *
     * @param ch The array of characters to write.
     * @param start The starting position in the array.
     * @param len The number of characters to write.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the characters, or if a handler further down
     *            the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    public void characters (char ch[], int start, int len)
        throws SAXException
    {
        writeEsc(ch, start, len, false);
        super.characters(ch, start, len);
    }


    /**
     * Write ignorable whitespace.
     *
     * Pass the event on down the filter chain for further processing.
     *
     * @param ch The array of characters to write.
     * @param start The starting position in the array.
     * @param length The number of characters to write.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the whitespace, or if a handler further down
     *            the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#ignorableWhitespace
     */
    public void ignorableWhitespace (char ch[], int start, int length)
        throws SAXException
    {
        writeEsc(ch, start, length, false);
        super.ignorableWhitespace(ch, start, length);
    }



    /**
     * Write a processing instruction.
     *
     * Pass the event on down the filter chain for further processing.
     *
     * @param target The PI target.
     * @param data The PI data.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the PI, or if a handler further down
     *            the filter chain raises an exception.
     * @see org.xml.sax.ContentHandler#processingInstruction
     */
    public void processingInstruction (String target, String data)
        throws SAXException
    {
        write("<?");
        write(target);
        write(' ');
        write(data);
        write("?>");
        if (elementLevel < 1) {
            write('\n');
        }
        super.processingInstruction(target, data);
    }




    ////////////////////////////////////////////////////////////////////
    // Additional markup.
    ////////////////////////////////////////////////////////////////////

    /**
     * Write an empty element.
     *
     * This method writes an empty element tag rather than a start tag
     * followed by an end tag.  Both a {@link #startElement
     * startElement} and an {@link #endElement endElement} event will
     * be passed on down the filter chain.
     *
     * @param uri The element's Namespace URI, or the empty string
     *        if the element has no Namespace or if Namespace
     *        processing is not being performed.
     * @param localName The element's local name (without prefix).  This
     *        parameter must be provided.
     * @param qName The element's qualified name (with prefix), or
     *        the empty string if none is available.  This parameter
     *        is strictly advisory: the writer may or may not use
     *        the prefix attached.
     * @param atts The element's attribute list.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the empty tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see #startElement
     * @see #endElement
     */
    public void emptyElement (String uri, String localName,
                              String qName, Attributes atts)
        throws SAXException
    {
        nsSupport.pushContext();
        write('<');
        writeName(uri, localName, qName, true);
        writeAttributes(atts);
        if (elementLevel == 1) {
            forceNSDecls();
        }
        writeNSDecls();
        write("/>");
        super.startElement(uri, localName, qName, atts);
        super.endElement(uri, localName, qName);
    }




    ////////////////////////////////////////////////////////////////////
    // Convenience methods.
    ////////////////////////////////////////////////////////////////////



    /**
     * Start a new element without a qname or attributes.
     *
     * <p>This method will provide a default empty attribute
     * list and an empty string for the qualified name.
     * It invokes {@link
     * #startElement(String, String, String, Attributes)}
     * directly.</p>
     *
     * @param uri The element's Namespace URI.
     * @param localName The element's local name.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the start tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see #startElement(String, String, String, Attributes)
     */
    public void startElement (String uri, String localName)
        throws SAXException
    {
        startElement(uri, localName, "", EMPTY_ATTS);
    }


    /**
     * Start a new element without a qname, attributes or a Namespace URI.
     *
     * <p>This method will provide an empty string for the
     * Namespace URI, and empty string for the qualified name,
     * and a default empty attribute list. It invokes
     * #startElement(String, String, String, Attributes)}
     * directly.</p>
     *
     * @param localName The element's local name.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the start tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see #startElement(String, String, String, Attributes)
     */
    public void startElement (String localName)
        throws SAXException
    {
        startElement("", localName, "", EMPTY_ATTS);
    }


    /**
     * End an element without a qname.
     *
     * <p>This method will supply an empty string for the qName.
     * It invokes {@link #endElement(String, String, String)}
     * directly.</p>
     *
     * @param uri The element's Namespace URI.
     * @param localName The element's local name.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the end tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see #endElement(String, String, String)
     */
    public void endElement (String uri, String localName)
        throws SAXException
    {
        endElement(uri, localName, "");
    }


    /**
     * End an element without a Namespace URI or qname.
     *
     * <p>This method will supply an empty string for the qName
     * and an empty string for the Namespace URI.
     * It invokes {@link #endElement(String, String, String)}
     * directly.</p>
     *
     * @param localName The element's local name.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the end tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see #endElement(String, String, String)
     */
    public void endElement (String localName)
        throws SAXException
    {
        endElement("", localName, "");
    }


    /**
     * Add an empty element without a qname or attributes.
     *
     * <p>This method will supply an empty string for the qname
     * and an empty attribute list.  It invokes
     * {@link #emptyElement(String, String, String, Attributes)}
     * directly.</p>
     *
     * @param uri The element's Namespace URI.
     * @param localName The element's local name.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the empty tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see #emptyElement(String, String, String, Attributes)
     */
    public void emptyElement (String uri, String localName)
        throws SAXException
    {
        emptyElement(uri, localName, "", EMPTY_ATTS);
    }


    /**
     * Add an empty element without a Namespace URI, qname or attributes.
     *
     * <p>This method will supply an empty string for the qname,
     * and empty string for the Namespace URI, and an empty
     * attribute list.  It invokes
     * {@link #emptyElement(String, String, String, Attributes)}
     * directly.</p>
     *
     * @param localName The element's local name.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the empty tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see #emptyElement(String, String, String, Attributes)
     */
    public void emptyElement (String localName)
        throws SAXException
    {
        emptyElement("", localName, "", EMPTY_ATTS);
    }


    /**
     * Write an element with character data content.
     *
     * <p>This is a convenience method to write a complete element
     * with character data content, including the start tag
     * and end tag.</p>
     *
     * <p>This method invokes
     * {@link #startElement(String, String, String, Attributes)},
     * followed by
     * {@link #characters(String)}, followed by
     * {@link #endElement(String, String, String)}.</p>
     *
     * @param uri The element's Namespace URI.
     * @param localName The element's local name.
     * @param qName The element's default qualified name.
     * @param atts The element's attributes.
     * @param content The character data content.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the empty tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see #startElement(String, String, String, Attributes)
     * @see #characters(String)
     * @see #endElement(String, String, String)
     */
    public void dataElement (String uri, String localName,
                             String qName, Attributes atts,
                             String content)
        throws SAXException
    {
        startElement(uri, localName, qName, atts);
        characters(content);
        endElement(uri, localName, qName);
    }


    /**
     * Write an element with character data content but no attributes.
     *
     * <p>This is a convenience method to write a complete element
     * with character data content, including the start tag
     * and end tag.  This method provides an empty string
     * for the qname and an empty attribute list.</p>
     *
     * <p>This method invokes
     * {@link #startElement(String, String, String, Attributes)},
     * followed by
     * {@link #characters(String)}, followed by
     * {@link #endElement(String, String, String)}.</p>
     *
     * @param uri The element's Namespace URI.
     * @param localName The element's local name.
     * @param content The character data content.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the empty tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see #startElement(String, String, String, Attributes)
     * @see #characters(String)
     * @see #endElement(String, String, String)
     */
    public void dataElement (String uri, String localName, String content)
        throws SAXException
    {
        dataElement(uri, localName, "", EMPTY_ATTS, content);
    }


    /**
     * Write an element with character data content but no attributes or Namespace URI.
     *
     * <p>This is a convenience method to write a complete element
     * with character data content, including the start tag
     * and end tag.  The method provides an empty string for the
     * Namespace URI, and empty string for the qualified name,
     * and an empty attribute list.</p>
     *
     * <p>This method invokes
     * {@link #startElement(String, String, String, Attributes)},
     * followed by
     * {@link #characters(String)}, followed by
     * {@link #endElement(String, String, String)}.</p>
     *
     * @param localName The element's local name.
     * @param content The character data content.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the empty tag, or if a handler further down
     *            the filter chain raises an exception.
     * @see #startElement(String, String, String, Attributes)
     * @see #characters(String)
     * @see #endElement(String, String, String)
     */
    public void dataElement (String localName, String content)
        throws SAXException
    {
        dataElement("", localName, "", EMPTY_ATTS, content);
    }


    /**
     * Write a string of character data, with XML escaping.
     *
     * <p>This is a convenience method that takes an XML
     * String, converts it to a character array, then invokes
     * {@link #characters(char[], int, int)}.</p>
     *
     * @param data The character data.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the string, or if a handler further down
     *            the filter chain raises an exception.
     * @see #characters(char[], int, int)
     */
    public void characters (String data)
        throws SAXException
    {
        char ch[] = data.toCharArray();
        characters(ch, 0, ch.length);
    }




    ////////////////////////////////////////////////////////////////////
    // Internal methods.
    ////////////////////////////////////////////////////////////////////


    /**
     * Force all Namespaces to be declared.
     *
     * This method is used on the root element to ensure that
     * the predeclared Namespaces all appear.
     */
    private void forceNSDecls ()
    {
        Iterator prefixes = forcedDeclTable.keySet().iterator();
        while (prefixes.hasNext()) {
            String prefix = (String)prefixes.next();
            doPrefix(prefix, null, true);
        }
    }


    /**
     * Determine the prefix for an element or attribute name.
     *
     * TODO: this method probably needs some cleanup.
     *
     * @param uri The Namespace URI.
     * @param qName The qualified name (optional); this will be used
     *        to indicate the preferred prefix if none is currently
     *        bound.
     * @param isElement true if this is an element name, false
     *        if it is an attribute name (which cannot use the
     *        default Namespace).
     */
    private String doPrefix (String uri, String qName, boolean isElement)
    {
        String defaultNS = nsSupport.getURI("");
        if ("".equals(uri)) {
            if (isElement && defaultNS != null)
                nsSupport.declarePrefix("", "");
            return null;
        }
        String prefix;
        if (isElement && defaultNS != null && uri.equals(defaultNS)) {
            prefix = "";
        } else {
            prefix = nsSupport.getPrefix(uri);
        }
        if (prefix != null) {
            return prefix;
        }
        prefix = (String) doneDeclTable.get(uri);
        if (prefix != null &&
            ((!isElement || defaultNS != null) &&
             "".equals(prefix) || nsSupport.getURI(prefix) != null)) {
            prefix = null;
        }
        if (prefix == null) {
            prefix = (String) prefixTable.get(uri);
            if (prefix != null &&
                ((!isElement || defaultNS != null) &&
                 "".equals(prefix) || nsSupport.getURI(prefix) != null)) {
                prefix = null;
            }
        }
        if (prefix == null && qName != null && !"".equals(qName)) {
            int i = qName.indexOf(':');
            if (i == -1) {
                if (isElement && defaultNS == null) {
                    prefix = "";
                }
            } else {
                prefix = qName.substring(0, i);
            }
        }
        for (;
             prefix == null || nsSupport.getURI(prefix) != null;
             prefix = "__NS" + ++prefixCounter)
            ;
        nsSupport.declarePrefix(prefix, uri);
        doneDeclTable.put(uri, prefix);
        return prefix;
    }


    /**
     * Write a raw character.
     *
     * @param c The character to write.
     * @exception org.xml.sax.SAXException If there is an error writing
     *            the character, this method will throw an IOException
     *            wrapped in a SAXException.
     */
    private void write (char c)
        throws SAXException
    {
        try {
            output.write(c);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }


    /**
     * Write a raw string.
     *
     * @param s
     * @exception org.xml.sax.SAXException If there is an error writing
     *            the string, this method will throw an IOException
     *            wrapped in a SAXException
     */
    private void write (String s)
    throws SAXException
    {
        try {
            output.write(s);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }


    /**
     * Write out an attribute list, escaping values.
     *
     * The names will have prefixes added to them.
     *
     * @param atts The attribute list to write.
     * @exception SAXException If there is an error writing
     *            the attribute list, this method will throw an
     *            IOException wrapped in a SAXException.
     */
    private void writeAttributes (Attributes atts)
        throws SAXException
    {
        int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            char ch[] = atts.getValue(i).toCharArray();
            write(' ');
            writeName(atts.getURI(i), atts.getLocalName(i),
                      atts.getQName(i), false);
            write("=\"");
            writeEsc(ch, 0, ch.length, true);
            write('"');
        }
    }


    /**
     * Write an array of data characters with escaping.
     *
     * @param ch The array of characters.
     * @param start The starting position.
     * @param length The number of characters to use.
     * @param isAttVal true if this is an attribute value literal.
     * @exception SAXException If there is an error writing
     *            the characters, this method will throw an
     *            IOException wrapped in a SAXException.
     */
    private void writeEsc (char ch[], int start,
                             int length, boolean isAttVal)
        throws SAXException
    {
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
            case '&':
                write("&amp;");
                break;
            case '\n':
                write("&#010;");
                break;
            case '\r':
                write("&#013;");
                break;
            case '<':
                write("&lt;");
                break;
            case '>':
                write("&gt;");
                break;
            case '\"':
                if (isAttVal) {
                    write("&quot;");
                } else {
                    write('\"');
                }
                break;
                default:
                    //UNIL modification: because of XML parser compatibility for ISO/IEC 10646
                    if(ch[i] == '\u0009') {
                        //ok for tab char or \t
                        write(ch[i]);
                    }else if(ch[i] == '\n') {
                        //ok for LF or u000A
                        write(ch[i]);
                    }else if(ch[i] == '\r') {
                        //ok for CR or u000D
                        write(ch[i]);
                    }else if(ch[i] >= '\u0020' && ch[i] <= '\uD7FF') {
                        //ok
                        write(ch[i]);
                    }else if(ch[i] >= '\uE000' && ch[i] <= '\uFFFD') {
                        //ok
                        write(ch[i]);
                    }
                    /*
                               //later for UTF-16 maybe
                             else if(ch[i] >= '\u10000' && ch[i] <= '\u10FFFF') {
                                 //ok
                               write(ch[i]);
                            }*/
                    else {
                        //nothing
                        System.err.println("Avoided char: "+ch[i] +" or as: "+ Integer.toString(ch[i]));
                    }
            }//switch
        }//for
    }

    /**
     * Write out the list of Namespace declarations.
     *
     * @exception org.xml.sax.SAXException This method will throw
     *            an IOException wrapped in a SAXException if
     *            there is an error writing the Namespace
     *            declarations.
     */
    private void writeNSDecls ()
        throws SAXException
    {
        Iterator prefixes = new EnumerationIterator(nsSupport.getDeclaredPrefixes());
        while (prefixes.hasNext()) {
            String prefix = (String) prefixes.next();
            String uri = nsSupport.getURI(prefix);
            if (uri == null) {
                uri = "";
            }
            char ch[] = uri.toCharArray();
            write(' ');
            if ("".equals(prefix)) {
                write("xmlns=\"");
            } else {
                write("xmlns:");
                write(prefix);
                write("=\"");
            }
            writeEsc(ch, 0, ch.length, true);
            write('\"');
        }
    }


    /**
     * Write an element or attribute name.
     *
     * @param uri The Namespace URI.
     * @param localName The local name.
     * @param qName The prefixed name, if available, or the empty string.
     * @param isElement true if this is an element name, false if it
     *        is an attribute name.
     * @exception org.xml.sax.SAXException This method will throw an
     *            IOException wrapped in a SAXException if there is
     *            an error writing the name.
     */
    private void writeName (String uri, String localName,
                              String qName, boolean isElement)
        throws SAXException
    {
        String prefix = doPrefix(uri, qName, isElement);
        if (prefix != null && !"".equals(prefix)) {
            write(prefix);
            write(':');
        }
        write(localName);
    }




    ////////////////////////////////////////////////////////////////////
    // Constants.
    ////////////////////////////////////////////////////////////////////

    private final Attributes EMPTY_ATTS = new AttributesImpl();




    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////

    private Map prefixTable;
    private Map forcedDeclTable;
    private Map doneDeclTable;
    private int elementLevel = 0;
    private Writer output;
    private NamespaceSupport nsSupport;
    private int prefixCounter = 0;
}