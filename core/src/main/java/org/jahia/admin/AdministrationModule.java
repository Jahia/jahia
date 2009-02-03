package org.jahia.admin;

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.params.ParamBean;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Feb 2, 2009
 * Time: 9:51:06 AM
 * To change this template use File | Settings | File Templates.
 */
public interface AdministrationModule {

    public String getUrlKey();

    public String getIcon();

    public String getLabel();

    public String getLink();

    public String getName();

    public String getTooltip();

    public boolean isServerModule();

    public void setServerModule(boolean serverModule);

    public String getActionURL(ParamBean paramBean) throws Exception;

    public boolean isEnabled(JahiaUser user, int siteID);

    public String getPermissionName();

    public String getUrlType();

    public String getUrlAction();

    public String getUrlParams();

    public void init(ServletContext servletContext);

    public void service(HttpServletRequest request, HttpServletResponse response) throws Exception;

    public boolean isSelected(ParamBean ctx);

}
