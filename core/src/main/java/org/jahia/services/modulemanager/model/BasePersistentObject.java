/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.modulemanager.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;

import java.io.Serializable;

/**
 * Base class for persistent objects, related to module management.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BasePersistentObject implements Serializable {

    private static final long serialVersionUID = -2497794314300084887L;

    private String identifier;

    private String name;

    private String path;

    public BasePersistentObject() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name the object (node) name
     */
    public BasePersistentObject(String name) {
        this();
        this.name = name;
    }

    @Field(uuid = true, jcrProtected = true)
    public String getIdentifier() {
        return identifier;
    }

    @Field(id = true, jcrProtected = true)
    public String getName() {
        if (name != null) {
            return name;
        } else if (path != null) {
            return StringUtils.substringAfterLast(path, "/");
        } else {
            return null;
        }
    }

    @Field(path = true, jcrProtected = true)
    public String getPath() {
        return path;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setName(String id) {
        this.name = id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}