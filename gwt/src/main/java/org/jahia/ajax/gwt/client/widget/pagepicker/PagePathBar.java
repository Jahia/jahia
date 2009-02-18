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

package org.jahia.ajax.gwt.client.widget.pagepicker;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaPageWrapper;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.service.JahiaServiceAsync;
import org.jahia.ajax.gwt.client.widget.language.LanguageSelectedListener;
import org.jahia.ajax.gwt.client.widget.language.LanguageSwitcher;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 3 sept. 2008
 * Time: 14:20:33
 * To change this template use File | Settings | File Templates.
 */
public class PagePathBar extends TopBar {

    private String parentPath;
    private String callback;
    private String operation;
    private ToolBar m_component ;
    private TextField pathTextField;

    public PagePathBar (String operation, String parentPath, String callback) {
        m_component = new ToolBar() ;
        m_component.add(new FillToolItem());
        pathTextField = new TextField() ;
        pathTextField.setId("sourcePageID");
        pathTextField.setWidth("200px");
        m_component.add(new AdapterToolItem(pathTextField));
        LanguageSwitcher languageSwitcher = new LanguageSwitcher(true, true, false, false, JahiaGWTParameters.getEngineLanguage(), true, new LanguageSelectedListener() {
            private final JahiaServiceAsync jahiaServiceAsync = JahiaService.App.getInstance();

            public void onLanguageSelected (String languageSelected) {
                jahiaServiceAsync.changeLocaleForCurrentEngine(languageSelected, new AsyncCallback() {
                    public void onFailure (Throwable throwable) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public void onSuccess (Object o) {
                        com.google.gwt.user.client.Window.Location.reload();
                    }
                });
            }

            public void onWorkflowSelected (String text) {
            }
        });
        m_component.add(new AdapterToolItem(languageSwitcher));
        this.callback = callback;
        this.parentPath = parentPath;
        this.operation = operation;
    }

    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                pathTextField.setName("sourcePageID");
            }
        });
    }

    public void handleNewSelection(Object leftTreeSelection, Object topTableSelection) {
        GWTJahiaPageWrapper selection = (GWTJahiaPageWrapper) topTableSelection ;

        if (selection == null) {
            pathTextField.reset();
        } else {
            Log.debug("Page "+selection.getLink()+" selected");
            Log.debug("parentPath " + parentPath);
            Log.debug("parentPID " + selection.getParentPid());
            if (selection.getPid() != 0 &&
                    (!operation.equals("movePage") || (!parentPath.contains("/" + selection.getPid() + "/") && !parentPath.endsWith("/" + selection.getParentPid() + "/") && !selection.isLocked()))) {
                pathTextField.setRawValue(String.valueOf(selection.getPid()));
                if ("SetUrl".equals(callback)) {
                    nativeSetUrl(selection.getLink());
                } else if ("setPid".equals(callback)) {
                    nativeSetPid(selection.getPid());
                } else {
                    nativeSetCustom(callback, selection.getPid(), selection.getLink(), selection.getTitle());
                }
            }
        }
    }

    public static native void nativeSetUrl(String url) /*-{
        $wnd.opener.SetUrl(url);
        $wnd.close();
    }-*/;

    public static native void nativeSetPid(int pid) /*-{
        $wnd.opener.setPid(pid);
        $wnd.close();
    }-*/;

    public Component getComponent() {
        return m_component;
    }

    public static native void nativeSetCustom(String callback, int pid, String url, String title) /*-{
        try {
            eval('$wnd.opener.' + callback)(pid, url, title);
        } catch (e) {};
        $wnd.close();
    }-*/;

}
