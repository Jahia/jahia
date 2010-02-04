package org.jahia.ajax.gwt.client.util.security;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;

import org.jahia.ajax.gwt.client.widget.security.RolePrincipalPanel;
import org.jahia.ajax.gwt.client.widget.security.RolesManager;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 2, 2010
 * Time: 4:43:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class RoleEditor {
    public static final String PERMISSION_ROLE = "gwtpermissionrole";
    public static final String ROLE_PRINCIPAL = "gwtroleprincipal";

    /**
     * Init role permission
     */
    public static void initPermissionRole() {

        final RootPanel panel = RootPanel.get(PERMISSION_ROLE);
        if (panel != null) {
            panel.add(new RolesManager(panel.getElement().getAttribute("rootPath")));
        }

    }

    /**
     * init Principal role
     */
    public static void initPrincipalRole() {
        final RootPanel panel = RootPanel.get(ROLE_PRINCIPAL);
        if (panel != null) {
            String principal = DOM.getElementAttribute(panel.getElement(), "principal");
            String server = DOM.getElementAttribute(panel.getElement(), "site");
            String site = DOM.getElementAttribute(panel.getElement(), "server");
            panel.add(new RolePrincipalPanel(true,false,"root"));
        }
    }
}
