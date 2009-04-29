/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.Reader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 30, 2008
 * Time: 3:12:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaGroupingFileReader extends JahiaCndReader {

    /**
     * Creates a new CND reader.
     *
     * @param r
     * @throws ParseException
     */
    public JahiaGroupingFileReader(Reader r, String filename, String systemId, NodeTypeRegistry registry)
            throws ParseException, IOException {
        super(r, filename, systemId, registry);
    }

    /**
     * Parses the definition
     *
     * @throws ParseException
     */
    public void parse() throws ParseException, IOException {
        nextToken();

        while (!currentTokenEquals(Lexer.EOF)) {
            parseNodeType();
        }
    }

    private void parseNodeType() throws ParseException {
        if (!currentTokenEquals(Lexer.BEGIN_NODE_TYPE_NAME)) {
            lexer.fail("Missing '" + Lexer.BEGIN_NODE_TYPE_NAME + "' delimiter for beginning of node type name");
        }
        nextToken();
        ExtendedNodeType nt = null;
        try {
            nt = registry.getNodeType(currentToken);
        } catch (NoSuchNodeTypeException e) {
            lexer.fail("No such node type",e);
        }
        nextToken();
        if (!currentTokenEquals(Lexer.END_NODE_TYPE_NAME)) {
            lexer.fail("Missing '" + Lexer.END_NODE_TYPE_NAME + "' delimiter for end of node type name, found " + currentToken);
        }
        nextToken();

        List groups = new ArrayList();

        parseGroup(nt, groups);
        nt.setGroupedItems(groups);
    }

    private void parseGroup(ExtendedNodeType nt, List groups) throws ParseException {
        while (true) {
            if (currentTokenEquals(Lexer.PROPERTY_DEFINITION) || currentTokenEquals(Lexer.CHILD_NODE_DEFINITION)) {
                String t = currentToken;
                nextToken();
                groups.add(t+currentToken);
            } else if (currentTokenEquals(Lexer.BEGIN_TYPE)) {
                List subGroup = new ArrayList();
                nextToken();
                parseGroup(nt, subGroup);
                if (currentTokenEquals(Lexer.END_TYPE)) {
                    groups.add(subGroup);
                } else {
                    lexer.fail("Missing '" + Lexer.END_TYPE + "', found " + currentToken);
                }
            } else {
                lexer.fail("Unexpected token " + currentToken);                
            }
            nextToken();
            if (currentTokenEquals(Lexer.LIST_DELIMITER)) {
                nextToken();
            } else {
                break;
            }
        }
    }

}
