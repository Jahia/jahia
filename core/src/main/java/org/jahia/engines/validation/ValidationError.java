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

package org.jahia.engines.validation;


/**
 * <p>Title: ValidationError</p>
 * <p>Description: The object of this class holds a validation error for a Jahia field.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ValidationError {

    private final Object source;
    private final String msgError;
    private String languageCode;
    // Used to link a ValidationError to a RessourceBundle message, so we can
    // easily use it in an EngineMessage.
    private final String ressourceBundleProp;
    private final String[] values;
    private boolean isBlocker = true;

    public ValidationError(Object newSource, String newMsgError) {
        this(newSource, newMsgError, null, null, true);
    }

    public ValidationError(Object newSource, String newMsgError,
                           String ressourceBundleProp, String[] values) {
        this(newSource, newMsgError, ressourceBundleProp, values, true);
    }

    public ValidationError(Object newSource, String newMsgError,
                           String ressourceBundleProp, String[] values, boolean isBlocker) {
        super();
        this.source = newSource;
        this.msgError = newMsgError;
        this.ressourceBundleProp = ressourceBundleProp;
        this.values = values;
        this.isBlocker = isBlocker;
    }

    public Object getSource() {
        return this.source;
    }

    public String getMsgError() {
        return this.msgError;
    }

    public String getRessourceBundleProp() {
        return ressourceBundleProp;
    }

    public String[] getValues() {
        return values;
    }

    public boolean isBlocker() {
        return isBlocker;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append(ValidationError.class.getName()).
                append(": Source: " + source).
                append(", Message: " + msgError).
                append(", RessourceBundleProp: " + ressourceBundleProp);
        return buff.toString();
    }
}
