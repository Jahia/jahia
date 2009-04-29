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
package org.jahia.jaas;

import java.security.Principal;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 9 nov. 2007
 * Time: 17:16:55
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPrincipal implements Principal {
    private boolean isSystem = false;
    private boolean isGuest = false;

    private String name;

    public JahiaPrincipal(String name, boolean system, boolean guest) {
        this.name = name;
        isSystem = system;
        isGuest = guest;
    }

    public JahiaPrincipal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public boolean isGuest() {
        return isGuest;
    }
}
