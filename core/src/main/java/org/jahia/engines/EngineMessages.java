/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
 package org.jahia.engines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

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

    public Set<Map.Entry<String, List<EngineMessage>>> getEntrySet() {
        return messages.entrySet();
    }

    public int getSize() {
        Iterator<String> propertyIter = getProperties().iterator();
        int size = 0;
        while (propertyIter.hasNext()) {
            String curPropertyName = (String) propertyIter.next();
            List<EngineMessage> curPropertyList = messages.get(curPropertyName);
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
        List<EngineMessage> propertyList = messages.get(property);
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
        final StringBuilder buff = new StringBuilder();
        buff.append("org.jahia.engines.EngineMessages : ");
        buff.append(messages);
        return buff.toString();
    }

}
