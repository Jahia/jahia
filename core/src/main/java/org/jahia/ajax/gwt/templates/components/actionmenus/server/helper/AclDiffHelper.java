/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.actionmenus.server.helper;

import org.apache.log4j.Logger;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.fields.ContentField;
import org.jahia.exceptions.JahiaException;
import org.jahia.ajax.usersession.userSettings;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffState;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffDetails;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.aclmanagement.server.ACLHelper;
import org.jahia.params.ProcessingContext;
import org.jahia.content.ContentObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 28 f�vr. 2008 - 16:03:35
 */
public class AclDiffHelper {

    private final static List<String> PERMISSIONS = new ArrayList<String>() ;
    static {
        PERMISSIONS.add("read") ;
        PERMISSIONS.add("write") ;
        PERMISSIONS.add("admin") ;
    }
    private final static Logger logger = Logger.getLogger(AclDiffHelper.class) ;

    /**
     * Check if there is an ACL break between the given object and its parent.
     *
     * @param therequest the current request
     * @param jParams the processing context
     * @param isDevMode development mode enabled
     * @param objectKey the current object key
     * @return the acl diff state (or null if no particular state)
     */
    public static GWTJahiaAclDiffState getAclDiffState(HttpServletRequest therequest, ProcessingContext jParams, boolean isDevMode, String objectKey) {
        Boolean aclDifferenceParam = ActionMenuServiceHelper.getUserInitialSettingForDevMode(therequest, userSettings.ACL_VISU_ENABLED, isDevMode);
        if (!isDevMode) {
            try {
                String value = (String) therequest.getSession().getAttribute(userSettings.ACL_VISU_ENABLED);
                aclDifferenceParam = value != null ? Boolean.valueOf(value) : null;
                if (aclDifferenceParam == null) {
                    aclDifferenceParam = org.jahia.settings.SettingsBean.getInstance().isAclDisp();
                }
            } catch (final IllegalStateException e) {
                logger.error(e, e);
            }
        }
        try {
            ContentObject obj = JahiaObjectCreator.getContentObjectFromString(objectKey) ;

            // check but should never be null
            if (obj == null) {
                logger.warn("Content object for key " + objectKey + " should not be null") ;
                return null ;
            }

            // only check for write rights, no admi rights required to display state / popup
            if (aclDifferenceParam) {
                aclDifferenceParam = obj.checkWriteAccess(jParams.getUser())  ;
            }

            if (aclDifferenceParam && !objectKey.equals("ContentPage_" + jParams.getSite().getHomePageID()) && (!obj.isAclSameAsParent() &&  (!obj.getACL().getACL().getEntries().isEmpty() || obj.getACL().getInheritance() == 1 ))) {
                return new GWTJahiaAclDiffState(objectKey) ;
            }
        } catch (final JahiaException je) {
            logger.error(je, je);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return null ;
    }

    public static GWTJahiaAclDiffDetails getAclDiffDetails(ProcessingContext jParams, String objectKey) {
        if (objectKey != null && objectKey.length() > 0) {
            try {
                ContentObject obj = JahiaObjectCreator.getContentObjectFromString(objectKey) ;

                // check but should never be null
                if (obj == null) {
                    logger.warn("Content object for key " + objectKey + " should not be null") ;
                    return null ;
                }

                GWTJahiaNodeACL acls = ACLHelper.getGWTJahiaNodeACL(obj.getACL(), jParams) ;
                Map<String, String> rights = new HashMap<String, String>() ;
                Map<String, String> inheritedRights = new HashMap<String, String>() ;
                for (GWTJahiaNodeACE ace: acls.getAce()) {
                    String principal = ace.getPrincipal() ;
                    StringBuilder permBuf = new StringBuilder() ;
                    StringBuilder inhPermBuf = new StringBuilder() ;
                    // build (rwa / rw- / r-- / ---) strings for local and inherited permissions
                    String inhFrom = ace.getInheritedFrom() ;
                    Map<String, String> inheritedPermissions = ace.getInheritedPermissions() ;
                    if (inhFrom == null || inheritedPermissions == null) {
                        inhPermBuf.append("   ") ;
                    } else {
                        for (String perm: PERMISSIONS) {
                            if (inheritedPermissions.containsKey(perm)) {
                                if (inheritedPermissions.get(perm).equalsIgnoreCase("grant")) {
                                    inhPermBuf.append(perm.substring(0, 1)) ;
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(principal +  " can " + perm + " (inh)") ;
                                    }
                                } else {
                                    inhPermBuf.append("-") ;
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(principal +  " cannot " + perm + " (inh)") ;
                                    }
                                }
                            } else {
                                inhPermBuf.append(" ") ;
                                if (logger.isDebugEnabled()) {
                                    logger.debug(principal +  " cannot " + perm + " (inh / not found)") ;
                                }
                            }
                        }
                    }
                    Map<String, String> permissions = ace.getPermissions() ;
                    if (ace.isInherited() || permissions == null) {
                        permBuf.append("   ") ;
                    } else {
                        for (String perm: PERMISSIONS) {
                            if (permissions.containsKey(perm)) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(permissions.get(perm));
                                }
                                if (permissions.get(perm).equalsIgnoreCase("grant")) {
                                    permBuf.append(perm.substring(0, 1)) ;
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(principal +  " can " + perm) ;
                                    }
                                } else {
                                    permBuf.append("-") ;
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(principal +  " cannot " + perm) ;
                                    }
                                }
                            } else {
                                // this case should never occur
                                permBuf.append(" ") ;
                                if (logger.isDebugEnabled()) {
                                    logger.debug(principal +  " cannot " + perm + " (not found)") ;
                                }
                            }
                        }
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug(principal + " : " + permBuf.toString()) ;
                        logger.debug(principal + " : " + inhPermBuf.toString()) ;
                    }
                    rights.put(principal, permBuf.toString()) ;
                    inheritedRights.put(principal, inhPermBuf.toString()) ;
                }

                String url = null ;
                if (obj instanceof ContentContainerList) {
                    url = ActionMenuServiceHelper.drawContainerListPropertiesLauncher(jParams, (ContentContainerList) obj, false, 0, "rightsMgmt");
                } else if (obj instanceof ContentContainer) {
                    url = ActionMenuServiceHelper.drawUpdateContainerLauncher(jParams, (ContentContainer) obj, false, 0, "rightsMgmt");
                } else if (obj instanceof ContentPage) {
                    url = ActionMenuServiceHelper.drawPagePropertiesLauncher(jParams, false, obj.getID(), "rightsMgmt");
                }
                if (url != null) {
                    return new GWTJahiaAclDiffDetails(url, rights, inheritedRights) ;
                }
            } catch (final JahiaException je) {
                logger.error(je, je);
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null ;
    }



}
