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
package org.jahia.services.cache;

import au.id.jericho.lib.html.Tag;

import java.io.Serializable;
import java.util.List;


/**
 * <p>This class represents an HTML page stored into an cache entry of the Html Cache. Each page is defined by the
 * page's body content and its content type.</p>
 *
 * @author Cedric Mailleux.
 * @version 1.0
 * @since Jahia 4.0
 */

public class SkeletonCacheEntry implements Serializable {
    static final long serialVersionUID = -7215380085109494834L; 

    /**
     * the HTML body content.
     */
    private String contentType;

    /**
     * the HTML content type.
     */
    private String contentBody;
    private transient List<? extends Tag> includeTag;
    private transient List<? extends Tag> varsTag;
    public SkeletonCacheEntry() {
    }

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String contentBody) {
        this.contentBody = contentBody;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<? extends Tag> getIncludeTag() {
        return includeTag;
    }

    public void setIncludeTag(List<? extends Tag> includeTag) {
        this.includeTag = includeTag;
    }

    public List<? extends Tag> getVarsTag() {
        return varsTag;
    }

    public void setVarsTag(List<? extends Tag> varsTag) {
        this.varsTag = varsTag;
    }
}