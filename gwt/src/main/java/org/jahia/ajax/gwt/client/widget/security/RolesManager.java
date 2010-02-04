package org.jahia.ajax.gwt.client.widget.security;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.widget.content.ContentManagerEmbedded;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 3, 2010
 * Time: 10:58:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class RolesManager extends LayoutContainer {
    
    private String rootPath;


    public RolesManager(final String rootPath) {
        super();
        this.rootPath = rootPath;
    }


    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        setLayout(new FillLayout());

        TabPanel tabPanel = new TabPanel();
        tabPanel.setBorders(false);
        tabPanel.setSize(600, 500);
        TabItem managerItem = new TabItem(Messages.get("label_rolemanager","Roles manager"));
        managerItem.add(new ContentManagerEmbedded(rootPath,null, null, null, ManagerConfigurationFactory.ROLESMANAGER));
        tabPanel.add(managerItem);

        TabItem rolePermisionItem = new TabItem(Messages.get("label_rolepermissionmapping","Roles/permission mapping"));
        rolePermisionItem.add(new PermissionRolePanel());
        tabPanel.add(rolePermisionItem);


        add(tabPanel);
    }
}
