/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.usermanager;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Predrag Viceic <Predrag.Viceic@ci.unil.ch>
 * @version 1.0
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

public class GroupManagerProviderBean {
    
    private static final transient Logger logger = Logger
            .getLogger(GroupManagerProviderBean.class);

    private String key;
    private String className;
    private String description;
    private String title;
    private boolean isReadOnly = true;
    private JahiaGroupManagerProvider instance;
    private boolean isDefault = false;
    private int priority;

    public GroupManagerProviderBean (String key,
                                     String className,
                                     String title,
                                     String description,
                                     boolean isDefault,
                                     boolean isReadOnly,
                                     int priority) {
        this.key = key;
        this.className = className;
        this.description = description;
        this.title = title;
        this.isDefault = isDefault;
        this.isReadOnly = isReadOnly;
        this.priority = priority;
    }

    public String getKey () {
        return key;
    }

    public void setKey (String key) {
        this.key = key;
    }

    public void setClassName (String className) {
        this.className = className;
    }

    public String getClassName () {
        return className;
    }

    public void setDescription (String description) {
        this.description = description;
    }

    public String getDescription () {
        return description;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public String getTitle () {
        return title;
    }

    public void setIsDefault (boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean getIsDefault () {
        return isDefault;
    }

    public void setIsReadOnly (boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public boolean getIsReadOnly () {
        return isReadOnly;
    }

    public void setPriority (int priority) {
        this.priority = priority;
    }

    public int getPriority () {
        return priority;
    }

    public JahiaGroupManagerProvider getInstance () {
        if (this.instance != null) {
            return this.instance;
        }

        if (this.className == null) {
            return null;
        }

        try {
            Class destClass = Class.forName (this.className);
            Class superClass = destClass.getSuperclass ();
            if (superClass == null) {
                return null;
            }
            if (!"org.jahia.services.usermanager.JahiaGroupManagerProvider".equals (
                    superClass.getName ())) {
                // class parent is not of correct type.
                return null;
            }
            Method getInstanceMethod = destClass.getMethod ("getInstance", (Class[])null);
            if (getInstanceMethod == null) {
                return null;
            }
            this.instance = (JahiaGroupManagerProvider) getInstanceMethod.invoke (null, (Object[])null);

        } catch (LinkageError le) {
            logger.error(le.getMessage(), le);
            this.instance = null;
        } catch (ClassNotFoundException cnfe) {
            logger.error(cnfe.getMessage(), cnfe);
            this.instance = null;
        } catch (NoSuchMethodException nsme) {
            logger.error(nsme.getMessage(), nsme);
            this.instance = null;
        } catch (InvocationTargetException ite) {
            logger.error(ite.getMessage(), ite);
            this.instance = null;
        } catch (IllegalAccessException iae) {
            logger.error(iae.getMessage(), iae);
            this.instance = null;
        }
        return this.instance;
    }

    public boolean equals (Object another) {
        if (this == another) return true;
        
        if (another != null && this.getClass() == another.getClass()) {
            final GroupManagerProviderBean right = (GroupManagerProviderBean) another;
            return objectEquals (getKey (), right.getKey ());
        }
        return false; 
    }

    private boolean objectEquals (String left, String right) {
        if ((left == null) && (right == null)) {
            return true;
        } else if ((left == null) && (right != null)) {
            return false;
        } else if ((left != null) && (right == null)) {
            return false;
        } else {
            // we are now guaranteed that neither left or right are null so we
            // can call the equals method safely.
            return left.equals (right);
        }
    }
}
