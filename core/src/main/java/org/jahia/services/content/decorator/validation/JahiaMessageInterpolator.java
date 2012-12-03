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
package org.jahia.services.content.decorator.validation;

import org.apache.log4j.Logger;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.i18n.JahiaTemplatesRBLoader;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.validation.MessageInterpolator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpolate a given constraint violation message using Jahia resource bundle.
 *
 * @author rincevent
 * @since : JAHIA 6.7
 *        Created : 19/10/12
 */
public class JahiaMessageInterpolator implements MessageInterpolator {
    private transient static Logger logger = Logger.getLogger(JahiaMessageInterpolator.class);

    /**
     * Regular expression used to do message interpolation.
     */
    private static final Pattern MESSAGE_PARAMETER_PATTERN = Pattern.compile("(\\{[^\\}]+?\\})");

    /**
     * Interpolate the message template based on the contraint validation context.
     * The locale is defaulted according to the <code>MessageInterpolator</code>
     * implementation. See the implementation documentation for more detail.
     *
     * @param messageTemplate The message to interpolate.
     * @param context         contextual information related to the interpolation
     * @return Interpolated error message.
     */
    public String interpolate(String messageTemplate, Context context) {
        return interpolate(messageTemplate, context, LocaleContextHolder.getLocale());
    }

    /**
     * Interpolate the message template based on the contraint validation context.
     * The <code>Locale</code> used is provided as a parameter.
     *
     * @param messageTemplate The message to interpolate.
     * @param context         contextual information related to the interpolation
     * @param locale          the locale targeted for the message
     * @return Interpolated error message.
     */
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        ResourceBundle resourceBundle = JahiaResourceBundle.lookupBundle(
                ResourceBundleMessageInterpolator.DEFAULT_VALIDATION_MESSAGES, locale, null, false);
        String key = messageTemplate.substring(1, messageTemplate.length() - 1);
        if (resourceBundle != null && resourceBundle.containsKey(key)) {
            return replaceAnnotationAttributes(resourceBundle.getString(key), context.getConstraintDescriptor().getAttributes());
        }
        final List<JahiaTemplatesPackage> availableTemplatePackages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages();
        List<String> processedRB = new ArrayList<String>(availableTemplatePackages.size() * 2);
        for (JahiaTemplatesPackage availableTemplatePackage : availableTemplatePackages) {
            final List<String> resourceBundleHierarchy = availableTemplatePackage.getResourceBundleHierarchy();
            for (String resourceBundleName : resourceBundleHierarchy) {
                if (!processedRB.contains(resourceBundleName)) {
                    final JahiaTemplatesRBLoader instance = JahiaTemplatesRBLoader.getInstance(
                            Thread.currentThread().getContextClassLoader(), availableTemplatePackage.getName());
                    try {
                        resourceBundle = JahiaResourceBundle.lookupBundle(resourceBundleName, locale, instance, false);
                        processedRB.add(resourceBundleName);
                        if (resourceBundle != null && resourceBundle.containsKey(key)) {
                            return replaceAnnotationAttributes(resourceBundle.getString(key), context.getConstraintDescriptor().getAttributes());
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }

        return messageTemplate;
    }

    private String replaceVariables(String message, ResourceBundle bundle, Locale locale, boolean recurse) {
        Matcher matcher = MESSAGE_PARAMETER_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        String resolvedParameterValue;
        while (matcher.find()) {
            String parameter = matcher.group(1);
            resolvedParameterValue = resolveParameter(parameter, bundle, locale, recurse);

            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolvedParameterValue));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String replaceAnnotationAttributes(String message, Map<String, Object> annotationParameters) {
        Matcher matcher = MESSAGE_PARAMETER_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String resolvedParameterValue;
            String parameter = matcher.group(1);
            Object variable = annotationParameters.get(removeCurlyBrace(parameter));
            if (variable != null) {
                if (variable.getClass().isArray()) {
                    resolvedParameterValue = Arrays.toString((Object[]) variable);
                } else {
                    resolvedParameterValue = variable.toString();
                }
            } else {
                resolvedParameterValue = parameter;
            }
            resolvedParameterValue = Matcher.quoteReplacement(resolvedParameterValue);
            matcher.appendReplacement(sb, resolvedParameterValue);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveParameter(String parameterName, ResourceBundle bundle, Locale locale, boolean recurse) {
        String parameterValue;
        try {
            if (bundle != null) {
                parameterValue = bundle.getString(removeCurlyBrace(parameterName));
                if (recurse) {
                    parameterValue = replaceVariables(parameterValue, bundle, locale, recurse);
                }
            } else {
                parameterValue = parameterName;
            }
        } catch (MissingResourceException e) {
            // return parameter itself
            parameterValue = parameterName;
        }
        return parameterValue;
    }

    private String removeCurlyBrace(String parameter) {
        return parameter.substring(1, parameter.length() - 1);
    }
}
