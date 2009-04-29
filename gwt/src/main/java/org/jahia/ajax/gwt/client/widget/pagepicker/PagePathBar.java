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
package org.jahia.ajax.gwt.client.widget.pagepicker;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
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
                    public void onFailure (Throwable throwable) { }

                    public void onSuccess (Object o) {
                        com.google.gwt.user.client.Window.Location.reload();
                    }
                });
            }
        });
        TextToolItem item = new TextToolItem("loading...") ;
        item.setEnabled(false);
        languageSwitcher.init(item);
        m_component.add(item);
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
                    (!operation.equals("movePage") || (!parentPath.contains("/" + selection.getPid() + "/") && !selection.isLocked()))) {
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
