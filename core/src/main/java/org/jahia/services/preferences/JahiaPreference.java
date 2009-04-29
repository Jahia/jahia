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
package org.jahia.services.preferences;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRNodeDecorator;

import javax.jcr.RepositoryException;
import java.security.Principal;


/**
 * User: jahia
 * Date: 20 mars 2008
 * Time: 09:53:32
 */
public class JahiaPreference<T extends JCRNodeWrapper>  {
    private Principal principal;
    private T node;

    public JahiaPreference(T node) {
        this.node = node;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal thePrincipal) {
        this.principal = thePrincipal;
    }

    public T getNode() {
        return node;
    }

}
