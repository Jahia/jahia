/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.templatestructure;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.JahiaBean;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.internal.gwt.GWTIncluder;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.Tag;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * <p>Title: Defines the body part of a jahia template</p>
 * <p>Description: This tag simply delimits what is the body part of a Jahia template.
 * All templates MUST have a body part. It should be used together with the template and templateHead tags. The tag also
 * implements the dynamic attributes interface allowing to specify whatever attribute should be passed to the HTML
 * body tag of the generated page.</p>
 * <p>Copyright: Copyright (c) 1999-2009</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Xavier Lawrence
 * @version 1.0
 * @jsp:tag name="templateBody" body-content="JSP" description="Defines the body part of a jahia template."
 * <p/>
 * <p><attriInfo>This tag simply delimits what is the body part of a Jahia template.
 * All templates MUST have a body part. It should be used together with the template and templateHead tags. The tag also
 * implements the dynamic attributes interface allowing to specify whatever attribute should be passed to the HTML
 * body tag of the generated page.
 * <p/>
 * <p><b>Example :</b>
 * <p/>
 * <%@ include file="common/declarations.jspf" %>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;template:template&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;    &lt;template:templateHead&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;%@ include file="common/template-head.jspf" %&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;utility:applicationResources/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;   &lt;/template:templateHead&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;   &lt;template:templateBody&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;div id="header"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;           &lt;template:include page="common/header.jsp"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;div id="pagecontent"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;           &lt;div class="content3cols"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;div id="columnA"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;template:include page="common/loginForm.jsp"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;template:include page="common/box/box.jsp"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                       &lt;template:param name="name" value="columnA_box"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;/template:include&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;/div&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;div id="columnC"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;template:include page="common/searchForm.jsp"/&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;!-- in HomePage we display site main properties --&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;div class="properties"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                       &lt;utility:displaySiteProperties/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;template:include page="common/box/box.jsp"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                       &lt;template:param name="name" value="columnC_box"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;/template:include&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;/div&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;div id="columnB"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;!--news--&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;template:include page="common/news/newsDisplay.jsp"/&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                       &lt;a class="bottomanchor" href="#pagetop"&gt;&lt;utility:resourceBundle
 * &nbsp;&nbsp;&nbsp;&nbsp;                               resourceName='pageTop' defaultValue="Page Top"/&gt;&lt;/a&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;/div&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;br class="clear"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;           &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;           &lt;!-- end of content3cols section --&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;!-- end of pagecontent section--&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;div id="footer"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;           &lt;template:include page="common/footer.jsp"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;   &lt;/template:templateBody&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;/template:template&gt;
 * </attriInfo>
 */
@SuppressWarnings("serial")
public class TemplateBodyTag extends AbstractJahiaTag implements DynamicAttributes {

    private final static transient Logger logger = Logger.getLogger(TemplateBodyTag.class);

    private final static String DEFAULT_CONTENT = "actualContent";
    private transient Map<String, Object> attributes = new HashMap<String, Object>();
    private String gwtScript;


