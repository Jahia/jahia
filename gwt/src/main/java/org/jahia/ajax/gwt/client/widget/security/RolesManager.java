package org.jahia.ajax.gwt.client.widget.security;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.content.ContentManagerEmbedded;

/**
 * User: ktlili
 * Date: Feb 3, 2010
 * Time: 10:58:17 AM
 */
public class RolesManager extends LayoutContainer {

    private String config;
    private String siteKey;

    public RolesManager(final String config, final String siteKey) {
        super();
        this.config = config;
        this.siteKey = siteKey != null && siteKey.length() > 0 ? siteKey : null;
    }


    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        setLayout(new FillLayout());

        JahiaContentManagementService.App.getInstance().getManagerConfiguration(config, new BaseAsyncCallback<GWTManagerConfiguration>() {
            public void onSuccess(GWTManagerConfiguration config) {
                PermissionsUtils.loadPermissions(config.getPermissions());
                final ContentManagerEmbedded cm = new ContentManagerEmbedded(null, null, null, config);
                final PermissionRolePanel pr = new PermissionRolePanel(siteKey);
                cm.getLinker().registerExtraComponent(pr);

                TabPanel tabPanel = new TabPanel();
                tabPanel.setBorders(false);
                tabPanel.setSize(600, 500);
                TabItem managerItem = new TabItem(Messages.get("label_rolemanager", "Role manager"));
                managerItem.add(cm);
                tabPanel.add(managerItem);

                TabItem rolePermisionItem = new TabItem(Messages.get("label_rolepermissionmapping", "Role/permission mapping"));
                rolePermisionItem.add(pr);
                tabPanel.add(rolePermisionItem);


                add(tabPanel);
                layout();
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error(throwable.getMessage(), throwable);
            }
        });

    }
}
