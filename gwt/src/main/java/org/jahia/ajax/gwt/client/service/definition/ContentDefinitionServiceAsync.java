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
package org.jahia.ajax.gwt.client.service.definition;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 25, 2008
 * Time: 6:20:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ContentDefinitionServiceAsync {


    public void getNodeType(String names, AsyncCallback<GWTJahiaNodeType> async);

    public void getNodeTypes(AsyncCallback<List<GWTJahiaNodeType>> async);

    public void getNodeTypes(List<String> names, AsyncCallback<List<GWTJahiaNodeType>> async);


}
