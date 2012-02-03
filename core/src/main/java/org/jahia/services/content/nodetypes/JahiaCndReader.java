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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.api.Constants;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.version.OnParentVersionAction;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * JahiaCndReader. Parses node type definitions written in the compact
 * node type definition format and returns a list of JahiaNodeTypeDef objects that
 * can then be used to register node types.
 * <p/>
 * The EBNF grammar of the compact node type definition:<br>
 * <pre>
 * cnd ::= ns_mapping* node_type_def+
 *
 * ns_mapping ::= "&lt;" prefix "=" namespace "&gt;"
 *
 * prefix ::= string
 *
 * namespace ::= string
 *
 * node_type_def ::= node_type_name [super_types] [options] {property_def | node_def}
 *
 * node_type_name ::= "[" string "]"
 *
 * super_types ::= "&gt;" string_list
 *
 * options ::= orderable_opt | mixin_opt | orderable_opt mixin_opt | mixin_opt orderable_opt
 *
 * orderable_opt ::= "orderable" | "ord" | "o"
 *
 * mixin_opt ::= "mixin" | "mix" | "m"
 *
 * property_def ::= "-" property_name [property_type_decl] [default_values] [attributes] [value_constraints]
 *
 * property_name ::= string
 *
 * property_type_decl ::= "(" property_type ")"
 *
 * property_type ::= "STRING" | "String |"string" |
 *                   "BINARY" | "Binary" | "binary" |
 *                   "LONG" | "Long" | "long" |
 *                   "DOUBLE" | "Double" | "double" |
 *                   "BOOLEAN" | "Boolean" | "boolean" |
 *                   "DATE" | "Date" | "date" |
 *                   "NAME | "Name | "name |
 *                   "PATH" | "Path" | "path" |
 *                   "REFERENCE" | "Reference" | "reference" |
 *                   "UNDEFINED" | "Undefined" | "undefined" | "*"
 *
 *
 * default_values ::= "=" string_list
 *
 * value_constraints ::= "&lt;" string_list
 *
 * node_def ::= "+" node_name [required_types] [default_type] [attributes]
 *
 * node_name ::= string
 *
 * required_types ::= "(" string_list ")"
 *
 * default_type ::= "=" string
 *
 * attributes ::= "primary" | "pri" | "!" |
 *                "autocreated" | "aut" | "a" |
 *                "mandatory" | "man" | "m" |
 *                "protected" | "pro" | "p" |
 *                "multiple" | "mul" | "*" |
 *                "COPY" | "Copy" | "copy" |
 *                "VERSION" | "Version" | "version" |
 *                "INITIALIZE" | "Initialize" | "initialize" |
 *                "COMPUTE" | "Compute" | "compute" |
 *                "IGNORE" | "Ignore" | "ignore" |
 *                "ABORT" | "Abort" | "abort"
 *
 * string_list ::= string {"," string}
 *
 * string ::= quoted_string | unquoted_string
 *
 * quoted_string :: = "'" unquoted_string "'"
 *
 * unquoted_string ::= [A-Za-z0-9:_]+
 * </pre>
 */
