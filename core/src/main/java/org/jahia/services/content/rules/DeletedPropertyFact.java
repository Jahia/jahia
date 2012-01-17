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

package org.jahia.services.content.rules;

import org.drools.spi.KnowledgeHelper;

import javax.jcr.RepositoryException;

/**
 * This class describe a property deletion event for the drools engine.
 * User: toto
 * Date: 17 janv. 2008
 * Time: 15:20:31
 * 
 */
public class DeletedPropertyFact {
    private String nodePath;
    private AddedNodeFact node;
    private String name;

    public DeletedPropertyFact(ChangedPropertyFact property, KnowledgeHelper drools) throws RepositoryException {
        name = property.getName();
        node = property.getNode();
        nodePath = node.getPath();
        property.getProperty().remove();
        drools.retract(property);
    }

    public DeletedPropertyFact(AddedNodeFact node, String property) throws RepositoryException {
        this.node = node;
        nodePath = node.getPath();
        name = property;
    }

    public String getName() {
        return name;
    }

    public AddedNodeFact getNode() {
        return node;
    }

    public String toString() {
        return "deleted "+nodePath+"/"+name;
    }

    /**
     * Returns the current JCR operation type.
     *
     * @return the current JCR operation type
     * @throws javax.jcr.RepositoryException in case of a repository access error
     * @since Jahia 6.6
     */
    public String getOperationType() {
        return node.getOperationType();
    }
}
