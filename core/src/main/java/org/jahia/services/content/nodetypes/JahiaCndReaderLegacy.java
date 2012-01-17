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
import org.jahia.services.content.JCRContentUtils;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.OnParentVersionAction;
import java.io.*;
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
public class JahiaCndReaderLegacy {
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
    protected List<ExtendedNodeType> nodeTypesList = new LinkedList();

    /**
     * the underlying LexerLegacy
     */
    protected LexerLegacy lexer;

    /**
     * the current token
     */
    protected String currentToken;

    /**
     * Creates a new CND reader.
     *
     * @param r
     * @throws ParseException
     */
    public JahiaCndReaderLegacy(Reader r, String filename, String systemId, NodeTypeRegistry registry)
            throws ParseException, IOException {
        this.systemId = systemId;
        this.registry = registry;
        this.filename = filename;
        lexer = new LexerLegacy(r, filename);
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
        while (!currentTokenEquals(LexerLegacy.EOF)) {
            if (!doNameSpace()) {
                break;
            }
        }
        while (!currentTokenEquals(LexerLegacy.EOF)) {
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
                while (!currentTokenEquals(LexerLegacy.BEGIN_NODE_TYPE_NAME) && !currentTokenEquals(LexerLegacy.EOF)) {
                    nextToken();
                }
            }
        }
        for (ExtendedNodeType type : nodeTypesList) {
            try {
                type.validate();
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
        if (!currentTokenEquals(LexerLegacy.BEGIN_NODE_TYPE_NAME)) {
            lexer.fail("Unexpected token '" + currentToken +"'");
        }
        nextToken();
        Name name = parseName(currentToken);
        ntd.setName(name);
        nextToken();
        if (!currentTokenEquals(LexerLegacy.END_NODE_TYPE_NAME)) {
            lexer.fail("Missing '" + LexerLegacy.END_NODE_TYPE_NAME + "' delimiter for end of node type name, found " + currentToken);
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
        if (!currentTokenEquals(LexerLegacy.EXTENDS)) {
            return;
        }
        do {
            nextToken();
            supertypes.add(currentToken);
            nextToken();
        } while (currentTokenEquals(LexerLegacy.LIST_DELIMITER));
        ntd.setDeclaredSupertypes(supertypes.toArray(new String[supertypes.size()]));
    }

    /**
     * processes the options
     *
     * @param ntd
     * @throws ParseException
     */
    private void doOptions(ExtendedNodeType ntd) throws ParseException, IOException {
        if (currentTokenEquals(LexerLegacy.ABSTRACT)) {
            ntd.setAbstract(true);
            nextToken();
        }

        if (currentTokenEquals(LexerLegacy.ORDERABLE)) {
            ntd.setHasOrderableChildNodes(true);
            nextToken();
            if (currentTokenEquals(LexerLegacy.MIXIN)) {
                ntd.setMixin(true);
                nextToken();
            }
        } else if (currentTokenEquals(LexerLegacy.MIXIN)) {
            ntd.setMixin(true);
            nextToken();
            if (currentTokenEquals(LexerLegacy.ORDERABLE)) {
                ntd.setHasOrderableChildNodes(true);
                nextToken();
            }
        }

        if (currentTokenEquals(LexerLegacy.ABSTRACT)) {
            ntd.setAbstract(true);
            nextToken();
        }

        if (currentTokenEquals(LexerLegacy.VALIDATOR)) {
            nextToken();
            if (currentTokenEquals(LexerLegacy.DEFAULT)) {
                nextToken();
                ntd.setValidator(currentToken);
                nextToken();
            } else {
                lexer.fail("Invalid validator");
            }
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
            if (currentTokenEquals(LexerLegacy.PROPERTY_DEFINITION)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.CHILD_NODE_DEFINITION)) {
                ExtendedNodeDefinition ndi = new ExtendedNodeDefinition(registry);
                nextToken();
                doChildNodeDefinition(ndi, ntd);
                ndi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_CONTAINERLIST)) {
                ExtendedNodeDefinition listNodeDef = new ExtendedNodeDefinition(registry);
                nextToken();
                doChildNodeDefinition(listNodeDef, ntd);
                ExtendedNodeType[] ctnTypes = listNodeDef.getRequiredPrimaryTypes();
                String ctnListTypeName = ctnTypes[0].getNameObject().getPrefix()+":";
                for (ExtendedNodeType ctnType : ctnTypes) {
                    ctnListTypeName += ctnType.getNameObject().getLocalName();
                }
                if (listNodeDef.isMandatory()) {
                    ctnListTypeName += "Mandatory";
                }
                ctnListTypeName += "List";
                String append = "";
                if (listNodeDef.getSelectorOptions().get("availableTypes")!=null) {
                    append+= JCRContentUtils.replaceColon(listNodeDef.getSelectorOptions().get("availableTypes"));
                }
                if (listNodeDef.getSelectorOptions().get("addMixin")!=null) {
                    append+= JCRContentUtils.replaceColon(listNodeDef.getSelectorOptions().get("addMixin"));
                }
                if (append.length()>0) {
                    ctnListTypeName += Integer.toHexString(append.hashCode());
                }
                listNodeDef.setRequiredPrimaryTypes(new String[] {ctnListTypeName});
//
//                if (listNodeDef.getSelectorOptions().get("availableTypes")!=null) {
//                    ExtendedNodeType typeSelectorType = new ExtendedNodeType(registry, systemId);
//                    typeSelectorType.setName(parseName(ctnListTypeName+"Types"));
//
//                }
//

                try {
                    registry.getNodeType(ctnListTypeName);
                } catch (NoSuchNodeTypeException e) {
                    ExtendedNodeType listType = new ExtendedNodeType(registry, systemId);
                    listType.setName(parseName(ctnListTypeName));
                    listType.setDeclaredSupertypes(new String[] {Constants.JAHIANT_CONTENTLIST});
                    listType.setHasOrderableChildNodes(true);
                    ExtendedNodeDefinition def = new ExtendedNodeDefinition(registry);
                    def.setName(parseName("*"));
                    String names[] = new String[ctnTypes.length];
                    for (int i = 0; i < ctnTypes.length; i++) {
                        names[i] = ctnTypes[i].getName();
                    }
                    def.setRequiredPrimaryTypes(names);
                    def.setAllowsSameNameSiblings(true);
                    def.setDeclaringNodeType(listType);
                    def.setMandatory(listNodeDef.isMandatory());
                    def.setSelectorOptions(listNodeDef.getSelectorOptions());
                    registry.addNodeType(listType.getNameObject(),listType);
                }
                listNodeDef.setAutoCreated(true);
                listNodeDef.setMandatory(true);
                listNodeDef.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_SINGLECONTAINER)) {
                ExtendedNodeDefinition listNodeDef = new ExtendedNodeDefinition(registry);
                nextToken();
                doChildNodeDefinition(listNodeDef, ntd);
                ExtendedNodeType[] ctnTypes = listNodeDef.getRequiredPrimaryTypes();
                String ctnListTypeName = ctnTypes[0].getNameObject().getPrefix()+":";
                for (ExtendedNodeType ctnType : ctnTypes) {
                    ctnListTypeName += ctnType.getNameObject().getLocalName();
                }
                if (listNodeDef.isMandatory()) {
                    ctnListTypeName += "Mandatory";
                }
                ctnListTypeName += "Single";
                listNodeDef.setRequiredPrimaryTypes(new String[] {ctnListTypeName});
                try {
                    registry.getNodeType(ctnListTypeName);
                } catch (NoSuchNodeTypeException e) {
                    ExtendedNodeType listType = new ExtendedNodeType(registry, systemId);
                    listType.setName(parseName(ctnListTypeName));
                    listType.setDeclaredSupertypes(new String[] {Constants.JAHIANT_CONTENTLIST});
                    listType.setHasOrderableChildNodes(true);
                    ExtendedNodeDefinition def = new ExtendedNodeDefinition(registry);
                    def.setName(parseName("*"));
                    String names[] = new String[ctnTypes.length];
                    for (int i = 0; i < ctnTypes.length; i++) {
                        names[i] = ctnTypes[i].getName();
                    }
                    def.setRequiredPrimaryTypes(names);
                    def.setAllowsSameNameSiblings(false);
                    def.setDeclaringNodeType(listType);
                    def.setMandatory(listNodeDef.isMandatory());
                    registry.addNodeType(listType.getNameObject(),listType);
                }
                listNodeDef.setAutoCreated(true);
                listNodeDef.setMandatory(true);
                listNodeDef.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_SMALLTEXTFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.STRING);
                pdi.setSelector(SelectorType.SMALLTEXT);
                pdi.setInternationalized(true);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_SHAREDSMALLTEXTFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.STRING);
                pdi.setSelector(SelectorType.SMALLTEXT);
                pdi.setInternationalized(false);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_BIGTEXTFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.STRING);
                pdi.setSelector(SelectorTypeLegacy.RICHTEXT);
                pdi.setInternationalized(true);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_DATEFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.DATE);
                pdi.setSelector(SelectorTypeLegacy.DATETIMEPICKER);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_PAGEFIELD)) {
                ExtendedNodeDefinition ndi = new ExtendedNodeDefinition(registry);
                ndi.setRequiredPrimaryTypes(new String[]{Constants.JAHIANT_PAGE_LINK});
                nextToken();
                doChildNodeDefinition(ndi, ntd);
                ndi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_FILEFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.STRING);
                pdi.setSelector(SelectorTypeLegacy.FILEPICKER);
                pdi.setInternationalized(true);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_PORTLETFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.REFERENCE);
                pdi.setSelector(SelectorTypeLegacy.PORTLET);
                pdi.setInternationalized(false);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_INTEGERFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.LONG);
                pdi.setSelector(SelectorTypeLegacy.SMALLTEXT);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_FLOATFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.DOUBLE);
                pdi.setSelector(SelectorTypeLegacy.SMALLTEXT);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_BOOLEANFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.BOOLEAN);
                pdi.setSelector(SelectorTypeLegacy.CHECKBOX);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_CATEGORYFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.STRING);
                pdi.setSelector(SelectorTypeLegacy.CATEGORY);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
            } else if (currentTokenEquals(LexerLegacy.JAHIA_COLORFIELD)) {
                ExtendedPropertyDefinition pdi = new ExtendedPropertyDefinition(registry);
                pdi.setRequiredType(PropertyType.STRING);
                pdi.setSelector(SelectorTypeLegacy.COLOR);
                nextToken();
                doPropertyDefinition(pdi, ntd);
                pdi.setDeclaringNodeType(ntd);
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
        if (!currentTokenEquals(LexerLegacy.BEGIN_TYPE)) {
            return;
        }
        nextToken();
        if (pdi.getRequiredType() == 0) {
            if (currentTokenEquals(LexerLegacy.STRING)) {
                pdi.setRequiredType(PropertyType.STRING);
            } else if (currentTokenEquals(LexerLegacy.BINARY)) {
                pdi.setRequiredType(PropertyType.BINARY);
            } else if (currentTokenEquals(LexerLegacy.LONG)) {
                pdi.setRequiredType(PropertyType.LONG);
            } else if (currentTokenEquals(LexerLegacy.DOUBLE)) {
                pdi.setRequiredType(PropertyType.DOUBLE);
            } else if (currentTokenEquals(LexerLegacy.BOOLEAN)) {
                pdi.setRequiredType(PropertyType.BOOLEAN);
            } else if (currentTokenEquals(LexerLegacy.DATE)) {
                pdi.setRequiredType(PropertyType.DATE);
            } else if (currentTokenEquals(LexerLegacy.NAME)) {
                pdi.setRequiredType(PropertyType.NAME);
            } else if (currentTokenEquals(LexerLegacy.PATH)) {
                pdi.setRequiredType(PropertyType.PATH);
            } else if (currentTokenEquals(LexerLegacy.REFERENCE)) {
                pdi.setRequiredType(PropertyType.REFERENCE);
            } else if (currentTokenEquals(LexerLegacy.WEAKREFERENCE)) {
                pdi.setRequiredType(ExtendedPropertyType.WEAKREFERENCE);
            } else if (currentTokenEquals(LexerLegacy.URI)) {
                pdi.setRequiredType(ExtendedPropertyType.URI);
            } else if (currentTokenEquals(LexerLegacy.DECIMAL)) {
                pdi.setRequiredType(ExtendedPropertyType.DECIMAL);
            } else if (currentTokenEquals(LexerLegacy.UNDEFINED)) {
                pdi.setRequiredType(PropertyType.UNDEFINED);
            } else {
                lexer.fail("Unknown type '" + currentToken + "' specified");
            }
            nextToken();
            if (currentTokenEquals(LexerLegacy.END_TYPE)) {
                nextToken();
            } else if (currentTokenEquals(LexerLegacy.LIST_DELIMITER)) {
                nextToken();
                doPropertySelector(pdi);
            } else {
                lexer.fail("Missing '" + LexerLegacy.END_TYPE + "' delimiter for end of property type");
            }
        } else {
            doPropertySelector(pdi);
        }
    }

    private void doPropertySelector(ExtendedPropertyDefinition pdi) throws ParseException, IOException {
        if (currentTokenEquals(LexerLegacy.SMALLTEXT)) {
            pdi.setSelector(SelectorTypeLegacy.SMALLTEXT);
        } else if (currentTokenEquals(LexerLegacy.RICHTEXT)) {
            pdi.setSelector(SelectorTypeLegacy.RICHTEXT);
        } else if (currentTokenEquals(LexerLegacy.CHOICELIST)) {
            pdi.setSelector(SelectorTypeLegacy.CHOICELIST);
        } else if (currentTokenEquals(LexerLegacy.DATEPICKER)) {
            pdi.setSelector(SelectorTypeLegacy.DATEPICKER);
        } else if (currentTokenEquals(LexerLegacy.DATETIMEPICKER)) {
            pdi.setSelector(SelectorTypeLegacy.DATETIMEPICKER);
        } else if (currentTokenEquals(LexerLegacy.CATEGORY)) {
            pdi.setSelector(SelectorTypeLegacy.CATEGORY);
        } else if (currentTokenEquals(LexerLegacy.FILEPICKER)) {
            pdi.setSelector(SelectorTypeLegacy.FILEPICKER);
        } else if (currentTokenEquals(LexerLegacy.FILEUPLOAD)) {
            pdi.setSelector(SelectorTypeLegacy.FILEUPLOAD);
        } else if (currentTokenEquals(LexerLegacy.COLOR)) {
            pdi.setSelector(SelectorTypeLegacy.COLOR);
        } else if (currentTokenEquals(LexerLegacy.CHECKBOX)) {
            pdi.setSelector(SelectorTypeLegacy.CHECKBOX);
        } else if (currentTokenEquals(LexerLegacy.PORTLETDEFINITION)) {
            pdi.setSelector(SelectorTypeLegacy.PORTLETDEFINITION);
        } else if (currentTokenEquals(LexerLegacy.PORTLET)) {
            pdi.setSelector(SelectorTypeLegacy.PORTLET);
        } else {
            lexer.fail("Unknown type '" + currentToken + "' specified");
        }
        nextToken();
        if (currentTokenEquals(LexerLegacy.BEGIN_NODE_TYPE_NAME)) {
            doSelectorOptions(pdi);
        }
        if (currentTokenEquals(LexerLegacy.END_TYPE)) {
            nextToken();
        } else{
            lexer.fail("Missing '" + LexerLegacy.END_TYPE + "' delimiter for end of property type");
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
        while (currentTokenEquals(LexerLegacy.ATTRIBUTE)) {
            if (currentTokenEquals(LexerLegacy.PRIMARY)) {
                ntd.setPrimaryItemName(pdi.getName());
            } else if (currentTokenEquals(LexerLegacy.AUTOCREATED)) {
                pdi.setAutoCreated(true);
            } else if (currentTokenEquals(LexerLegacy.MANDATORY)) {
                pdi.setMandatory(true);
            } else if (currentTokenEquals(LexerLegacy.PROTECTED)) {
                pdi.setProtected(true);
            } else if (currentTokenEquals(LexerLegacy.MULTIPLE)) {
                pdi.setMultiple(true);
            } else if (currentTokenEquals(LexerLegacy.HIDDEN)) {
                pdi.setHidden(true);
            } else if (currentTokenEquals(LexerLegacy.INTERNATIONALIZED)) {
                pdi.setInternationalized(true);
            } else if (currentTokenEquals(LexerLegacy.INDEXED)) {
                nextToken();
                if (currentTokenEquals(LexerLegacy.DEFAULT)) {
                    nextToken();
                    if (currentTokenEquals(LexerLegacy.NO)) {
                        pdi.setIndex(ExtendedPropertyDefinition.INDEXED_NO);
                    } else if (currentTokenEquals(LexerLegacy.TOKENIZED)) {
                        pdi.setIndex(ExtendedPropertyDefinition.INDEXED_TOKENIZED);
                    } else if (currentTokenEquals(LexerLegacy.UNTOKENIZED)) {
                        pdi.setIndex(ExtendedPropertyDefinition.INDEXED_UNTOKENIZED);
                    } else {
                        lexer.fail("Invalid value for indexed [ no | tokenized | untokenized ] "+currentToken);
                    }
                } else {
                    lexer.fail("Invalid value for indexed " + currentToken);
                }
            } else if (currentTokenEquals(LexerLegacy.SCOREBOOST)) {
                nextToken();
                if (currentTokenEquals(LexerLegacy.DEFAULT)) {
                    nextToken();
                    try {
                        pdi.setScoreboost(Double.parseDouble(currentToken));
                    } catch (NumberFormatException e) {
                        lexer.fail("Invalid value for score boost "+currentToken);
                    }
                } else {
                    lexer.fail("Invalid value for score boost " + currentToken);
                }
            } else if (currentTokenEquals(LexerLegacy.ANALYZER)) {
                nextToken();
                if (currentTokenEquals(LexerLegacy.DEFAULT)) {
                    nextToken();
                    pdi.setAnalyzer(currentToken);
                } else {
                    lexer.fail("Invalid value for tokenizer " + currentToken);
                }

            } else if (currentTokenEquals(LexerLegacy.SORTABLE)) {
                pdi.setQueryOrderable(true);
            } else if (currentTokenEquals(LexerLegacy.FACETABLE)) {
                pdi.setFacetable(true);
            } else if (currentTokenEquals(LexerLegacy.FULLTEXTSEARCHABLE)) {
                nextToken();
                if (currentTokenEquals(LexerLegacy.DEFAULT)) {
                    nextToken();
                    if (currentTokenEquals(LexerLegacy.NO)) {
                        pdi.setFullTextSearchable(Boolean.FALSE);
                    } else if (currentTokenEquals(LexerLegacy.YES)) {
                        pdi.setFullTextSearchable(Boolean.TRUE);
                    }
                }
            } else if (currentTokenEquals(LexerLegacy.COPY)) {
                pdi.setOnParentVersion(OnParentVersionAction.COPY);
            } else if (currentTokenEquals(LexerLegacy.VERSION)) {
                pdi.setOnParentVersion(OnParentVersionAction.VERSION);
            } else if (currentTokenEquals(LexerLegacy.INITIALIZE)) {
                pdi.setOnParentVersion(OnParentVersionAction.INITIALIZE);
            } else if (currentTokenEquals(LexerLegacy.COMPUTE)) {
                pdi.setOnParentVersion(OnParentVersionAction.COMPUTE);
            } else if (currentTokenEquals(LexerLegacy.IGNORE)) {
                pdi.setOnParentVersion(OnParentVersionAction.IGNORE);
            } else if (currentTokenEquals(LexerLegacy.ABORT)) {
                pdi.setOnParentVersion(OnParentVersionAction.ABORT);
            }
            nextToken();
        }
    }

    /**
     * processes the property default values
     *
     * @param pdi
     * @throws ParseException
     */
    private void doPropertyDefaultValue(ExtendedPropertyDefinition pdi) throws ParseException, IOException {
        if (!currentTokenEquals(LexerLegacy.DEFAULT)) {
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
        if (!currentTokenEquals(LexerLegacy.CONSTRAINT)) {
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
            if (currentTokenEquals(LexerLegacy.BEGIN_TYPE)) {
                nextToken();
                List<String> params = new ArrayList<String>();
                while (!currentTokenEquals(LexerLegacy.END_TYPE)) {
                    params.add(currentToken);
                    nextToken();
                }
                nextToken();

                values.add(new DynamicValueImpl(v, params, pdi.getRequiredType(), isConstraint, pdi));
            } else {
                values.add(new ValueImpl(v, pdi.getRequiredType(), isConstraint));
            }
        } while (currentTokenEquals(LexerLegacy.LIST_DELIMITER));
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
        if (!currentTokenEquals(LexerLegacy.BEGIN_TYPE)) {
            return;
        }
        if (ndi.getRequiredPrimaryTypeNames() == null) {
            List<String> types = new ArrayList<String>();
            do {
                nextToken();
                types.add(currentToken);
                nextToken();
            } while (currentTokenEquals(LexerLegacy.LIST_DELIMITER));

            if (currentTokenEquals(LexerLegacy.BEGIN_NODE_TYPE_NAME)) {
                doSelectorOptions(ndi);
            }
            if (currentTokenEquals(LexerLegacy.END_TYPE)) {
                nextToken();
            } else{
                lexer.fail("Missing '" + LexerLegacy.END_TYPE + "' delimiter for end of child node type");
            }
            ndi.setRequiredPrimaryTypes(types.toArray(new String[types.size()]));
        } else {
            nextToken();
            doChildNodeSelector(ndi);
        }
    }

    private void doChildNodeSelector(ExtendedNodeDefinition ndi) throws ParseException, IOException {
        if (currentTokenEquals(LexerLegacy.PAGE)) {
            ndi.setSelector(SelectorTypeLegacy.PAGE);
        } else {
            lexer.fail("Unknown type '" + currentToken + "' specified");
        }
        nextToken();
        if (currentTokenEquals(LexerLegacy.BEGIN_NODE_TYPE_NAME)) {
            doSelectorOptions(ndi);
        }
        if (currentTokenEquals(LexerLegacy.END_TYPE)) {
            nextToken();
        } else{
            lexer.fail("Missing '" + LexerLegacy.END_TYPE + "' delimiter for end of property type");
        }
    }

    /**
     * processes the childnode default types
     *
     * @param ndi
     * @throws ParseException
     */
    private void doChildNodeDefaultType(ExtendedNodeDefinition ndi) throws ParseException, IOException {
        if (!currentTokenEquals(LexerLegacy.DEFAULT)) {
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
        while (currentTokenEquals(LexerLegacy.ATTRIBUTE)) {
            if (currentTokenEquals(LexerLegacy.PRIMARY)) {
                ntd.setPrimaryItemName(ndi.getName());
            } else if (currentTokenEquals(LexerLegacy.AUTOCREATED)) {
                ndi.setAutoCreated(true);
            } else if (currentTokenEquals(LexerLegacy.MANDATORY)) {
                ndi.setMandatory(true);
            } else if (currentTokenEquals(LexerLegacy.HIDDEN)) {
                ndi.setHidden(true);
            } else if (currentTokenEquals(LexerLegacy.PROTECTED)) {
                ndi.setProtected(true);
            } else if (currentTokenEquals(LexerLegacy.MULTIPLE)) {
                ndi.setAllowsSameNameSiblings(true);
            } else if (currentTokenEquals(LexerLegacy.FULLTEXTSEARCHABLE)) {
                nextToken();
                if (currentTokenEquals(LexerLegacy.DEFAULT)) {
                    nextToken();
                    }
            } else if (currentTokenEquals(LexerLegacy.COPY)) {
                ndi.setOnParentVersion(OnParentVersionAction.COPY);
            } else if (currentTokenEquals(LexerLegacy.VERSION)) {
                ndi.setOnParentVersion(OnParentVersionAction.VERSION);
            } else if (currentTokenEquals(LexerLegacy.INITIALIZE)) {
                ndi.setOnParentVersion(OnParentVersionAction.INITIALIZE);
            } else if (currentTokenEquals(LexerLegacy.COMPUTE)) {
                ndi.setOnParentVersion(OnParentVersionAction.COMPUTE);
            } else if (currentTokenEquals(LexerLegacy.IGNORE)) {
                ndi.setOnParentVersion(OnParentVersionAction.IGNORE);
            } else if (currentTokenEquals(LexerLegacy.ABORT)) {
                ndi.setOnParentVersion(OnParentVersionAction.ABORT);
            } else if (currentTokenEquals(LexerLegacy.WORKFLOW)) {
                nextToken();
                if (currentTokenEquals(LexerLegacy.DEFAULT)) {
                    nextToken();
                    ndi.setWorkflow(currentToken);
                } else {
                    lexer.fail("Invalid value for workflow " + currentToken);
                }
            }
            nextToken();
        }
    }

    private void doSelectorOptions(ExtendedItemDefinition pdi) throws ParseException, IOException {
        nextToken();
        Map<String,String> options = new HashMap<String,String>();
        while (true) {
            String key = currentToken;
            String value = "";
            nextToken();
            if (currentTokenEquals(LexerLegacy.DEFAULT)) {
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
            if (currentTokenEquals(LexerLegacy.END_NODE_TYPE_NAME)) {
                nextToken();
                break;
            }
            if (!currentTokenEquals(LexerLegacy.LIST_DELIMITER)) {
                lexer.fail("Missing '" + LexerLegacy.END_NODE_TYPE_NAME + "' delimiter");
            }
            nextToken();
        }
        pdi.setSelectorOptions(options);
    }

    /**
     * Gets the next token from the underlying LexerLegacy.
     *
     * @see LexerLegacy#getNextToken()
     * @throws ParseException if the LexerLegacy fails to get the next token.
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
        for (String e : s) {
            if (currentToken.equals(e)) {
                return true;
            }
        }
        return false;
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
