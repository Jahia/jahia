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

 package org.jahia.resourcebundle;

/**
 * <p>Title: I18N Resource bundle message object</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ResourceMessage {

    private Object[] parameters;
    private String resourceKey;

    public ResourceMessage () {
    }

    public ResourceMessage (String resourceKey) {
        this.resourceKey = resourceKey;
        this.parameters = null;
    }

    public ResourceMessage (String resourceKey, Object[] parameters) {
        this.resourceKey = resourceKey;
        this.parameters = parameters;
    }

    public ResourceMessage (String resourceKey, Object parameter) {
        this.resourceKey = resourceKey;
        Object[] parameters = new Object[1];
        parameters[0] = parameter;
        this.parameters = parameters;
    }

    public ResourceMessage (String resourceKey, Object parameter1,
                            Object parameter2) {
        this.resourceKey = resourceKey;
        Object[] parameters = new Object[2];
        parameters[0] = parameter1;
        parameters[1] = parameter2;
        this.parameters = parameters;
    }

    public ResourceMessage (String resourceKey, Object parameter1,
                            Object parameter2, Object parameter3) {
        this.resourceKey = resourceKey;
        Object[] parameters = new Object[3];
        parameters[0] = parameter1;
        parameters[1] = parameter2;
        parameters[2] = parameter3;
        this.parameters = parameters;
    }

    public ResourceMessage (String resourceKey, Object parameter1,
                            Object parameter2, Object parameter3,
                            Object parameter4) {
        this.resourceKey = resourceKey;
        Object[] parameters = new Object[4];
        parameters[0] = parameter1;
        parameters[1] = parameter2;
        parameters[2] = parameter3;
        parameters[3] = parameter4;
        this.parameters = parameters;
    }

    public Object[] getParameters () {
        return parameters;
    }

    public void setParameters (Object[] parameters) {
        this.parameters = parameters;
    }

    public String getResourceKey () {
        return resourceKey;
    }

    public void setResourceKey (String resourceKey) {
        this.resourceKey = resourceKey;
    }

}