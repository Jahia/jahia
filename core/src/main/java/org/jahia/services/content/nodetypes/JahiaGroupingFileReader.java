/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import java.util.List;
import java.util.ArrayList;

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
