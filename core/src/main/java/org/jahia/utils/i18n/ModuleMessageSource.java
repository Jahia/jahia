/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.drools.core.util.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.templates.JahiaModuleAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

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
