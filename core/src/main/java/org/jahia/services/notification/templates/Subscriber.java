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
package org.jahia.services.notification.templates;

import org.jahia.services.usermanager.JahiaUser;

/**
 * The subscriber information bean, exposed into the Groovy template scope.
 * 
 * @author Sergiy Shyrkov
 */
public class Subscriber {

    private String email;

    private String firstName;

    private String fullName;

    private String secondName;

    private JahiaUser user;

    /**
     * Initializes an instance of this class.
     * 
     * @param firstName
     * @param secondName
     * @param fullName
     * @param email
     * @param user
     */
    public Subscriber(String firstName, String secondName, String fullName,
            String email, JahiaUser user) {
        super();
        this.firstName = firstName;
        this.secondName = secondName;
        this.fullName = fullName;
        this.email = email;
        this.user = user;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSecondName() {
        return secondName;
    }

    public JahiaUser getUser() {
        return user;
    }

}