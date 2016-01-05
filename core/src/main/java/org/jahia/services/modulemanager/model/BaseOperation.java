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

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;

/**
 * Base class for operation data objects.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BaseOperation extends BasePersistentObject {

    private static final long serialVersionUID = -4651648193497928439L;

    private String info;

    private String state = "open";

    /**
     * Initializes an instance of this class.
     */
    public BaseOperation() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of the operation
     */
    public BaseOperation(String name) {
        super(name);
    }

    @Field(jcrName = "j:info")
    public String getInfo() {
        return info;
    }

    @Field(jcrName = "j:state")
    public String getState() {
        return state;
    }

    /**
     * Returns <code>true</code> if the operation is completed; either successfully or failed, but there is no more processing possible.
     * 
     * @return <code>true</code> if the operation is completed; either successfully or failed, but there is no more processing possible
     */
    public boolean isCompleted() {
        return state != null && ("successful".equals(state) || "failed".equals(state));
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setState(String state) {
        this.state = state;
    }

}
