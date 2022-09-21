/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
public class HierarchicalResourceBundle extends ResourceBundle {

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
                if (lookupChain.get(0) != null) {
                    bundle = ResourceBundles.get(lookupChain.get(0), sourceLocale);
                } else {
                    bundle = NONEXISTENT_BUNDLE;
                }
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
                    nextBundle = ResourceBundles.get(nextBundleName, sourceLocale);
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

    /**
     * Get formatted message
     *
     * @param key
     * @param defaultValue
     * @param arguments
     * @return
     */
    public String getFormatted(String key, String defaultValue, Object... arguments) {
        return Messages.format(getString(key, defaultValue), arguments);
    }

    /**
     * Get message depending on a key. If not found, return the default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(String key, String defaultValue) {
        String message;
        try {
            message = getString(key);
        } catch (MissingResourceException e) {
            message = defaultValue;
        }
        return message;
    }


}
