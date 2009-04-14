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

 package org.jahia.engines;

import java.io.Serializable;

import org.apache.struts.action.ActionMessage;


/**
 * <p>Title: EngineMessage bean object. </p> <p>Description: Inspired by Struts ActionMessage,
 * but more JavaBean compliant so that it can work with JSTL and better with Jahia's
 * localization classes.</p> <p>Copyright: Copyright (c) 2002</p> <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class EngineMessage implements Serializable {

    private final String key;
    private final Object[] values;

    public EngineMessage () {
        this (null, null);
    }

    public EngineMessage (String key) {
        this (key, null);
    }

    public EngineMessage (String key, Object... value1) {
        this.key = key;
        this.values = value1;
    }


    public String getKey () {
        return key;
    }

    public Object[] getValues () {
        return values;
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("org.jahia.engines.EngineMessage").
                append(" key: ").append(key).append(" values: ");
        if (values != null) {
            for (int i=0; i<values.length; i++) {
                buff.append(values[i]);
                buff.append(", ");
            }
        } else {
            buff.append("null");
        }
        return buff.toString();
    }

    public boolean equals (Object o) {
        return this.toString().equals(o.toString());
    }

    public ActionMessage toActionMessage() {
        return new ActionMessage(getKey(), getValues());
    }
}