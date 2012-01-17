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

package org.jahia.services.content.nodetypes;

import java.io.StreamTokenizer;
import java.io.Reader;
import java.io.IOException;

import org.jahia.utils.ArrayUtils;

/**
 * Lexer
 */
public class LexerLegacy {
    public static final char SINGLE_QUOTE = '\'';
    public static final char DOUBLE_QUOTE = '\"';
    public static final char BEGIN_NODE_TYPE_NAME = '[';
    public static final char END_NODE_TYPE_NAME = ']';
    public static final char EXTENDS = '>';
    public static final char LIST_DELIMITER = ',';
    public static final char PROPERTY_DEFINITION = '-';
    public static final char CHILD_NODE_DEFINITION = '+';
    public static final char BEGIN_TYPE = '(';
    public static final char END_TYPE = ')';
    public static final char DEFAULT = '=';
    public static final char CONSTRAINT = '<';

    public static final String[] ABSTRACT = new String[] {"abstract", "abs", "a"};
    public static final String[] ORDERABLE = new String[] {"orderable", "ord", "o"};
    public static final String[] MIXIN = new String[]{"mixin", "mix", "m"};
    public static final String[] VALIDATOR = new String[]{"validator", "val", "v"};

    public static final String[] PRIMARY = new String[]{"primary", "pri", "!"};
    public static final String[] AUTOCREATED = new String[]{"autocreated", "aut", "a"};
    public static final String[] MANDATORY = new String[]{"mandatory", "man", "m"};
    public static final String[] PROTECTED = new String[]{"protected", "pro", "p"};
    public static final String[] INTERNATIONALIZED = new String[]{"internationalized", "i15d", "i"};
    public static final String[] INDEXED = new String[]{"indexed", "ind", "x"};
    public static final String[] SCOREBOOST = new String[]{"scoreboost", "boost", "b"};
    public static final String[] NO = new String[]{"no","n"};
    public static final String[] YES = new String[]{"yes","y"};
    public static final String[] TOKENIZED = new String[]{"tokenized","tok","t"};
    public static final String[] UNTOKENIZED = new String[]{"untokenized","untok","u"};
    public static final String[] ANALYZER = new String[]{"analyzer"};
    public static final String[] SORTABLE = new String[]{"sortable"};
    public static final String[] FACETABLE = new String[]{"facetable"};
    public static final String[] FULLTEXTSEARCHABLE = new String[]{"fulltextsearchable", "fts"};
    public static final String[] MULTIPLE = new String[]{"multiple", "mul", "*"};
    public static final String[] HIDDEN = new String[]{"hidden", "Hidden", "HIDDEN"};

    public static final String[] COPY = new String[]{"copy", "Copy", "COPY"};
    public static final String[] VERSION = new String[]{"version", "Version", "VERSION"};
    public static final String[] INITIALIZE = new String[]{"initialize", "Initialize", "INITIALIZE"};
    public static final String[] COMPUTE = new String[]{"compute", "Compute", "COMPUTE"};
    public static final String[] IGNORE = new String[]{"ignore", "Ignore", "IGNORE"};
    public static final String[] ABORT = new String[]{"abort", "Abort", "ABORT"};

    public static final String[] WORKFLOW = new String[]{"workflow", "Workflow", "WORKFLOW"};

    public static final String[] ATTRIBUTE = ArrayUtils.join(PRIMARY,
            AUTOCREATED, MANDATORY, PROTECTED, INTERNATIONALIZED, INDEXED,
            SCOREBOOST, ANALYZER, SORTABLE, FACETABLE, FULLTEXTSEARCHABLE,
            MULTIPLE, COPY, VERSION, INITIALIZE, COMPUTE, IGNORE, ABORT,
            WORKFLOW, HIDDEN);

    public static final String[] STRING = {"string", "String", "STRING"};
    public static final String[] BINARY = {"binary", "Binary", "BINARY"};
    public static final String[] LONG = {"long", "Long", "LONG"};
    public static final String[] DOUBLE = {"double", "Double", "DOUBLE"};
    public static final String[] BOOLEAN = {"boolean", "Boolean", "BOOLEAN"};
    public static final String[] DATE = {"date", "Date", "DATE"};
    public static final String[] NAME = {"name", "Name", "NAME"};
    public static final String[] PATH = {"path", "Path", "PATH"};
    public static final String[] REFERENCE = {"reference", "Reference", "REFERENCE"};
    public static final String[] WEAKREFERENCE = {"WEAKREFERENCE", "WeakReference", "weakreference"};
    public static final String[] URI = {"URI", "Uri", "uri"};
    public static final String[] DECIMAL = {"DECIMAL", "Decimal", "decimal"};

