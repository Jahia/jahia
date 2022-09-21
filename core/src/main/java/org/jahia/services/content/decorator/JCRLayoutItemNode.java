/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import javax.jcr.Node;

/**
 * 
 * User: jahia
 * Date: 18 mars 2009
 * Time: 16:39:18
 * 
 */
public class JCRLayoutItemNode extends JCRNodeDecorator {

    public JCRLayoutItemNode(JCRNodeWrapper node) {
        super(node);
    }

    public Node getPortlet() throws RepositoryException {
        return getProperty("j:portlet").getNode();
    }

    public void setPortlet(JCRNodeWrapper portletNode) throws RepositoryException {
        setProperty("j:portlet", portletNode);
    }

    public int getColumnIndex() throws RepositoryException {
        return (int) getProperty("j:columnIndex").getLong();
    }

    public void setColumnIndex(int columnIndex) throws RepositoryException {
        setProperty("j:columnIndex", columnIndex);
    }

    public int getRowIndex() throws RepositoryException {
        return (int) getProperty("j:rowIndex").getLong();
    }

    public void setRowIndex(int rowIndex) throws RepositoryException {
        setProperty("j:rowIndex", rowIndex);
    }

    public String getStatus() throws RepositoryException {
        return getProperty("j:status").getString();
    }

    public void setStatus(String status) throws RepositoryException {
        setProperty("j:status", status);
    }
}
