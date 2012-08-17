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

package org.jahia.services.mail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jahia.exceptions.JahiaException;

/**
 * Mail configuration values.
 * 
 * @author Sergiy Shyrkov
 */
public class MailSettings implements Serializable {

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

    private boolean configugationValid;

    private String from;

    private String uri;

    private String notificationLevel;

    private int notificationSeverity;

    private boolean serviceActivated;

    private String to;

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
     * Returns the host.
     * 
     * @return the host
     * @deprecated use {@link #getUri()} instead
     */
    @Deprecated
    public String getHost() {
        return uri;
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
    public boolean isConfigugationValid() {
        return configugationValid;
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
        this.configugationValid = configugationValid;
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
     * Sets the value of host.
     * 
     * @param host
     *            the host to set
     * @deprecated use {@link #setUri(String)} instead
     */
    @Deprecated
    public void setHost(String host) {
        setUri(host);
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
        if (uri.contains("@")) {
            uri = StringUtils.substringAfterLast(uri, "@");
        }
        if (uri.contains(":")) {
            String portPart = StringUtils.substringAfterLast(uri, ":");
            port = Integer.parseInt(StringUtils.substringBefore(portPart, "["));
        }

        return port;
    }

    public String getSmtpHost() {
        String uri = getUri();
        if (uri.contains("@")) {
            uri = StringUtils.substringAfterLast(uri, "@");
        }
        if (uri.contains(":")) {
            uri = StringUtils.substringBeforeLast(uri, ":");
        }

        return uri;
    }

    public Map<String, String> getOptions() {
        String uri = getUri();
        Map<String, String> options = new HashMap<String, String>();
        if (uri.contains("@")) {
            uri = StringUtils.substringAfterLast(uri, "@");
        }
        if (uri.contains(":")) {
            String portPart = StringUtils.substringAfterLast(uri, ":");
            // check if there are any custom options, e.g.
            // [mail.smtp.starttls.enable=true,mail.debug=true]
            String optionsPart = StringUtils.substringBetween(portPart, "[",
                    "]");
            if (optionsPart != null && optionsPart.length() > 0) {
                String props[] = StringUtils.split(optionsPart, ",");
                for (String theProperty : props) {
                    String keyValue[] = StringUtils.split(theProperty, "=");
                    options.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        if (getUser() != null) {
            options.put("mail.smtp.auth", "true");
        }

        return options;
    }

}