    /**
     * Allows the template developer to specify a specific GWT javascript file to use rather than the default one.
     *
     * @param gwtScript The GWT javascript file to use if the default one does not suit needs
     * @jsp:attribute name="gwtScript" required="false" rtexprvalue="true"
     * description="Allows the template developer to specify a specific GWT javascript file to use rather than the default one."
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setGwtScript(String gwtScript) {
        this.gwtScript = gwtScript;
    }

    public int doStartTag() {
        try {
            boolean useGwt = false;
            ServletRequest request = pageContext.getRequest();
            JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

            // check the gwtForGuest attribute from parent tag
            Tag parent = getParent();
            boolean gwtForGuest = false;
            if (parent instanceof TemplateTag) {
                gwtForGuest = ((TemplateTag) parent).enableGwtForGuest();
            }
            if (jData != null) {
                useGwt = AdvPreviewSettings.isInUserAliasingMode() || isLogged() || gwtForGuest;
            }

            StringBuilder buf = new StringBuilder("<body");
            for (String param : attributes.keySet()) {
                buf.append(" ").append(param).append("=\"").append(attributes.get(param)).append("\"");
            }
            buf.append(">");
            if (jData.page().getContentPage().isMarkedForDelete()) {
                buf.append("<div class=\"markedForDelete\">");
            }
            if (useGwt) {
                addMandatoryGwtMessages(jData);
                if (!isLogged() && gwtForGuest) {
                    gwtScript = "guest";
                } else if (gwtScript == null || gwtScript.equals("")) {
                    gwtScript = "general";
                }
                if (isLiveMode()) {
                    buf.append(GWTIncluder.generateGWTImport(pageContext, new StringBuilder("org.jahia.ajax.gwt.template.").append(gwtScript).append(".live.Live").toString())).append("\n");
                } else {

                    if (checkGAprofilePresent(jData)) {
                        String gviz =
                                "<script type='text/javascript' src='http://www.google.com/jsapi'></script>" +
                                        "<script type='text/javascript'>" +
                                        "google.load('visualization', '1', {packages:['annotatedtimeline','piechart','geomap']});" +
                                        "</script>";
                        buf.append(gviz);
                    }

                    buf.append(GWTIncluder.generateGWTImport(pageContext, new StringBuilder("org.jahia.ajax.gwt.template.").append(gwtScript).append(".edit.Edit").toString())).append("\n");
                }

                if (isLogged()) {
                    addToolbarMessageResources();
                    // jahia module entry for toolbar
                    buf.append("\n\t<div id=\"gwt-jahiatoolbar\" class=\"jahia-admin-gxt " + JahiaType.TOOLBARS_MANAGER + "-gxt\" jahiatype=\"").append(JahiaType.TOOLBARS_MANAGER).append("\" content=\"").append(DEFAULT_CONTENT).append("\"></div>\n");
                }
            }
            buf.append("\t<div id=\"").append(DEFAULT_CONTENT).append("\">");

            pageContext.getOut().println(buf.toString());
        } catch (Exception e) {
            logger.error("Error while writing to JspWriter", e);
        }
        return EVAL_BODY_INCLUDE;
    }

    private void addToolbarMessageResources() {
        // add messages required by the subscriptions toolbar
        addGwtDictionaryMessage("subscriptions.toolbar.page.windowTitle", getMessage("subscriptions.toolbar.page.windowTitle"));
        addGwtDictionaryMessage("subscriptions.toolbar.page.event.contentPublished", getMessage("subscriptions.toolbar.page.event.contentPublished"));
        addGwtDictionaryMessage("subscriptions.toolbar.page.event.commentAdded", getMessage("subscriptions.toolbar.page.event.commentAdded"));
        addGwtDictionaryMessage("subscriptions.toolbar.page.includeChildren", getMessage("subscriptions.toolbar.page.includeChildren"));
    }

    /**
     * Add mandatory messages
     * @param jData
     */
    private void addMandatoryGwtMessages(JahiaData jData) {
        addGwtDictionaryMessage("display", JahiaResourceBundle.getJahiaInternalResource("toolbar.message.display", jData.getProcessingContext().getLocale()));
        addGwtDictionaryMessage("reset", JahiaResourceBundle.getJahiaInternalResource("toolbar.message.reset", jData.getProcessingContext().getLocale()));
        addGwtDictionaryMessage("hide_alert", JahiaResourceBundle.getJahiaInternalResource("toolbar.message.hide.alert", jData.getProcessingContext().getLocale()));
        addGwtDictionaryMessage("hide_warning", JahiaResourceBundle.getJahiaInternalResource("toolbar.message.hide.warning", jData.getProcessingContext().getLocale()));
        addGwtDictionaryMessage("hide_all", JahiaResourceBundle.getJahiaInternalResource("toolbar.message.hide.all", jData.getProcessingContext().getLocale()));
    }

    public int doEndTag() {
        final StringBuilder buf = new StringBuilder("\n\t</div>\n");

        ServletRequest request = pageContext.getRequest();
        try {
            JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            if (jData.page().getContentPage().isMarkedForDelete()) {
                buf.append("</div>");
            }

            if (checkGAprofileOn(jData) && isLiveMode()) {
                buf.append(gaTrackingCode(((JahiaData) request.getAttribute("org.jahia.data.JahiaData"))));
            }
            // Generate jahia_gwt_dictionnary
            Map<String, String> dictionaryMap = getJahiaGwtDictionary();
            if (dictionaryMap != null) {
                buf.append("<script type='text/javascript'>\n");
                buf.append(generateJahiaGwtDictionary());
                buf.append("</script>\n");
            }
            buf.append("</body>");

            pageContext.getOut().println(buf.toString());
        } catch (Exception e) {
            logger.error("Error while writing to JspWriter", e);
        }

        // reset attributes
        gwtScript = null;
        attributes = new HashMap<String, Object>();
        return EVAL_PAGE;
    }

