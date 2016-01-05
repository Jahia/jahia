/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Apr 30, 2008
 * Time: 3:12:14 PM
 * 
 */
public class JahiaGroupingFileReader extends JahiaCndReader {

    /**
     * Creates a new CND reader.
     *
     * @param r
     * @throws ParseException
     */
    public JahiaGroupingFileReader(Reader r, String filename, String systemId, NodeTypeRegistry registry)
            throws ParseException {
        super(r, filename, systemId, registry);
    }

    /**
     * Parses the definition
     *
     * @throws ParseException
     */
    public void parse() throws ParseException {
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
