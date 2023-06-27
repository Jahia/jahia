/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.mail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.NameValuePair;
import org.jahia.exceptions.JahiaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mail configuration values.
 *
 * @author Sergiy Shyrkov
 */
public class MailSettings implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(MailSettings.class);

    private static final long serialVersionUID = -3891985143146442266L;

    private static final FastHashMap MAIL_NOTIFICATION_LEVELS;

    static {
        MAIL_NOTIFICATION_LEVELS = new FastHashMap(4);
        MAIL_NOTIFICATION_LEVELS.put("Disabled", new Integer(0));
        MAIL_NOTIFICATION_LEVELS.put("Standard", new Integer(
                JahiaException.CRITICAL_SEVERITY));
        MAIL_NOTIFICATION_LEVELS.put("Wary", new Integer(
                JahiaException.ERROR_SEVERITY));
        MAIL_NOTIFICATION_LEVELS.put("Paranoid", new Integer(
                JahiaException.WARNING_SEVERITY));
        MAIL_NOTIFICATION_LEVELS.setFast(true);
    }

    private boolean configurationValid;

    private String from;

    private String uri;

    private String notificationLevel;

    private int notificationSeverity;

    private boolean serviceActivated;

    private String to;

    private boolean workflowNotificationsDisabled;

    /**
     * Initializes an instance of this class.
     */
    public MailSettings() {
        super();
        setNotificationLevel("Disabled");
    }

    /**
     * Initializes an instance of this class.
     *
     * @param serviceEnabled
     *            is service enabled
     * @param uri
     *            the mail server connection URI
     * @param from
     *            sender address
     * @param to
     *            recipient address
     * @param notificationLevel
     *            event notification level
     */
    public MailSettings(boolean serviceEnabled, String uri, String from,
            String to, String notificationLevel) {
        super();
        this.serviceActivated = serviceEnabled;
        this.uri = uri;
        this.from = from;
        this.to = to;
        setNotificationLevel(notificationLevel);
    }

    /**
     * Returns the from.
     *
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns the notificationLevel.
     *
     * @return the notificationLevel
     */
    public String getNotificationLevel() {
        return notificationLevel;
    }

    /**
     * Returns the notification severity.
     *
     * @return the notification severity
     */
    public int getNotificationSeverity() {
        return notificationSeverity;
    }

    /**
     * Returns the to.
     *
     * @return the to
     */
    public String getTo() {
        return to;
    }

    /**
     * Returns the mail server connection URI.
     *
     * @return the mail server connection URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Returns the configugationValid.
     *
     * @return the configugationValid
     */
    public boolean isConfigurationValid() {
        return configurationValid;
    }

    /**
     * Returns the serviceEnabled.
     *
     * @return the serviceEnabled
     */
    public boolean isServiceActivated() {
        return serviceActivated;
    }

    /**
     * Sets the value of configugationValid.
     *
     * @param configugationValid
     *            the configugationValid to set
     */
    public void setConfigurationValid(boolean configugationValid) {
        this.configurationValid = configugationValid;
    }

    /**
     * Sets the value of from.
     *
     * @param from
     *            the from to set
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Sets the value of notificationLevel.
     *
     * @param notificationLevel
     *            the notificationLevel to set
     */
    public void setNotificationLevel(String notificationLevel) {
        this.notificationLevel = notificationLevel;
        this.notificationSeverity = ((Integer) MAIL_NOTIFICATION_LEVELS
                .get(notificationLevel)).intValue();
    }

    /**
     * Sets the value of serviceActivated.
     *
     * @param serviceActivated
     *            is the service activated
     */
    public void setServiceActivated(boolean serviceActivated) {
        this.serviceActivated = serviceActivated;
    }

    /**
     * Sets the value of to.
     *
     * @param to
     *            the to to set
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Sets the value of uri.
     *
     * @param uri
     *            the connection URI to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getUser() {
        String uri = getUri();
        String user = null;
        if (uri.contains("@")) {
            String authPart = StringUtils.substringBeforeLast(uri, "@");
            if (authPart.contains(":")) {
                user = StringUtils.substringBefore(authPart, ":");
            } else {
                user = authPart;
            }
        }

        return user;
    }

    public String getPassword() {
        String uri = getUri();
        String pwd = null;
        if (uri.contains("@")) {
            String authPart = StringUtils.substringBeforeLast(uri, "@");
            if (authPart.contains(":")) {
                pwd = StringUtils.substringAfter(authPart, ":");
            }
        }

        return pwd;
    }

    public int getPort() {
        String uri = getUri();
        int port = 0;
        try {
            URI parsedUri = new URI(uri);
            if (parsedUri.getPort() != -1) {
                port = parsedUri.getPort();
            }
        } catch (URISyntaxException e) {
            logger.error("Invalid uri", e);
        }
        return port;
    }

    public String getSmtpHost() {
        String uri = getUri();
        String smtpHost = "";
        try {
            URI parsedUri = new URI(uri);
            smtpHost = parsedUri.getHost();
        } catch (URISyntaxException e) {
            logger.error("Invalid uri", e);
        }
        return smtpHost;
    }

    public Map<String, String> getOptions() {
        String uri = getUri();
        Map<String, String> options = new HashMap<String, String>();

        try {
            URI parsedUri = new URI(uri);
            String query = parsedUri.getQuery();

            if (query != null) {
                List<NameValuePair> params = URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
                for (NameValuePair param : params) {
                    options.put(param.getName(), param.getValue());
                }
            }
        } catch (URISyntaxException e) {
            logger.error("Invalid uri", e);
        }

        if (getUser() != null) {
            options.put("mail.smtp.auth", "true");
        }

        return options;
    }

    /**
     * Returns the value of the flag for workflow tasks e-mail notifications.
     *
     * @return <code>true</code> if the e-mail notifications for workflow tasks are globally enabled; <code>false</code> if they are
     *         disabled globally
     */
    public boolean isWorkflowNotificationsDisabled() {
        return workflowNotificationsDisabled;
    }

    /**
     * Updates the flag for disabling e-mail notifications for workflow tasks.
     *
     * @param workflowNotificationsDisabled <code>true</code> to disable workflow task notifications; <code>false</code> to enable it
     */
    public void setWorkflowNotificationsDisabled(boolean workflowNotificationsDisabled) {
        this.workflowNotificationsDisabled = workflowNotificationsDisabled;
    }

}
