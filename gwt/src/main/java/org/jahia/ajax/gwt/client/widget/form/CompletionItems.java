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
package org.jahia.ajax.gwt.client.widget.form;

import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

import com.extjs.gxt.ui.client.store.ListStore;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 23 juil. 2008
 * Time: 11:31:18
 * To change this template use File | Settings | File Templates.
 */
public interface CompletionItems {

    public ListStore getCompletionItems(GWTJahiaPageContext jahiaPage, String match);

    public String getValueKey();

}
