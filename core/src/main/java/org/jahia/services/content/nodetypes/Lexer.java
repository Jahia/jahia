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

import javax.jcr.query.qom.QueryObjectModelConstants;
import java.io.StreamTokenizer;
import java.io.Reader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Lexer
 */
public class Lexer {
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

    public static final String[] ORDERABLE = new String[] {"orderable", "ord", "o"};
    public static final String[] MIXIN = new String[]{"mixin", "mix", "m"};
    public static final String[] ABSTRACT = new String[]{"abstract", "abs", "a"};
    public static final String[] NOQUERY = new String[]{"noquery", "nq"};
    public static final String[] QUERY = new String[]{"query", "q"};
    public static final String[] PRIMARYITEM = new String[]{"primaryitem", "!"};
    public static final String[] MIXIN_EXTENDS = new String[]{"extends"};

    public static final String[] VALIDATOR = new String[]{"validator", "val", "v"};

    public static final String[] PRIMARY = new String[]{"primary", "pri", "!"};
    public static final String[] AUTOCREATED = new String[]{"autocreated", "aut", "a"};
    public static final String[] MANDATORY = new String[]{"mandatory", "man", "m"};
    public static final String[] PROTECTED = new String[]{"protected", "pro", "p"};
    public static final String[] MULTIPLE = new String[]{"multiple", "mul", "*"};
    public static final String[] SNS = new String[]{"sns", "*", "multiple"};
    public static final String[] INTERNATIONALIZED = new String[]{"internationalized", "i15d", "i18n", "i"};

    public static final String[] ITEMTYPE = new String[]{"itemtype", "type"};
    public static final String[] INDEXED = new String[]{"indexed", "ind", "x"};
    public static final String[] SCOREBOOST = new String[]{"scoreboost", "boost", "b"};
    public static final String[] NO = new String[]{"no","n"};
    public static final String[] YES = new String[]{"yes","y"};
    public static final String[] TOKENIZED = new String[]{"tokenized","tok","t"};
    public static final String[] UNTOKENIZED = new String[]{"untokenized","untok","u"};
    public static final String[] ANALYZER = new String[]{"analyzer"};
    public static final String[] FACETABLE = new String[]{"facetable"};
    public static final String[] HIERARCHICAL = new String[]{"hierarchical"};

    public static final String[] SORTABLE = new String[]{"sortable"};
    public static final String[] FULLTEXTSEARCHABLE = new String[]{"fulltextsearchable", "fts"};

    public static final String[] QUERYOPS = new String[]{"queryops", "qop"};
    public static final String[] NOFULLTEXT = new String[]{"nofulltext", "nof"};
    public static final String[] NOQUERYORDER = new String[]{"noqueryorder", "nqord"};

    public static final String[] HIDDEN = new String[]{"hidden", "Hidden", "HIDDEN"};

    public static final String[] ONCONFLICT = new String[]{"onconflict"};
    public static final String[] USE_LATEST = new String[]{"latest"};
    public static final String[] USE_OLDEST = new String[]{"oldest"};
    public static final String[] NUMERIC_USE_MIN = new String[]{"min"};
    public static final String[] NUMERIC_USE_MAX = new String[]{"max"};
    public static final String[] NUMERIC_SUM = new String[]{"sum"};

    public static final String[] COPY = new String[]{"copy", "Copy", "COPY"};
    public static final String[] VERSION = new String[]{"version", "Version", "VERSION"};
    public static final String[] INITIALIZE = new String[]{"initialize", "Initialize", "INITIALIZE"};
    public static final String[] COMPUTE = new String[]{"compute", "Compute", "COMPUTE"};
    public static final String[] IGNORE = new String[]{"ignore", "Ignore", "IGNORE"};
    public static final String[] ABORT = new String[]{"abort", "Abort", "ABORT"};

//    public static final String[] LIVECONTENT = new String[]{"livecontent", "LiveContent", "LIVECONTENT"};

    public static final String[] WORKFLOW = new String[]{"workflow", "Workflow", "WORKFLOW"};

    public static final String[] PROP_ATTRIBUTE;
    public static final String[] NODE_ATTRIBUTE;

    public static final String[] ALL_OPERATORS = new String[]{
            QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
            QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO,
            QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN,
            QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO,
            QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN,
            QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO,
            QueryObjectModelConstants.JCR_OPERATOR_LIKE
    };

