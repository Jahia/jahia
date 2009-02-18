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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.service.UserManagerService;
import org.jahia.ajax.gwt.client.service.UserManagerServiceAsync;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 23 juil. 2008
 * Time: 11:25:19
 * To change this template use File | Settings | File Templates.
 */
public class UserCompletionItems implements CompletionItems {

    private UserManagerServiceAsync service;
    public UserCompletionItems() {
        service = UserManagerService.App.getInstance();
    }

    public ListStore<GWTJahiaUser> getCompletionItems(final GWTJahiaPageContext jahiaPage, final String match) {

        if (service == null){
            return null;
        }

        ListStore<GWTJahiaUser> result = null;
        MessageBox alertMsg = new MessageBox();
        try {
            // data proxy
            RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaUser>> proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaUser>>() {
                @Override
                protected void load(PagingLoadConfig pageLoaderResult,
                                    AsyncCallback<PagingLoadResult<GWTJahiaUser>> callback) {
                     service.searchUsers(match,pageLoaderResult.getOffset(), pageLoaderResult.getLimit(), null, callback);
                }
            };
            BasePagingLoader loader = new BasePagingLoader<PagingLoadConfig,
                    PagingLoadResult<GWTJahiaUser>>(proxy);
            result = new ListStore<GWTJahiaUser>(loader);
        } catch ( Throwable t ){
            alertMsg.setMessage(t.getMessage() + "_"  + t.toString());
            alertMsg.show();
            //fillTestData();
        }
        return result;
    }

    public String getValueKey() {
        return "userName";
    }

    /*
    private void fillTestData(){
        List<String> names = new ArrayList<String>();
        names.add("toto");
        names.add("toto1");
        names.add("toto3");
        names.add("toto2");
        names.add("toto5");
        usernames.put("to",names);
        usernames.put("tot",names);
        usernames.put("toto",names);
        names = new ArrayList<String>();
        names.add("toto1");
        usernames.put("toto1",names);

        names = new ArrayList<String>();
        names.add("titi");
        names.add("titi1");
        names.add("titi2");
        names.add("titi5");
        names.add("titi6");
        usernames.put("ti",names);
        usernames.put("tit",names);
        usernames.put("titi",names);

        names = new ArrayList<String>();
        names.add("root");
        usernames.put("ro",names);
        usernames.put("roo",names);
        usernames.put("root",names);

    }
    */
}
