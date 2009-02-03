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
