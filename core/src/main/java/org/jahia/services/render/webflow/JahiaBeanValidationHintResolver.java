/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.services.render.webflow;

import org.jahia.osgi.BundleUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.validation.BeanValidationHintResolver;

import java.util.ArrayList;
import java.util.List;

public class JahiaBeanValidationHintResolver extends BeanValidationHintResolver {

    public Class<?>[] resolveValidationHints(Object model, String flowId, String stateId, String[] hints) throws FlowExecutionException {

        if (ObjectUtils.isEmpty(hints)) {
            return null;
        }

        List<Class<?>> result = new ArrayList<>();
        for (String hint : hints) {
            if (hint.equalsIgnoreCase("Default")) {
                hint = "javax.validation.groups.Default";
            }
            Class<?> resolvedHint = toClass(hint);
            if ((resolvedHint == null) && (model != null)) {
                resolvedHint = findInnerClass(model.getClass(), StringUtils.capitalize(hint));
            }
            if (resolvedHint == null) {
                resolvedHint = handleUnresolvedHint(model, flowId, stateId, hint);
            }
            if (resolvedHint != null) {
                result.add(resolvedHint);
            }
        }

        return result.toArray(new Class<?>[result.size()]);
    }

    private Class<?> toClass(String hint) {
        try {
            return BundleUtils.loadModuleClass(hint);
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return null;
    }

    private Class<?> findInnerClass(Class<?> targetClass, String hint) {
        try {
            return BundleUtils.loadModuleClass(targetClass.getName() + "$" + hint);
        } catch (ClassNotFoundException e) {
            Class<?> superClass = targetClass.getSuperclass();
            if (superClass != null) {
                return findInnerClass(superClass, hint);
            }
        }
        return null;
    }
}
