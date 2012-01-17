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

import org.jahia.content.ObjectKey;

/**
 * 
 * User: ktlili
 * Date: 26 janv. 2007
 * Time: 16:26:42
 */
public class EntryPointObjectKey extends ObjectKey {

    private static final long serialVersionUID = 1135214230216807396L;
    
    public static final String ENTRY_POINT_TYPE = "entrypoint";
    
    /*TO DO: ckeck value of this IDInType */
    private String IDInType = "100";
    private int idInType;

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method. Please do not use this constructor directly
     */
    public EntryPointObjectKey() {}

    public EntryPointObjectKey(String appID_entryPointName) {
        super(ENTRY_POINT_TYPE, appID_entryPointName);
    }

    public EntryPointObjectKey(String appID_entryPointName, String objectKey) {
        super(ENTRY_POINT_TYPE, appID_entryPointName, objectKey);
    }

    public String getType() {
        return ENTRY_POINT_TYPE;
    }

    public String getIDInType() {
        return this.IDInType;
    }

    public int getIdInType() {
        if (idInType <= 0) {
            try {
                idInType = Integer.parseInt(IDInType);
            } catch (NumberFormatException e) {
                idInType = -1;
            }
        }
        return idInType;
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return new EntryPointObjectKey(IDInType, objectKey);
    }

}
