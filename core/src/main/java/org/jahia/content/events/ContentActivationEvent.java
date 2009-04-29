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
 package org.jahia.content.events;

import java.util.*;

import org.jahia.content.ObjectKey;
import org.jahia.data.events.JahiaEvent;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;
import org.jahia.content.ContentObject;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */
public class ContentActivationEvent extends JahiaEvent {

    private JahiaUser user;
    private Set<String> languageCodes;
    private boolean versioningActive;
    private JahiaSaveVersion saveVersion;
    private StateModificationContext stateModifContext;
    private ActivationTestResults result;

    public ContentActivationEvent(Object source,
                                  ObjectKey objectKey,
                                  JahiaUser user,
                                  Set<String> languageCodes,
                                  boolean versioningActive,
                                  JahiaSaveVersion saveVersion,
                                  ProcessingContext jParams,
                                  StateModificationContext stateModifContext,
                                  ActivationTestResults result) {
        super(source, jParams, objectKey);
        this.user = user;
        this.languageCodes = languageCodes;
        this.versioningActive = versioningActive;
        this.saveVersion = saveVersion;
        this.stateModifContext = stateModifContext;
        this.result = result;
    }

    public JahiaUser getUser() {
        return user;
    }

    public Set<String> getLanguageCodes() {
        return languageCodes;
    }

    public boolean getVersioningActive() {
        return versioningActive;
    }

    public JahiaSaveVersion getSaveVersion() {
        return saveVersion;
    }

    public StateModificationContext getStateModifContext() {
        return stateModifContext;
    }

    public ActivationTestResults getActivationTestResults() {
        return result;
    }

    public ObjectKey getObjectKey() {
        return (ObjectKey)getObject();
    }

    public ContentObject getContentObject() {
        return (ContentObject)getSource();
    }

}
