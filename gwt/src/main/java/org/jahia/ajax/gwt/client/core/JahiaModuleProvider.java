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
package org.jahia.ajax.gwt.client.core;

/**
 * User: jahia
 * Date: 2 avr. 2008
 * Time: 11:34:41
 */
public abstract class JahiaModuleProvider {
    /**
     * This  method returns a JahiaModule instance depending on the jahiaType.
     *
     * @param jahiaType the jahia type
     * @return a new instance of the corresponding JahiaModule
     */
    public abstract JahiaModule getJahiaModuleByJahiaType(String jahiaType);
}
