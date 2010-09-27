/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.contentengine.EditContentEnginePopupListener;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionContextMenu;

import java.util.*;

/**
 * First module of any rendered element.
 * Sub content will be created as ListModule or SimpleModule.
 */
public class MainModule extends Module {

    private static MainModule module;
    private String originalHtml;
    private EditLinker editLinker;
    private ActionContextMenu contextMenu;
    private GWTEditConfiguration config;

    Map<Element, Module> m;
    protected LayoutContainer scrollContainer;

    public MainModule(final String html, final String path, final String template, GWTEditConfiguration config) {
        super("main", path, template, null, null, null, null, new FlowLayout());
        setScrollMode(Style.Scroll.NONE);

        this.id = "main";
        this.originalHtml = html;
        this.path = path;
        this.template = template;
        this.config = config;
        this.depth = 0;

        head = new Header();
        head.setText("Page : " + path);
        head.addStyleName("x-panel-header");
        head.setStyleAttribute("z-index", "999");
        head.setStyleAttribute("position", "relative");
        head.addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
                refresh(EditLinker.REFRESH_MAIN);
            }
        }));

        scrollContainer = new LayoutContainer(new FlowLayout());
        add(head);
        add(scrollContainer);
        scrollContainer.addStyleName("gwt-body-edit");
        Hover.getInstance().setMainModule(this);
        Selection.getInstance().setMainModule(this);

        module = this;
        exportStaticMethod();
    }

    public void initWithLinker(EditLinker linker) {
        this.editLinker = linker;
        if ("".equals(Window.Location.getHash())) {
            display(originalHtml);
        } else {
            String hash = Window.Location.getHash();
            goToHashMarker(hash);
        }

        sinkEvents(Event.ONCLICK + Event.ONDBLCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT);

        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                makeSelected();
            }
        };

        // on click listener
        addListener(Events.OnClick, listener);

        // on double click listener
        addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this, editLinker));

        if (config.getContextMenu() != null) {
            // contextMenu
            contextMenu = new ActionContextMenu(config.getContextMenu(), editLinker) {
                @Override
                public void beforeShow() {
                    makeSelected();
                    super.beforeShow();
                }
            };
            setContextMenu(contextMenu);

            addListener(Events.ContextMenu, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    editLinker.getSelectionContext().refresh(LinkerSelectionContext.MAIN_AREA_CONTEXT_MENU);
                }
            });
        }

    }

    /**
     * select current module
     */
    public void makeSelected() {
        if (selectable) {
            editLinker.onModuleSelection(MainModule.this);
        }
    }

    public EditLinker getEditLinker() {
        return editLinker;
    }

    public void refresh(int flag) {
        if ((flag & Linker.REFRESH_MAIN) != 0) {
            refresh();
        }
    }

    private void refresh() {
        JahiaContentManagementService.App.getInstance()
                .getRenderedContent(path, null, editLinker.getLocale(), template, "gwt", moduleParams, true,
                        config.getName(), new BaseAsyncCallback<GWTRenderResult>() {
                            public void onSuccess(GWTRenderResult result) {
                                int i = scrollContainer.getVScrollPosition();
                                head.setText("Page : " + path);

                                Selection.getInstance().hide();
                                Hover.getInstance().removeAll();
                                display(result.getResult());

                                scrollContainer.setVScrollPosition(i);
                                List<String> list = new ArrayList<String>(1);
                                list.add(path);
                                editLinker.getMainModule().unmask();
                                editLinker.onModuleSelection(MainModule.this);
                                editLinker.getSidePanel().refresh(Linker.REFRESH_WORKFLOW);
                                switchStaticAssets(result.getStaticAssets());
                            }

                        });

    }

    private int maxLink = -1;
    private int maxScript = -1;

    private void switchStaticAssets(Map<String, Set<String>> assets) {
        Set<String> values = assets.get("css");
        int m = removeAllAssets("link", "href", values);
        if (maxLink == -1) maxLink = m;
        for (String s : values) {
            addAsset("css", s, ++maxLink);
        }

        values = assets.get("javascript");
        m = removeAllAssets("script", "src", values);
        if (maxScript == -1) maxScript = m;
        for (String s : values) {
            addAsset("javascript", s, ++maxScript);
        }
    }

    private native int removeAllAssets(String tagname, String attrname, Set values) /*-{
        var links = $doc.getElementsByTagName(tagname);
        for (var i=links.length-1; i>=0; i--){ //search backwards within nodelist for matching elements to remove
            if (links[i] && links[i].getAttribute("id")!=null && links[i].getAttribute("id").indexOf("staticAsset")==0
                && !values.@java.util.Set::contains(Ljava/lang/Object;)(links[i].getAttribute(attrname))) {

                links[i].parentNode.removeChild(links[i]) //remove element by calling parentNode.removeChild()
            } else if (links[i]) {
                values.@java.util.Set::remove(Ljava/lang/Object;)(links[i].getAttribute(attrname))
            }
        }
        return links.length;
    }-*/;

    private native void addAsset(String filetype, String filename, int i) /*-{
        if (filetype=="javascript"){ //if filename is a external JavaScript file
            var fileref=$doc.createElement('script')
            fileref.setAttribute("id","staticAsset"+filetype+i)
            fileref.setAttribute("type","text/javascript")
            fileref.setAttribute("src", filename)
            $doc.getElementsByTagName("head")[0].appendChild(fileref)
        } else if (filetype=="css"){ //if filename is an external CSS file
            var fileref=$doc.createElement("link")
            fileref.setAttribute("id","staticAsset"+filetype+i)
            fileref.setAttribute("rel", "stylesheet")
            fileref.setAttribute("type", "text/css")
            fileref.setAttribute("href", filename)
            $doc.getElementsByTagName("head")[0].appendChild(fileref)
        }

    }-*/;

    private void display(String result) {
        scrollContainer.removeAll();
        scrollContainer.setScrollMode(Style.Scroll.AUTO);
        html = new HTML(result);
        scrollContainer.add(html);
        ModuleHelper.tranformLinks(html);
        ModuleHelper.initAllModules(this, html);
        ModuleHelper.buildTree(this);
        long start = System.currentTimeMillis();
        parse();
        Log.info("Parse : "+(System.currentTimeMillis() - start));
        layout();
    }

    @Override
    protected void onAfterLayout() {
        super.onAfterLayout();
        if (m != null) {
            ModuleHelper.move(m);
        }
        scrollContainer.setHeight(getHeight() - head.getOffsetHeight());
        scrollContainer.setWidth(getWidth());
    }

    protected void onResize(int width, int height) {
        super.onResize(width, height);
        scrollContainer.setHeight(getHeight() - head.getOffsetHeight());
        scrollContainer.setWidth(getWidth());
        if (editLinker.getSelectedModule() != null) {
            Selection.getInstance().hide();
            Selection.getInstance().show();
        }
    }

    public LayoutContainer getContainer() {
        return scrollContainer;
    }

    public void parse() {
        m = ModuleHelper.parse(this, null);
    }

    public String getModuleId() {
        return "main";
    }

    public void goTo(String path, String template) {
        mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
        this.path = path;
        this.template = template;
        setUrlMarker(path, template, "");
        refresh();
    }

    public static void staticGoTo(String path, String template, String param) {
        Map<String,String> params = null;
        if (param.length() > 0) {
            params = new HashMap<String,String>();
            for (String s : param.split("&")) {
                final String[] key = param.split("=");
                params.put(key[0], key[1]);
            }
        }
        module.mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
        module.path = path;
        module.template = template;
        module.moduleParams = params;
        setUrlMarker(path, template, param);
        module.refresh();
    }

    private static void setUrlMarker(String path, String template, String param) {
        String currentHref = Window.Location.getHref();
        if (currentHref.indexOf("#") > 0) {
            currentHref = currentHref.substring(0,currentHref.indexOf("#"));
        }
        Window.Location.assign(currentHref+"#"+ path + ":" + (template == null ? "" : template) + ":"+(param == null ? "" : param));
    }

    private void goToHashMarker(String hash) {
        int index = hash.indexOf(":");
        String url = hash.substring(1, index);
        int index2 = hash.indexOf(":", index + 1);
        String template = hash.substring(index+1, index2);
        String param = hash.substring(index2+1);
        staticGoTo(url, template, param);
    }
    
    public void switchLanguage(String language) {
        mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
        editLinker.setLocale(language);
        refresh();
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
        if (node.getNodeTypes().contains("jnt:page") || node.getInheritedNodeTypes().contains("jnt:page")) {
//            editManager.getEditLinker().getCreatePageButton().setEnabled(true);
        }
        if (node.isShared()) {
//            this.setStyleAttribute("background","rgb(210,50,50) url("+ JahiaGWTParameters.getContextPath()+"/css/images/andromeda/rayure.png)");
            this.setToolTip(new ToolTipConfig(Messages.get("info_important", "Important"),
                    Messages.get("info_sharednode", "This is a shared node")));
        }
        if (node.getSiteUUID() != null && !JahiaGWTParameters.getSiteUUID().equals(node.getSiteUUID())) {
            JahiaGWTParameters.setSiteUUID(node.getSiteUUID());
            JahiaGWTParameters.setSiteKey(node.getSiteKey());
            JahiaContentManagementService.App.getInstance().getGrantedPermissions(new BaseAsyncCallback<List<GWTJahiaPermission>>() {
                public void onSuccess(List<GWTJahiaPermission> result) {
                    PermissionsUtils.loadPermissions(result);
                }
            });
        }
    }

    public GWTEditConfiguration getConfig() {
        return config;
    }

    public void handleNewModuleSelection(Module selectedModule) {
        Selection l = Selection.getInstance();
        l.hide();
        if (selectedModule != null) {
            l.select(selectedModule);
            l.show();
        }
        l.layout();
    }

    public void handleNewSidePanelSelection(GWTJahiaNode node) {

    }

    public boolean isDraggable() {
        return false;
    }

    public static native void exportStaticMethod() /*-{
       $wnd.goTo = function(path,template,params) {
          @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::staticGoTo(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(path,template,params);
       }
    }-*/;


}
