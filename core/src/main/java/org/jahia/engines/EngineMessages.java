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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.struts.action.ActionMessages;

/**
 * <p>Title: Container for EngineMessage objects</p> <p>Description: Inspired by Struts
 * ActionMessages, but more JavaBean compliant so that it can work with JSTL and better with
 * Jahia's localization classes. </p> <p>Copyright: Copyright (c) 2002</p> <p>Company: Jahia
 * Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class EngineMessages {

    public static final String GLOBAL_MESSAGE = "org.jahia.engines.global_message";
    public static final String CONTEXT_KEY = "engineMessages";

    Map messages = new HashMap();

    public EngineMessages () {
    }

    public void add (EngineMessage message) {
        add (GLOBAL_MESSAGE, message);
    }

    public void add (String property,
                     EngineMessage message) {
        List propertyList = (List) messages.get (property);
        if (propertyList == null) {
            propertyList = new ArrayList();
        }
        propertyList.add (message);
        messages.put (property, propertyList);
    }

    public Set getProperties () {
        return messages.keySet ();
    }

    public Set getEntrySet () {
        return messages.entrySet ();
    }

    public int getSize () {
        Iterator propertyIter = getProperties ().iterator ();
        int size = 0;
        while (propertyIter.hasNext ()) {
            String curPropertyName = (String) propertyIter.next ();
            List curPropertyList = (List) messages.get (curPropertyName);
            size += curPropertyList.size ();
        }
        return size;
    }

    public List getMessages () {
        List fullList = new ArrayList();
        Iterator propertyIter = getProperties ().iterator ();
        while (propertyIter.hasNext ()) {
            String curPropertyName = (String) propertyIter.next ();
            List curPropertyList = (List) messages.get (curPropertyName);
            fullList.addAll (curPropertyList);
        }
        return fullList;
    }

    public List getMessages (String property) {
        List propertyList = (List) messages.get (property);
        return propertyList;
    }

    public int getSize (String property) {
        List propertyList = (List) messages.get (property);
        if (propertyList != null) {
            return propertyList.size ();
        } else {
            return 0;
        }
    }

    public boolean isEmpty () {
        return messages.isEmpty ();
    }

    public void saveMessages (ServletRequest request) {
        request.setAttribute (CONTEXT_KEY, this);
    }

    /**
     * save message as contextPrefix + CONTEXT_KEY attribute
     *
     * @param contextPrefix String
     * @param request ServletRequest
     */
    public void saveMessages (String contextPrefix, ServletRequest request) {
        request.setAttribute (contextPrefix + CONTEXT_KEY , this);
    }

    public void saveMessages (PageContext pageContext) {
        pageContext.setAttribute (CONTEXT_KEY, this);
    }

    public void saveMessages (PageContext pageContext, int scope) {
        pageContext.setAttribute (CONTEXT_KEY, this, scope);
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("org.jahia.engines.EngineMessages : ");
        buff.append(messages);
        return buff.toString();
    }
    
    public ActionMessages toActionMessages() {
        ActionMessages msgs = new ActionMessages();
        for (String property : (Set<String>) getProperties()) {
            List<EngineMessage> messagesByProperty = getMessages(property);
            if (messagesByProperty != null) {
                for (EngineMessage engineMessage : messagesByProperty) {
                    msgs.add(property, engineMessage.toActionMessage());
                }
            }
        }
        return msgs;
    }
}
