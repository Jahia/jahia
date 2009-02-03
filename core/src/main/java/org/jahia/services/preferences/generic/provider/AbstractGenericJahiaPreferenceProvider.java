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

package org.jahia.services.preferences.generic.provider;

import org.jahia.services.preferences.AbstractJahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreferenceKey;
import org.jahia.services.preferences.JahiaPreferenceValue;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedAttributeException;
import org.jahia.services.preferences.exception.JahiaPreferencesNotValidException;
import org.jahia.services.preferences.generic.GenericJahiaPreferenceKey;
import org.jahia.services.preferences.generic.GenericJahiaPreferenceValue;

/**
 * User: jahia
 * Date: 27 mars 2008
 * Time: 17:13:51
 */
public abstract class AbstractGenericJahiaPreferenceProvider extends AbstractJahiaPreferencesProvider {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractGenericJahiaPreferenceProvider.class);

    public JahiaPreferenceKey createEmptyJahiaPreferenceKey() {
        return new GenericJahiaPreferenceKey();
    }

    public JahiaPreferenceValue createEmptyJahiaPreferenceValue() {
        return new GenericJahiaPreferenceValue();
    }



    /**
     * @param jahiaPreferenceValue
     * @return true if the preference value is correct
     * @throws org.jahia.services.preferences.exception.JahiaPreferencesNotValidException
     *
     */
    public boolean validate(JahiaPreferenceValue jahiaPreferenceValue) throws JahiaPreferencesNotValidException {
        return true;
    }

    /**
     * Get the type of the provider. Each provider has a unique type.
     *
     * @return
     */
    public String getType() {
        return GenericJahiaPreferenceAPIProvider.PROVIDER_TYPE;
    }

    protected GenericJahiaPreferenceValue getAsGenericJahiaPreferenceValue(JahiaPreferenceValue jahiaPreferenceValue) {
        GenericJahiaPreferenceValue genericJahiaPreferenceValue;
        if (jahiaPreferenceValue instanceof GenericJahiaPreferenceValue) {
            genericJahiaPreferenceValue = (GenericJahiaPreferenceValue) jahiaPreferenceValue;
        } else {
            genericJahiaPreferenceValue = new GenericJahiaPreferenceValue();
            try {
                genericJahiaPreferenceValue.copy(jahiaPreferenceValue);
            } catch (JahiaPreferenceNotDefinedAttributeException e) {
                logger.error(e, e);
            }
        }
        return genericJahiaPreferenceValue;
    }

    protected GenericJahiaPreferenceKey getAsGenericJahiaPreferenceKey(JahiaPreferenceKey jahiaPreferenceKey) {
        GenericJahiaPreferenceKey layoutmanagerJahiaPreferenceKey;
        if (jahiaPreferenceKey instanceof GenericJahiaPreferenceKey) {
            layoutmanagerJahiaPreferenceKey = (GenericJahiaPreferenceKey) jahiaPreferenceKey;
        } else {
            layoutmanagerJahiaPreferenceKey = new GenericJahiaPreferenceKey();
            try {
                layoutmanagerJahiaPreferenceKey.copy(jahiaPreferenceKey);
            } catch (JahiaPreferenceNotDefinedAttributeException e) {
                logger.error(e, e);
            }
        }
        return layoutmanagerJahiaPreferenceKey;
    }
}
