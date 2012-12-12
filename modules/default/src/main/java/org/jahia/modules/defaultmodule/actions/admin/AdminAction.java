package org.jahia.modules.defaultmodule.actions.admin;

import org.jahia.bin.Action;
import org.jahia.utils.i18n.Messages;

import java.util.Locale;

/**
 * Abstract action for admin tasks
 */
public abstract class AdminAction extends Action {

    public String getMessage(Locale locale, String key) {
        return Messages.getInternal(key, locale);
    }

}
