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
 package org.jahia.services.usermanager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * <p>Title: Small bean to manage provider info </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Inc.</p>
 *
 * @author Serge Huber
 * @version 3.0
 */

public class UserManagerProviderBean {
    
    private static final transient Logger logger = Logger
            .getLogger(UserManagerProviderBean.class);

    private String key;
    private String className;
    private String description;
    private String title;
    private boolean isReadOnly = true;
    private JahiaUserManagerProvider instance;
    private boolean isDefault = false;
    private int priority = 99;

    public UserManagerProviderBean (String key,
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

    public JahiaUserManagerProvider getInstance () {
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
            if (!"org.jahia.services.usermanager.JahiaUserManagerProvider".equals (
                    superClass.getName ())) {
                // class parent is not of correct type.
                return null;
            }
            Method getInstanceMethod = destClass.getMethod ("getInstance", (Class[])null);
            if (getInstanceMethod == null) {
                return null;
            }
            this.instance = (JahiaUserManagerProvider) getInstanceMethod.invoke (null, (Object[])null);
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

    public boolean equals (Object obj) {
        if (!(obj instanceof UserManagerProviderBean)) {
            return false;
        }
        UserManagerProviderBean right = (UserManagerProviderBean) obj;
        return objectEquals (getKey (), right.getKey ());
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