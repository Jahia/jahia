/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.templatestructure;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.JahiaBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.sites.JahiaSite;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.internal.gwt.GWTIncluder;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.Tag;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    boolean useGwt = false;
    private boolean editDivOpen = false;

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
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);

            useGwt = renderContext.isEditMode();

            ServletRequest request = pageContext.getRequest();
            // JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

            // check the gwtForGuest attribute from parent tag
            Tag parent = getParent();
            boolean gwtForGuest = false;
            if (parent instanceof TemplateTag) {
                gwtForGuest = ((TemplateTag) parent).enableGwtForGuest();
            }

            StringBuilder buf = new StringBuilder("<body");
            for (String param : attributes.keySet()) {
                buf.append(" ").append(param).append("=\"").append(attributes.get(param)).append("\"");
            }
            buf.append(">");
            //if (jData.page() != null && jData.page().getContentPage().isMarkedForDelete()) {
            //    buf.append("<div class=\"markedForDelete\">");
            //}
//            if (useGwt) {
//                if (!isLogged() && gwtForGuest) {
//                    if (gwtScript.equals("")) {
//                        gwtScript = "guest";
//                    }
//                } else if (gwtScript == null || gwtScript.equals("")) {
//                    gwtScript = "general";
//                }
//                if (isLiveMode()) {
//                    buf.append(GWTIncluder.generateGWTImport(pageContext, new StringBuilder("org.jahia.ajax.gwt.template.").append(gwtScript).append(".live.Live").toString())).append("\n");
//                } else {
//
//                    if (checkGAprofilePresent(jData)) {
//                        String gviz =
//                                "<script type='text/javascript' src='http://www.google.com/jsapi'></script>" +
//                                        "<script type='text/javascript'>" +
//                                        "google.load('visualization', '1', {packages:['annotatedtimeline','piechart','geomap']});" +
//                                        "</script>";
//                        buf.append(gviz);
//                    }
//
//                    buf.append(GWTIncluder.generateGWTImport(pageContext, new StringBuilder("org.jahia.ajax.gwt.template.").append(gwtScript).append(".edit.Edit").toString())).append("\n");
//                }
//
//                if (isLogged()) {
//                    if (renderContext == null || !renderContext.isEditMode()) {
//                        addToolbarMessageResources();
//                        // jahia module entry for toolbar
//                        buf.append("\n\t<div id=\"gwt-jahiatoolbar\" class=\"jahia-admin-gxt " + JahiaType.TOOLBARS_MANAGER + "-gxt\" jahiatype=\"").append(JahiaType.TOOLBARS_MANAGER).append("\" content=\"").append(DEFAULT_CONTENT).append("\"></div>\n");
//                    }
//                }
//            }
            buf.append("\t<div id=\"").append(DEFAULT_CONTENT).append("\">");

            pageContext.getOut().println(buf.toString());

            if (renderContext != null) {
                if (renderContext.isEditMode()) {
                    Resource r = (Resource) pageContext.getRequest().getAttribute("currentResource");
                    addEditModeResources();
                    pageContext.getRequest().setAttribute("jahia.engines.gwtModuleIncluded", Boolean.TRUE);
                    pageContext.getOut().println(GWTIncluder.generateGWTImport(pageContext, "org.jahia.ajax.gwt.module.edit.Edit"));
                    pageContext.getOut().println("<div class=\"jahia-template-gxt editmode-gxt\" id=\"editmode\" jahiatype=\"editmode\" path=\"" + r.getNode().getPath() + "\" locale=\"" + r.getLocale() + "\" template=\"" + r.getResolvedTemplate() + "\">");
                    editDivOpen = true;
                } else {
//                    Resource r = (Resource) pageContext.getRequest().getAttribute("currentResource");
//                    request.setAttribute("templateWrapper", "bodywrapper");
//                    String out = RenderService.getInstance().render(r, renderContext);
//                    pageContext.getOut().print(out);
//                    return SKIP_BODY;
                }
            }

        } catch (Exception e) {
            logger.error("Error while writing to JspWriter", e);
        }
        return EVAL_BODY_INCLUDE;
    }

    private void addToolbarMessageResources() {
        // add messages required by the subscriptions toolbar
        addGwtDictionaryMessage("subscriptions.toolbar.page.windowTitle", getMessage("subscriptions.toolbar.page.windowTitle"));
        addGwtDictionaryMessage("subscriptoolbar.analytics.timeOnSitetions.toolbar.page.event.contentPublished", getMessage("subscriptions.toolbar.page.event.contentPublished"));
        addGwtDictionaryMessage("subscriptions.toolbar.page.event.commentAdded", getMessage("subscriptions.toolbar.page.event.commentAdded"));
        addGwtDictionaryMessage("subscriptions.toolbar.page.includeChildren", getMessage("subscriptions.toolbar.page.includeChildren"));

        //google analytics resource messages
        addGwtDictionaryMessage("siteStatistics", getMessage("toolbar.analytics.siteStatistics"));
        addGwtDictionaryMessage("pageStatistics", getMessage("toolbar.analytics.pageStatistics"));
        addGwtDictionaryMessage("analyticsProfile", getMessage("toolbar.analytics.analyticsProfile"));
        addGwtDictionaryMessage("userAccount", getMessage("toolbar.analytics.userAccount"));
        addGwtDictionaryMessage("profile", getMessage("toolbar.analytics.profile"));
        addGwtDictionaryMessage("login", getMessage("toolbar.analytics.login"));
        addGwtDictionaryMessage("trackedurl", getMessage("toolbar.analytics.trackedurl"));
        addGwtDictionaryMessage("state", getMessage("toolbar.analytics.state"));
        addGwtDictionaryMessage("languageSelection", getMessage("toolbar.analytics.languageSelection"));
        addGwtDictionaryMessage("selectAll", getMessage("toolbar.analytics.selectAll"));
        addGwtDictionaryMessage("selecLanguage", getMessage("toolbar.analytics.selecLanguage"));
        addGwtDictionaryMessage("selectBeginDate", getMessage("toolbar.analytics.selectBeginDate"));
        addGwtDictionaryMessage("selectEndDate", getMessage("toolbar.analytics.selectEndDate"));
        addGwtDictionaryMessage("showData", getMessage("toolbar.analytics.showData"));
        addGwtDictionaryMessage("downloadingData", getMessage("toolbar.analytics.downloadingData"));
        addGwtDictionaryMessage("bounceRate", getMessage("toolbar.analytics.bounceRate"));
        addGwtDictionaryMessage("browser", getMessage("toolbar.analytics.browser"));
        addGwtDictionaryMessage("connectionSpeed", getMessage("toolbar.analytics.connectionSpeed"));
        addGwtDictionaryMessage("direct", getMessage("toolbar.analytics.direct"));
        addGwtDictionaryMessage("newVisits", getMessage("toolbar.analytics.newVisits"));
        addGwtDictionaryMessage("pagesPerVisit", getMessage("toolbar.analytics.pagesPerVisit"));
        addGwtDictionaryMessage("averageTime", getMessage("toolbar.analytics.averageTime"));
        addGwtDictionaryMessage("percentage", getMessage("toolbar.analytics.percentage"));
        addGwtDictionaryMessage("search", getMessage("toolbar.analytics.search"));
        addGwtDictionaryMessage("bounces", getMessage("toolbar.analytics.bounces"));
        addGwtDictionaryMessage("timeOnSite", getMessage("toolbar.analytics.timeOnSite"));
        addGwtDictionaryMessage("keyword", getMessage("toolbar.analytics.keyword"));
        addGwtDictionaryMessage("visitors", getMessage("toolbar.analytics.visitors"));
        addGwtDictionaryMessage("pageStatistics", getMessage("toolbar.analytics.pageStatistics"));
        addGwtDictionaryMessage("error", getMessage("toolbar.analytics.errorMessage"));
        addGwtDictionaryMessage("visits", getMessage("toolbar.analytics.visits"));
        addGwtDictionaryMessage("source", getMessage("toolbar.analytics.source"));
        addGwtDictionaryMessage("referral", getMessage("toolbar.analytics.referral"));
        addGwtDictionaryMessage("percentageNewVisits", getMessage("toolbar.analytics.percentageNewVisits"));
        addGwtDictionaryMessage("index", getMessage("toolbar.analytics.index"));
        addGwtDictionaryMessage("exit", getMessage("toolbar.analytics.exit"));
        addGwtDictionaryMessage("pageViews", getMessage("toolbar.analytics.pageViews"));
        addGwtDictionaryMessage("timeOnPage", getMessage("toolbar.analytics.timeOnPage"));
        addGwtDictionaryMessage("uniquePageviews", getMessage("toolbar.analytics.uniquePageviews"));
        addGwtDictionaryMessage("geographicMap", getMessage("toolbar.analytics.geographicMap"));
        addGwtDictionaryMessage("annotatedTimelime", getMessage("toolbar.analytics.annotatedTimelime"));

    }

    private void addEditModeResources() {
        addGwtDictionaryMessage("fm_copyright", Jahia.COPYRIGHT_TXT + " " + Jahia.VERSION  + "." + Jahia.getPatchNumber() + " r" + Jahia.getBuildNumber());
        addGwtDictionaryMessage("fm_newdir", getJahiaInternalResourceValue("toolbar.manager.button.createFolder"));
        addGwtDictionaryMessage("fm_newdirname", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.newDirName.label"));
        addGwtDictionaryMessage("fm_newcontent", getJahiaInternalResourceValue("toolbar.manager.button.newContent"));
        addGwtDictionaryMessage("fm_newpage", getJahiaInternalResourceValue("toolbar.manager.button.newPage"));
        addGwtDictionaryMessage("fm_newmashup", getJahiaInternalResourceValue("toolbar.manager.button.newMashup"));
        addGwtDictionaryMessage("fm_newrssmashup", getJahiaInternalResourceValue("toolbar.manager.button.newRssMashup"));
        addGwtDictionaryMessage("fm_newgadgetmashup", getJahiaInternalResourceValue("toolbar.manager.button.newGoogleGadgetMashup"));
        addGwtDictionaryMessage("fm_copy", getJahiaInternalResourceValue("toolbar.manager.button.copy"));
        addGwtDictionaryMessage("fm_cut", getJahiaInternalResourceValue("toolbar.manager.button.cut"));
        addGwtDictionaryMessage("fm_paste", getJahiaInternalResourceValue("toolbar.manager.button.paste"));
        addGwtDictionaryMessage("fm_pasteref", getJahiaInternalResourceValue("toolbar.manager.button.pasteReference"));
        addGwtDictionaryMessage("fm_lock", getJahiaInternalResourceValue("toolbar.manager.button.lock"));
        addGwtDictionaryMessage("fm_unlock", getJahiaInternalResourceValue("toolbar.manager.button.unlock"));
        addGwtDictionaryMessage("fm_remove", getJahiaInternalResourceValue("toolbar.manager.button.delete"));
        addGwtDictionaryMessage("fm_rename", getJahiaInternalResourceValue("toolbar.manager.button.rename"));
        addGwtDictionaryMessage("fm_zip", getJahiaInternalResourceValue("toolbar.manager.button.zip"));
        addGwtDictionaryMessage("fm_unzip", getJahiaInternalResourceValue("toolbar.manager.button.unzip"));
        addGwtDictionaryMessage("fm_download", getJahiaInternalResourceValue("toolbar.manager.button.download"));
        addGwtDictionaryMessage("fm_preview", getJahiaInternalResourceValue("toolbar.manager.button.preview"));
        addGwtDictionaryMessage("fm_downloadMessage", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.downloadMessage.label"));
        addGwtDictionaryMessage("fm_upload", getJahiaInternalResourceValue("toolbar.manager.button.upload"));
        addGwtDictionaryMessage("fm_webfolder", getJahiaInternalResourceValue("toolbar.manager.button.openIEFolder"));
        addGwtDictionaryMessage("fm_webfolderMessage", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.webFolderMessage.label"));
        addGwtDictionaryMessage("fm_thumbs", getJahiaInternalResourceValue("toolbar.manager.button.thumbs"));
        addGwtDictionaryMessage("fm_icons_detailed", getJahiaInternalResourceValue("toolbar.manager.button.icons.detailed"));
        addGwtDictionaryMessage("fm_list", getJahiaInternalResourceValue("toolbar.manager.button.list"));
        addGwtDictionaryMessage("fm_crop", getJahiaInternalResourceValue("toolbar.manager.button.crop"));
        addGwtDictionaryMessage("fm_resize", getJahiaInternalResourceValue("toolbar.manager.button.resize"));
        addGwtDictionaryMessage("fm_rotate", getJahiaInternalResourceValue("toolbar.manager.button.rotate"));
        addGwtDictionaryMessage("fm_rotateLeft", getJahiaInternalResourceValue("toolbar.manager.button.rotateLeft"));
        addGwtDictionaryMessage("fm_rotateRight", getJahiaInternalResourceValue("toolbar.manager.button.rotateRight"));
        addGwtDictionaryMessage("fm_refresh", getJahiaInternalResourceValue("toolbar.manager.button.refresh"));
        addGwtDictionaryMessage("fm_newcategory", getJahiaInternalResourceValue("toolbar.manager.button.newCategory"));

        addGwtDictionaryMessage("fm_copying", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.copying.label"));
        addGwtDictionaryMessage("fm_cutting", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.cutting.label"));
        addGwtDictionaryMessage("fm_downloading", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.downloading.label"));
        addGwtDictionaryMessage("fm_locking", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.locking.label"));
        addGwtDictionaryMessage("fm_mounting", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.mounting.label"));
        addGwtDictionaryMessage("fm_newfoldering", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.newfoldering.label"));
        addGwtDictionaryMessage("fm_pasting", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.pasting.label"));
        addGwtDictionaryMessage("fm_pastingref", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.pastingref.label"));
        addGwtDictionaryMessage("fm_removing", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.removing.label"));
        addGwtDictionaryMessage("fm_renaming", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.renaming.label"));
        addGwtDictionaryMessage("fm_unlocking", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unlocking.label"));
        addGwtDictionaryMessage("fm_unmounting", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unmounting.label"));
        addGwtDictionaryMessage("fm_unzipping", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unzipping.label"));
        addGwtDictionaryMessage("fm_webfoldering", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.webfoldering.label"));
        addGwtDictionaryMessage("fm_zipping", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.zipping.label"));

        addGwtDictionaryMessage("fm_confArchiveName", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.archiveName.label"));
        addGwtDictionaryMessage("fm_confMultiRemove", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.multiRemove.label"));
        addGwtDictionaryMessage("fm_confNewName", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.newName.label"));
        addGwtDictionaryMessage("fm_confOverwrite", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.overwrite.label"));
        addGwtDictionaryMessage("fm_confRemove", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.remove.label"));
        addGwtDictionaryMessage("fm_confUnlock", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.unlock.label"));
        addGwtDictionaryMessage("fm_confUnmount", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.confirm.unmount.label"));

        addGwtDictionaryMessage("fm_failCopy", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.copy.label"));
        addGwtDictionaryMessage("fm_failCrop", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.crop.label"));
        addGwtDictionaryMessage("fm_failCut", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.cut.label"));
        addGwtDictionaryMessage("fm_failDelete", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.delete.label"));
        addGwtDictionaryMessage("fm_failDownload", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.download.label"));
        addGwtDictionaryMessage("fm_failMount", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.mount.label"));
        addGwtDictionaryMessage("fm_failNewdir", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.newDir.label"));
        addGwtDictionaryMessage("fm_failPaste", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.paste.label"));
        addGwtDictionaryMessage("fm_failPasteref", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.pasteref.label"));
        addGwtDictionaryMessage("fm_failRename", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.rename.label"));
        addGwtDictionaryMessage("fm_failResize", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.resize.label"));
        addGwtDictionaryMessage("fm_failRotate", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.rotate.label"));
        addGwtDictionaryMessage("fm_failSaveSearch", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.saveSearch.label"));
        addGwtDictionaryMessage("fm_failUnlock", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.unlock.label"));
        addGwtDictionaryMessage("fm_failUnmount", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.unmount.label"));
        addGwtDictionaryMessage("fm_failUnmountLock1", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.unmountLock1.label"));
        addGwtDictionaryMessage("fm_failUnmountLock2", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.unmountLock2.label"));
        addGwtDictionaryMessage("fm_failUnzip", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.unzip.label"));
        addGwtDictionaryMessage("fm_failWebfolder", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.webfolder.label"));
        addGwtDictionaryMessage("fm_failZip", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.failure.zip.label"));

        addGwtDictionaryMessage("fm_warningLock", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.warning.lock.label"));
        addGwtDictionaryMessage("fm_warningSystemLock", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.warning.systemLock.label"));

        addGwtDictionaryMessage("fm_selection", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.selection.label"));
        addGwtDictionaryMessage("fm_deselect", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.deselect.label"));
        addGwtDictionaryMessage("fm_width", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.width.label"));
        addGwtDictionaryMessage("fm_height", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.height.label"));
        addGwtDictionaryMessage("fm_ratio", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.ratio.label"));
        addGwtDictionaryMessage("fm_newname", getJahiaInternalResourceValue("toolbar.manager.button.rename"));
        addGwtDictionaryMessage("fm_mount", getJahiaInternalResourceValue("toolbar.manager.button.mount"));
        addGwtDictionaryMessage("fm_unmount", getJahiaInternalResourceValue("toolbar.manager.button.unmount"));
        addGwtDictionaryMessage("fm_mountpoint", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.mountpoint.label"));
        addGwtDictionaryMessage("fm_mountDisclaimerLabel", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.mount.disclaimer.label"));
        addGwtDictionaryMessage("fm_mountDisclaimer", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.mount.disclaimer"));
        addGwtDictionaryMessage("fm_serveraddress", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.serveraddress.label"));
        addGwtDictionaryMessage("fm_fileMenu", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.fileMenu.label"));
        addGwtDictionaryMessage("fm_editMenu", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.editMenu.label"));
        addGwtDictionaryMessage("fm_remoteMenu", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.remoteMenu.label"));
        addGwtDictionaryMessage("fm_imageMenu", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.imageMenu.label"));
        addGwtDictionaryMessage("fm_viewMenu", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.viewMenu.label"));
        addGwtDictionaryMessage("fm_browse", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.browse.label"));
        addGwtDictionaryMessage("fm_saveSearch", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.saveSearch.label"));
        addGwtDictionaryMessage("fm_saveSearchName", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.saveSearchName.label"));
        addGwtDictionaryMessage("fm_search", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.search.label"));
        addGwtDictionaryMessage("fm_filters", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.filters.label"));
        addGwtDictionaryMessage("fm_mimes", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.mimes.label"));
        addGwtDictionaryMessage("fm_nodes", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.nodes.label"));
        addGwtDictionaryMessage("fm_information", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.information.label"));
        addGwtDictionaryMessage("fm_properties", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.properties.label"));
        addGwtDictionaryMessage("fm_portlets", getJahiaInternalResourceValue("org.jahia.admin.components.ManageComponents.applicationsList.label"));
        addGwtDictionaryMessage("fm_roles", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.roles.label"));
        addGwtDictionaryMessage("fm_modes", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.modes.label"));
        addGwtDictionaryMessage("fm_authorizations", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.authorizations.label"));

        addGwtDictionaryMessage("fm_alreadyExists", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.alreadyExists.label"));

        addGwtDictionaryMessage("fm_usages", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.label"));

        addGwtDictionaryMessage("fm_info_name", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.name.label"));
        addGwtDictionaryMessage("fm_info_path", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.path.label"));
        addGwtDictionaryMessage("fm_info_size", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.size.label"));
        addGwtDictionaryMessage("fm_info_lastModif", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.lastModif.label"));
        addGwtDictionaryMessage("fm_info_lock", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.lock.label"));
        addGwtDictionaryMessage("fm_info_nbFiles", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.nbFiles.label"));
        addGwtDictionaryMessage("fm_info_nbFolders", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.nbFolders.label"));
        addGwtDictionaryMessage("fm_info_totalSize", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.info.totalSize.label"));

        addGwtDictionaryMessage("fm_save", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.properties.save.label"));
        addGwtDictionaryMessage("fm_saveAndNew", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.properties.saveAndNew.label"));
        addGwtDictionaryMessage("fm_restore", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.properties.restore.label"));

        addGwtDictionaryMessage("fm_page", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.page.label"));
        addGwtDictionaryMessage("fm_language", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.language.label"));
        addGwtDictionaryMessage("fm_workflow", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.workflow.label"));
        addGwtDictionaryMessage("fm_versioned", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.versioned.label"));
        addGwtDictionaryMessage("fm_live", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.live.label"));
        addGwtDictionaryMessage("fm_staging", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.staging.label"));
        addGwtDictionaryMessage("fm_notify", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.usages.notify.label"));

        addGwtDictionaryMessage("fm_uploadFiles", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.uploadFile.label"));
        addGwtDictionaryMessage("fm_autoUnzip", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.autoUnzip.label"));
        addGwtDictionaryMessage("fm_addFile", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.addFile.label"));
        addGwtDictionaryMessage("fm_cancel", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.cancel.label"));
        addGwtDictionaryMessage("fm_ok", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.ok.label"));
        addGwtDictionaryMessage("fm_checkUploads", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.checkUploads.label"));

        addGwtDictionaryMessage("fm_thumbFilter", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbFilter.label"));
        addGwtDictionaryMessage("fm_thumbSort", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbSort.label"));
        addGwtDictionaryMessage("fm_thumbSortName", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbSortName.label"));
        addGwtDictionaryMessage("fm_thumbSortSize", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbSortSize.label"));
        addGwtDictionaryMessage("fm_thumbSortLastModif", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbSortLastModif.label"));
        addGwtDictionaryMessage("fm_invertSort", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.invertSort.label"));

        addGwtDictionaryMessage("fm_column_type", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.column.type.label"));
        addGwtDictionaryMessage("fm_column_locked", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.column.locked.label"));
        addGwtDictionaryMessage("fm_column_name", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.column.name.label"));
        addGwtDictionaryMessage("fm_column_path", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.column.path.label"));
        addGwtDictionaryMessage("fm_column_size", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.column.size.label"));
        addGwtDictionaryMessage("fm_column_date", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.column.date.label"));
        addGwtDictionaryMessage("fm_column_provider", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.column.provider.label"));

        addGwtDictionaryMessage("fm_repository_savedSearch", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.savedSearch.label"));
        addGwtDictionaryMessage("fm_repository_portletDefinitionRepository", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.label"));
        addGwtDictionaryMessage("fm_select_portlet", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.description.label"));
        addGwtDictionaryMessage("fm_repository_myRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.myRepository.label"));
        addGwtDictionaryMessage("fm_repository_usersRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.usersRepository.label"));
        addGwtDictionaryMessage("fm_repository_myExternalRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.myExternalRepository.label"));
        addGwtDictionaryMessage("fm_repository_sharedRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.sharedRepository.label"));
        addGwtDictionaryMessage("fm_repository_websiteRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.websiteRepository.label"));
        addGwtDictionaryMessage("fm_repository_myMashupRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.myMashupRepository.label"));
        addGwtDictionaryMessage("fm_repository_sharedMashupRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.sharedMashupRepository.label"));
        addGwtDictionaryMessage("fm_repository_websiteMashupRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.websiteMashupRepository.label"));
        addGwtDictionaryMessage("fm_repository_siteRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.siteRepository.label"));
        addGwtDictionaryMessage("fm_repository_globalRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.globalRepository.label"));
        addGwtDictionaryMessage("fm_repository_categoryRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.categoryRepository.label"));
        addGwtDictionaryMessage("fm_repository_tagRepository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.tagRepository.label"));

        addGwtDictionaryMessage("fm_portlet_ready", getJahiaInternalResourceValue("org.jahia.admin.components.ManageComponents.portletReady.label"));
        addGwtDictionaryMessage("fm_portlet_deploy", getJahiaInternalResourceValue("org.jahia.admin.components.ManageComponents.deployNewComponents.label"));
        addGwtDictionaryMessage("fm_portlet_preparewar", getJahiaInternalResourceValue("org.jahia.admin.components.ManageComponents.deploy.preparewar.label"));


        addGwtDictionaryMessage("fm_login", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.login.label"));
        addGwtDictionaryMessage("fm_logout", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.logout.label"));
        addGwtDictionaryMessage("fm_username", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.username.label"));
        addGwtDictionaryMessage("fm_password", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.password.label"));
        addGwtDictionaryMessage("fm_import", getJahiaInternalResourceValue("toolbar.manager.button.import"));
        addGwtDictionaryMessage("fm_importfile", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.importfile.label"));
        addGwtDictionaryMessage("fm_export", getJahiaInternalResourceValue("toolbar.manager.button.export"));
        addGwtDictionaryMessage("fm_exportlink", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.exportlink.label"));

        addGwtDictionaryMessage("ae_principal", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.principal.label"));
        addGwtDictionaryMessage("ae_restore_inheritance", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.restoreInheritance.label"));
        addGwtDictionaryMessage("ae_inherited_from", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.inheritedFrom.label"));
        addGwtDictionaryMessage("ae_inherited", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.inherited.label"));
        addGwtDictionaryMessage("ae_restore_all_inheritance", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.restoreAllInheritance.label"));
        addGwtDictionaryMessage("ae_break_all_inheritance", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.breakAllInheritance.label"));
        addGwtDictionaryMessage("ae_remove", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.remove.label"));
        addGwtDictionaryMessage("ae_save", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.save.label"));
        addGwtDictionaryMessage("ae_restore", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.restore.label"));
        addGwtDictionaryMessage("um_adduser", getJahiaInternalResourceValue("org.jahia.engines.users.SelectUG_Engine.newUsers.label"));
        addGwtDictionaryMessage("um_addgroup", getJahiaInternalResourceValue("org.jahia.engines.users.SelectUG_Engine.newGroups.label"));

        addGwtDictionaryMessage("mw_mashups", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.label"));
        addGwtDictionaryMessage("mw_select_portlet_def", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.label"));
        addGwtDictionaryMessage("mw_ok", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.ok.label"));
        addGwtDictionaryMessage("mw_params", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.parameters.label"));
        addGwtDictionaryMessage("mw_edit_params", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.parameters.edit.label"));
        addGwtDictionaryMessage("mw_prop_load_error", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.props.load.error.label"));
        addGwtDictionaryMessage("mw_modes_permissions", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modesperm.label"));
        addGwtDictionaryMessage("mw_modes_permissions_description", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modesperm.description.label"));
        addGwtDictionaryMessage("mw_modes_adduser", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modes.adduser.label"));
        addGwtDictionaryMessage("mw_modes_addgroup", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modes.addgroup.label"));
        addGwtDictionaryMessage("mw_roles_adduser", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.adduser.label"));
        addGwtDictionaryMessage("mw_roles_addgroup", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.addgroup.label"));
        addGwtDictionaryMessage("mw_roles_perm", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.rolesperm.label"));
        addGwtDictionaryMessage("mw_roles_perm_desc", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.rolesperm.description.label"));
        addGwtDictionaryMessage("mw_finish", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.finish.label"));
        addGwtDictionaryMessage("mw_save_as", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.saveas.label"));

        addGwtDictionaryMessage("mw_name", getJahiaInternalResourceValue("org.jahia.admin.components.ManageComponents.applicationName.label"));
        addGwtDictionaryMessage("mw_description", getJahiaInternalResourceValue("org.jahia.admin.components.ManageComponents.applicationDesc.label"));
        addGwtDictionaryMessage("mw_finish_description", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.saveas.label"));
        addGwtDictionaryMessage("mw_no_role", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.any"));

        addGwtDictionaryMessage("wizard_button_cancel", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.cancel"));
        addGwtDictionaryMessage("wizard_button_finish", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.finish"));
        addGwtDictionaryMessage("wizard_button_next", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.next"));
        addGwtDictionaryMessage("wizard_button_prev", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.prev"));
        addGwtDictionaryMessage("wizard_steps_of", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.steps.of"));
        addGwtDictionaryMessage("wizard_steps_current", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.steps.current"));
        addGwtDictionaryMessage("wizard_header_title", getJahiaInternalResourceValue("org.jahia.engines.wizard.title"));

        addGwtDictionaryMessage("add_content_wizard_column_label", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.column.label"));
        addGwtDictionaryMessage("add_content_wizard_column_name", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.column.name"));
        addGwtDictionaryMessage("add_content_wizard_card_defs_text", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.defsCard.text"));
        addGwtDictionaryMessage("add_content_wizard_card_defs_title", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.defsCard.title"));
        addGwtDictionaryMessage("add_content_wizard_card_form_error_props", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.error.props"));
        addGwtDictionaryMessage("add_content_wizard_card_form_error_title", getJahiaInternalResourceValue("org.jahia.admin.error.label"));
        addGwtDictionaryMessage("add_content_wizard_card_form_error_save", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.error.save"));
        addGwtDictionaryMessage("add_content_wizard_card_form_success_title", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.success"));
        addGwtDictionaryMessage("add_content_wizard_card_form_success_save", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.success.save"));
        addGwtDictionaryMessage("add_content_wizard_card_form_text", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.text"));
        addGwtDictionaryMessage("add_content_wizard_card_form_title", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.formCard.title"));
        addGwtDictionaryMessage("add_content_wizard_card_name_node_name", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.nameCard.nodeName"));
        addGwtDictionaryMessage("add_content_wizard_card_name_node_type", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.nameCard.nodeType"));
        addGwtDictionaryMessage("add_content_wizard_card_name_text", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.nameCard.text"));
        addGwtDictionaryMessage("add_content_wizard_card_name_title", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.nameCard.title"));
        addGwtDictionaryMessage("add_content_wizard_title", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.title"));
        addGwtDictionaryMessage("add_content_wizard_title", getJahiaInternalResourceValue("org.jahia.engines.contentmanager.addContentWizard.title"));

        addGwtDictionaryMessage("em_repository", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.repository.globalRepository.label"));
        addGwtDictionaryMessage("em_drag", getJahiaInternalResourceValue("org.jahia.jcr.edit.drag.label"));
        addGwtDictionaryMessage("em_contentlist", getJahiaInternalResourceValue("org.jahia.jcr.edit.contentlist.label"));
        addGwtDictionaryMessage("em_savetemplate", getJahiaInternalResourceValue("org.jahia.jcr.edit.savetemplate.label"));
        addGwtDictionaryMessage("em_content", getJahiaInternalResourceValue("org.jahia.jcr.edit.content.label"));
        addGwtDictionaryMessage("em_area", getJahiaInternalResourceValue("org.jahia.jcr.edit.area.label"));

        addGwtDictionaryMessage("publication_currentStatus", getJahiaInternalResourceValue("org.jahia.jcr.publication.currentStatus"));
        addGwtDictionaryMessage("publication_path", getJahiaInternalResourceValue("org.jahia.jcr.publication.path"));
        addGwtDictionaryMessage("publication_publicationAllowed", getJahiaInternalResourceValue("org.jahia.jcr.publication.publicationAllowed"));
        addGwtDictionaryMessage("publication_publicationComments", getJahiaInternalResourceValue("org.jahia.jcr.publication.publicationComments"));
        addGwtDictionaryMessage("publication_publish", getJahiaInternalResourceValue("org.jahia.jcr.publication.publish"));
        addGwtDictionaryMessage("publication_status_modified", getJahiaInternalResourceValue("org.jahia.jcr.publication.status_modified"));
        addGwtDictionaryMessage("publication_status_notyetpublished", getJahiaInternalResourceValue("org.jahia.jcr.publication.status_notyetpublished"));
        addGwtDictionaryMessage("publication_status_published", getJahiaInternalResourceValue("org.jahia.jcr.publication.status_published"));
        addGwtDictionaryMessage("publication_unpublished_text", getJahiaInternalResourceValue("org.jahia.jcr.publication.unpublished_text"));
        addGwtDictionaryMessage("publication_published_text", getJahiaInternalResourceValue("org.jahia.jcr.publication.published_text"));
        addGwtDictionaryMessage("publication_unpublished_title", getJahiaInternalResourceValue("org.jahia.jcr.publication.unpublished_title"));
        addGwtDictionaryMessage("publication_published_title", getJahiaInternalResourceValue("org.jahia.jcr.publication.published_title"));

        addGwtDictionaryMessage("ece_content", getJahiaInternalResourceValue("org.jahia.jcr.edit.content.tab"));
        addGwtDictionaryMessage("ece_layout", getJahiaInternalResourceValue("org.jahia.jcr.edit.layout.tab"));
        addGwtDictionaryMessage("ece_metadata", getJahiaInternalResourceValue("org.jahia.jcr.edit.metadata.tab"));
        addGwtDictionaryMessage("ece_classification", getJahiaInternalResourceValue("org.jahia.jcr.edit.classification.tab"));
        addGwtDictionaryMessage("ece_options", getJahiaInternalResourceValue("org.jahia.jcr.edit.options.tab"));        
        addGwtDictionaryMessage("ece_rights", getJahiaInternalResourceValue("org.jahia.jcr.edit.rights.tab"));
        addGwtDictionaryMessage("ece_categories", getJahiaInternalResourceValue("org.jahia.jcr.edit.categories.tab"));
        addGwtDictionaryMessage("ece_tags", getJahiaInternalResourceValue("org.jahia.jcr.edit.tags.tab"));
        addGwtDictionaryMessage("ece_publication", getJahiaInternalResourceValue("org.jahia.jcr.edit.publication.tab"));
    }
    public int doEndTag() {
        final StringBuilder buf = new StringBuilder("\n\t</div>\n");

        ServletRequest request = pageContext.getRequest();
        try {
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
            // JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            //if (jData.page() != null && jData.page().getContentPage().isMarkedForDelete()) {
            //    buf.append("</div>");
            //}

            if (checkGAprofileOn(renderContext.getSite()) && isLiveMode()) {
                buf.append(gaTrackingCode(((JahiaData) request.getAttribute("org.jahia.data.JahiaData"))));
            }
            if (useGwt) {
                // Generate jahia_gwt_dictionnary
                Map<String, String> dictionaryMap = getJahiaGwtDictionary();
                if (dictionaryMap != null) {
                    buf.append("<script type='text/javascript'>\n");
                    buf.append(generateJahiaGwtDictionary());
                    buf.append("</script>\n");
                }
            }
            if (editDivOpen) {
                buf.append("</div>");
            }

            buf.append("</body>");

            pageContext.getOut().println(buf.toString());
        } catch (Exception e) {
            logger.error("Error while writing to JspWriter", e);
        }

        // reset attributes
        gwtScript = null;
        attributes = new HashMap<String, Object>();
        useGwt = false;
        editDivOpen = false;
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
                            String uuid = ContentPage.getPage(jData.getProcessingContext().getPageID()).getUUID();
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
    private boolean checkGAprofileOn(JahiaSite jahiaSite) {
        boolean atLeast1TPon = false;
        // google analytics
        Iterator<?> it = ((jahiaSite.getSettings()).keySet()).iterator();
        // check if at list one profile is enabled
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith("jahiaGAprofile")) {
                if (Boolean.valueOf(jahiaSite.getSettings().getProperty(jahiaSite.getSettings().getProperty(key) + "_" + jahiaSite.getSiteKey() + "_trackingEnabled"))) {
                    atLeast1TPon = true;
                    break;
                }
            }
        }
        return atLeast1TPon;
    }

    // check if there is at least one profile is configured
    private boolean checkGAprofilePresent(JahiaSite jahiaSite) {
        boolean atLeast1TPconf = false;
        // google analytics
        Iterator<?> it = ((jahiaSite.getSettings()).keySet()).iterator();
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
