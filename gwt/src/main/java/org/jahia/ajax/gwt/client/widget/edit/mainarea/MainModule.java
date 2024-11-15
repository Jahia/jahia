/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.core.client.*;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.WindowUtil;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.DeleteItemWindow;
import org.jahia.ajax.gwt.client.widget.content.util.ContentHelper;
import org.jahia.ajax.gwt.client.widget.contentengine.*;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.InfoLayers;
import org.jahia.ajax.gwt.client.widget.edit.ToolbarHeader;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionContextMenu;
import org.jahia.ajax.gwt.client.widget.toolbar.action.LanguageSwitcherActionItem;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowDashboardEngine;

import java.util.*;

/**
 * First module of any rendered element.
 * Sub content will be created as ListModule or SimpleModule.
 */
public class MainModule extends Module {

    private static final String[] RESERVED_REQUESTPARAMETERS = new String[]{"channel", "variant"};

    private static MainModule module;

    private EditLinker editLinker;
    private Storage storage;
    private ActionContextMenu contextMenu;
    private GWTEditConfiguration config;
    private Map<String, GWTEditConfiguration> configs = new HashMap<String, GWTEditConfiguration>();

    private InfoLayers infoLayers = new InfoLayers();
    private Map<String, Boolean> activeLayers = new HashMap<String, Boolean>();
    private boolean ctrlActive = false;
    private static Map<Module, Selection> selections = new HashMap<Module, Selection>();

    private boolean needParseAfterLayout = false;
    private Map<Element, Module> moduleMap;
    protected LayoutContainer scrollContainer;
    protected LayoutContainer center;
    protected EditFrame frame;
    private Element mainModuleElement;
    protected String frameError = null;
    protected boolean frameErrorRedirect = false;

    private final LayoutContainer headContainer;
    private String newLocation = null;
    private boolean firstLoad = true;
    protected String template;

    private GWTJahiaChannel activeChannel;
    private String activeChannelVariant;
    private static boolean globalSelectionDisabled = false;

    protected Point framePosition = new Point(0, 0);

    public MainModule(final String path, final String template, String nodeTypes, GWTEditConfiguration config) {
        super("main", path, nodeTypes, new BorderLayout());
        setScrollMode(Style.Scroll.NONE);
        addStyleName("mainmodule");
        this.id = "main";
        this.path = path;
        this.template = template;
        this.config = config;
        configs.put(this.config.getName(), this.config);

        this.depth = 0;

        storage = Storage.getSessionStorageIfSupported();
        if (storage != null) {
            String user = storage.getItem("currentUser");
            if (user != null && !user.equals(JahiaGWTParameters.getCurrentUser())) {
                storage.clear();
            }
            storage.setItem("currentUser", JahiaGWTParameters.getCurrentUser());
        }
        head = new ToolbarHeader();
        head.addStyleName("x-panel-header");
        head.setStyleAttribute("z-index", "999");
        head.setStyleAttribute("position", "relative");
        headContainer = new LayoutContainer(new FitLayout());
        headContainer.addStyleName("mainmodule-head-container");
        headContainer.add(head);
        String fullscreen = com.google.gwt.user.client.Window.Location.getParameter("fullscreen");
        if (fullscreen == null) {
            add(headContainer, new BorderLayoutData(Style.LayoutRegion.NORTH, 32));
            if (config.getMainModuleToolbar() == null || config.getMainModuleToolbar().getGwtToolbarItems().isEmpty()) {
                headContainer.hide();
            }
        }

        frame = new EditFrame();
        frame.setWidth("100%");
        frame.setHeight("100%");
        frame.addStyleName("x-noshim");
        frame.sinkEvents(Event.ONLOAD /*+ Event.ONCLICK + Event.ONCONTEXTMENU + Event.ONMOUSEOVER + Event.ONMOUSEOUT*/);


        scrollContainer = new LayoutContainer(new FlowLayout()) {
            @Override
            protected void onRightClick(ComponentEvent ce) {
                if (contextMenu != null && fireEvent(Events.ContextMenu, ce)) {
                    Point position = MainModule.getInstance().getContainer().getPosition(false);
                    final int x = ce.getClientX() + position.x;
                    final int y = ce.getClientY() + position.y;
                    ce.stopEvent();
                    DeferredCommand.addCommand(new Command() {

                        @Override
                        public void execute() {
                            onShowContextMenu(x, y);
                        }
                    });
                }
            }

        };
        scrollContainer.addStyleName("gwt-body-edit");
        scrollContainer.setStyleAttribute("position", "relative");
        scrollContainer.addListener(Events.OnClick, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                if (contextMenu != null && contextMenu.isVisible()) {
                    contextMenu.hide();
                }
            }
        });
        scrollContainer.add(frame);

        center = new LayoutContainer(new FitLayout());
        center.setScrollMode(Style.Scroll.NONE);

        add(center, new BorderLayoutData(Style.LayoutRegion.CENTER));

        Hover.getInstance().setMainModule(this);

        module = this;
        exportStaticMethod();
    }

    boolean inframe = false;

    public boolean isInframe() {
        return inframe;
    }

    public static boolean isGlobalSelectionDisabled() {
        return globalSelectionDisabled;
    }

    public Map<Module, Selection> getSelections() {
        return selections;
    }

    public GWTConfiguration.DragAndDropBehavior getDragAndDropBehavior() {
        return config.getDragAndDropBehavior();
    }

    public void initWithLinker(EditLinker linker) {
        this.editLinker = linker;

        ((ToolbarHeader) head).removeAllTools();
        if (config.getMainModuleToolbar() != null && !config.getMainModuleToolbar().getGwtToolbarItems().isEmpty()) {
            for (GWTJahiaToolbarItem item : config.getMainModuleToolbar().getGwtToolbarItems()) {
                ((ToolbarHeader) head).addItem(linker, item);
            }
            ToolButton refresh = new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {

                @Override
                public void componentSelected(IconButtonEvent event) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(Linker.REFRESH_MAIN, true);
                    refresh(data);
                }
            });
            head.addTool(refresh);
            refresh.addStyleName("window-actions-refresh");

            ((ToolbarHeader) head).attachTools();

            headContainer.add(head);
            headContainer.show();
        } else {
            headContainer.hide();
        }


        String location = newLocation;
        newLocation = null;
        String hash = Window.Location.getHash();
        if (location == null && !hash.equals("") && hash.contains("|")) {
            location = hash.substring(hash.indexOf('|') + 1);
        }
        if (location == null) {
            location = Window.Location.getPath();
            String queryString = Window.Location.getQueryString();
            if (queryString != null && !queryString.equals("")) {
                location += queryString;
            }
        }
        if (location.contains("://")) {
            location = location.substring(location.indexOf("://") + 3);
            location = location.substring(location.indexOf("/"));
        }
        if (location.startsWith(JahiaGWTParameters.getContextPath() + config.getDefaultUrlMapping() + "/")) {
            location = location.replaceFirst(config.getDefaultUrlMapping(), config.getDefaultUrlMapping() + "frame");
        }
        if (location.contains("frame/") && !isValidUrl(location)) {
            String start = location.substring(0, location.indexOf("frame/"));
            start = start.substring(JahiaGWTParameters.getContextPath().length());
            location = location.replaceFirst(start + "frame/", config.getDefaultUrlMapping() + "frame/");
        }

        resetFramePosition();

        goToUrl(URL.decode(location), true, true, true);

