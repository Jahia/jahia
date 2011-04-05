package org.jahia.services.uicomponents.bean.contentmanager;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 4/5/11
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class StudioVisibility extends Visibility {

    @Override
    public boolean getRealValue(JCRNodeWrapper contextNode, JahiaUser jahiaUser, Locale locale, HttpServletRequest request) {
        return contextNode.getPath().startsWith("/templateSets");
    }
}
