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
        return StringUtils.join(messages, "||");
    }

    /**
     * return resources messages as a String using || as separator
     * @param locale
     * @return messages joined
     */
    public String getMessagesValueAsString(Locale locale) {
        List<String> translatedMessages = new ArrayList<String>();
        for (String m : messages) {
            translatedMessages.add(Messages.getInternal(m,locale,m));
        }
        return StringUtils.join(translatedMessages, "||");
    }

}
