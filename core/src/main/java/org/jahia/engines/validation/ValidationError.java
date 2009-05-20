/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
