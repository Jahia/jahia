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
package org.jahia.utils.i18n;

import org.drools.core.util.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.templates.JahiaModuleAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Message source implementation that uses current module's resource bundle for I18N messages.
 * 
 * @author Sergiy Shyrkov
 */
public class ModuleMessageSource implements MessageSource, JahiaModuleAware {

    private ConcurrentMap<Locale, ResourceBundle> bundles = null;

    private JahiaTemplatesPackage module;

    private boolean useCodeAsDefaultMessage;

    private ResourceBundle getBundle(Locale locale) {
        if (null == bundles) {
            bundles = new ConcurrentHashMap<Locale, ResourceBundle>(2);
        }

        ResourceBundle bundle = bundles.get(locale);
        if (null == bundle) {
            bundle = ResourceBundles.get(module, locale);
            bundles.put(locale, bundle);
        }

        return bundle;
    }

    private String getDefaultMessage(String code) {
        if (useCodeAsDefaultMessage) {
            return code;
        }
        return null;
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();
        if (codes == null) {
            codes = StringUtils.EMPTY_STRING_ARRAY;
        }
        for (String code : codes) {
            String msg = Messages.get(getBundle(locale), code, null);
            if (msg != null) {
                return Messages.format(msg, locale, resolveArguments(locale, resolvable.getArguments()));
            }
        }
        String defaultMessage = resolvable.getDefaultMessage();
        if (defaultMessage != null) {
            return Messages.format(defaultMessage, locale, resolveArguments(locale, resolvable.getArguments()));
        }
        if (codes.length > 0) {
            String fallback = getDefaultMessage(codes[0]);
            if (fallback != null) {
                return fallback;
            }
        }
        throw new NoSuchMessageException(codes.length > 0 ? codes[codes.length - 1] : null, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        try {
            return Messages.format(getBundle(locale).getString(code), locale, resolveArguments(locale, args));
        } catch (MissingResourceException e) {
            throw new NoSuchMessageException(code, locale);
        }
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return Messages.getWithArgs(getBundle(locale), code, defaultMessage, args);
    }

    private Object[] resolveArguments(Locale locale, Object... args) {
        if (args == null || args.length == 0 || !(args[0] instanceof MessageSourceResolvable)) {
            return args;
        }

        List<String> msgs = new LinkedList<String>();
        for (Object arg : args) {
            if (arg instanceof MessageSourceResolvable) {
                msgs.add(getMessage((MessageSourceResolvable) arg, locale));
            } else {
                msgs.add(arg != null ? arg.toString() : null);
            }
        }

        return msgs.toArray();
    }

    @Override
    public void setJahiaModule(JahiaTemplatesPackage module) {
        this.module = module;
    }

    /**
     * Set whether to use the message code as default message instead of throwing a NoSuchMessageException. Useful for development and
     * debugging. Default is "false".
     * <p>
     * Note: In case of a MessageSourceResolvable with multiple codes (like a FieldError) and a MessageSource that has a parent
     * MessageSource, do <i>not</i> activate "useCodeAsDefaultMessage" in the <i>parent</i>: Else, you'll get the first code returned as
     * message by the parent, without attempts to check further codes.
     * <p>
     * To be able to work with "useCodeAsDefaultMessage" turned on in the parent, AbstractMessageSource and AbstractApplicationContext
     * contain special checks to delegate to the internal {@link #getMessageInternal} method if available. In general, it is recommended to
     * just use "useCodeAsDefaultMessage" during development and not rely on it in production in the first place, though.
     */
    public void setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
        this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
    }
}
