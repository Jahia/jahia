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

//  JahiaUndefinedField
//  YG      17.07.2001

package org.jahia.data.fields;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.EntryLoadRequest;


public class JahiaUndefinedField extends JahiaField implements JahiaAllowApplyChangeToAllLangField {

    /**
     * constructor
     * YG    17.07.2001
     */
    public JahiaUndefinedField(Integer ID,
                               Integer jahiaID,
                               Integer pageID,
                               Integer ctnid,
                               Integer fieldDefID,
                               Integer fieldType,
                               Integer connectType,
                               String fieldValue,
                               Integer rank,
                               Integer aclID,
                               Integer versionID,
                               Integer versionStatus,
                               String languageCode) {
        super(ID, jahiaID, pageID, ctnid, fieldDefID, fieldType, connectType,
                fieldValue, rank, aclID, versionID, versionStatus, languageCode);

        if (isShared()) {
            this.languageCode = ContentField.SHARED_LANGUAGE;
        }

    } // end constructor


    public void load(int loadFlag, ProcessingContext jParams, EntryLoadRequest loadRequest)
            throws JahiaException {
    }

    public boolean save(ProcessingContext jParams)
            throws JahiaException {
        return true;
    }

    public String getEngineName() {
        return "org.jahia.engines.shared.Undefined_Field";
    }

    public String getFieldContent4Ranking() {
        return "";
    }

    public String getIconNameOff() {
        return "undefined";
    }

    public String getIconNameOn() {
        return "undefined_on";
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
     */
    public boolean isShared() {
        return true;
    }

    /**
     * Copy the internal value of current language to another language.
     * Must be implemented by conctrete field for specific implementation.
     *
     * @param aField A same field in another language
     */
    public void copyValueInAnotherLanguage(JahiaField aField, ProcessingContext jParams)
            throws JahiaException {
        if (aField == null) {
            return;
        }
        aField.setValue(this.getValue());
        aField.setRawValue(this.getRawValue());
        aField.setObject(this.getObject());
    }

}