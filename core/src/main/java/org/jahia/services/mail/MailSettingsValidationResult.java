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
package org.jahia.services.mail;

/**
 * Mail settings validation result.
 * 
 * @author Sergiy Shyrkov
 */
public class MailSettingsValidationResult {
    public static final MailSettingsValidationResult SUCCESSFULL = new MailSettingsValidationResult();

    private Object[] args;

    private String messageKey;

    private String property;

    private boolean success = true;

    
    private MailSettingsValidationResult() {
        super();
    }

    public MailSettingsValidationResult(String property, String messageKey) {
        this(property, messageKey, null);
    }

    public MailSettingsValidationResult(String property,
            String messageKey, Object[] args) {
        super();
        this.success = false;
        this.property = property;
        this.messageKey = messageKey;
        this.args = args;
    }

    /**
     * Returns the args.
     * 
     * @return the args
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * Returns the messageKey.
     * 
     * @return the messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * Returns the property.
     * 
     * @return the property
     */
    public String getProperty() {
        return property;
    }

    /**
     * Returns the success.
     * 
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

}
