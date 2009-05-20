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
package org.jahia.ajax.gwt.client.service.acl;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * User: rfelden
 * Date: 27 nov. 2008 - 10:55:40
 */
public interface ACLServiceAsync {

    void getACL(int aclid, AsyncCallback<GWTJahiaNodeACL> async);

    void setACL(int aclid, GWTJahiaNodeACL acl, AsyncCallback async);

    void getACL(int aclid, boolean newAcl, String sessionIdentifier, AsyncCallback<GWTJahiaNodeACL> async);

    void setACL(int aclid, boolean newAcl, String sessionIdentifier, GWTJahiaNodeACL acl, AsyncCallback async);

}
