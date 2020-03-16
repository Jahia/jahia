package org.jahia.ajax.gwt.module.empty.client;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.EmptyLinker;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.DeleteItemWindow;
import org.jahia.ajax.gwt.client.widget.content.util.ContentHelper;
import org.jahia.ajax.gwt.client.widget.contentengine.*;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.poller.ContentUnpublishedEvent;
import org.jahia.ajax.gwt.client.widget.poller.EventDispatcherPollListener;
import org.jahia.ajax.gwt.client.widget.poller.ProcessPollingEvent;
import org.jahia.ajax.gwt.client.widget.poller.TaskEvent;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EmptyEntryPoint extends CommonEntryPoint {

    private static EmptyEntryPoint instance;

    public static EmptyEntryPoint getInstance() {
        return instance;
    }

    private Linker linker;
    private GWTEditConfiguration configuration;

    @Override
    public void onModuleLoad() {
        super.onModuleLoad();

        instance = this;


        JahiaContentManagementService.App.getInstance().getEditConfiguration("/", "editmode", "default", new BaseAsyncCallback<GWTEditConfiguration>() {

            @Override
            public void onSuccess(GWTEditConfiguration config) {
                PermissionsUtils.loadPermissions(config.getPermissions());
                if (config.isEventDispatchingEnabled()) {
                    new EventDispatcherPollListener(TaskEvent.class, ProcessPollingEvent.class, ContentUnpublishedEvent.class);
                }

                exposeFunctions();

                JahiaGWTParameters.setSiteNode(config.getSiteNode());

                configuration = config;
                linker = new EmptyLinker(config);
            }

            @Override
            public void onApplicationFailure(Throwable throwable) {
                Log.error("Error when loading EditConfiguration", throwable);
                Window.Location.assign(JahiaGWTParameters.getContextPath() + "/errors/error_404.jsp");
            }
        });


    }

    public GWTEditConfiguration getConfiguration() {
        return configuration;
    }

    public Linker getLinker() {
        return linker;
    }

    private native void exposeFunctions() /*-{
        var nsAuthoringApi = $wnd.authoringApi || {};
        $wnd.authoringApi = nsAuthoringApi;

        nsAuthoringApi.createContent = $wnd.createContent = function (path, types, includeSubTypes, skipRefreshOnSave) {
            if (typeof includeSubTypes !== 'undefined') {
                @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::createContent(*)(path, types, includeSubTypes, skipRefreshOnSave);
            } else {
                @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::createContent(*)(path, types, true, skipRefreshOnSave);
            }
        };

        nsAuthoringApi.editContent = $wnd.editContent = function (path, displayName, types, inheritedTypes, uuid, skipRefreshOnSave, jsConfig) {
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::editContent(*)(path, displayName, types, inheritedTypes, uuid, skipRefreshOnSave, jsConfig);
        };

        nsAuthoringApi.openWorkflow = $wnd.openWorkflow = function (target) {
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::openWorkflow(*)(target);
        };
        nsAuthoringApi.showPageEdit = $wnd.showPageEdit = function (target, path, template, nodetypes) {
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::showPageEdit(*)(target, path, template, nodetypes);
        };

        nsAuthoringApi.switchSite = function (siteKey, lang) {
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::switchSite(*)(siteKey, lang);
        };

        nsAuthoringApi.switchLanguage = function (lang) {
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::switchLanguage(Ljava/lang/String;)(lang);
        };

        nsAuthoringApi.deleteContent = $wnd.deleteContent = function (uuid, path, displayName, types, inheritedTypes, skipRefreshOnDelete, deletePermanently) {
            console.log('Calling delete content', uuid, path, displayName, types, inheritedTypes, skipRefreshOnDelete, deletePermanently);
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::deleteContent(*)(uuid, path, displayName, types, inheritedTypes, skipRefreshOnDelete, deletePermanently);
        };

        nsAuthoringApi.deleteContents = $wnd.deleteContents = function (objects, skipRefreshOnDelete, deletePermanently) {
            console.log('Calling delete contents', objects, skipRefreshOnDelete, deletePermanently);
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::deleteContents(*)(objects, skipRefreshOnDelete, deletePermanently);
        };

        nsAuthoringApi.undeleteContent = $wnd.undeleteContent = function (uuid, path, displayName, nodeName) {
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::undeleteContent(*)(uuid, path, displayName, nodeName);
        };
        nsAuthoringApi.undeleteContents = $wnd.undeleteContents = function (objects) {
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::undeleteContents(*)(objects);
        };

        nsAuthoringApi.openPublicationWorkflow = function (uuids, allSubTree, allLanguages, checkForUnpublication) {
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::openPublicationWorkflow(*)(uuids, allSubTree, allLanguages, checkForUnpublication)
        };

        nsAuthoringApi.getEditTabs = function (path, uuid, displayName, types, inheritedTypes, hasOrderableChildNodes) {
            return @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::getEditTabs(*)(path, uuid, displayName, types, inheritedTypes, hasOrderableChildNodes);
        };

        if (!$wnd.jahia) {
            $wnd.jahia = new Object();
        }
        $wnd.jahia.alert = function (title, message) {
            @org.jahia.ajax.gwt.module.empty.client.EmptyEntryPoint::alert(Ljava/lang/String;Ljava/lang/String;)(title, message);
        };
    }-*/;

    /**
     * Alert message
     *
     * @param title
     * @param message
     */
    static void alert(String title, String message) {
        MessageBox.alert(title != null ? title : "Info", message, null);
    }

    public static JavaScriptObject getEditTabs(String path, String uuid, String displayName, JsArrayString nodeTypes, JsArrayString inheritedNodeTypes, boolean hasOrderableChildNodes) {
        GWTJahiaNode node = MainModule.getGwtJahiaNode(uuid, path, displayName, nodeTypes, inheritedNodeTypes);
        JSONArray editTabs = new JSONArray();
        List<GWTEngineTab> gwtEngineTabs = resolveEditTabs(hasOrderableChildNodes, getInstance().getLinker().getConfig().getEngineConfiguration(node), node);
        for (int idx = 0; idx < gwtEngineTabs.size(); idx++) {
            JSONObject jsonTab = new JSONObject();
            GWTEngineTab gwtEngineTab = gwtEngineTabs.get(idx);
            jsonTab.put("id", new JSONString(gwtEngineTab.getId()));
            jsonTab.put("title", new JSONString(gwtEngineTab.getTitle()));
            if (gwtEngineTab.getRequiredPermission() != null && gwtEngineTab.getRequiredPermission().length() > 0) {
                jsonTab.put("requiredPermission", new JSONString(gwtEngineTab.getRequiredPermission()));
            }
            editTabs.set(idx, jsonTab);
        }
        return editTabs.getJavaScriptObject();
    }

    private static List<GWTEngineTab> resolveEditTabs(boolean hasOrderableChildNodes, GWTEngineConfiguration config, GWTJahiaNode node) {
        List<GWTEngineTab> gwtEngineTabs = new ArrayList<GWTEngineTab>();
        for (GWTEngineTab tabConfig : config.getEngineTabs()) {
            EditEngineTabItem tabItem = tabConfig.getTabItem();
            if (tabConfig.showInEngine() &&
                    (tabItem.getHideForTypes().isEmpty() || !node.isNodeType(tabItem.getHideForTypes())) &&
                    ((hasOrderableChildNodes && tabItem.isOrderableTab()) || (!tabItem.isOrderableTab() && (tabItem.getShowForTypes().isEmpty() || node.isNodeType(tabItem.getShowForTypes()))))) {
                gwtEngineTabs.add(tabConfig);
            }
        }
        return gwtEngineTabs;
    }

    public static void editContent(String path, String displayName, JsArrayString nodeTypes, JsArrayString inheritedNodeTypes, String uuid, boolean skipRefreshOnSave, EditEngineJSConfig jsConfig) {
        GWTJahiaNode node = MainModule.getGwtJahiaNode(uuid, path, displayName, nodeTypes, inheritedNodeTypes);
        EngineLoader.showEditEngine(getInstance().getLinker(), node, null, skipRefreshOnSave, jsConfig);
    }

    public static void openWorkflow(String target) {
        WorkflowHistoryPanel widget = new WorkflowHistoryPanel(getInstance().getLinker());
        RootPanel rootPanel = RootPanel.get(target);
        rootPanel.clear();
        rootPanel.add(widget);
    }

    public static void showPageEdit(String target, String path, String template, String nodeTypes) {
        final RootPanel panel = RootPanel.get(target);
        panel.add(new MainModule(path, template, nodeTypes, getInstance().getConfiguration()));
    }

    public static void deleteContent(String uuid, String path, String displayName, JsArrayString nodeTypes,
                                     JsArrayString inheritedNodeTypes, boolean skipRefreshOnDelete,
                                     boolean deletePermanently) {
        GWTJahiaNode node = MainModule.getGwtJahiaNode(uuid, path, displayName, nodeTypes, inheritedNodeTypes);

        Linker linker = getInstance().getLinker();
        LinkerSelectionContext selectionContext = linker.getSelectionContext();

        selectionContext.setMainNode(node);
        selectionContext.setSelectedNodes(Collections.singletonList(node));
        selectionContext.refresh(LinkerSelectionContext.SELECTED_NODE_ONLY);

        DeleteItemWindow window = new DeleteItemWindow(linker, selectionContext, deletePermanently, skipRefreshOnDelete, JahiaGWTParameters.getBaseEditUrl());
        window.show();
    }

    public static void deleteContents(JsArray objects, boolean skipRefreshOnDelete, boolean deletePermanently) {
        Linker linker = getInstance().getLinker();
        LinkerSelectionContext selectionContext = linker.getSelectionContext();
        List<GWTJahiaNode> nodes = MainModule.getGwtJahiaNodes(objects);
        if (nodes.size() > 0) {
            selectionContext.setMainNode(nodes.get(0));
            selectionContext.setSelectedNodes(nodes);
            selectionContext.refresh(LinkerSelectionContext.SELECTED_NODE_ONLY);

            DeleteItemWindow window = new DeleteItemWindow(linker, selectionContext, deletePermanently, skipRefreshOnDelete, JahiaGWTParameters.getBaseEditUrl());
            window.show();
        }
    }

    public static void undeleteContent(final String uuid, final String path, String displayName, final String nodeName) {

        String message = Messages.getWithArgs(
                "message.undelete.confirm",
                "Do you really want to undelete the selected resource {0}?",
                new String[]{displayName}
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
        final List<GWTJahiaNode> nodes = MainModule.getGwtJahiaNodes(objects);

        String message = Messages.getWithArgs(
                "message.undelete.multiple.confirm",
                "Do you really want to undelete the {0} selected resources?",
                new String[]{String.valueOf(nodes.size())});
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

    /**
     * Function, exposed into JSNI (native JavaScript), to switch to the specified site and language
     *
     * @param siteKey the target site key
     * @param lang    the target language code
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
                JahiaGWTParameters.setLanguage(JahiaGWTParameters.getLanguage(lang));
            }
        });
    }

    private static void validateSiteLanguage(GWTJahiaNode siteNode, String lang) {
        @SuppressWarnings("unchecked") List<String> langs = (List<String>) siteNode.get("j:languages");
        if (!langs.contains(lang)) {
            throw new RuntimeException("Language '" + lang + "' is not a valid '" + siteNode.getName() + "' site language");
        }
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
        JahiaGWTParameters.setLanguage(JahiaGWTParameters.getLanguage(lang));
    }

    /**
     * Function, exposed into JSNI (native JavaScript), to call the create content wizard.
     *
     * @param path              the node path, where the content will be created
     * @param nodeTypes         the allowed node types to show in the content type selector
     * @param includeSubTypes   if <code>true</code>, also the sub-types of the specified types will be shown in the content type selector;
     *                          <code>false</code> means only the specified node types will be allowed
     * @param skipRefreshOnSave whether to avoid refreshing the edit engine on content save
     */
    public static void createContent(String path, JsArrayString nodeTypes, boolean includeSubTypes, boolean skipRefreshOnSave) {
        GWTJahiaNode parent = new GWTJahiaNode();
        parent.setPath(path);
        ContentActions.showContentWizard(getInstance().getLinker(),
                nodeTypes != null && nodeTypes.length() > 0 ? nodeTypes.join(" ") : "jmix:droppableContent", parent,
                includeSubTypes, skipRefreshOnSave);
    }

    public static void openPublicationWorkflow(JsArrayString uuids, boolean allSubTree, boolean allLanguages, boolean checkForUnpublication) {
        PublicationWorkflow.openPublicationWorkflow(MainModule.convertArray(uuids), getInstance().getLinker(), allSubTree, allLanguages, checkForUnpublication);
    }
}
