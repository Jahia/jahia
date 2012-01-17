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

package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.EditContentEnginePopupListener;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.InfoLayers;
import org.jahia.ajax.gwt.client.widget.edit.ToolbarHeader;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionContextMenu;
import org.jahia.ajax.gwt.client.widget.toolbar.action.SiteSwitcherActionItem;

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

    private InfoLayers infoLayers = new InfoLayers();
    private Map<String, Boolean> activeLayers = new HashMap<String, Boolean>();

    Map<Element, Module> m;
    protected LayoutContainer scrollContainer;

    public MainModule(final String html, final String path, final String template, String nodeTypes, GWTEditConfiguration config) {
        super("main", path, template, nodeTypes, new FlowLayout());
        setScrollMode(Style.Scroll.NONE);

        this.id = "main";
        this.originalHtml = html;
        this.path = path;
        this.template = template;
        this.config = config;
        this.depth = 0;

        head = new ToolbarHeader();
        head.setText(Messages.get("label.page", "Page") + ": " + path);
        head.addStyleName("x-panel-header");
        head.setStyleAttribute("z-index", "999");
        head.setStyleAttribute("position", "relative");

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

        for (GWTJahiaToolbarItem item : config.getMainModuleToolbar().getGwtToolbarItems()) {
            ((ToolbarHeader)head).addItem(linker, item);
        }

        head.addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
                refresh(EditLinker.REFRESH_MAIN);
            }
        }));


        if ("".equals(Window.Location.getHash())) {
            display(originalHtml);
        } else {
            String hash = Window.Location.getHash();
            goToHashMarker(hash);
        }

        scrollContainer.sinkEvents(Event.ONCLICK + Event.ONDBLCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT);

        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                makeSelected();
            }
        };

        // on click listener
        scrollContainer.addListener(Events.OnClick, listener);

        // on double click listener
        scrollContainer.addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this, editLinker));

        if (config.getContextMenu() != null) {
            // contextMenu
            contextMenu = new ActionContextMenu(config.getContextMenu(), editLinker) {
                @Override
                public boolean beforeShow() {
                    makeSelected();

                    if (editLinker.getSelectionContext().getSingleSelection() == editLinker.getSelectionContext().getMainNode()) {
                        return false;
                    }
                    return super.beforeShow();
                }
            };
            scrollContainer.setContextMenu(contextMenu);

        }

        infoLayers.initWithLinker(linker);
    }

    /**
     * select current module
     */
    public void makeSelected() {
        if (selectable) {
            editLinker.onModuleSelection(null);
//            editLinker.onModuleSelection(MainModule.this);
        }
    }

    public EditLinker getEditLinker() {
        return editLinker;
    }

    public void refresh(int flag) {
        if ((flag & Linker.REFRESH_MAIN) != 0) {
            refresh(path, template, (flag & Linker.REFRESH_MAIN_IMAGES) != 0);
        }
    }

    private void refresh(final String previousPath, final String previousTemplate, final boolean forceImageRefresh) {
        JahiaContentManagementService.App.getInstance()
                .getRenderedContent(path, null, editLinker.getLocale(), template, "gwt", moduleParams, true,
                        config.getName(), new BaseAsyncCallback<GWTRenderResult>() {
                            public void onSuccess(GWTRenderResult result) {
                                int i = scrollContainer.getVScrollPosition();
                                head.setText("Page : " + path);
                                nodeTypes = result.getNodeTypes();
                                Selection.getInstance().hide();
                                Hover.getInstance().removeAll();
                                infoLayers.removeAll();

                                display(result.getResult(), forceImageRefresh);

                                scrollContainer.setVScrollPosition(i);
                                List<String> list = new ArrayList<String>(1);
                                list.add(path);
                                editLinker.getMainModule().unmask();
                                editLinker.onModuleSelection(MainModule.this);
                                switchStaticAssets(result.getStaticAssets());
                            }

                            @Override public void onApplicationFailure(Throwable caught) {
                                if (!previousPath.equals(path)) {
                                    path = previousPath;
                                    template = previousTemplate;
                                    editLinker.onMainSelection(previousPath, previousTemplate, null);
                                }
                                editLinker.getMainModule().unmask();
                            }
                        });

    }

    private Map<String,Integer> maxValues = new HashMap<String, Integer>();

    private void switchStaticAssets(Map<String, List<String>> assets) {
        switchStaticAssets(assets, "css", "link", "href");
        switchStaticAssets(assets, "javascript", "script", "src");
    }

    private void switchStaticAssets(Map<String, List<String>> assets, String fileType, String tagName, String tagAttribute) {
        List oldValues = new ArrayList();
        getAssets(tagName, tagAttribute, oldValues);
        Element head = (Element) getHead();
        List<String> newValues = assets.get(fileType);

        Integer maxValue = oldValues.size() + 1;

        if (!maxValues.containsKey(fileType)) {
            maxValues.put(fileType, maxValue);
        } else {
            maxValue = maxValues.get(fileType);
        }

        int j = 0;

        Element oldElement = null;
        Element lastElement = null;
        String oldValue = null;

        for (; newValues != null && (!newValues.isEmpty() || j<oldValues.size()) ; j++) {
            while (j < oldValues.size()) {
                oldElement = (Element) oldValues.get(j);
                oldValue = DOM.getElementAttribute(oldElement, tagAttribute);
                if (!newValues.contains(oldValue)) {
                    // Remove current element as it is not supposed to stay
                    head.removeChild(oldElement);
                    j++;
                } else {
                    lastElement = oldElement;
                    break;
                }
            }
            if (j < oldValues.size()) {
                if (!newValues.isEmpty()) {
                    String newValue = newValues.remove(0);
                    if (newValue.equals(oldValue)) {
                        // Elements are equal, don't change
                    } else {
                        Element newElem = createAsset(fileType, (maxValue++), newValue);
                        if (newElem != null) {
                            head.insertBefore(newElem, oldElement);
                        }
                        // Stay on current element for next comparison
                        j--;
                    }
                } else {
                    head.removeChild(oldElement);
                }
            } else {
                if (newValues.size() > 0) {
                    Element newElem = createAsset(fileType, j, newValues.remove(0));
                    if (newElem != null) {
                        if (lastElement != null) {
                            head.insertAfter(newElem, lastElement);
                            lastElement = newElem;
                        } else {
                            head.appendChild(newElem);
                        }
                    }
                }
            }
        }
        maxValues.put(fileType, maxValue);
    }

    private Element createAsset(String filetype, int j, String newValue) {
        Element newElem = null;
        if (filetype.equals("javascript")) {
            newElem = DOM.createElement("script");
            newElem.setAttribute("id","staticAsset"+filetype+j);
            newElem.setAttribute("type", "text/javascript");
            newElem.setAttribute("src", newValue);
        } else if (filetype.equals("css")) { //if filename is an external CSS file
            newElem = DOM.createElement("link");
            newElem.setAttribute("id","staticAsset"+filetype+j);
            newElem.setAttribute("rel", "stylesheet");
            newElem.setAttribute("type", "text/css");
            newElem.setAttribute("href", newValue);
        }

        return newElem;
    }

    private native int getAssets(String tagname, String attrname, List results) /*-{
        var links = $doc.getElementsByTagName(tagname);
        if (links != null) {
            for (var i=0; i<links.length; i++){
                if (links[i] && links[i].getAttribute("id")!=null && links[i].getAttribute("id").indexOf("staticAsset")==0) {
                    results.@java.util.List::add(Ljava/lang/Object;)(links[i])
                }
            }

            return links.length;
        } else {
            return 0;
        }

    }-*/;

    private native Object getHead() /*-{
        return $doc.getElementsByTagName("head")[0];
    }-*/;

    public static native void evalScripts(Element element) /*-{
        var scripts = element.getElementsByTagName("script");

        for (i=0; i < scripts.length; i++) {
            // if src, eval it, otherwise eval the body
            if (!scripts[i].hasAttribute("src")) {
                var src = scripts[i].getAttribute("src");
                var script = $doc.createElement('script');
                script.setAttribute("src", src);
                $doc.getElementsByTagName('body')[0].appendChild(script);
            } else {
                $wnd.eval(scripts[i].innerHTML);
            }
        }
    }-*/;

    public static native void setDocumentTitle(String title) /*-{
        $doc.title = title;
    }-*/;


    private void display(String result) {
        display(result, false);
    }

    private void display(String result, boolean forceImageReload) {
        scrollContainer.removeAll();
        scrollContainer.setScrollMode(Style.Scroll.AUTO);
        html = new HTML(result);
        if (forceImageReload) {
            refreshImages(html);
        }
        scrollContainer.add(html);
        ModuleHelper.tranformLinks(html);
        ModuleHelper.initAllModules(this, html);
        ModuleHelper.buildTree(this);
        long start = System.currentTimeMillis();
        parse();
        Log.info("Parse : "+(System.currentTimeMillis() - start));
        layout();
    }

    private void refreshImages(HTML html) {
        NodeList<com.google.gwt.dom.client.Element> elementsByTagName = html.getElement()
                .getElementsByTagName("img");
        if (elementsByTagName == null) {
            return;
        }
        String base = JahiaGWTParameters.getContextPath() + "/files/default/";
        String suffix = "tst=" + System.currentTimeMillis();
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            com.google.gwt.dom.client.Element el = elementsByTagName.getItem(i);
            String url = el.getAttribute("src");
            if (url != null && url.startsWith(base)) {
                el.setAttribute("src", url + (url.indexOf("?") == -1 ? "?" : "&") + suffix);
            }
        }
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

    public static void staticGoTo(String path, String template, String param) {
        module.editLinker.onMainSelection(path, template, param);
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

    public void switchLanguage(GWTJahiaLanguage language) {
        mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
        editLinker.setLocale(language);
        editLinker.refresh(Linker.REFRESH_MAIN + Linker.REFRESH_PAGES);
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
//        if (node.getNodeTypes().contains("jnt:page") || node.getInheritedNodeTypes().contains("jnt:page")) {
//            editManager.getEditLinker().getCreatePageButton().setEnabled(true);
//        }
        if (node.isShared()) {
//            this.setStyleAttribute("background","rgb(210,50,50) url("+ JahiaGWTParameters.getContextPath()+"/css/images/andromeda/rayure.png)");
            this.setToolTip(new ToolTipConfig(Messages.get("info_important", "Important"),
                    Messages.get("info_sharednode", "This is a shared node")));
        }
        if (node.getSiteUUID() != null && !JahiaGWTParameters.getSiteUUID().equals(node.getSiteUUID())) {
            JahiaGWTParameters.setSite(node, editLinker);
            SiteSwitcherActionItem.refreshAllSitesList(editLinker);
            editLinker.getSidePanel().refresh(EditLinker.REFRESH_ALL);
        }

        setDocumentTitle(Messages.get("label."+config.getName().substring(0,config.getName().length()-4), config.getName()) + " - " + node.getDisplayName());

        editLinker.handleNewMainNodeLoaded();
    }

    public GWTEditConfiguration getConfig() {
        return config;
    }

    public void handleNewModuleSelection(Module selectedModule) {
        Selection l = Selection.getInstance();
        l.hide();
        if (selectedModule != null && !(selectedModule instanceof MainModule)) {
            l.select(selectedModule);
            l.show();
        }
        ((ToolbarHeader)head).handleNewModuleSelection(selectedModule);

        l.layout();
    }

    public void handleNewMainSelection(String path, String template, String param) {
        Map<String,List<String>> params = null;
        if (param != null && param.length() > 0) {
            params = new HashMap<String,List<String>>();
            for (String s : param.split("&")) {
                final String[] key = s.split("=");
                String decodedKey = URL.decode(key[0]);
                if (!params.containsKey(decodedKey)) {
                    params.put(decodedKey, new ArrayList<String>());
                }
                params.get(decodedKey).add(URL.decode(key[1]));
            }
        }

        if ((this.path != null ? this.path.equals(path) : path == null) &&
                (this.template != null ? this.template.equals(template) : template == null) &&
                (this.moduleParams != null ? this.moduleParams.equals(params) : params == null)) {
            return;
        }

        String previousPath = this.path;
        String previousTemplate = this.template;

        this.path = path;
        this.template = template;

        moduleParams = params;

        module.mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
        setUrlMarker(path, template, param);
        module.refresh(previousPath, previousTemplate, false);

    }

    public void setInfoLayer(String key, boolean value) {
        if (value) {
            activeLayers.put(key, value);
        } else {
            activeLayers.remove(key);
        }
        refreshInfoLayer();
    }

    public void refreshInfoLayer() {
        infoLayers.removeAll();
        if (!activeLayers.isEmpty()) {
            infoLayers.setMainModule(this);

            List<Module> modules = ModuleHelper.getModules();

            String lastUnpublished = null;
            if (modules != null) {
                for (Module m : modules) {
                    if (m instanceof AreaModule || !m.getPath().endsWith("*")) {
                        GWTJahiaNode currentNode = m.getNode();
                        if (currentNode != null) {
                            List<LayoutContainer> images = new ArrayList<LayoutContainer>();
                            if (activeLayers.containsKey("acl") && currentNode.isHasAcl()) {
                                AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon(
                                        "viewACLStatus");
                                LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
                                layoutContainer.add(icon.createImage());
                                images.add(layoutContainer);
                            }
                            if (activeLayers.containsKey("publication")) {
                                GWTJahiaPublicationInfo info = currentNode.getAggregatedPublicationInfo();
                                if (lastUnpublished == null || !currentNode.getPath().startsWith(lastUnpublished)) {
                                    if (info.isLocked()) {
                                        AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon(
                                                "publication/locked");
                                        LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
                                        layoutContainer.add(icon.createImage());
                                        images.add(layoutContainer);
                                    }

                                    if (info.getStatus() == GWTJahiaPublicationInfo.NOT_PUBLISHED ||
                                        info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                                        lastUnpublished = currentNode.getPath();
                                        if (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                                            AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon(
                                                    "publication/unpublished");
                                            LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
                                            layoutContainer.add(icon.createImage());
                                            images.add(layoutContainer);
                                        } else {
                                            AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon(
                                                    "publication/notpublished");
                                            LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
                                            layoutContainer.add(icon.createImage());
                                            images.add(layoutContainer);
                                        }
                                    } else if (info.getStatus() == GWTJahiaPublicationInfo.MODIFIED) {
                                        AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon(
                                                "publication/modified");
                                        LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
                                        layoutContainer.add(icon.createImage());
                                        images.add(layoutContainer);
                                    } else if (info.getStatus() ==
                                               GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE) {
                                        AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon(
                                                "publication/mandatorylanguageunpublishable");
                                        LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
                                        layoutContainer.add(icon.createImage());
                                        images.add(layoutContainer);
                                    } else if (info.getStatus() == GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_VALID) {
                                        AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon(
                                                "publication/mandatorylanguagevalid");
                                        LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
                                        layoutContainer.add(icon.createImage());
                                        images.add(layoutContainer);
                                    }
                                }
                            }
                            if (activeLayers.containsKey("visibility")) {
                                Map<GWTJahiaNode, ModelData> visibility = currentNode.getVisibilityInfo();
                                if (!visibility.isEmpty()) {
                                    if (currentNode.isVisible()) {
                                        AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon(
                                                "visibilityStatusGreen");
                                        Image image = icon.createImage();
                                        LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
                                        layoutContainer.add(image);
                                        Set<Map.Entry<GWTJahiaNode, ModelData>> entries = visibility.entrySet();
                                        String toolTip = "";
                                        for (Map.Entry<GWTJahiaNode, ModelData> entry : entries) {
                                            if(!"".equals(toolTip)) {
                                                toolTip+="<br/>";
                                            }
                                            XTemplate tpl = XTemplate.create((String) entry.getValue().get(
                                                    "xtemplate"));
                                            toolTip += tpl.applyTemplate(com.extjs.gxt.ui.client.util.Util.getJsObject(
                                                    entry.getKey()));
                                        }
                                        layoutContainer.setToolTip(toolTip);
                                        images.add(layoutContainer);
                                    } else {
                                        AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon(
                                                "visibilityStatusRed");
                                        LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
                                        layoutContainer.add(icon.createImage());
                                        Set<Map.Entry<GWTJahiaNode, ModelData>> entries = visibility.entrySet();
                                        String toolTip = "";
                                        for (Map.Entry<GWTJahiaNode, ModelData> entry : entries) {
                                            if(!"".equals(toolTip)) {
                                                toolTip+="<br/>";
                                            }
                                            XTemplate tpl = XTemplate.create((String) entry.getValue().get(
                                                    "xtemplate"));
                                            toolTip += tpl.applyTemplate(com.extjs.gxt.ui.client.util.Util.getJsObject(
                                                    entry.getKey()));
                                        }
                                        layoutContainer.setToolTip(toolTip);
                                        images.add(layoutContainer);
                                    }
                                }
                            }
                            if (!images.isEmpty()) {
                                infoLayers.addInfoLayer(m, images, null, true, "1");
                            }
                        }
                    }
                }
            }
        }
    }


    public boolean isDraggable() {
        return false;
    }

    public static native void exportStaticMethod() /*-{
        $wnd.goTo = function(path,template,params) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::staticGoTo(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(path,template,params);
        }
    }-*/;

    public InfoLayers getInfoLayers() {
        return infoLayers;
    }
}
