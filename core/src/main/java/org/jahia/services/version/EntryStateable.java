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
package org.jahia.services.version;

/**
 * <p>Title: EntryState-able interface</p>
 * <p>Description: Under this name that sorta invents a new word in the english
 * language is hidden an interface that "tags" a class to be
 * EntryState-able. This allows it to be used in conjunction with the version
 * service and therefore to offer a central location to "resolve" content entries
 *  and not have to reimplement the resolver for each EntryState-able class.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author David Jilli
 * @version 1.0
 */
public interface EntryStateable {

    public String   getLanguageCode();

    public int      getVersionID();

    public int      getWorkflowState();

}
