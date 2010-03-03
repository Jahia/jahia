package org.jahia.ajax.gwt.client.widget.content.compare;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 2, 2010
 * Time: 9:32:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class VersionViewer extends ContentPanel {
    private GWTJahiaNode currentNode;
    private Linker linker = null;
    private String locale;
    private int currentMode = Constants.MODE_LIVE;
    private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    private String workspace = "default";

    /**
     * Constructor
     *
     * @param node
     * @param mode
     * @param linker
     */
    public VersionViewer(GWTJahiaNode node, int mode, String locale, Linker linker) {
        super();
        this.linker = linker;
        this.currentNode = node;
        this.currentMode = mode;
        this.locale = locale;
        this.workspace = "default";
        if (currentMode == Constants.MODE_LIVE) {
            workspace = "live";
        }
        init();
    }

    /**
     * init component
     */
    private void init() {
        final ToolBar headerToolBar = new ToolBar();
        setTopComponent(headerToolBar);
        // combo box that allows to select the version
        final ComboBox<GWTJahiaNodeVersion> versionComboBox = new ComboBox<GWTJahiaNodeVersion>();
        versionComboBox.setForceSelection(true);
        versionComboBox.setWidth(400);
        versionComboBox.setEditable(false);
        if (currentMode == Constants.MODE_PREVIEW || currentMode == Constants.MODE_STAGING) {
            versionComboBox.setEmptyText(Messages.get("label_staging_version", "Staging version"));
        } else {
            versionComboBox.setEmptyText(Messages.get("label_live_version", "Live version"));
        }

        // prepare data before rendering
        versionComboBox.setView(new ListView<GWTJahiaNodeVersion>() {
            @Override
            protected GWTJahiaNodeVersion prepareData(GWTJahiaNodeVersion model) {
                super.prepareData(model);
                if (model.getVersionNumber() != null) {
                    model.set("displayField", Messages.get("label_version", "Version ") + model.getVersionNumber() + " (" + DateTimeFormat.getFormat("d/MM/y hh:mm").format(model.getDate()) + ")");
                } else {
                    if (currentMode == Constants.MODE_PREVIEW || currentMode == Constants.MODE_STAGING) {
                        model.set("displayField", Messages.get("label_staging_version", "Staging version"));
                    } else {
                        model.set("displayField", Messages.get("label_live_version", "Live version"));
                    }
                }
                return model;
            }
        });
        versionComboBox.setDisplayField("displayField");

        // ToDO: add a template to display more information about he version like the comment
        //versionComboBox.setTemplate(getTemplate());

        // load version with pagination
        final RpcProxy<PagingLoadResult<GWTJahiaNodeVersion>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaNodeVersion>>() {
            @Override
            public void load(Object loadConfig, AsyncCallback<PagingLoadResult<GWTJahiaNodeVersion>> callback) {
                loadVersions((PagingLoadConfig) loadConfig, callback);
            }
        };

        // paging loader
        final PagingLoader<PagingLoadResult<GWTJahiaNodeVersion>> loader = new BasePagingLoader<PagingLoadResult<GWTJahiaNodeVersion>>(proxy);
        loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {
            public void handleEvent(LoadEvent be) {
                be.<ModelData>getConfig().set("start", be.<ModelData>getConfig().get("offset"));
            }
        });

        // list store that contains previous version and current workspace version
        final ListStore<GWTJahiaNodeVersion> store = new ListStore<GWTJahiaNodeVersion>(loader);
        versionComboBox.setStore(store);
        versionComboBox.setLazyRender(false);
        versionComboBox.setTypeAhead(true);
        versionComboBox.setHideTrigger(false);
        versionComboBox.setPageSize(10);
        //versionComboBox.setTriggerAction(ComboBox.TriggerAction.ALL);

        versionComboBox.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNodeVersion>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNodeVersion> event) {
                if (versionComboBox.getValue() != null) {
                    Log.debug("Swtich to the following url: " + versionComboBox.getValue().getNode().getUrl());
                    load(versionComboBox.getValue());
                } else {
                    Log.debug("Loading version is null");
                    setUrl(null);
                }
            }
        });


        // add in the toolbar
        headerToolBar.add(versionComboBox);

        load(null);
    }

    /**
     * Load versions
     *
     * @param loadConfig
     * @param callback
     */
    private void loadVersions(PagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<GWTJahiaNodeVersion>> callback) {
        int limit = 500;
        int offset = 0;
        if (loadConfig != null) {
            limit = loadConfig.getLimit();
            offset = loadConfig.getOffset();
        }

        JahiaContentManagementService.App.getInstance().getVersions(currentNode, workspace, limit, offset, callback);

    }

    /**
     * Render widget
     */
    private void load(GWTJahiaNodeVersion version) {
        if (currentNode != null) {
            mask();
            if (version == null || (version.getVersionNumber() == null || version.getVersionNumber().length() == 0)) {
                // version is not specified. Current.
                contentService.getNodeURL(currentNode.getPath(), locale, currentMode, new AsyncCallback<String>() {
                    public void onSuccess(String url) {
                        setUrl(url);
                        unmask();
                    }

                    public void onFailure(Throwable throwable) {
                        Log.error("", throwable);
                        unmask();
                    }
                });
            } else {
                contentService.getNodeURL(version.getNode().getPath(), version.getVersionNumber(), workspace, locale, currentMode, new AsyncCallback<String>() {
                    public void onSuccess(String url) {
                        setUrl(url);
                        unmask();
                    }

                    public void onFailure(Throwable throwable) {
                        Log.error("", throwable);
                        unmask();
                    }
                });
            }
        }
    }

    private native String getTemplate() /*-{
     return [
     '<tpl for="."><div class="search-item">',
     '<h3><span>{date:date("MM/dd/y")}<br />by {author}</span>{title}</h3>',
     '{excerpt}',
     '</div></tpl>'
     ].join("");
   }-*/;
}
