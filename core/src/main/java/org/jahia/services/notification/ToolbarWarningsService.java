/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.notification;



import org.apache.commons.lang.StringUtils;
import org.jahia.utils.i18n.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Simple service used to fill potential warnings during startup/execution
 * return them as translated messages
 */
public class ToolbarWarningsService {


    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ToolbarWarningsService INSTANCE = new ToolbarWarningsService();
    }

    public static ToolbarWarningsService getInstance() {
        return Holder.INSTANCE;
    }

    List<String> messages;

    /**
     * Add a message as a resource key
     * @param message
     */
    public void addMessage(String message) {
        if (messages == null) {
            messages = new ArrayList<String>();
        }
        messages.add(message);
    }

    public String getMessagesAsString() {
        return messages != null?StringUtils.join(messages, "||"):"";
    }

    /**
     * return resources messages as a String using || as separator
     * @param locale
     * @return messages joined
     */
    public String getMessagesValueAsString(Locale locale) {
        if (messages == null) {
            return "";
        }
        List<String> translatedMessages = new ArrayList<String>();
        for (String m : messages) {
            translatedMessages.add(Messages.getInternal(m,locale,m));
        }
        return StringUtils.join(translatedMessages, "||");
    }

}
