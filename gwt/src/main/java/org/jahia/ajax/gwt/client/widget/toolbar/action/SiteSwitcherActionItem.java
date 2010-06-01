package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:19:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class SiteSwitcherActionItem extends BaseActionItem {
    private transient ComboBox<GWTJahiaNode> mainComponent;
    private GWTJahiaNode selectedSite;

    private String root = "/sites";

    public void setRoot(String root) {
        this.root = root;
    }

    public SiteSwitcherActionItem() {

    }

    public void setSelectedSite(GWTJahiaNode site) {
        this.selectedSite = site;
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        initMainComponent();
        JahiaContentManagementService.App.getInstance().getRoot(root, Arrays.asList("jnt:virtualsite"), null, null,null,null,null, new BaseAsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> sites) {
                mainComponent.getStore().removeAll();
                mainComponent.getStore().add(sites);
                for (GWTJahiaNode site : sites) {
                    if (site.getUUID().equals(JahiaGWTParameters.getSiteUUID())) {
                        mainComponent.setValue(site);
                        break;
                    }
                }

                mainComponent.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                        final GWTJahiaNode jahiaNode = event.getSelection().get(0);
                        JahiaGWTParameters.setSiteUUID(jahiaNode.getUUID());
                        ((EditLinker) linker).getSidePanel().refresh(EditLinker.REFRESH_ALL);
                        ((EditLinker) linker).getMainModule().goTo(jahiaNode.getPath()+"/home", null);
                    }
                });
            }

            public void onApplicationFailure(Throwable throwable) {
                mainComponent.getStore().removeAll();
            }
        });
    }

    /**
     * init main component
     */
    private void initMainComponent() {
        mainComponent = new ComboBox<GWTJahiaNode>();
        mainComponent.setStore(new ListStore<GWTJahiaNode>());
        mainComponent.setDisplayField("displayName");
        mainComponent.setValueField("uuid");
        mainComponent.setTypeAhead(true);
        mainComponent.setTriggerAction(ComboBox.TriggerAction.ALL);
        mainComponent.setForceSelection(true);
        mainComponent.setValue(selectedSite);
        setEnabled(true);
    }


    @Override
    public Component getCustomItem() {
        return mainComponent;
    }


    @Override
    public void setEnabled(boolean enabled) {
        mainComponent.setEnabled(enabled);
    }

}