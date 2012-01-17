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

package org.jahia.data.applications;

import org.jahia.services.applications.ApplicationsManagerServiceImpl;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.content.JCRContentUtils;
import org.slf4j.Logger;

import javax.portlet.PortletMode;
import java.io.Serializable;
import java.util.Locale;

/**
 * <p>Title: EntryPointInstance for a web application</p>
 * <p>Description: This class represents an entry point instance into a web
 * application. It is an instance of an EntryPointDefinition, meaning that
 * a web application can have multiple EntryPointDefinitions, which in turn
 * may be multiple EntryPointInstances. Only the ApplicationBean and the
 * EntryPointInstance are persistant objects. The EntryPointDefinitions are
 * objects that are built dynamically by the ApplicationManagerProvider
 * services.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 * @todo properties not implemented, will do later.
 */

public class EntryPointInstance implements Serializable {
    private transient static final Logger logger = org.slf4j.LoggerFactory.getLogger(EntryPointInstance.class);
    private String ID;
    private String defName;
    private String contextName;
    private String resKeyName;
    private long expirationTime;
    private String cacheScope;


    public EntryPointInstance(String ID, String contextName, String definitionName) {
        this.defName = contextName + "." + definitionName;
        this.ID = ID;
        this.contextName = contextName;
    }

    public EntryPointInstance(String ID, String contextName, String definitionName, String resKeyName) {
        this.defName = contextName + "." + definitionName;
        this.ID = ID;
        this.contextName = contextName;
        this.resKeyName = resKeyName;
    }

    public String getID() {
        return ID;
    }

    public String getDefName() {
        return defName;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getContextName() {
        return contextName;
    }

    public String getResKeyName() {
        return resKeyName;
    }

    public void setResKeyName(String resKeyName) {
        this.resKeyName = resKeyName;
    }

    public String getDisplayName(Locale locale) {
        // Todo get the localized display name for this entry point from the resource manager
        return resKeyName;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setCacheScope(String cacheScope) {
        this.cacheScope = cacheScope;
    }

    public String getCacheScope() {
        return cacheScope;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public boolean isUserInRole(JahiaUser user, String role, String workspaceName) {
        // This method maps servlet roles on Jahia's groups
        return JCRContentUtils.hasPermission(workspaceName, ApplicationsManagerServiceImpl.getWebAppQualifiedNodeName(contextName,role),ID);
    }

    public boolean isModeAllowed(JahiaUser user, String mode, String workspaceName) {
        // mode view is mandatory for all user
        if(mode != null && mode.equalsIgnoreCase(PortletMode.VIEW.toString())){
            return true;
        }
        return JCRContentUtils.hasPermission(workspaceName, ApplicationsManagerServiceImpl.getPortletQualifiedNodeName(contextName, defName,mode),ID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntryPointInstance that = (EntryPointInstance) o;

        if (!ID.equals(that.ID)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}