public class JahiaCndReader {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaCndReader.class);
    
    protected String systemId;
    protected String filename;

    /**
     * the registry where the types should be stored
     */
    protected NodeTypeRegistry registry;

    /**
     * the list of parsed nodetype defs
     */
    protected List<ExtendedNodeType> nodeTypesList = new LinkedList<ExtendedNodeType>();

    /**
     * the underlying lexer
     */
    protected Lexer lexer;

    /**
     * the current token
     */
    protected String currentToken;


    /**
     * Checks if the provided token is semantically equal to the given
     * argument.
     *
     * @param token the tokens to be compared
     * @param s the tokens to compare with
     * @return <code>true</code> if equals; <code>false</code> otherwise.
     */
    protected static boolean tokenEquals(String token, String[] s) {
        for (String e : s) {
            if (token.equals(e)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the parsed property type or <code>-1</code> if the type is not recognized.
     * 
     * @param token
     *            the token to parse property type from
     * @return the parsed property type or <code>-1</code> if the type is not recognized
     */
    public static int getPropertyType(String token) {
        if (tokenEquals(token, Lexer.STRING)) {
            return PropertyType.STRING;
        } else if (tokenEquals(token, Lexer.BINARY)) {
            return PropertyType.BINARY;
        } else if (tokenEquals(token, Lexer.LONG)) {
            return PropertyType.LONG;
        } else if (tokenEquals(token, Lexer.DOUBLE)) {
            return PropertyType.DOUBLE;
        } else if (tokenEquals(token, Lexer.BOOLEAN)) {
            return PropertyType.BOOLEAN;
        } else if (tokenEquals(token, Lexer.DATE)) {
            return PropertyType.DATE;
        } else if (tokenEquals(token, Lexer.NAME)) {
            return PropertyType.NAME;
        } else if (tokenEquals(token, Lexer.PATH)) {
            return PropertyType.PATH;
        } else if (tokenEquals(token, Lexer.REFERENCE)) {
            return PropertyType.REFERENCE;
        } else if (tokenEquals(token, Lexer.WEAKREFERENCE)) {
            return ExtendedPropertyType.WEAKREFERENCE;
        } else if (tokenEquals(token, Lexer.URI)) {
            return ExtendedPropertyType.URI;
        } else if (tokenEquals(token, Lexer.DECIMAL)) {
            return ExtendedPropertyType.DECIMAL;
        } else if (tokenEquals(token, Lexer.UNDEFINED)) {
            return PropertyType.UNDEFINED;
        } else {
            return -1;
        }
    }

    /**
     * Returns the parsed selector type or <code>-1</code> if the selector is not recognized.
     * 
     * @param token
     *            the token to parse property type from
     * @return the parsed selector type or <code>-1</code> if the selector is not recognized
     */
    public static int getSelectorType(String token) {
        if (tokenEquals(token, Lexer.SMALLTEXT)) {
            return SelectorType.SMALLTEXT;
        } else if (tokenEquals(token, Lexer.RICHTEXT)) {
            return SelectorType.RICHTEXT;
        } else if (tokenEquals(token, Lexer.TEXTAREA)) {
            return SelectorType.TEXTAREA;
        } else if (tokenEquals(token, Lexer.CHOICELIST)) {
            return SelectorType.CHOICELIST;
        } else if (tokenEquals(token, Lexer.CRON)) {
            return SelectorType.CRON;
        } else if (tokenEquals(token, Lexer.DATEPICKER)) {
            return SelectorType.DATEPICKER;
        } else if (tokenEquals(token, Lexer.DATETIMEPICKER)) {
            return SelectorType.DATETIMEPICKER;
        } else if (tokenEquals(token, Lexer.CATEGORY)) {
            return SelectorType.CATEGORY;
        } else if (tokenEquals(token, Lexer.CONTENTPICKER)) {
            return SelectorType.CONTENTPICKER;
        } else if (tokenEquals(token, Lexer.FILEUPLOAD)) {
            return SelectorType.FILEUPLOAD;
        } else if (tokenEquals(token, Lexer.COLOR)) {
            return SelectorType.COLOR;
        } else if (tokenEquals(token, Lexer.CHECKBOX)) {
            return SelectorType.CHECKBOX;
        } else {
            return -1;
        }
    }

    /**
     * Creates a new CND reader.
     *
     * @param r
     * @throws ParseException
     */
    public JahiaCndReader(Reader r, String filename, String systemId, NodeTypeRegistry registry)
            throws ParseException, IOException {
        this.systemId = systemId;
        this.registry = registry;
        this.filename = filename;
        lexer = new Lexer(r, filename);
    }

    /**
     * Returns the list of parsed nodetype definitions.
     *
     * @return a List of JahiaNodeTypeDef objects
     */
    public List<ExtendedNodeType> getNodeTypesList() {
        return nodeTypesList;
    }

    /**
     * Parses the definition
     *
     * @throws ParseException
     */
    public void parse() throws ParseException, IOException {
        nextToken();
        while (!currentTokenEquals(Lexer.EOF)) {
            if (!doNameSpace()) {
                break;
            }
        }
        while (!currentTokenEquals(Lexer.EOF)) {
            ExtendedNodeType ntd = new ExtendedNodeType(registry, systemId);
            try {
                doNodeTypeName(ntd);
                doSuperTypes(ntd);
                doOptions(ntd);
                doItemDefs(ntd);

                registry.addNodeType(ntd.getNameObject(),ntd);
                nodeTypesList.add(ntd);
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
                nextToken();    
                while (!currentTokenEquals(Lexer.BEGIN_NODE_TYPE_NAME) && !currentTokenEquals(Lexer.EOF)) {
                    nextToken();
                }
            }
        }
        for (ExtendedNodeType type : nodeTypesList) {
            try {
                type.validate();
                if (!type.getPrefix().equals("nt") && !type.isMixin() && !type.isNodeType(Constants.MIX_REFERENCEABLE)) {
                    int length = type.getDeclaredSupertypeNames().length;
                    String[] newTypes = new String[length + 1];
                    System.arraycopy(type.getDeclaredSupertypeNames(), 0, newTypes, 0, length);
                    newTypes[length] = Constants.MIX_REFERENCEABLE;
                    type.setDeclaredSupertypes(newTypes);
                    type.validate();
                }
            } catch (NoSuchNodeTypeException e) {
                throw new ParseException("Cannot validate supertypes for : "+type.getName(),e,0,0,filename);
            }
        }

    }
    
    /**
     * processes the namespace declaration
     *
     * @return
     * @throws ParseException
     */
    private boolean doNameSpace() throws ParseException, IOException {
        if (!currentTokenEquals('<')) {
            return false;
        }
        nextToken();
        String prefix = currentToken;

        nextToken();
        if (!currentTokenEquals('=')) {
            lexer.fail("Missing = in namespace decl.");
        }

        nextToken();
        String uri = currentToken;
        nextToken();
        if (!currentTokenEquals('>')) {
            lexer.fail("Missing > in namespace decl.");
        }

        registry.getNamespaces().put(prefix, uri);

        nextToken();
        return true;
    }

    /**
     * processes the nodetype name
     *
     * @param ntd
     * @throws ParseException
     */
    private void doNodeTypeName(ExtendedNodeType ntd) throws ParseException,IOException {
        if (!currentTokenEquals(Lexer.BEGIN_NODE_TYPE_NAME)) {
            lexer.fail("Unexpected token '" + currentToken +"'");
        }
        nextToken();
        Name name = parseName(currentToken);
        ntd.setName(name);
        nextToken();
        if (!currentTokenEquals(Lexer.END_NODE_TYPE_NAME)) {
            lexer.fail("Missing '" + Lexer.END_NODE_TYPE_NAME + "' delimiter for end of node type name, found " + currentToken);
        }
        nextToken();

    }

    /**
     * processes the superclasses
     *
     * @param ntd
     * @throws ParseException
     */
    private void doSuperTypes(ExtendedNodeType ntd) throws ParseException, IOException {
        // a set would be nicer here, in case someone defines a supertype twice.
        // but due to issue [JCR-333], the resulting node type definition is
        // not symmetric anymore and the tests will fail.
        List<String> supertypes = new ArrayList<String>();
        if (!currentTokenEquals(Lexer.EXTENDS)) {
            return;
        }
        do {
            nextToken();
            parseName(currentToken); // check name validity
            supertypes.add(currentToken);
            nextToken();
        } while (currentTokenEquals(Lexer.LIST_DELIMITER));
        ntd.setDeclaredSupertypes(supertypes.toArray(new String[supertypes.size()]));
    }

    /**
     * processes the options
     *
     * @param ntd
     * @throws ParseException
     */
    private void doOptions(ExtendedNodeType ntd) throws ParseException, IOException {
        boolean hasOption = true;
        while (hasOption) {
            if (currentTokenEquals(Lexer.ORDERABLE)) {
                nextToken();
                ntd.setHasOrderableChildNodes(true);
            } else if (currentTokenEquals(Lexer.MIXIN)) {
                nextToken();
                ntd.setMixin(true);
            } else if (currentTokenEquals(Lexer.ABSTRACT)) {
                nextToken();
                ntd.setAbstract(true);
            } else if (currentTokenEquals(Lexer.NOQUERY)) {
                nextToken();
                ntd.setQueryable(false);
            } else if (currentTokenEquals(Lexer.QUERY)) {
                nextToken();
                ntd.setQueryable(true);
            } else if (currentTokenEquals(Lexer.PRIMARYITEM)) {
                nextToken();
                ntd.setPrimaryItemName(currentToken);
                nextToken();
            } else if (currentTokenEquals(Lexer.VALIDATOR)) {
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    nextToken();
                    ntd.setValidator(currentToken);
                    nextToken();
                } else {
                    lexer.fail("Invalid validator");
                }
            } else if (currentTokenEquals(Lexer.ITEMTYPE)) {
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    nextToken();
                    ntd.setItemsType(currentToken);
                    nextToken();
                } else {
                    lexer.fail("Invalid validator");
                }
            } else if (currentTokenEquals(Lexer.MIXIN_EXTENDS)) {
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    do {
                        nextToken();
                        ntd.addMixinExtend(currentToken);
                        nextToken();
                    } while (currentTokenEquals(Lexer.LIST_DELIMITER));
                } else {
                    lexer.fail("Invalid validator");
                }
//            } else if (currentTokenEquals(Lexer.LIVECONTENT)) {
//                nextToken();
//                ntd.setLiveContent(true);
            } else {
                hasOption = false;
            }

            // todo handle new options
        }
    }

    /**
     * processes the item definitions
     *
     * @param ntd
     * @throws ParseException, IOException
     */
    private void doItemDefs(ExtendedNodeType ntd) throws ParseException, IOException {
        while (true) {
            if (currentTokenEquals(Lexer.PROPERTY_DEFINITION)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(Lexer.CHILD_NODE_DEFINITION)) {
                ExtendedNodeDefinition ndi = new ExtendedNodeDefinition(registry);
                nextToken();
                doChildNodeDefinition(ndi, ntd);
                ndi.setDeclaringNodeType(ntd);
            } else {
                return;
            }
        }
    }

    /**
     * processes the property definition
     *
     * @param pdi
     * @param ntd
     * @throws ParseException
     */
    private void doPropertyDefinition(ExtendedPropertyDefinition pdi, ExtendedNodeType ntd)
            throws ParseException, IOException {
        Name name = parseName(currentToken);
        pdi.setName(name);
        nextToken();
        doPropertyType(pdi);
        doPropertyDefaultValue(pdi);
        doPropertyAttributes(pdi, ntd);
        doPropertyValueConstraints(pdi);
    }

    /**
     * processes the property type
     *
     * @param pdi
     * @throws ParseException
     */
    private void doPropertyType(ExtendedPropertyDefinition pdi) throws ParseException, IOException {
        if (!currentTokenEquals(Lexer.BEGIN_TYPE)) {
            return;
        }
        nextToken();
        if (pdi.getRequiredType() == 0) {
            int propType = getPropertyType(currentToken);
            if (propType >= 0) {
                pdi.setRequiredType(propType);
            } else {
                lexer.fail("Unknown type '" + currentToken + "' specified");
            }
            nextToken();
            if (currentTokenEquals(Lexer.END_TYPE)) {
                nextToken();
            } else if (currentTokenEquals(Lexer.LIST_DELIMITER)) {
                nextToken();
                doPropertySelector(pdi);
            } else {
                lexer.fail("Missing '" + Lexer.END_TYPE + "' delimiter for end of property type");
            }
        } else {
            doPropertySelector(pdi);
        }
    }

    private void doPropertySelector(ExtendedPropertyDefinition pdi) throws ParseException, IOException {
        int selector = getSelectorType(currentToken);
        if (selector >= 0) {
            pdi.setSelector(selector);
        } else {
            lexer.fail("Unknown type '" + currentToken + "' specified");
        }
        nextToken();
        if (currentTokenEquals(Lexer.BEGIN_NODE_TYPE_NAME)) {
            doSelectorOptions(pdi);
        }
        if (currentTokenEquals(Lexer.END_TYPE)) {
            nextToken();
        } else{
            lexer.fail("Missing '" + Lexer.END_TYPE + "' delimiter for end of property type");
        }
    }

    /**
     * processes the property attributes
     *
     * @param pdi
     * @param ntd
     * @throws ParseException
     */
    private void doPropertyAttributes(ExtendedPropertyDefinition pdi, ExtendedNodeType ntd) throws ParseException, IOException {
        while (currentTokenEquals(Lexer.PROP_ATTRIBUTE)) {
            if (currentTokenEquals(Lexer.PRIMARY)) {
                ntd.setPrimaryItemName(pdi.getName());
            } else if (currentTokenEquals(Lexer.AUTOCREATED)) {
                pdi.setAutoCreated(true);
            } else if (currentTokenEquals(Lexer.MANDATORY)) {
                pdi.setMandatory(true);
            } else if (currentTokenEquals(Lexer.PROTECTED)) {
                pdi.setProtected(true);
            } else if (currentTokenEquals(Lexer.MULTIPLE)) {
                pdi.setMultiple(true);
            } else if (currentTokenEquals(Lexer.HIDDEN)) {
                pdi.setHidden(true);
            } else if (currentTokenEquals(Lexer.INTERNATIONALIZED)) {
                pdi.setInternationalized(true);
            } else if (currentTokenEquals(Lexer.ITEMTYPE)) {
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    nextToken();
                    pdi.setItemType(currentToken);
                } else {
                    lexer.fail("Invalid value for indexed " + currentToken);
                }
            } else if (currentTokenEquals(Lexer.INDEXED)) {
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    nextToken();
                    if (currentTokenEquals(Lexer.NO)) {
                        pdi.setIndex(ExtendedPropertyDefinition.INDEXED_NO);
                    } else if (currentTokenEquals(Lexer.TOKENIZED)) {
                        pdi.setIndex(ExtendedPropertyDefinition.INDEXED_TOKENIZED);
                    } else if (currentTokenEquals(Lexer.UNTOKENIZED)) {
                        pdi.setIndex(ExtendedPropertyDefinition.INDEXED_UNTOKENIZED);
                    } else {
                        lexer.fail("Invalid value for indexed [ no | tokenized | untokenized ] "+currentToken);
                    }
                } else {
                    lexer.fail("Invalid value for indexed " + currentToken);
                }
            } else if (currentTokenEquals(Lexer.SCOREBOOST)) {
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    nextToken();
                    try {
                        pdi.setScoreboost(Double.parseDouble(currentToken));
                    } catch (NumberFormatException e) {
                        lexer.fail("Invalid value for score boost "+currentToken);
                    }
                } else {
                    lexer.fail("Invalid value for score boost " + currentToken);
                }
            } else if (currentTokenEquals(Lexer.ANALYZER)) {
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    nextToken();
                    pdi.setAnalyzer(currentToken);
                } else {
                    lexer.fail("Invalid value for tokenizer " + currentToken);
                }
                
            } else if (currentTokenEquals(Lexer.SORTABLE)) {
                // deprecated , use NOQUERYORDER
                pdi.setQueryOrderable(true);
            } else if (currentTokenEquals(Lexer.FACETABLE)) {
                pdi.setFacetable(true);
            } else if (currentTokenEquals(Lexer.HIERARCHICAL)) {
                pdi.setHierarchical(true);
            } else if (currentTokenEquals(Lexer.FULLTEXTSEARCHABLE)) {
                // deprecated , use NOFULLTEXT
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    nextToken();
                    if (currentTokenEquals(Lexer.NO)) {
                        pdi.setFullTextSearchable(Boolean.FALSE);
                    } else if (currentTokenEquals(Lexer.YES)) {
                        pdi.setFullTextSearchable(Boolean.TRUE);
                    }
                }
            } else if (currentTokenEquals(Lexer.ONCONFLICT)) {
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    nextToken();
                    if (currentTokenEquals(Lexer.USE_LATEST)) {
                        pdi.setOnConflict(OnConflictAction.USE_LATEST);
                    } else if (currentTokenEquals(Lexer.USE_OLDEST)) {
                        pdi.setOnConflict(OnConflictAction.USE_OLDEST);
                    } else if (currentTokenEquals(Lexer.NUMERIC_USE_MIN)) {
                        pdi.setOnConflict(OnConflictAction.NUMERIC_USE_MIN);
                    } else if (currentTokenEquals(Lexer.NUMERIC_USE_MAX)) {
                        pdi.setOnConflict(OnConflictAction.NUMERIC_USE_MAX);
                    } else if (currentTokenEquals(Lexer.NUMERIC_SUM)) {
                        pdi.setOnConflict(OnConflictAction.NUMERIC_SUM);
                    }
                }
            } else if (currentTokenEquals(Lexer.COPY)) {
                pdi.setOnParentVersion(OnParentVersionAction.COPY);
            } else if (currentTokenEquals(Lexer.VERSION)) {
                pdi.setOnParentVersion(OnParentVersionAction.VERSION);
            } else if (currentTokenEquals(Lexer.INITIALIZE)) {
                pdi.setOnParentVersion(OnParentVersionAction.INITIALIZE);
            } else if (currentTokenEquals(Lexer.COMPUTE)) {
                pdi.setOnParentVersion(OnParentVersionAction.COMPUTE);
            } else if (currentTokenEquals(Lexer.IGNORE)) {
                pdi.setOnParentVersion(OnParentVersionAction.IGNORE);
            } else if (currentTokenEquals(Lexer.ABORT)) {
                pdi.setOnParentVersion(OnParentVersionAction.ABORT);
            } else if (currentTokenEquals(Lexer.NOFULLTEXT)) {
                pdi.setFullTextSearchable(false);
            } else if (currentTokenEquals(Lexer.NOQUERYORDER)) {
                pdi.setQueryOrderable(false);
            } else if (currentTokenEquals(Lexer.QUERYOPS)) {
                doPropertyQueryOperators(pdi);
            }


            nextToken();
        }
    }

    /**
     * processes the property query operators
     *
     * @param pd the property definition builder
     * @throws ParseException if an error occurs
     */
    private void doPropertyQueryOperators(ExtendedPropertyDefinition pd)
            throws ParseException {
        if (!currentTokenEquals(Lexer.QUERYOPS)) {
            return;
        }
        nextToken();

        String[] ops = currentToken.split(",");
        List<String> queryOps = new LinkedList<String>();
        for (String op : ops) {
            String s = op.trim();
            if (s.equals(Lexer.QUEROPS_EQUAL)) {
                queryOps.add(QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO);
            } else if (s.equals(Lexer.QUEROPS_NOTEQUAL)) {
                queryOps.add(QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO);
            } else if (s.equals(Lexer.QUEROPS_LESSTHAN)) {
                queryOps.add(QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN);
            } else if (s.equals(Lexer.QUEROPS_LESSTHANOREQUAL)) {
                queryOps.add(QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO);
            } else if (s.equals(Lexer.QUEROPS_GREATERTHAN)) {
                queryOps.add(QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN);
            } else if (s.equals(Lexer.QUEROPS_GREATERTHANOREQUAL)) {
                queryOps.add(QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO);
            } else if (s.equals(Lexer.QUEROPS_LIKE)) {
                queryOps.add(QueryObjectModelConstants.JCR_OPERATOR_LIKE);
            } else {
                lexer.fail("'" + s + "' is not a valid query operator");
            }
        }
        pd.setAvailableQueryOperators(queryOps.toArray(new String[queryOps.size()]));
    }

    /**
     * processes the property default values
     *
     * @param pdi
     * @throws ParseException
     */
    private void doPropertyDefaultValue(ExtendedPropertyDefinition pdi) throws ParseException, IOException {
        if (!currentTokenEquals(Lexer.DEFAULT)) {
            return;
        }
        List<Value> defaultValues = doValuesList(pdi, false);
        pdi.setDefaultValues(defaultValues.toArray(new Value[defaultValues.size()]));
    }

    /**
     * processes the property value constraints
     *
     * @param pdi
     * @throws ParseException
     */
    private void doPropertyValueConstraints(ExtendedPropertyDefinition pdi) throws ParseException, IOException {
        if (!currentTokenEquals(Lexer.CONSTRAINT)) {
            return;
        }
        List<Value> constraints = doValuesList(pdi, true);
        pdi.setValueConstraints(constraints.toArray(new Value[constraints.size()]));
    }

    private List<Value> doValuesList(ExtendedPropertyDefinition pdi, boolean isConstraint) throws ParseException {
        List<Value> values = new ArrayList<Value>();
        do {
            nextToken();
            String v = currentToken;
            nextToken();
            if (currentTokenEquals(Lexer.BEGIN_TYPE)) {
                nextToken();
                List<String> params = new ArrayList<String>();
                while (!currentTokenEquals(Lexer.END_TYPE)) {
                    params.add(currentToken);
                    nextToken();
                }
                nextToken();

                values.add(new DynamicValueImpl(v, params, pdi.getRequiredType(), isConstraint, pdi));
            } else {
                values.add(new ValueImpl(v, pdi.getRequiredType(), isConstraint));
            }
        } while (currentTokenEquals(Lexer.LIST_DELIMITER));
        return values;
    }

    /**
     * processes the childnode definition
     *
     * @param ndi
     * @param ntd
     * @throws ParseException
     */
    private void doChildNodeDefinition(ExtendedNodeDefinition ndi, ExtendedNodeType ntd)
            throws ParseException, IOException {
        Name name = parseName(currentToken);
        ndi.setName(name);
        nextToken();
        doChildNodeRequiredTypes(ndi);
        doChildNodeDefaultType(ndi);
        doChildNodeAttributes(ndi, ntd);
    }

    /**
     * processes the childnode required types
     *
     * @param ndi
     * @throws ParseException
     */
    private void doChildNodeRequiredTypes(ExtendedNodeDefinition ndi) throws ParseException, IOException {
        if (!currentTokenEquals(Lexer.BEGIN_TYPE)) {
            return;
        }
        if (ndi.getRequiredPrimaryTypeNames() == null) {
            List<String> types = new ArrayList<String>();
            do {
                nextToken();
                types.add(currentToken);
                nextToken();
            } while (currentTokenEquals(Lexer.LIST_DELIMITER));

            if (currentTokenEquals(Lexer.BEGIN_NODE_TYPE_NAME)) {
                doSelectorOptions(ndi);
            }
            if (currentTokenEquals(Lexer.END_TYPE)) {
                nextToken();
            } else{
                lexer.fail("Missing '" + Lexer.END_TYPE + "' delimiter for end of child node type");
            }
            ndi.setRequiredPrimaryTypes(types.toArray(new String[types.size()]));
        } else {
            nextToken();
            doChildNodeSelector(ndi);
        }
    }

    private void doChildNodeSelector(ExtendedNodeDefinition ndi) throws ParseException, IOException {
        if (currentTokenEquals(Lexer.PAGE)) {
            ndi.setSelector(SelectorType.PAGE);
        } else {
            lexer.fail("Unknown type '" + currentToken + "' specified");
        }
        nextToken();
        if (currentTokenEquals(Lexer.BEGIN_NODE_TYPE_NAME)) {
            doSelectorOptions(ndi);
        }
        if (currentTokenEquals(Lexer.END_TYPE)) {
            nextToken();
        } else{
            lexer.fail("Missing '" + Lexer.END_TYPE + "' delimiter for end of property type");
        }
    }

    /**
     * processes the childnode default types
     *
     * @param ndi
     * @throws ParseException
     */
    private void doChildNodeDefaultType(ExtendedNodeDefinition ndi) throws ParseException, IOException {
        if (!currentTokenEquals(Lexer.DEFAULT)) {
            return;
        }
        nextToken();

        ndi.setDefaultPrimaryType(currentToken);
        nextToken();
    }

    /**
     * processes the childnode attributes
     *
     * @param ndi
     * @param ntd
     * @throws ParseException
     */
    private void doChildNodeAttributes(ExtendedNodeDefinition ndi, ExtendedNodeType ntd) throws ParseException, IOException {
        while (currentTokenEquals(Lexer.NODE_ATTRIBUTE)) {
            if (currentTokenEquals(Lexer.PRIMARY)) {
                ntd.setPrimaryItemName(ndi.getName());
            } else if (currentTokenEquals(Lexer.AUTOCREATED)) {
                ndi.setAutoCreated(true);
            } else if (currentTokenEquals(Lexer.MANDATORY)) {
                ndi.setMandatory(true);
            } else if (currentTokenEquals(Lexer.HIDDEN)) {
                ndi.setHidden(true);
            } else if (currentTokenEquals(Lexer.PROTECTED)) {
                ndi.setProtected(true);
            } else if (currentTokenEquals(Lexer.MULTIPLE) /*|| currentTokenEquals(Lexer.SNS)*/) {
                ndi.setAllowsSameNameSiblings(true);                
            } else if (currentTokenEquals(Lexer.COPY)) {
                ndi.setOnParentVersion(OnParentVersionAction.COPY);
            } else if (currentTokenEquals(Lexer.VERSION)) {
                ndi.setOnParentVersion(OnParentVersionAction.VERSION);
            } else if (currentTokenEquals(Lexer.INITIALIZE)) {
                ndi.setOnParentVersion(OnParentVersionAction.INITIALIZE);
            } else if (currentTokenEquals(Lexer.COMPUTE)) {
                ndi.setOnParentVersion(OnParentVersionAction.COMPUTE);
            } else if (currentTokenEquals(Lexer.IGNORE)) {
                ndi.setOnParentVersion(OnParentVersionAction.IGNORE);
            } else if (currentTokenEquals(Lexer.ABORT)) {
                ndi.setOnParentVersion(OnParentVersionAction.ABORT);
            } else if (currentTokenEquals(Lexer.ITEMTYPE)) {
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    nextToken();
                    ndi.setItemType(currentToken);
                } else {
                    lexer.fail("Invalid value for indexed " + currentToken);
                }
            } else if (currentTokenEquals(Lexer.WORKFLOW)) {
                nextToken();
                if (currentTokenEquals(Lexer.DEFAULT)) {
                    nextToken();
                    ndi.setWorkflow(currentToken);
                } else {
                    lexer.fail("Invalid value for workflow " + currentToken);
                }
//            } else if (currentTokenEquals(Lexer.LIVECONTENT)) {
//                ndi.setLiveContent(true);
            }

            //todo : handle new attributes

            nextToken();
        }
    }

    private void doSelectorOptions(ExtendedItemDefinition pdi) throws ParseException, IOException {
        nextToken();
        Map<String,String> options = new LinkedHashMap<String,String>();
        while (true) {
            String key = currentToken;
            String value = "";
            nextToken();
            if (currentTokenEquals(Lexer.DEFAULT)) {
                nextToken();
                value = currentToken;
                nextToken();
            }
            if (key.equals("addListMixin") || key.equals("addMixin") || key.equals("availableTypes")) {
                for (String s : value.split(",")) {
                    try {
                        registry.getNodeType(s);
                    } catch (NoSuchNodeTypeException e) {
                        lexer.fail("Cannot find type : "+s);
                    }
                }
            }
            options.put(key, value);
            if (currentTokenEquals(Lexer.END_NODE_TYPE_NAME)) {
                nextToken();
                break;
            }
            if (!currentTokenEquals(Lexer.LIST_DELIMITER)) {
                lexer.fail("Missing '" + Lexer.END_NODE_TYPE_NAME + "' delimiter");
            }
            nextToken();
        }
        pdi.setSelectorOptions(options);
    }

    /**
     * Gets the next token from the underlying lexer.
     *
     * @see Lexer#getNextToken()
     * @throws ParseException if the lexer fails to get the next token.
     */
    protected void nextToken() throws ParseException {
        currentToken = lexer.getNextToken();
    }

    /**
     * Checks if the {@link #currentToken} is semantically equal to the given
     * argument.
     *
     * @param s the tokens to compare with
     * @return <code>true</code> if equals; <code>false</code> otherwise.
     */
    protected boolean currentTokenEquals(String[] s) {
        return tokenEquals(currentToken, s);
    }

    /**
     * Checks if the {@link #currentToken} is semantically equal to the given
     * argument.
     *
     * @param c the tokens to compare with
     * @return <code>true</code> if equals; <code>false</code> otherwise.
     */
    protected boolean currentTokenEquals(char c) {
        return currentToken.length() == 1 && currentToken.charAt(0) == c;
    }

    /**
     * Checks if the {@link #currentToken} is semantically equal to the given
     * argument.
     *
     * @param s the tokens to compare with
     * @return <code>true</code> if equals; <code>false</code> otherwise.
     */
    protected boolean currentTokenEquals(String s) {
        return currentToken.equals(s);
    }

    protected Name parseName(String name) throws ParseException {
        Name res = new Name(name, registry.getNamespaces());
        if (!StringUtils.isEmpty(res.getPrefix()) && res.getUri() == null) {
            lexer.fail("Cannot parse name");
        }
        return res;
    }
}
