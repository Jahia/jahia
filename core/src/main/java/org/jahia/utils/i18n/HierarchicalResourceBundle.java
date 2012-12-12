/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils.i18n;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Custom implementation of the property-based {@link ResourceBundle} that considers resource bundle of "dependent" modules in the lookup
 * chain.
 * 
 * @author Sergiy Shyrkov
 */
class HierarchicalResourceBundle extends ResourceBundle {

    private static final ResourceBundle NONEXISTENT_BUNDLE = new ResourceBundle() {
        public Enumeration<String> getKeys() {
            return null;
        }

        protected Object handleGetObject(String key) {
            return null;
        }

        public String toString() {
            return "NONEXISTENT_BUNDLE";
        }
    };

    private ResourceBundle bundle;

    private List<String> lookupChain;

    private Locale sourceLocale;

    HierarchicalResourceBundle(List<String> lookupChain, Locale sourceLocale) {
        super();
        this.lookupChain = lookupChain;
        this.sourceLocale = sourceLocale;
    }

    private ResourceBundle getBundle() {
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(lookupChain.get(0), sourceLocale,
                        JahiaResourceBundleControl.getInstance());
            } catch (MissingResourceException e) {
                bundle = NONEXISTENT_BUNDLE;
            }
        }

        return bundle;
    }

    @Override
    public Enumeration<String> getKeys() {
        return getBundle().getKeys();
    }

    @Override
    public Object handleGetObject(String key) {
        String value = null;
        MissingResourceException mre = null;
        ResourceBundle rb = getBundle();
        if (rb != NONEXISTENT_BUNDLE) {
            if (rb instanceof JahiaPropertyResourceBundle) {
                value = ((JahiaPropertyResourceBundle) rb).getStringInternal(key);
            } else {
                try {
                    value = rb.getString(key);
                } catch (MissingResourceException e) {
                    mre = e;
                }
            }
        }

        if (value != null) {
            return value;
        }

        if (lookupChain.size() > 1) {
            for (String nextBundleName : lookupChain.subList(1, lookupChain.size())) {
                ResourceBundle nextBundle = null;
                try {
                    nextBundle = ResourceBundle.getBundle(nextBundleName, sourceLocale,
                            JahiaResourceBundleControl.getInstance());
                } catch (MissingResourceException e) {
                    continue;
                }
                if (nextBundle instanceof JahiaPropertyResourceBundle) {
                    value = ((JahiaPropertyResourceBundle) nextBundle).getStringInternal(key);
                } else {
                    try {
                        value = nextBundle.getString(key);
                    } catch (MissingResourceException e) {
                        mre = e;
                    }
                }
                if (value != null) {
                    break;
                }
            }
        }

        if (value == null) {
            throw mre != null ? mre : new MissingResourceException("Cannot find resource " + key + " for bundle "
                    + lookupChain.get(0), lookupChain.get(0), key);
        }

        return value;
    }

    @Override
    protected void setParent(ResourceBundle parent) {
        // on parent support
        super.setParent(null);
    }
}
