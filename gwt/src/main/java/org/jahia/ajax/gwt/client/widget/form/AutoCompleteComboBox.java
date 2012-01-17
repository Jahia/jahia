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

package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;

import java.util.List;

/**
 * 
 * User: ktlili
 * Date: Feb 24, 2010
 * Time: 5:09:39 PM
 * 
 */
public class AutoCompleteComboBox extends ComboBox<GWTJahiaNode> {

    public AutoCompleteComboBox(final List<String> nodeType, final int maxResult) {
        this(nodeType, maxResult, "name");
    }

    public AutoCompleteComboBox(final List<String> nodeType, final int maxResult, final String field) {
        setDisplayField(field);
        final ListStore<GWTJahiaNode> store = new ListStore<GWTJahiaNode>(new BaseListLoader(
                new RpcProxy<List<GWTJahiaNode>>() {
                    @Override
                    protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaNode>> asyncCallback) {
                        if (field.equals("name")) {
                            JahiaContentManagementService.App.getInstance().searchSQL("select * from [" + nodeType.get(
                                    0) + "] as content where name(content) like '" + getRawValue() + "%'", maxResult,
                                    nodeType, null, null, null,true, asyncCallback);
                        } else {
                            JahiaContentManagementService.App.getInstance().searchSQL("select * from [" + nodeType.get(
                                    0) + "] as content where content.'"+field+"' like '" + getRawValue() + "%'", maxResult,
                                    nodeType, null, null, null,true, asyncCallback);
                        }
                    }
                }));
        setStore(store);
        setTypeAhead(true);
        setTypeAheadDelay(100);
        setTriggerAction(ComboBox.TriggerAction.ALL);
        setWidth(500);
        setMinChars(2);
        setQueryDelay(100);
    }

}
