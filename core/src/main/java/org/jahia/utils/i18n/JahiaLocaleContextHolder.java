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
 *     Copyright (C) 2002-2021 Jahia Solutions Group. All rights reserved.
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
package org.jahia.utils.i18n;

import org.springframework.core.NamedThreadLocal;

import java.util.Locale;

public abstract class JahiaLocaleContextHolder {
    private static final ThreadLocal<Locale> localeContextHolder = new NamedThreadLocal<>("Jahia user Locale context");

    private JahiaLocaleContextHolder() {
    }

    public static void resetLocale() {
        localeContextHolder.remove();
    }

    public static Locale getLocale() {
        Locale locale = localeContextHolder.get();
        return locale != null ? locale : Locale.getDefault();
    }

    public static void setLocale(Locale locale) {
        localeContextHolder.set(locale);
    }
}