    public static final String[] SMALLTEXT = {"text", "Text", "TEXT"};
    public static final String[] RICHTEXT = {"richtext", "RichText", "RICHTEXT"};
    public static final String[] CHOICELIST = {"choicelist", "Choicelist", "CHOICELIST"};
    public static final String[] DATETIMEPICKER = {"datetimepicker", "DateTimePicker", "DATETIMEPICKER"};
    public static final String[] DATEPICKER = {"datepicker", "DatePicker", "DATEPICKER"};
    public static final String[] CATEGORY = {"category", "Category", "CATEGORY"};
    public static final String[] FILEPICKER = {"file", "File", "FILE"};
    public static final String[] FILEUPLOAD = {"fileupload", "FileUpload", "FILEUPLOAD"};
    public static final String[] PORTLET = {"portlet", "Portlet", "PORTLET"};
    public static final String[] PORTLETDEFINITION = {"portletdefinition", "PortletDefinition", "PORTLETDEFINITION"};
    public static final String[] PAGE = {"page", "Page", "PAGE"};
    public static final String[] COLOR = {"color", "Color", "COLOR"};
    public static final String[] CHECKBOX = {"checkbox", "Checkbox", "CHECKBOX"};

    public static final String[] UNDEFINED = new String[]{"undefined", "Undefined", "UNDEFINED", "*"};

    public static final String[] JAHIA_CONTAINERLIST = {"containerList"};
    public static final String[] JAHIA_SINGLECONTAINER = {"singleContainer"};

    public static final String[] JAHIA_SMALLTEXTFIELD = {"smallText"};
    public static final String[] JAHIA_SHAREDSMALLTEXTFIELD = {"sharedSmallText"};
    public static final String[] JAHIA_BIGTEXTFIELD = {"bigText"};
    public static final String[] JAHIA_DATEFIELD = {"date"};
    public static final String[] JAHIA_PAGEFIELD = {"page"};
    public static final String[] JAHIA_FILEFIELD = {"file"};
    public static final String[] JAHIA_PORTLETFIELD = {"portlet"};
    public static final String[] JAHIA_INTEGERFIELD = {"integer"};
    public static final String[] JAHIA_FLOATFIELD = {"float"};
    public static final String[] JAHIA_BOOLEANFIELD = {"boolean"};
    public static final String[] JAHIA_CATEGORYFIELD = {"category"};
    public static final String[] JAHIA_COLORFIELD = {"color"};

    public static final String EOF = "eof";

    private final StreamTokenizer st;

    private final String filename;

    public LexerLegacy(Reader r, String filename) {
        this.filename = filename;
        st = new StreamTokenizer(r);

        st.eolIsSignificant(false);

        st.lowerCaseMode(false);

        st.slashSlashComments(true);
        st.slashStarComments(true);

        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars(':', ':');
        st.wordChars('_', '_');

        st.quoteChar(SINGLE_QUOTE);
        st.quoteChar(DOUBLE_QUOTE);

        st.ordinaryChar(BEGIN_NODE_TYPE_NAME);
        st.ordinaryChar(END_NODE_TYPE_NAME);
        st.ordinaryChar(EXTENDS);
        st.ordinaryChar(LIST_DELIMITER);
        st.ordinaryChar(PROPERTY_DEFINITION);
        st.ordinaryChar(CHILD_NODE_DEFINITION);
        st.ordinaryChar(BEGIN_TYPE);
        st.ordinaryChar(END_TYPE);
        st.ordinaryChar(DEFAULT);
        st.ordinaryChar(CONSTRAINT);
    }

    /**
     * getNextToken
     *
     * @return
     * @throws ParseException
     */
    public String getNextToken() throws ParseException {
        try {
            int tokenType = st.nextToken();
            if (tokenType == StreamTokenizer.TT_EOF) {
                return EOF;
            } else if (tokenType == StreamTokenizer.TT_WORD
                    || tokenType == SINGLE_QUOTE
                    || tokenType == DOUBLE_QUOTE) {
                return st.sval;
            } else if (tokenType == StreamTokenizer.TT_NUMBER) {
                return String.valueOf(st.nval);
            } else {
                return new String(new char[] {(char) tokenType});
            }
        } catch (IOException e) {
            fail("IOException while attempting to read input stream", e);
            return null;
        }
    }

    public void fail(String message) throws ParseException {
        throw new ParseException(message, st.lineno(), -1, filename);
    }

    public void fail(String message, Throwable e) throws ParseException {
        throw new ParseException(message, e, st.lineno(), -1, filename);
    }

    public void fail(Throwable e) throws ParseException {
        throw new ParseException(e, st.lineno(), -1, filename);
    }
}