    private boolean isLiveMode() {
        final JahiaBean jBean = (JahiaBean) pageContext.getAttribute("jahia", PageContext.REQUEST_SCOPE);
        return jBean.getRequestInfo().isNormalMode();
    }

    public void setDynamicAttribute(String s, String s1, Object o) throws JspException {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(s1, o);
    }

    // google analytics
    private String gaTrackingCode(JahiaData jData) {

        String GATC = "\n<script type=\"text/javascript\">\n" + "var gaJsHost = ((\"https:\" == document.location.protocol) " +
                "? \"https://ssl.\" : \"http://www.\");\n" + "document.write(unescape(\"%3Cscript src='\" + gaJsHost + \"" +
                "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E\"));\n" + "</script>\n" +
                "<script type=\"text/javascript\">\ntry{\n";

        JahiaSite currentSite = jData.getProcessingContext().getSite();
        // get enabled profiles
        //Map<String, String> enabledProfiles = new HashMap<String, String>();
        Iterator<?> it = ((currentSite.getSettings()).keySet()).iterator();
        // check if at list one profile is enabled
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith("jahiaGAprofile")) {
                if (Boolean.valueOf(currentSite.getSettings().getProperty(currentSite.getSettings().getProperty(key) + "_" + currentSite.getSiteKey() + "_trackingEnabled"))) {
                    // enabledProfiles.put(key, currentSite.getSettings().getProperty(currentSite.getSettings().getProperty(key)));
                    String acc = currentSite.getSettings().getProperty(currentSite.getSettings().getProperty(key) + "_" + currentSite.getSiteKey() + "_gaUserAccount");
                    String tracker = "var " + (currentSite.getSettings().getProperty(key).replace(" ", "")) + "Tracker = _gat._getTracker('" + acc + "');\n";
                    String trackedUrls = currentSite.getSettings().getProperty(currentSite.getSettings().getProperty(key) + "_" + currentSite.getSiteKey() + "_trackedUrls");
                    String url = "";
                    if (trackedUrls.equals("virtual")) {
                        org.jahia.services.importexport.ImportExportService ies = ServicesRegistry.getInstance().getImportExportService();
                        try {
                            String uuid = ies.getUuid(ContentPage.getPage(jData.getProcessingContext().getPageID()));
                            String lang = jData.getProcessingContext().getLocale().toString();
                            url = "'/Unique_Universal_id/" + uuid + "/" + lang + "/'";
                        } catch (JahiaException e) {
                            logger.error("Error in gaTrackingCode", e);
                        }
                    }
                    String trackPageview = (currentSite.getSettings().getProperty(key).replace(" ", "")) + "Tracker._trackPageview(" + url + ");\n";
                    logger.info(tracker);
                    GATC = GATC + tracker + trackPageview;
                }
            }
        }
        GATC = GATC + "\n} catch(err) {}\n</script>";
        return GATC;
    }

    // check if there is at least one profile is enabled
    private boolean checkGAprofileOn(JahiaData jData) {
        boolean atLeast1TPon = false;
        JahiaSite currentSite = jData.getProcessingContext().getSite();
        // google analytics
        Iterator<?> it = ((currentSite.getSettings()).keySet()).iterator();
        // check if at list one profile is enabled
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith("jahiaGAprofile")) {
                if (Boolean.valueOf(currentSite.getSettings().getProperty(currentSite.getSettings().getProperty(key) + "_" + currentSite.getSiteKey() + "_trackingEnabled"))) {
                    atLeast1TPon = true;
                    break;
                }
            }
        }
        return atLeast1TPon;
    }

    // check if there is at least one profile is configured
    private boolean checkGAprofilePresent(JahiaData jData) {
        boolean atLeast1TPconf = false;
        JahiaSite currentSite = jData.getProcessingContext().getSite();
        // google analytics
        Iterator<?> it = ((currentSite.getSettings()).keySet()).iterator();
        // check if at list one profile is enabled
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith("jahiaGAprofile")) {
                logger.info("AT least one profile in the db !!!!");
                atLeast1TPconf = true;
                break;
            }
        }
        return atLeast1TPconf;
    }
}
