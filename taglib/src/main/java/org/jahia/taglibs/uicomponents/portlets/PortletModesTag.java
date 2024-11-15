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
package org.jahia.taglibs.uicomponents.portlets;

import org.apache.pluto.container.PortletWindow;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.beans.portlets.PortletModeBean;
import org.jahia.data.beans.portlets.PortletWindowBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.services.render.RenderContext;
import org.jahia.utils.i18n.ResourceBundles;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.*;

/**
 * <p>Title: Renders list of portlet modes</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 *          <p/>
 *          <p/>
 *          Bean that contains all information relative to a portlet
 *          window.</p>
 *          <p>Description: Used to build user interfaces for template developers when
 *          using portlets</p>
 * @jsp:tag name="portletModes" body-content="empty"
 * description="Renders list of portlet modes.
 * <p/>
 * <p><attriInfo>Use this tag to display the portlet's supported modes which are dependant on each portlet. Examples include Edit, View,
 * Preview etc...
 * <p/>
 * <p>This tag is typically used in conjunction with <a href='windowStates.html' target='tagFrame'>jahiaHtml:windowStates</a>.
 * <p/>
 * <p><b>Example :</b> Go to <a href='windowStates.html' target='tagFrame'>jahiaHtml:windowStates</a>.
 * <p/>
 * <p/>
 * <p>Introduced as from Jahia version 4.5
 * <p/>
 * </attriInfo>"
 */

@SuppressWarnings("serial")
public class PortletModesTag extends TagSupport {

    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(PortletModesTag.class);

    private String namePostFix = "";
    private String name = null;
    private String resourceBundle = ResourceBundles.JAHIA_INTERNAL_RESOURCES;
    private String listCSSClass = "portletModes";
    private String currentCSSClass = "current";
    private JCRNodeWrapper node = null;
    private String var;

    /**
     * @return name of the pageContext attribute holding the {@link PortletWindowBean}
     * @jsp:attribute name="name" required="true" rtexprvalue="true"
     * description="name of the pageContext attribute holding the PortletWindowBean
     * <p/>
     * <p><attriInfo>
     * </attriInfo>"
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PortletModesTag() {
    }

    /**
     * @return String to append to portlet name's resource bundle key
     * @jsp:attribute name="namePostFix" required="false" rtexprvalue="true"
     * description="String to append to portlet name's resource bundle key
     * <p/>
     * <p><attriInfo>This corresponds to the following code in HTMLToolbox.drawWindowStateList() :
     * <br>getResource(resourceBundle,\"org.jahia.taglibs.html.portlets.portletmodes.\" + curPortletModeBean.getName()
     * + \".label\" + namePostFix  + \"</span></a>\")
     * <p>Default is \"\".
     * </attriInfo>"
     */
    public String getNamePostFix() {
        return namePostFix;
    }

    public void setNamePostFix(String namePostFix) {
        this.namePostFix = namePostFix;
    }

    /**
     * @return the name of the resource bundle
     * @jsp:attribute name="resourceBundle" required="false" rtexprvalue="true"
     * description="resource bundle to use to find portlet mode labels.
     * <p/>
     * <p><attriInfo>Default is 'JahiaInternalResources'.
     * </attriInfo>"
     */
    public String getResourceBundle() {
        return resourceBundle;
    }

