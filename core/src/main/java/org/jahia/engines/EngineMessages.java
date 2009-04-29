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

    Map<String, List<EngineMessage>> messages = new HashMap<String, List<EngineMessage>>();

    public EngineMessages() {
    }

    public void add(EngineMessage message) {
        add(GLOBAL_MESSAGE, message);
    }

    public void add(String property, EngineMessage message) {
        List<EngineMessage> propertyList = messages.get(property);
        if (propertyList == null) {
            propertyList = new ArrayList<EngineMessage>();
        }
        propertyList.add(message);
        messages.put(property, propertyList);
    }

    public Set<String> getProperties() {
        return messages.keySet();
    }

    public Set getEntrySet() {
        return messages.entrySet();
    }

    public int getSize() {
        Iterator propertyIter = getProperties().iterator();
        int size = 0;
        while (propertyIter.hasNext()) {
            String curPropertyName = (String) propertyIter.next();
            List curPropertyList = messages.get(curPropertyName);
            size += curPropertyList.size();
        }
        return size;
    }

    public List<EngineMessage> getMessages() {
        List<EngineMessage> fullList = new ArrayList<EngineMessage>();
        Iterator<String> propertyIter = getProperties().iterator();
        while (propertyIter.hasNext()) {
            String curPropertyName = propertyIter.next();
            List<EngineMessage> curPropertyList = messages.get(curPropertyName);
            fullList.addAll(curPropertyList);
        }
        return fullList;
    }

    public List<EngineMessage> getMessages(String property) {
        return messages.get(property);
    }

    public int getSize(String property) {
        List propertyList = messages.get(property);
        if (propertyList != null) {
            return propertyList.size();
        } else {
            return 0;
        }
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void saveMessages(ServletRequest request) {
        request.setAttribute(CONTEXT_KEY, this);
    }

    /**
     * save message as contextPrefix + CONTEXT_KEY attribute
     *
     * @param contextPrefix String
     * @param request ServletRequest
     */
    public void saveMessages(String contextPrefix, ServletRequest request) {
        request.setAttribute(contextPrefix + CONTEXT_KEY , this);
    }

    public void saveMessages(PageContext pageContext) {
        pageContext.setAttribute(CONTEXT_KEY, this);
    }

    public void saveMessages(PageContext pageContext, int scope) {
        pageContext.setAttribute(CONTEXT_KEY, this, scope);
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("org.jahia.engines.EngineMessages : ");
        buff.append(messages);
        return buff.toString();
    }
    
    public ActionMessages toActionMessages() {
        ActionMessages msgs = new ActionMessages();
        for (String property : getProperties()) {
            List<EngineMessage> messagesByProperty = getMessages(property);
            if (messagesByProperty != null) {
                for (EngineMessage engineMessage : messagesByProperty) {
                    msgs.add(property, engineMessage);
                }
            }
        }
        return msgs;
    }
}
