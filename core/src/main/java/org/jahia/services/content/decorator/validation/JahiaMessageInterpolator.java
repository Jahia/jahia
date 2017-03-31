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
package org.jahia.services.content.decorator.validation;

import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.i18n.ResourceBundles;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.validation.MessageInterpolator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpolate a given constraint violation message using Jahia resource bundle.
 *
 * @author rincevent
 * @since JAHIA 7.0
 *        Created : 19/10/12
 */
public class JahiaMessageInterpolator implements MessageInterpolator {
    private transient static Logger logger = Logger.getLogger(JahiaMessageInterpolator.class);

    /**
     * The name of the default message bundle.
     */
    private static final String DEFAULT_VALIDATION_MESSAGES = "org.hibernate.validator.ValidationMessages";

    /**
     * Regular expression used to do message interpolation.
     */
    private static final Pattern MESSAGE_PARAMETER_PATTERN = Pattern.compile("(\\{[^\\}]+?\\})");

    /**
     * Interpolate the message template based on the constraint validation context.
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
     * Interpolate the message template based on the constraint validation context.
     * The <code>Locale</code> used is provided as a parameter.
     *
     * @param messageTemplate The message to interpolate.
     * @param context         contextual information related to the interpolation
     * @param locale          the locale targeted for the message
     * @return Interpolated error message.
     */
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        ResourceBundle resourceBundle = ResourceBundles.get(DEFAULT_VALIDATION_MESSAGES, locale);
        String key = messageTemplate.substring(1, messageTemplate.length() - 1);
        if (resourceBundle != null && resourceBundle.containsKey(key)) {
            return replaceAnnotationAttributes(resourceBundle.getString(key), context.getConstraintDescriptor().getAttributes());
        }
        final List<JahiaTemplatesPackage> availableTemplatePackages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages();
        Set<String> processedRB = new HashSet<String>();
        for (JahiaTemplatesPackage pkg : availableTemplatePackages) {
            String rbName = pkg.getResourceBundleName();
            if (rbName == null || processedRB.contains(rbName)) {
                continue;
            }
            processedRB.add(rbName);
            try {
                ResourceBundle rb = ResourceBundles.get(rbName, locale);
                if (rb != null && rb.containsKey(key)) {
                    return replaceAnnotationAttributes(rb.getString(key), context.getConstraintDescriptor()
                            .getAttributes());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        String msg = Messages.getInternal(key, locale, null);
        if (msg != null) {
            return replaceAnnotationAttributes(msg, context.getConstraintDescriptor().getAttributes());
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