    public void setResourceBundle(String resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * @return CSS class to use to display portlet mode labels
     * @jsp:attribute name="listCSSClass" required="false" rtexprvalue="true"
     * description="CSS class to use to display portlet mode labels.
     * <p/>
     * <p><attriInfo>Default is 'portletModes'.
     * </attriInfo>"
     */
    public String getListCSSClass() {
        return listCSSClass;
    }

    public void setListCSSClass(String listCSSClass) {
        this.listCSSClass = listCSSClass;
    }

    /**
     * @return CSS class to use to display the currently selected portlet mode label
     * @jsp:attribute name="currentCSSClass" required="false" rtexprvalue="true"
     * description="CSS class to use to display the currently selected portlet mode label.
     * <p/>
     * <p><attriInfo>Default is 'current'.
     * </attriInfo>"
     */
    public String getCurrentCSSClass() {
        return currentCSSClass;
    }

    public void setCurrentCSSClass(String currentCSSClass) {
        this.currentCSSClass = currentCSSClass;
    }

    public JCRNodeWrapper getNode() {
        return node;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    @Override
    public int doStartTag() throws JspException {

        RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
        PortletWindowBean portletWindowBean = null;
        if (name != null) {
            portletWindowBean = (PortletWindowBean) pageContext.
                    findAttribute(name);
        } else if (node != null && node instanceof JCRPortletNode) {
            try {
                EntryPointInstance entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance((JCRPortletNode) node);
                if (entryPointInstance == null) {
                    logger.error("User " + renderContext.getUser().getName() + " could not load the portlet instance :" + node.getIdentifier()+" Or it the portlet is not available anymore.........................................................................................");
                }

                if (entryPointInstance != null) {
                    ApplicationBean appBean = ServicesRegistry.getInstance().
                            getApplicationsManagerService().
                            getApplicationByContext(entryPointInstance.getContextName());
                    if (appBean != null) {
                        String defName = entryPointInstance.getDefName();
                        int separatorPos = defName.indexOf("###");
                        String portletWindowID = "1";
                        if (separatorPos != -1) {
                            String portletDefName = defName.substring(0, separatorPos);
                            String portletEntityID = defName.substring(separatorPos + "###".length());
                            defName = portletDefName;
                            portletWindowID = portletEntityID;
                        } else {
                            int plutoSeperatorPos = defName.lastIndexOf(".");
                            if (plutoSeperatorPos != -1) {
//                                String portletContext = defName.substring(0, plutoSeperatorPos);
                                String portletDefName = defName.substring(plutoSeperatorPos + ".".length());
                                defName = portletDefName;
                                portletWindowID = entryPointInstance.getID();
                            }
                        }
                        EntryPointDefinition entryPointDefinition = appBean.getEntryPointDefinitionByName(defName);
                        PortletWindow window = ServicesRegistry.getInstance().getApplicationsManagerService().getPortletWindow(entryPointInstance, portletWindowID, renderContext.getUser(), renderContext.getRequest(), renderContext.getResponse(), pageContext.getServletContext(), node.getSession().getWorkspace().getName());
                        if(window!=null) {
                            portletWindowBean = new PortletWindowBean(renderContext.getUser(), renderContext.getRequest(), window);
                            portletWindowBean.setEntryPointInstance(entryPointInstance);
                            portletWindowBean.setEntryPointDefinition(entryPointDefinition);
                        }
                    }
                }
            } catch (RepositoryException e) {
                throw new JspTagException(e);
            } catch (JahiaException e) {
                throw new JspTagException(e);
            } catch (Exception e) {
                logger.error("Error accessing portlet : "+node.getPath()+" "+e.getMessage());
                return SKIP_BODY;
            }

        } else {
            throw new JspTagException("Either node or name must be defined");
        }
        if (portletWindowBean == null) {
            logger.error("Couldn't find any PortletWindowBean with name " + name + " or for node " + node);
            return SKIP_BODY;
        }

        JspWriter out = pageContext.getOut();
        try {

            drawPortletModeList(portletWindowBean, namePostFix,
                    resourceBundle, listCSSClass,
                    currentCSSClass, renderContext.getMainResourceLocale(), renderContext.getMainResource().getWorkspace(), out);

        } catch (IOException ioe) {
            logger.error("IO exception while trying to display action menu for object " + name, ioe);
        }

        return SKIP_BODY;
    }

    public int doEndTag()
            throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        namePostFix = "";
        name = null;
        resourceBundle = ResourceBundles.JAHIA_INTERNAL_RESOURCES;
        listCSSClass = "portletModes";
        currentCSSClass = "current";
        node = null;
        var = null;
        return EVAL_PAGE;
    }

    public void drawPortletModeList(final PortletWindowBean portletWindowBean,
                                    final String namePostFix,
                                    final String resourceBundle,
                                    final String listCSSClass,
                                    final String currentCSSClass,
                                    final Locale locale,
                                    final String workspaceName,
                                    final JspWriter out)
            throws IOException {
        final List<PortletModeBean> portletModeBeansIterList = portletWindowBean.getPortletModeBeans(workspaceName);
        // draw mode links only if there is more than 1 mode
        if (portletModeBeansIterList.size() < 2) {
            return;
        }
        Map<String, String> modeUrls = new HashMap<String, String>();
        StringBuilder s = new StringBuilder();
        s.append("<ul class=\"");
        s.append(listCSSClass);
        s.append("\">\n");
        for (PortletModeBean curPortletModeBean : portletWindowBean.getPortletModeBeans(workspaceName)) {
            if (curPortletModeBean.getName().equals(portletWindowBean.
                    getCurrentPortletModeBean().getName())) {
                s.append("<li class=\"");
                s.append(currentCSSClass);
                s.append("\">\n");
            } else {
                s.append("<li>");
            }
            s.append("<a class=\"").append(curPortletModeBean.getName()).
                    append("\" title=\"").append(curPortletModeBean.getName()).
                    append("\" href=\"").append(curPortletModeBean.getURL()).
                    append("\">").append("<span>").append(getResource(locale, resourceBundle,
                    "org.jahia.taglibs.html.portlets.portletmodes." +
                            curPortletModeBean.getName() + ".label" +
                            namePostFix)).append("</span></a>");
            s.append("</li>");
            modeUrls.put(getResource(locale, resourceBundle,
                    "org.jahia.taglibs.html.portlets.portletmodes." +
                            curPortletModeBean.getName() + ".label" +
                            namePostFix), curPortletModeBean.getURL());
        }
        s.append("</ul>");
        if (var != null) {
            pageContext.setAttribute(var, modeUrls);
        } else {
            out.print(s.toString());
        }
    }

    public String getResource(Locale locale,
                              final String resourceBundle,
                              final String resourceName) {
        ResourceBundle res;
        String resValue = null;

        try {
            res = ResourceBundle.getBundle(resourceBundle, locale);
            resValue = res.getString(resourceName);
        } catch (MissingResourceException mre) {
            logger.warn("Error accessing resource " + resourceName +
                    " in bundle " + resourceBundle + " for locale " +
                    locale + ":" + mre.getMessage());
        }
        return resValue;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }
}