    static {
        ArrayList<String> attr = new ArrayList<String>();
        attr.addAll(Arrays.asList(PRIMARY));
        attr.addAll(Arrays.asList(AUTOCREATED));
        attr.addAll(Arrays.asList(MANDATORY));
        attr.addAll(Arrays.asList(PROTECTED));
        attr.addAll(Arrays.asList(INTERNATIONALIZED));
        attr.addAll(Arrays.asList(INDEXED));
        attr.addAll(Arrays.asList(SCOREBOOST));
        attr.addAll(Arrays.asList(ANALYZER));
        attr.addAll(Arrays.asList(SORTABLE));
        attr.addAll(Arrays.asList(FACETABLE));
        attr.addAll(Arrays.asList(HIERARCHICAL));
        attr.addAll(Arrays.asList(FULLTEXTSEARCHABLE));
        attr.addAll(Arrays.asList(MULTIPLE));
        attr.addAll(Arrays.asList(QUERYOPS));
        attr.addAll(Arrays.asList(NOFULLTEXT));
        attr.addAll(Arrays.asList(NOQUERYORDER));
        attr.addAll(Arrays.asList(ONCONFLICT));
        attr.addAll(Arrays.asList(COPY));
        attr.addAll(Arrays.asList(VERSION));
        attr.addAll(Arrays.asList(INITIALIZE));
        attr.addAll(Arrays.asList(COMPUTE));
        attr.addAll(Arrays.asList(IGNORE));
        attr.addAll(Arrays.asList(ABORT));
        attr.addAll(Arrays.asList(HIDDEN));
        attr.addAll(Arrays.asList(ITEMTYPE));
        PROP_ATTRIBUTE = attr.toArray(new String[attr.size()]);
        attr = new ArrayList<String>();
        attr.addAll(Arrays.asList(PRIMARY));
        attr.addAll(Arrays.asList(AUTOCREATED));
        attr.addAll(Arrays.asList(MANDATORY));
        attr.addAll(Arrays.asList(PROTECTED));
        attr.addAll(Arrays.asList(INTERNATIONALIZED));
        attr.addAll(Arrays.asList(SNS));
        attr.addAll(Arrays.asList(COPY));
        attr.addAll(Arrays.asList(VERSION));
        attr.addAll(Arrays.asList(INITIALIZE));
        attr.addAll(Arrays.asList(COMPUTE));
        attr.addAll(Arrays.asList(IGNORE));
        attr.addAll(Arrays.asList(ABORT));
        attr.addAll(Arrays.asList(WORKFLOW));
        attr.addAll(Arrays.asList(HIDDEN));
        attr.addAll(Arrays.asList(ITEMTYPE));
//        attr.addAll(Arrays.asList(LIVECONTENT));
        NODE_ATTRIBUTE = attr.toArray(new String[attr.size()]);
    }

    public static final String QUEROPS_EQUAL = "=";
    public static final String QUEROPS_NOTEQUAL = "<>";
    public static final String QUEROPS_LESSTHAN = "<";
    public static final String QUEROPS_LESSTHANOREQUAL = "<=";
    public static final String QUEROPS_GREATERTHAN = ">";
    public static final String QUEROPS_GREATERTHANOREQUAL = ">=";
    public static final String QUEROPS_LIKE = "LIKE";

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
    public static final String[] TEXTAREA = {"textarea", "TextArea", "TEXTAREA"};
    public static final String[] CHOICELIST = {"choicelist", "Choicelist", "CHOICELIST"};
    public static final String[] DATETIMEPICKER = {"datetimepicker", "DateTimePicker", "DATETIMEPICKER"};
    public static final String[] DATEPICKER = {"datepicker", "DatePicker", "DATEPICKER"};
    public static final String[] CATEGORY = {"category", "Category", "CATEGORY"};
    public static final String[] CONTENTPICKER = {"picker", "Picker", "PICKER"};
    public static final String[] FILEUPLOAD = {"fileupload", "FileUpload", "FILEUPLOAD"};
    public static final String[] PAGE = {"page", "Page", "PAGE"};
    public static final String[] COLOR = {"color", "Color", "COLOR"};
    public static final String[] CHECKBOX = {"checkbox", "Checkbox", "CHECKBOX"};
    public static final String[] CRON = {"cron", "Cron", "CRON"};

    public static final String[] UNDEFINED = new String[]{"undefined", "Undefined", "UNDEFINED", "*"};

    public static final String EOF = "eof";

    private final StreamTokenizer st;

    private final String filename;

    /**
     * Constructor
     * @param r
     */
    public Lexer(Reader r, String filename) {
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

    /**
     * Creates a failure exception including the current line number and systemid.
     * @param message message
     * @throws ParseException the created exception
     */
    public void fail(String message) throws ParseException {
        throw new ParseException(message, st.lineno(), -1, filename);
    }

    /**
     * Creates a failure exception including the current line number and systemid.
     * @param message message
     * @param e root cause
     * @throws ParseException the created exception
     */
    public void fail(String message, Throwable e) throws ParseException {
        throw new ParseException(message, e, st.lineno(), -1, filename);
    }

     /**
     * Creates a failure exception including the current line number and systemid.
     * @param e root cause
     * @throws ParseException the created exception
     */
     public void fail(Throwable e) throws ParseException {
        throw new ParseException(e, st.lineno(), -1, filename);
    }
}