//        scrollContainer.sinkEvents();

        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent ce) {
                setCtrlActive(ce);
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
                        Module selectedModule = editLinker.getSelectedModule();
                        return selectedModule != null && selectedModule instanceof SimpleModule && super.beforeShow();
                    }
                    return super.beforeShow();
                }
            };
            scrollContainer.setContextMenu(contextMenu);

        }

        infoLayers.initWithLinker(linker);
    }

    private boolean isValidUrl(String url) {
        return url.startsWith(JahiaGWTParameters.getContextPath() + config.getDefaultUrlMapping() + "frame/");
    }

    private void layoutChannel() {
        center.removeAll();


        if (activeChannel == null || "generic".equals(activeChannel.getValue()) || !config.isSupportChannelsDisplay()) {
            scrollContainer.setPosition(0, 0);
            center.setLayout(new FitLayout());
            center.setScrollMode(Style.Scroll.NONE);
            center.add(scrollContainer);
        } else {
            int variantIndex = activeChannel.getVariants().indexOf(activeChannelVariant);
            if (variantIndex == -1) {
                variantIndex = 0;
            }

            // first let setup the device decorator layout container
            LayoutContainer deviceDecoratorContainer = new LayoutContainer(new AbsoluteLayout());
            deviceDecoratorContainer.setBorders(false);
            int[] decoratorImageSize = activeChannel.getVariantDecoratorImageSize(variantIndex);
            if (decoratorImageSize.length == 0) {
                decoratorImageSize = new int[]{-1, -1};
            }
            deviceDecoratorContainer.setSize(decoratorImageSize[0], decoratorImageSize[1]);
            deviceDecoratorContainer.setStyleAttribute("margin-left", "auto");
            deviceDecoratorContainer.setStyleAttribute("margin-right", "auto");
            AbsoluteData deviceOuterData = new AbsoluteData(0, 0);
            deviceOuterData.setMargins(new Margins(0, 0, 0, 0));
            if (activeChannel.getVariantDecoratorImage(variantIndex) != null) {
                deviceDecoratorContainer.add(new Image(JahiaGWTParameters.getContextPath() + activeChannel.getVariantDecoratorImage(variantIndex)), deviceOuterData);
            }

            int[] usableResolution = getUsableDeviceResolution(activeChannel, variantIndex);
            scrollContainer.setSize(usableResolution[0], usableResolution[1]);
            scrollContainer.setScrollMode(Style.Scroll.NONE);

            int[] screenPosition = null;
            screenPosition = activeChannel.getVariantDecoratorScreenPosition(variantIndex);
            if (screenPosition == null || screenPosition.length == 0) {
                screenPosition = new int[]{0, 0};
            }

            AbsoluteData deviceData = new AbsoluteData(screenPosition[0], screenPosition[1]);
            deviceData.setMargins(new Margins(0, 0, 0, 0));
            deviceDecoratorContainer.add(scrollContainer, deviceData);

            center.setLayout(new FlowLayout());
            center.setScrollMode(Style.Scroll.AUTO);
            center.add(deviceDecoratorContainer);
        }

//        layout();

    }

    private int[] getUsableDeviceResolution(GWTJahiaChannel activeChannel, int activeChannelIndex) {
        int[] usableResolution;
        if (activeChannel != null) {
            usableResolution = activeChannel.getVariantUsableResolution(activeChannelIndex);
            if (usableResolution.length == 0) {
                usableResolution = new int[]{
                        -1,
                        -1
                };
            }
        } else {
            usableResolution = new int[]{
                    getWidth(),
                    getHeight() - (head != null ? head.getOffsetHeight() : 0)
            };
        }
        return usableResolution;
    }

    /**
     * select current module
     */
    public void makeSelected() {
        if (selectable && !MainModule.isGlobalSelectionDisabled()) {
            editLinker.onModuleSelection(null);
        }
    }

    /**
     * Returns an instance of the edit linker.
     *
     * @return an instance of the edit linker
     */
    public EditLinker getEditLinker() {
        return editLinker;
    }

    /**
     * Performs refresh of the main module, based on the provided data.
     *
     * @param data the refresh data
     */
    public void refresh(Map<String, Object> data) {

        if (!config.isRefreshEnabled()) {
            return;
        }
        if (data != null && (data.containsKey(Linker.REFRESH_ALL) || data.containsKey(Linker.REFRESH_MAIN) || needRefresh(data))) {
            saveCurrentFramePosition();
            boolean forceCssRefresh = false;
            boolean forceJavascriptRefresh = false;
            String newPath = null;
            if (data.containsKey("node")) {
                GWTJahiaNode n = (GWTJahiaNode) data.get("node");
                if (n != null) {
                    newPath = n.getPath();
                    forceCssRefresh = n.getNodeTypes().contains("jnt:cssFile");
                    forceJavascriptRefresh = n.getNodeTypes().contains("jnt:javascriptFile");
                }
            }
            boolean isMainDeleted = newPath != null && data.containsKey(Linker.MAIN_DELETED) && (Boolean) data.get(Linker.MAIN_DELETED);
            // we preserve the available query string during refresh of main area
            // change the url to the parent path if main url has been deleted
            final String url = getUrl(isMainDeleted ? newPath : path, template, activeChannel != null ? activeChannel.getValue() : null, activeChannelVariant, true);
            goToUrl(url, data.containsKey("forceImageRefresh"), forceCssRefresh, forceJavascriptRefresh);
        }
    }

    public boolean needRefresh(Map<String, Object> data) {

        // refresh if node is modified
        GWTJahiaNode node = (GWTJahiaNode) data.get("node");
        if (node != null && node.getPath().equals(path)) {
            return true;
        }

        // refresh if main node has been deleted
        if (data.containsKey(Linker.MAIN_DELETED) && (Boolean) data.get(Linker.MAIN_DELETED)) {
            return true;
        }

        @SuppressWarnings("unchecked") List<String> paths = (List<String>) data.get("publishedNodes");
        if (paths != null) {
            for (String s : paths) {
                List<Module> modules = ModuleHelper.getModulesByPath().get(s);
                if (modules != null && !modules.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Changes the frame URL to the provided one.
     *
     * @param url                    the URL to be used for the frame
     * @param forceImageRefresh      should we force refresh of the images?
     * @param forceCssRefresh        should we force refresh of the CSS files?
     * @param forceJavascriptRefresh should we force refresh of the JavaScript files?
     */
    public void goToUrl(final String url, final boolean forceImageRefresh, boolean forceCssRefresh, boolean forceJavascriptRefresh) {
        mask(Messages.get("label.loading", "Loading..."), "x-mask-loading");
        setChannelFromUrl(url);
        layoutChannel();
        frame.setForceImageRefresh(forceImageRefresh);
        frame.setForceCssRefresh(forceCssRefresh);
        frame.setForceJavascriptRefresh(forceJavascriptRefresh);
        frame.setUrl(url);
        // Defer layout
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                center.layout(true);
            }
        });
    }

    public void goToSettingsUrl(final String url) {
        boolean onlyHashHasChanged = false;
        if (url.contains("#") && frame.url.contains("#")) {
            onlyHashHasChanged = (frame.url.contains("#") ? frame.url.substring(0, frame.url.indexOf('#')) : frame.url).equals(url.contains("#") ? url.substring(0, url.indexOf('#')) : url);
        }
        if (!onlyHashHasChanged) {
            goToUrl(url, false, false, false);
        } else {
            frame.setUrl(url);
        }
    }

    private void setChannelFromUrl(String url) {
        if (config.isSupportChannelsDisplay()) {
            activeChannel = editLinker.getActiveChannel();
            activeChannelVariant = editLinker.getActiveChannelVariant();

            Map<String, String> params = getParamsFromUrl(url);
            if (params.containsKey("channel")) {
                String channelName = params.get("channel");
                for (GWTJahiaChannel gwtJahiaChannel : JahiaGWTParameters.getChannels()) {
                    if (gwtJahiaChannel.getValue().equals(channelName)) {
                        this.activeChannel = gwtJahiaChannel;
                        this.activeChannelVariant = null;
                        break;
                    }
                }

                if (params.containsKey("variant")) {
                    activeChannelVariant = params.get("variant");
                }
            }
        } else {
            activeChannel = null;
            activeChannelVariant = null;
        }
    }

    private Map<String, String> getParamsFromUrl(String url) {
        Map<String, String> m = new HashMap<String, String>();
        if (url.contains("#")) {
            url = url.substring(0, url.indexOf('#'));
        }
        if (url.contains("?")) {
            String[] params = url.substring(url.indexOf('?') + 1).split("&");
            for (String param : params) {
                if (param.contains("=")) {
                    String[] v = param.split("=");
                    m.put(v[0], v[1]);
                }
            }
        }
        return m;
    }

    public static void waitingMask(String text) {
        getInstance().mask(text, "x-mask-loading");
    }

    public static void hideMask() {
        getInstance().unmask();
    }

    /**
     * Function, exposed into JSNI (native JavaScript), to call the create content wizard.
     *
     * @param path the node path, where the content will be created
     * @param nodeTypes the allowed node types to show in the content type selector
     * @param includeSubTypes if <code>true</code>, also the sub-types of the specified types will be shown in the content type selector;
     *            <code>false</code> means only the specified node types will be allowed
     * @param skipRefreshOnSave whether to avoid refreshing the edit engine on content save
     */
    public static void createContent(String path, JsArrayString nodeTypes, boolean includeSubTypes, boolean skipRefreshOnSave) {
        GWTJahiaNode parent = new GWTJahiaNode();
        parent.setPath(path);
        ContentActions.showContentWizard(getInstance().getEditLinker(),
                nodeTypes != null && nodeTypes.length() > 0 ? nodeTypes.join(" ") : "jmix:droppableContent", parent,
                includeSubTypes, skipRefreshOnSave);
    }

    public static void editContent(String path, String displayName, JsArrayString nodeTypes, JsArrayString inheritedNodeTypes, String uuid, boolean skipRefreshOnSave, EditEngineJSConfig jsConfig) {
        GWTJahiaNode node = getGwtJahiaNode(uuid, path, displayName, nodeTypes, inheritedNodeTypes);
        if (node.getDisplayName() != null) {
            EditLinker.setSelectionOnBodyAttributes(node);
        }
        EngineLoader.showEditEngine(getInstance().getEditLinker(), node, null, skipRefreshOnSave, jsConfig);
    }

    public static JavaScriptObject getEditTabs(String path, String uuid, String displayName, JsArrayString nodeTypes, JsArrayString inheritedNodeTypes, boolean hasOrderableChildNodes) {
        GWTJahiaNode node = getGwtJahiaNode(uuid, path, displayName, nodeTypes, inheritedNodeTypes);

        JSONArray editTabs = new JSONArray();
        List<GWTEngineTab> gwtEngineTabs = EditContentEngine.resolveTabs(hasOrderableChildNodes, getInstance().getEditLinker().getConfig().getEngineConfiguration(node), node);
        for (int idx = 0; idx < gwtEngineTabs.size(); idx++) {
            JSONObject jsonTab = new JSONObject();
            GWTEngineTab gwtEngineTab = gwtEngineTabs.get(idx);
            jsonTab.put("id", new JSONString(gwtEngineTab.getId()));
            jsonTab.put("title", new JSONString(gwtEngineTab.getTitle()));
            editTabs.set(idx, jsonTab);
        }
        return editTabs.getJavaScriptObject();
    }

    public static void deleteContent(String uuid, String path, String displayName, JsArrayString nodeTypes, JsArrayString inheritedNodeTypes, boolean skipRefreshOnDelete, boolean deletePermanently) {

        GWTJahiaNode node = getGwtJahiaNode(uuid, path, displayName, nodeTypes, inheritedNodeTypes);
        if (node.getDisplayName() != null) {
            EditLinker.setSelectionOnBodyAttributes(node);
        }

        EditLinker editLinker = getInstance().getEditLinker();
        LinkerSelectionContext selectionContext = editLinker.getSelectionContext();

        selectionContext.setMainNode(node);
        selectionContext.setSelectedNodes(Collections.singletonList(node));
        selectionContext.refresh(LinkerSelectionContext.SELECTED_NODE_ONLY);

        DeleteItemWindow window = new DeleteItemWindow(editLinker, selectionContext.getSelectedNodes(), deletePermanently, skipRefreshOnDelete, JahiaGWTParameters.getBaseEditUrl());
        window.show();
    }

    public static void deleteContents(JsArray objects, boolean skipRefreshOnDelete, boolean deletePermanently) {
        EditLinker editLinker = getInstance().getEditLinker();
        LinkerSelectionContext selectionContext = editLinker.getSelectionContext();
        List<GWTJahiaNode> nodes = getGwtJahiaNodes(objects);
        if (nodes.size() > 0) {
            selectionContext.setMainNode(nodes.get(0));
            selectionContext.setSelectedNodes(nodes);
            selectionContext.refresh(LinkerSelectionContext.SELECTED_NODE_ONLY);

            DeleteItemWindow window = new DeleteItemWindow(editLinker, selectionContext.getSelectedNodes(), deletePermanently, skipRefreshOnDelete, JahiaGWTParameters.getBaseEditUrl());
            window.show();
        }
    }

    public static void undeleteContent(final String uuid, final String path, String displayName, final String nodeName) {

        String message = Messages.getWithArgs(
            "message.undelete.confirm",
            "Do you really want to undelete the selected resource {0}?",
            new String[] {displayName}
        );

        MessageBox.confirm(Messages.get("label.information", "Information"), message, new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent event) {
                if (event.getButtonClicked().getItemId().equalsIgnoreCase(Dialog.YES)) {
                    JahiaContentManagementService.App.getInstance().undeletePaths(Collections.singletonList(path), new BaseAsyncCallback<Object>() {

                        @Override
                        public void onApplicationFailure(Throwable throwable) {
                            Log.error(throwable.getMessage(), throwable);
                            MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
                        }

                        @Override
                        public void onSuccess(Object result) {
                            ContentHelper.sendContentModificationEvent(uuid, path, nodeName, "update", null);
                        }
                    });
                }
            }
        });
    }

    public static void undeleteContents(JsArray objects) {
        final List<GWTJahiaNode> nodes = getGwtJahiaNodes(objects);

        String message = Messages.getWithArgs(
                "message.undelete.multiple.confirm",
                "Do you really want to undelete the {0} selected resources?",
                new String[] { String.valueOf(nodes.size()) });
        final List<String> paths = new ArrayList<String>();
        for (GWTJahiaNode node : nodes) {
            paths.add(node.getPath());
        }
        MessageBox.confirm(Messages.get("label.information", "Information"), message, new Listener<MessageBoxEvent>() {
            @Override
            public void handleEvent(MessageBoxEvent event) {
                if (event.getButtonClicked().getItemId().equalsIgnoreCase(Dialog.YES)) {
                    JahiaContentManagementService.App.getInstance().undeletePaths(paths, new BaseAsyncCallback<Object>() {

                        @Override
                        public void onApplicationFailure(Throwable throwable) {
                            Log.error(throwable.getMessage(), throwable);
                            MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
                        }

                        @Override
                        public void onSuccess(Object result) {
                            for (GWTJahiaNode node : nodes) {
                                ContentHelper.sendContentModificationEvent(node.getUUID(), node.getPath(), node.getName(), "update", null);
                            }
                        }
                    });
                }
            }
        });
    }

    public static void redrawSelection() {
        for (Selection selection : selections.values()) {
            selection.hide();
            selection.show();
        }
    }

    public static void translateContent(String path, final String sourceLang, final String destLang, final String saveCallback) {
        Map<String, List<Module>> modulesByPath = ModuleHelper.getModulesByPath();
        final List<Module> modules = (modulesByPath != null) ? modulesByPath.get(path):null;

        final TranslateContentEngine.TranslateContentEngineSaveCallback translateContentEngineSaveCallback = saveCallback != null ? new TranslateContentEngine.TranslateContentEngineSaveCallback() {
            @Override
            public void onSave() {
                doCall(saveCallback);
            }
        } : null;

        if (modules != null && modules.size() > 0) {
            displayTranslateEngine(modules.get(0).getNode(), sourceLang, destLang, translateContentEngineSaveCallback);
        } else {
            JahiaContentManagementService.App.getInstance().getNodes(Collections.singletonList(path), GWTJahiaNode.DEFAULT_FIELDS, new BaseAsyncCallback<List<GWTJahiaNode>>() {
                @Override
                public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                    if (gwtJahiaNodes.size() > 0) {
                        displayTranslateEngine(gwtJahiaNodes.get(0), sourceLang, destLang, translateContentEngineSaveCallback);
                    }
                }
            });
        }
    }

    private static void displayTranslateEngine(GWTJahiaNode node, String sourceLang, String destLang, TranslateContentEngine.TranslateContentEngineSaveCallback saveCallback) {
        TranslateContentEngine engine = new TranslateContentEngine(node, null,
                JahiaGWTParameters.getLanguage(sourceLang),
                JahiaGWTParameters.getLanguage(destLang));
        engine.setTranslateContentEngineSaveCallback(saveCallback);
        engine.show();
    }

    private static native void doCall(String key) /*-{
        eval('$wnd.' + key)();
    }-*/;

    public static void displayAlert(String title, String message) {
        MessageBox.alert(title, message, null);
    }

    public static void displayAlertInfo(String title, String message) {
        MessageBox.info(title, message, null);
    }

    public static void displayInfo(String title, String message, int duration) {
        InfoConfig infoConfig = new InfoConfig(title, message);
        infoConfig.display = duration;
        Info.display(infoConfig);
    }

    public static void openPublicationWorkflow(JsArrayString uuids, boolean allSubTree, boolean allLanguages, boolean checkForUnpublication) {
        PublicationWorkflow.openPublicationWorkflow(convertArray(uuids), getInstance().getEditLinker(), allSubTree, allLanguages, checkForUnpublication);
    }

    public static void openWorkflowDashboard(){
        new WorkflowDashboardEngine(getInstance().getEditLinker()).show();
    }

    private static GWTJahiaNode getGwtJahiaNode(JavaScriptObject object) {
        JavaScripObjectWrapper obj = new JavaScripObjectWrapper(object);
        return getGwtJahiaNode(obj.getString("uuid"), obj.getString("path"), obj.getString("displayName"), (JsArrayString) obj.get("nodeTypes"), (JsArrayString) obj.get("inheritedNodeTypes"));
    }

    public static GWTJahiaNode getGwtJahiaNode(String uuid, String path, String displayName, JsArrayString nodeTypes, JsArrayString inheritedNodeTypes) {
        if (displayName == null) {
            List<Module> modules = ModuleHelper.getModulesByPath().get(path);
            return modules.get(0).getNode();
        } else {
            return getGwtJahiaNode(uuid, path, path.substring(path.lastIndexOf("/") + 1), displayName, nodeTypes, inheritedNodeTypes);
        }
    }

    public static GWTJahiaNode getGwtJahiaNode(String uuid, String path, String name, String displayName, JsArrayString nodeTypes, JsArrayString inheritedNodeTypes) {
        List<String> types = convertArray(nodeTypes);
        List<String> inheritedTypes = convertArray(inheritedNodeTypes);
        GWTJahiaNode node = new GWTJahiaNode();
        node.setName(name);
        node.setDisplayName(displayName);
        node.setUUID(uuid);
        node.setPath(path);
        node.setNodeTypes(types);
        node.setInheritedNodeTypes(inheritedTypes);
        node.setFile(types.contains("nt:file") || inheritedTypes.contains("nt:file"));
        return node;
    }

    public static List<GWTJahiaNode> getGwtJahiaNodes(JsArray objects) {
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
        for (int i = 0; i < objects.length(); i++) {
            nodes.add(getGwtJahiaNode(objects.get(i)));
        }
        return nodes;
    }

    public static List<String> convertArray(JsArrayString jsArrayString) {
        ArrayList<String> l = new ArrayList<String>();
        for (int i = 0; i < jsArrayString.length(); i++) {
            l.add((String) jsArrayString.get(i));
        }
        return l;
    }

    /**
     * Function, exposed into JSNI (native JavaScript), to switch language to the specified one
     *
     * @param lang the target language code
     */
    public static void switchLanguage(String lang) {
        if (lang.equals(JahiaGWTParameters.getLanguage())) {
            return;
        }
        validateSiteLanguage(JahiaGWTParameters.getSiteNode(), lang);
        getInstance().getEditLinker().setLocale(JahiaGWTParameters.getLanguage(lang));
    }

    /**
     * Function, exposed into JSNI (native JavaScript), to switch to the specified site and language
     *
     * @param siteKey the target site key
     * @param lang the target language code
     */
    public static void switchSite(final String siteKey, final String lang) {

        if (siteKey.equals(JahiaGWTParameters.getSiteKey()) && lang.equals(JahiaGWTParameters.getLanguage())) {
            return;
        }

        JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList("/sites/" + siteKey), GWTJahiaNode.DEFAULT_SITE_FIELDS, new BaseAsyncCallback<List<GWTJahiaNode>>() {

            @Override
            public void onSuccess(List<GWTJahiaNode> result) {
                GWTJahiaNode siteNode = result.get(0);
                validateSiteLanguage(siteNode, lang);
                JahiaGWTParameters.setSiteNode(siteNode);
                getInstance().getEditLinker().setLocale(JahiaGWTParameters.getLanguage(lang));
            }
        });
    }

    private static void validateSiteLanguage(GWTJahiaNode siteNode, String lang) {
        @SuppressWarnings("unchecked") List<String> langs = (List<String>) siteNode.get("j:languages");
        if (!langs.contains(lang)) {
            throw new RuntimeException("Language '" + lang + "' is not a valid '" + siteNode.getName() + "' site language");
        }
    }

    public String getUrl(String path, String template, String channel, String variant) {
        return getUrl(path, template, channel, variant, false);
    }

    public String getUrl(String path, String template, String channel, String variant, boolean preserveQueryString) {
        if (template != null && "default".equals(template)) {
            template = null;
        }
        StringBuilder url = new StringBuilder(getBaseUrl() + URL.encodePathSegment(path).replaceAll("%2F", "/") + (template != null && !template.equals("") ? ("." + template) : "") + ".html");
        // add channel parameters if needed
        if (channel != null && channel.equals("generic") && (editLinker.getActiveChannel() == null || editLinker.getActiveChannel().getValue().equals("generic"))) {
            channel = null;
            variant = null;
        } else if (channel == null && editLinker.getActiveChannel() != null && !editLinker.getActiveChannel().getValue().equals("generic")) {
            channel = editLinker.getActiveChannel().getValue();
            variant = editLinker.getActiveChannelVariant();
        }

        if (channel != null && channel.length() > 0) {
            url.append("?channel=").append(channel);
            if (variant != null && variant.length() > 0) {
                url.append("&variant=").append(variant);
            }
        }

        if (preserveQueryString) {
            appendQueryString(url);
        }

        return url.toString();
    }

    /**
     * Appends the current URL query string (if present) to the specified one.
     */
    private static void appendQueryString(StringBuilder url) {
        List<String[]> paramsToPreserve = getQueryStringParametersToPreserve(RESERVED_REQUESTPARAMETERS);

        if (paramsToPreserve == null || paramsToPreserve.isEmpty()) {
            // no query string available
            return;
        }

        url.append(url.indexOf("?") == -1 ? '?' : '&');
        boolean first = true;
        for (String[] p : paramsToPreserve) {
            if (!first) {
                url.append('&');
            } else {
                first = false;
            }
            url.append(p[0]).append('=').append(URL.encodeQueryString(p[1]));
        }
    }

    private static List<String[]> getQueryStringParametersToPreserve(String... reservedParameters) {
        String queryString = Window.Location.getQueryString();
        if (queryString == null || queryString.length() == 0 || queryString.equals("?")) {
            // no query string available
            return null;
        }

        // we need to rebuild the query string to avoid duplicate entries
        Map<String, List<String>> params = Window.Location.getParameterMap();
        if (params.isEmpty()) {
            // no parameters to append
            return null;
        }

        if (reservedParameters != null && reservedParameters.length > 0) {
            // filter out reserved parameters
            params = new HashMap<String, List<String>>(params);
            for (String reserved : reservedParameters) {
                params.remove(reserved);
            }
        }

        if (params.isEmpty()) {
            // all are filtered out: no parameters left to append
            return null;
        }

        List<String[]> toAppend = new ArrayList<String[]>();
        for (Map.Entry<String, List<String>> p : params.entrySet()) {
            for (String v : p.getValue()) {
                toAppend.add(new String[]{p.getKey(), v});
            }
        }

        return toAppend;
    }

    /**
     * Computes the base URL for the main module frame.
     *
     * @return the base URL for the main module frame
     */
    public String getBaseUrl() {
        String baseUrl = JahiaGWTParameters.getBaseUrl();
        baseUrl = baseUrl.substring(0, baseUrl.indexOf("/" + JahiaGWTParameters.getWorkspace() + "/"));
        baseUrl += "frame/" + JahiaGWTParameters.getWorkspace();
        baseUrl += "/" + (editLinker.getLocale() == null ? JahiaGWTParameters.getLanguage() : editLinker.getLocale());
        return baseUrl;
    }

    @Override
    public Element getInnerElement() {
        if (frame != null) {
            IFrameElement iframe = IFrameElement.as(frame.getElement());
            com.google.gwt.dom.client.Element body = iframe.getContentDocument().getElementsByTagName("body").getItem(0);
            return (Element) body;
        }
        return super.getInnerElement();
    }

    public static native void setDocumentTitle(String title) /*-{
        $doc.title = title;
        if($wnd.parent !== $wnd) {
            $wnd.parent.postMessage({msg:'setTitle', title:title}, $wnd.location.origin);
        }
    }-*/;

    private void refreshImages(Element element) {
        NodeList<com.google.gwt.dom.client.Element> elementsByTagName = element
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
                el.setAttribute("src", url + (url.indexOf('?') == -1 ? "?" : "&") + suffix);
            }
        }
    }

    private void refreshCSS(Element element) {
        NodeList<com.google.gwt.dom.client.Element> elementsByTagName = element
                .getElementsByTagName("link");
        if (elementsByTagName == null) {
            return;
        }
        String base = JahiaGWTParameters.getContextPath() + "/modules/";
        String suffix = "tst=" + System.currentTimeMillis();
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            com.google.gwt.dom.client.Element el = elementsByTagName.getItem(i);
            String url = el.getAttribute("href");
            String type = el.getAttribute("type");
            if (type != null && type.equals("text/css") && url != null && url.startsWith(base)) {
                el.setAttribute("href", url + (url.indexOf('?') == -1 ? "?" : "&") + suffix);
            }
        }
    }

    private void refreshScripts(Element element) {
        NodeList<com.google.gwt.dom.client.Element> elementsByTagName = element
                .getElementsByTagName("script");
        if (elementsByTagName == null) {
            return;
        }
        String base = JahiaGWTParameters.getContextPath() + "/modules/";
        String suffix = "tst=" + System.currentTimeMillis();
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            com.google.gwt.dom.client.Element el = elementsByTagName.getItem(i);
            String url = el.getAttribute("src");
            String type = el.getAttribute("type");
            if (type != null && type.equals("text/javascript") && url != null && url.startsWith(base)) {
                el.setAttribute("src", url + (url.indexOf('?') == -1 ? "?" : "&") + suffix);
            }
        }
    }

    @Override
    protected void onAfterLayout() {
        super.onAfterLayout();
        if (needParseAfterLayout) {
            if (moduleMap != null) {
                ModuleHelper.move(moduleMap);
            }
            scrollContainer.setHeight(getHeight() - (head != null ? head.getOffsetHeight() : 0));
            scrollContainer.setWidth(getWidth());

            if (activeChannel != null && !"default".equals(activeChannel.getValue())) {
                int[] usableResolution = getUsableDeviceResolution(activeChannel, editLinker.getActiveChannelVariantIndex());
                scrollContainer.setSize(usableResolution[0], usableResolution[1]);
            }
            needParseAfterLayout = false;
        }
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        scrollContainer.setHeight(getHeight() - (head != null ? head.getOffsetHeight() : 0));
        scrollContainer.setWidth(getWidth());

        if (activeChannel != null && !"default".equals(activeChannel.getValue())) {
            int[] usableResolution = getUsableDeviceResolution(activeChannel, editLinker.getActiveChannelVariantIndex());
            scrollContainer.setSize(usableResolution[0], usableResolution[1]);
        }

        if (editLinker.getSelectedModule() != null) {
            redrawSelection();
        }
    }

    @Override
    public LayoutContainer getContainer() {
        return scrollContainer;
    }

    public boolean isCtrlActive() {
        return ctrlActive;
    }

    public void setCtrlActive(DomEvent event) {
        this.ctrlActive = Window.Navigator.getPlatform().toLowerCase().contains("mac") ? DOM.eventGetMetaKey(event.getEvent()) : event.isControlKey();
    }

    public void parse(List<Element> el) {
        if (moduleMap != null) {
            ModuleHelper.deleteAll(moduleMap);
            List<Component> l = new ArrayList<Component>(scrollContainer.getItems());
            for (Component component : l) {
                if (component instanceof Module) {
                    component.removeFromParent();
                }
            }
        }
        moduleMap = ModuleHelper.parse(this, null, el);
    }

    @Override
    public String getModuleId() {
        return "main";
    }

    public static void staticGoTo(String path, String template) {
        module.goTo(path, template, null, null);
    }

    public static void staticGoTo(String path, String template, String channel, String variant) {
        module.goTo(path, template, channel, variant);
    }

    public static void staticGoToUrl(String path) {
        module.resetFramePosition();
        module.goToUrl(path, false, false, false);
    }

    public static void popState(String location, String config) {
        MainModule m = getInstance();
        if (!config.equals(m.getConfig().getName())) {
            m.getEditLinker().switchConfig(m.configs.get(config), location + "##", true, true, null);
        } else {
            module.resetFramePosition();
            m.goToUrl(location + "##", false, false, false);
            sendMessage("edit frame history updated", location);
        }
    }

    public void goTo(String path, String template, String channel, String variant) {
        resetFramePosition();
        goToUrl(getUrl(path, template, channel, variant), false, false, false);
    }

    private void updateUrlBar(String path) {
        String currentHref = Window.Location.getHref();
        if (currentHref.endsWith(path)) {
            return;
        }

        if (supportPushState()) {
            if (!path.endsWith("##")) {
                String pathWithoutFrame = path.replaceFirst("frame/", "/");
                if (Window.Location.getQueryString().contains("gwt.codesvr") && !pathWithoutFrame.contains("gwt.codesvr")) {
                    Map<String, String> m = getParamsFromUrl(Window.Location.getQueryString());
                    pathWithoutFrame += (pathWithoutFrame.contains("?") ? '&' : '?') + "gwt.codesvr=" + m.get("gwt.codesvr");
                }
                if (!pathWithoutFrame.equals(currentHref) || firstLoad) {
                    firstLoad = false;
                    pushState(pathWithoutFrame, path, config.getName());
                }
            }
        } else {
            int hashPosition = currentHref.indexOf('#');
            if (hashPosition > 0) {
                currentHref = currentHref.substring(0, hashPosition);
            }
            Window.Location.assign(currentHref + "#" + MainModule.getInstance().getConfig().getName() + "|" + URL.encode(path));
        }
        path = path.substring(path.indexOf(JahiaGWTParameters.getContextPath() + JahiaGWTParameters.getServletPath()));

        if (storage != null) {
            String storedPath = path;
            if (storedPath.endsWith("##")) {
                storedPath = storedPath.substring(0, storedPath.length() - 2);
            }
            storage.setItem(MainModule.getInstance().getConfig().getName() + "_path", storedPath);
        }
    }

    public void switchLanguage(GWTJahiaLanguage language) {
        if (!language.getLanguage().equals(JahiaGWTParameters.getLanguage())) {
            editLinker.setLocale(language);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put(Linker.REFRESH_MAIN, true);
            data.put("event", "languageChanged");
            editLinker.refresh(data);
        }
    }

    public void switchChannel(GWTJahiaChannel channel, String variant) {
        editLinker.setActiveChannelVariant(variant);
        editLinker.setActiveChannel(channel);
        if (!editLinker.isInSettingsPage()) {
            goTo(path, template, channel.getValue(), variant);
        }
    }


    public void setNodeTypes(String nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setReferenceTypes(String referenceTypes) {
        this.referenceTypes = referenceTypes;
    }

    public void setMainModuleElement(Element mainModuleElement) {
        this.mainModuleElement = mainModuleElement;
    }

    @Override
    public void setNode(GWTJahiaNode node) {
        mainModule = this;
        super.setNode(node);
        if (node.isMarkedForDeletion()) {
            overlayLabel = new HTML(Messages.get("label.deleted", "Deleted"));
            overlayLabel.setStyleName("deleted-overlay");
            overlayColorText = "#f00";
            opacity = "0.4";
        }
        if (node.isShared()) {
            this.setToolTip(new ToolTipConfig(Messages.get("info_important", "Important"),
                    Messages.get("info_sharednode", "This is a shared node")));
        }

        final List<String> types = node.getNodeTypes();
        final List<String> inheritedTypes = node.getInheritedNodeTypes();
        editLinker.setInSettingsPage(!types.contains("jnt:page") && !types.contains("jnt:template") && !types.contains("jnt:content") &&
                !inheritedTypes.contains("jnt:page") && !inheritedTypes.contains("jnt:template") && !inheritedTypes.contains("jnt:content"));

        setDocumentTitle(Messages.get("label." + config.getName().substring(0, config.getName().length() - 4), config.getName()) + " - " + node.getDisplayName());

        if (head != null) {
            ((ToolbarHeader) head).handleNewMainNodeLoaded(node);
        }

        editLinker.handleNewMainNodeLoaded();
        if (head != null && head instanceof ToolbarHeader) {
            ((ToolbarHeader) head).handleNewMainNodeLoaded(node);
        }
        if (overlayLabel != null) {
            DOM.setStyleAttribute(mainModuleElement, "position", "relative");
            mainModuleElement.getParentElement().insertFirst(overlayLabel.getElement());
            overlayLabel.setHeight("100%");
            overlayLabel.setWidth("100%");

            DOM.setStyleAttribute(mainModuleElement, "opacity", opacity);
            layout();
        }
    }

    public GWTEditConfiguration getConfig() {
        return config;
    }

    /**
     * Set edit mode configuration and location.
     * A path can be explicitly specified.
     * It can also be stored and recalled from the HTML5 web storage, if available.
     *
     * @param config
     * @param newPath
     */
    public void setConfig(GWTEditConfiguration config, String newPath) {
        JahiaGWTParameters.changeServletMapping(this.config.getDefaultUrlMapping(), config.getDefaultUrlMapping());

        activeChannel = null;
        activeChannelVariant = null;
        String currentFrameUrl = frame.getCurrentFrameUrl();
        // detect if the configuration use the same url format as the default one.
        // used for the content manager configuration that has a custom url resolver
        String urlPart = "frame/" + JahiaGWTParameters.getWorkspace() + "/" +
                (editLinker.getLocale() == null ? JahiaGWTParameters.getLanguage() : editLinker.getLocale());
        boolean canSwitchConfig = currentFrameUrl.contains(this.config.getDefaultUrlMapping() + urlPart);
        if (newPath != null) {
            newLocation = newPath;
        } else if (canSwitchConfig) {
            newLocation = currentFrameUrl.replaceFirst(this.config.getDefaultUrlMapping(), config.getDefaultUrlMapping());
        } else {
            // switch to default site when current site is system site
            String path = JahiaGWTParameters.getSiteNode().getSiteKey().equals("systemsite") ?
                    config.getDefaultLocation() :
                    JahiaGWTParameters.getSiteNode().get(GWTJahiaNode.HOMEPAGE_PATH) + ".html";
            newLocation = currentFrameUrl.substring(0, currentFrameUrl.indexOf(this.config.getDefaultUrlMapping())) +
                    config.getDefaultUrlMapping() + urlPart + path;
        }

        this.config = config;

        configs.put(this.config.getName(), this.config);
    }

    public void handleNewModuleSelection(Module selectedModule) {
        Selection l = new Selection(selectedModule);
        if (!ctrlActive || selectedModule == null) {
            for (Selection s : selections.values()) {
                s.hide();
            }
            selections.clear();
        }
        if (selectedModule != null && !(selectedModule instanceof MainModule)) {
            selections.put(selectedModule, l);
            l.select();
            l.show();
        }
        if (head != null) {
            ((ToolbarHeader) head).handleNewLinkerSelection();
        }

        l.layout();
    }

    public void handleNewMainSelection(String path, String template) {
        if (storage != null) {
            storage.setItem(MainModule.getInstance().getConfig().getName() + "_nodePath", path);
        }

        Map<String, List<String>> params = null;
        if ((this.path != null ? this.path.equals(path) : path == null) &&
                (this.template != null ? this.template.equals(template) : template == null) &&
                (this.moduleParams != null ? this.moduleParams.equals(params) : params == null)) {
            return;
        }

        this.path = path;
        this.template = template;

        moduleParams = params;

        if (head != null) {
            ((ToolbarHeader) head).handleNewLinkerSelection();
        }
    }

    public void setInfoLayer(String key, boolean value) {
        if (value) {
            activeLayers.put(key, value);
        } else {
            activeLayers.remove(key);
        }
        refreshInfoLayer();
    }

    public static void setGlobalSelectionDisabled(boolean globalSelectionDisabled) {
        MainModule.globalSelectionDisabled = globalSelectionDisabled;
    }

    public void refreshInfoLayer() {
        infoLayers.removeAll();

        handleTranslateStatus();

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
                                        StringBuilder toolTip = new StringBuilder();
                                        for (Map.Entry<GWTJahiaNode, ModelData> entry : entries) {
                                            if (toolTip.length() > 0) {
                                                toolTip.append("<br/>");
                                            }
                                            XTemplate tpl = XTemplate.create((String) entry.getValue().get(
                                                    "xtemplate"));
                                            toolTip.append(tpl.applyTemplate(com.extjs.gxt.ui.client.util.Util.getJsObject(
                                                    entry.getKey())));
                                        }
                                        layoutContainer.setToolTip(toolTip.toString());
                                        images.add(layoutContainer);
                                    } else {
                                        AbstractImagePrototype icon = ToolbarIconProvider.getInstance().getIcon(
                                                "visibilityStatusRed");
                                        LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
                                        layoutContainer.add(icon.createImage());
                                        Set<Map.Entry<GWTJahiaNode, ModelData>> entries = visibility.entrySet();
                                        StringBuilder toolTip = new StringBuilder();
                                        for (Map.Entry<GWTJahiaNode, ModelData> entry : entries) {
                                            if (toolTip.length() > 0) {
                                                toolTip.append("<br/>");
                                            }
                                            XTemplate tpl = XTemplate.create((String) entry.getValue().get(
                                                    "xtemplate"));
                                            toolTip.append(tpl.applyTemplate(com.extjs.gxt.ui.client.util.Util.getJsObject(
                                                    entry.getKey())));
                                        }
                                        layoutContainer.setToolTip(toolTip.toString());
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

    private void handleTranslateStatus() {
        List<Module> modules = ModuleHelper.getModules();

        if (modules != null) {
            for (Module m : modules) {
                if (m instanceof SimpleModule) {
                    if (activeLayers.containsKey("translate")) {
                        ((SimpleModule) m).showTranslatableModule();
                    } else {
                        ((SimpleModule) m).hideTranslatableModule();
                    }
                }
            }
        }
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    public static MainModule getInstance() {
        return module;
    }

    public static native void exportStaticMethod() /*-{
        var nsAuthoringApi = $wnd.authoringApi || {};
        $wnd.authoringApi = nsAuthoringApi;

        nsAuthoringApi.goTo = $wnd.goTo = function (path, template, params) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::staticGoTo(Ljava/lang/String;Ljava/lang/String;)(path, template);
        }
        nsAuthoringApi.goToUrl = $wnd.goToUrl = function (path, template, params) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::staticGoToUrl(Ljava/lang/String;)(path);
        }
        $wnd.addEventListener("popstate", function (event) {
            if (event.state) {
                @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::popState(Ljava/lang/String;Ljava/lang/String;)(event.state.location, event.state.config);
            }
        });
        nsAuthoringApi.waitingMask = $wnd.waitingMask = function (text) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::waitingMask(Ljava/lang/String;)(text);
        };
        nsAuthoringApi.hideMask = $wnd.hideMask = function () {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::hideMask()();
        };
        nsAuthoringApi.createContent = $wnd.createContent = function (path, types, includeSubTypes, skipRefreshOnSave) {
            if (typeof includeSubTypes !== 'undefined') {
                @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::createContent(*)(path, types, includeSubTypes, skipRefreshOnSave);
            } else {
                @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::createContent(*)(path, types, true, skipRefreshOnSave);
            }
        };
        nsAuthoringApi.editContent = $wnd.editContent = function (path, displayName, types, inheritedTypes, uuid, skipRefreshOnSave, jsConfig) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::editContent(*)(path, displayName, types, inheritedTypes, uuid, skipRefreshOnSave, jsConfig);
        };
        nsAuthoringApi.deleteContent = $wnd.deleteContent = function (uuid, path, displayName, types, inheritedTypes, skipRefreshOnDelete, deletePermanently) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::deleteContent(*)(uuid, path, displayName, types, inheritedTypes, skipRefreshOnDelete, deletePermanently);
        };
        nsAuthoringApi.deleteContents = $wnd.deleteContents = function (objects, skipRefreshOnDelete, deletePermanently) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::deleteContents(*)(objects, skipRefreshOnDelete, deletePermanently);
        };
        nsAuthoringApi.undeleteContent = $wnd.undeleteContent = function (uuid, path, displayName, nodeName) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::undeleteContent(*)(uuid, path, displayName, nodeName);
        };
        nsAuthoringApi.undeleteContents = $wnd.undeleteContents = function (objects) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::undeleteContents(*)(objects);
        };
        nsAuthoringApi.disableGlobalSelection = $wnd.disableGlobalSelection = function (value) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::globalSelectionDisabled = value;
        };
        nsAuthoringApi.displayAlert = $wnd.displayAlert = function (title, message) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::displayAlert(*)(title, message);
        };
        nsAuthoringApi.displayAlertInfo = $wnd.displayAlertInfo = function (title, message) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::displayAlertInfo(*)(title, message);
        };
        nsAuthoringApi.displayInfo = $wnd.displayInfo = function (title, message, duration) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::displayInfo(*)(title, message, duration);
        };
        nsAuthoringApi.openPublicationWorkflow = function (uuids, allSubTree, allLanguages, checkForUnpublication) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::openPublicationWorkflow(*)(uuids, allSubTree, allLanguages, checkForUnpublication)
        };
        nsAuthoringApi.openWorkflowDashboard = function () {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::openWorkflowDashboard(*)()
        };
        nsAuthoringApi.switchLanguage = function (lang) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::switchLanguage(Ljava/lang/String;)(lang);
        }
        nsAuthoringApi.switchSite = function (siteKey, lang) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::switchSite(*)(siteKey, lang);
        }
        nsAuthoringApi.translateContent = function (nodePath, sourceLang, destLang, saveCallback) {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::translateContent(*)(nodePath, sourceLang, destLang, saveCallback);
        };
        nsAuthoringApi.redrawSelection = function () {
            @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::redrawSelection(*)();
        };

        nsAuthoringApi.getEditTabs = function (path, uuid, displayName, types, inheritedTypes, hasOrderableChildNodes) {
            return @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::getEditTabs(*)(path, uuid, displayName, types, inheritedTypes, hasOrderableChildNodes);
        };
    }-*/;

    public static native void exposeTopStaticMethod() /*-{
        var nsTopAuthoringApi = $wnd.top.authoringApi || {};
        $wnd.top.authoringApi = nsTopAuthoringApi;

        nsTopAuthoringApi.refreshContent = function () {
            return @org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule::refreshContent(*)();
        };
    }-*/;

    public static native void removeTopStaticMethod() /*-{
        if ($wnd.top.authoringApi) {
            delete $wnd.top.authoringApi.refreshContent;
        }
    }-*/;

    public InfoLayers getInfoLayers() {
        return infoLayers;
    }

    private native boolean supportPushState() /*-{
        if ($wnd.history && $wnd.history.pushState) {
            return true;
        } else {
            return false;
        }
    }-*/;

    private native boolean pushState(String path, String location, String config) /*-{
        var state = {location: location, config: config};
        var push = true;
        if ($wnd.history.state) {
            $wnd.history.pushState(state, null, path);
        } else {
            $wnd.history.replaceState(state, null, path);
            push= false;
        }
        if($wnd.parent !== $wnd) {
            $wnd.parent.postMessage({msg:'edit frame history updated', state:state, isPushState:push, url:path}, $wnd.location.origin)
        }
        return true;
    }-*/;

    private static native boolean sendMessage(String message, String location) /*-{
        if($wnd.parent !== $wnd) {
            $wnd.parent.postMessage({msg:message, state:{location: location}, isPushState:false, url:location}, $wnd.location.origin)
        }
        return true;
    }-*/;


    public static void refreshContent() {
        Map<String, Object> data = new HashMap<>();
        data.put(Linker.REFRESH_ALL, true);

        getInstance().getEditLinker().refresh(data);
    }

    public void goToExternalUrl(String url) {
        mask(Messages.get("label.loading", "Loading..."), "x-mask-loading");
        frame.setExternalUrl(url);
    }

    private void parseFrameContent(boolean forceImageRefresh, boolean forceCssRefresh, boolean forceJavascriptRefresh) {
        try {
            frameError = null;
            final IFrameElement iframe = IFrameElement.as(frame.getElement());
            Document contentDocument = iframe.getContentDocument();
            Element body = (Element) contentDocument.getElementsByTagName("body").getItem(0);
            Element head = (Element) contentDocument.getElementsByTagName("head").getItem(0);
            if (forceImageRefresh) {
                refreshImages(body);
            }
            if (head != null) {
                if (forceCssRefresh) {
                    refreshCSS(head);
                }
                if (forceJavascriptRefresh) {
                    refreshScripts(head);
                }
            }
            Hover.getInstance().removeAll();
            List<Element> el = null;
            List<Element> elBody = null;
            if ("true".equals(body.getAttribute("jahia-parse-html"))) {
                Element innerElement = getInnerElement();
                elBody = ModuleHelper.getAllJahiaTypedElementsRec(body);
                if (body.equals(innerElement)) {
                    el = elBody;
                } else {
                    el = ModuleHelper.getAllJahiaTypedElementsRec(getInnerElement());
                }

            } else if (body.getAttribute("jahia-error-code") != null && !"".equals(body.getAttribute("jahia-error-code"))) {
                frameError = body.getAttribute("jahia-error-code");
            } else {
                NodeList<com.google.gwt.dom.client.Element> el1 = body.getElementsByTagName("div");
                int i = 0;
                Element e = null;
                while (i < el1.getLength()) {
                    e = (Element) el1.getItem(i);
                    if ("mainmodule".equals(e.getAttribute(ModuleHelper.JAHIA_TYPE))) {
                        el = Arrays.asList(e);
                        elBody = Arrays.asList(e);
                        break;
                    }
                    i++;
                }


            }
            if (contextMenu != null) {
                contextMenu.hide();
            }

            if (el != null && elBody != null) {
                ModuleHelper.tranformLinks((Element) contentDocument.getDocumentElement());
                ModuleHelper.initAllModules(MainModule.this, body, el, elBody, config);
                editLinker.getSidePanel().enable();
            } else {
                // if body empty, this is not a jahia page
                path = "";
                // clear side panel
                editLinker.getSidePanel().disable();
            }
            editLinker.getMainModule().unmask();
            needParseAfterLayout = true;
            layout();
        } catch (Exception e) {
            Log.error("Error in EditFrame: " + e.getMessage(), e);
        }
    }

    // reset the frame position to its initial value
    public void resetFramePosition() {
        framePosition = new Point(0, 0);
    }

    // save the current position of the frame
    public Point saveCurrentFramePosition() {
        if (!GXT.isIE10) {

            framePosition.x = WindowUtil.getScrollLeft(getInnerElement());
            framePosition.y = WindowUtil.getScrollTop(getInnerElement());
        } else {
            framePosition.x = getIE10FrameLeft();
            framePosition.y = getIE10FrameTop();
        }
        return framePosition;
    }

    private class EditFrame extends Frame {
        private String url = null;
        private boolean forceImageRefresh = false;
        private boolean forceCssRefresh = false;
        private boolean forceJavascriptRefresh = false;

        private EditFrame() {
            super();
            super.addStyleName("window-iframe");
        }

        @Override
        public void onBrowserEvent(Event event) {
            try {
                if (event.getTypeInt() == Event.ONLOAD) {
                    sendEventOnParent(IFrameElement.as(frame.getElement()));

                    if (isValidUrl(url) && config.isNeedFrameParsing()) {
                        final IFrameElement iframe = IFrameElement.as(frame.getElement());
                        Document contentDocument = iframe.getContentDocument();

                        parseFrameContent(forceImageRefresh, forceCssRefresh, forceJavascriptRefresh);

                        GWTJahiaLanguage language = JahiaGWTParameters.getLanguage(mainModuleElement.getAttribute("locale"));
                        if (language != null && !language.getLanguage().equals(JahiaGWTParameters.getLanguage())) {
                            JahiaGWTParameters.setLanguage(language);
                            editLinker.setLocale(language);
                            LanguageSwitcherActionItem.setLanguage(language);
                            Map<String, Object> data = new HashMap<>();
                            data.put(Linker.REFRESH_ALL, true);
                            editLinker.getSidePanel().refresh(data);
                        }
                        DOM.sinkEvents((Element) contentDocument.getDocumentElement(), Event.ONMOUSEMOVE + Event.ONMOUSEUP + Event.ONCONTEXTMENU + Event.ONCLICK + Event.ONMOUSEDOWN);
                        DOM.setEventListener((Element) contentDocument.getDocumentElement(), new EventListener() {

                            @Override
                            public void onBrowserEvent(Event event) {
                                if (event.getTypeInt() == Event.ONMOUSEMOVE || event.getTypeInt() == Event.ONMOUSEUP || event.getTypeInt() == Event.ONMOUSEDOWN) {
                                    inframe = true;
                                    Event.fireNativePreviewEvent(event);
                                    inframe = false;
                                } else {
                                    GWT.log("event:" + event.getTypeInt());
                                    scrollContainer.onBrowserEvent(event);
                                }
                            }
                        });
                        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                            @Override
                            public void execute() {
                                if (frameError != null && !frameErrorRedirect) {
                                    frameErrorRedirect = true;
                                    if (frameError.equals("503")) {
                                        handleError503();
                                    }
                                    String currentPath = node.getPath();
                                    JahiaContentManagementService.App.getInstance().getDisplayableNodePath(currentPath.substring(0, currentPath.lastIndexOf("/")), true, new AsyncCallback<String>() {
                                        @Override
                                        public void onFailure(Throwable throwable) {
                                            setUrl(getBaseUrl() + config.getDefaultLocation());
                                        }

                                        @Override
                                        public void onSuccess(String path) {
                                            if (path != null) {
                                                staticGoTo(path, null);
                                            } else {
                                                setUrl(getBaseUrl() + config.getDefaultLocation());
                                            }
                                        }
                                    });
                                } else {
                                    frameErrorRedirect = false;
                                    onGWTFrameReady(iframe);
                                    moveToSavePosition();
                                    // once pgae rendered, reset the frame position
                                    resetFramePosition();
                                }
                            }

                            private void handleError503() {
                                MessageBox.alert(
                                        Messages.get("label.error.503.title", "503 - Service temporary unavailable"),
                                        Messages.get("label.error.503.description",
                                                "The server is temporarily unable to service your request"
                                                        + " due to maintenance downtime or capacity problems.")
                                                + "<br>"
                                                + Messages.get("label.error.maintenance.description",
                                                "We are sorry for the inconvenience. Please check back later."),
                                        null);
                            }
                        });
                    } else {
                        editLinker.getMainModule().unmask();
                    }
                    // always set url regarding the frame value
                    updateUrlBar(frame.getCurrentFrameUrl());
                }
            } catch (Exception e) {
                Log.error("Error in EditFrame: " + e.getMessage(), e);
            }
        }

        private String getCurrentFrameUrl() {
            return getFrameUrl(IFrameElement.as(frame.getElement()));
        }

        @Override
        public void setUrl(String url) {
            url = getPathFromUrl(url);
            if (isValidUrl(url)) {
                if (getElement() != null) {
                    this.url = url;
                    super.setUrl(url);
                } else {
                    this.url = url;
                }
            }
        }

        public void setExternalUrl(String url) {
            if (isAttached()) {
                this.url = url;
                super.setUrl(url);
            } else {
                this.url = url;
            }
        }

        public void setForceImageRefresh(boolean forceImageRefresh) {
            this.forceImageRefresh = forceImageRefresh;
        }

        public void setForceCssRefresh(boolean forceCssRefresh) {
            this.forceCssRefresh = forceCssRefresh;
        }

        public void setForceJavascriptRefresh(boolean forceJavascriptRefresh) {
            this.forceJavascriptRefresh = forceJavascriptRefresh;
        }

        @Override
        protected void onAttach() {
            super.onAttach();
            IFrameElement iframe = IFrameElement.as(frame.getElement());
            iframe.setAttribute("frameborder", "0");
//            iframe.setAttribute("sandbox", "allows-scripts");
            if (url != null && !super.getUrl().endsWith(URL.encode(url))) {
                super.setUrl(url);
            }
        }

        public final native String sendEventOnParent(IFrameElement iFrameElement) /*-{
            iFrameElement.contentWindow.parent.parent.dispatchEvent(new Event('iframeloaded', {"bubbles":true, "cancelable":false}));
        }-*/;

        public final native String getFrameUrl(IFrameElement iFrameElement) /*-{
            // This is known to work on all modern browsers.
            return iFrameElement.contentWindow.location.href;
        }-*/;

        public final native String setFrameUrl(IFrameElement iFrameElement) /*-{
            // This is known to work on all modern browsers.
            return iFrameElement.contentWindow.location.href;
        }-*/;

        public final native String onGWTFrameReady(IFrameElement iFrameElement) /*-{
            if (iFrameElement.contentWindow.onGWTFrameLoaded != null) {
                var onFrameLoaded = iFrameElement.contentWindow.onGWTFrameLoaded;
                for (var i = 0; i < onFrameLoaded.length; i++) {
                    onFrameLoaded[i]()
                }
                iFrameElement.contentWindow.onGWTFrameLoaded = [];
            }
        }-*/;
    }

    private String getPathFromUrl(String url) {
        if (url.contains("://")) {
            url = url.substring(url.indexOf("://") + 3);
            url = url.substring(url.indexOf('/'));
        }
        return url;
    }

    public String getTemplate() {
        return template;
    }

    public final int getIE10FrameTop() {
        return getIE10FrameTop(IFrameElement.as(frame.getElement()));
    }

    public final int getIE10FrameLeft() {
        return getIE10FrameLeft(IFrameElement.as(frame.getElement()));
    }

    public final void moveToSavePosition() {
        scrollTo(IFrameElement.as(frame.getElement()), framePosition.x, framePosition.y);
    }

    public final native int getIE10FrameTop(IFrameElement iFrameElement) /*-{
        if (iFrameElement.contentWindow.document.documentElement != null) {
            return iFrameElement.contentWindow.document.documentElement.scrollTop;
        }
    }-*/;

    public final native int getIE10FrameLeft(IFrameElement iFrameElement) /*-{
        if (iFrameElement.contentWindow.document.documentElement != null) {
            return iFrameElement.contentWindow.document.documentElement.scrollLeft;
        }
    }-*/;

    public final native void scrollTo(IFrameElement iFrameElement, int x, int y) /*-{
        iFrameElement.contentWindow.scrollTo(x, y);
    }-*/;
}
