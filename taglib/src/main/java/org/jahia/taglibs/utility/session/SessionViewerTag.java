/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.utility.session;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.taglibs.AbstractJahiaTag;

/**
 * <p>Title: Debugging tool that displays the content of the current HTTP
 * session.</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 *
 * @jsp:tag name="sessionViewer" body-content="empty"
 * description="Debugging tool that displays the content of the current HTTP session attributes.
 *
 * <p><attriInfo>Typical output:
 *
 * <p>
 *
 *<fieldset>
 *
 Name<br>
 Type<br>
 Value<br>
 2_1_contentContainermain_1_sort_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerSorterBean<br>
 <br>
 op<br>
 java.lang.String<br>
 edit<br>
 AddContainer<br>
 java.lang.String<br>
 true<br>
 org.jahia.services.multilang.currentlocale<br>
 java.util.Locale<br>
 en<br>
 5_1_siteSettings_search_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.services.search.ContainerSearcher<br>
 Query=<br>
 2_1_contentContainermain_1_search_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.services.search.ContainerSearcher<br>
 Query=<br>
 2_1_contentContainermain_1_filter_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerFilters<br>
 Container filters:[]<br>
 3_1_topMenu_search_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.services.search.ContainerSearcher<br>
 Query=<br>
 3_1_topMenu_search_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.services.search.ContainerSearcher<br>
 Query=<br>
 5_1_siteSettings_filter_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerFilters<br>
 Container filters:[]<br>
 4_1_quickLinkContainer_search_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.services.search.ContainerSearcher<br>
 Query=<br>
 5_1_siteSettings_sort_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerSorterBean<br>
 <br>
 4_1_quickLinkContainer_filter_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerFilters<br>
 Container filters:[]<br>
 4_1_quickLinkContainer_sort_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerSorterBean<br>
 <br>
 jahia_session_engineMap<br>
 java.util.HashMap<br>
 Key<br>
 Key type<br>
 Value type<br>
 Value<br>
 javaScriptPath<br>
 java.lang.String<br>
 java.lang.String<br>
 /jahia/javascript/jahia.js<br>
 containerParentID<br>
 java.lang.String<br>
 java.lang.Integer<br>
 0<br>
 engineUrl<br>
 java.lang.String<br>
 java.lang.String<br>
 /jahia/Jahia/engineName/addcontainer/op/edit/pid/1?clistid=0&cdefid=20&cpid=1&cparentid=0<br>
 enableContentDefinition<br>
 java.lang.String<br>
 java.lang.Boolean<br>
 true<br>
 screen<br>
 java.lang.String<br>
 java.lang.String<br>
 categories<br>
 addcontainer.fieldIDs<br>
 java.lang.String<br>
 java.util.List<br>
 [-1, -2, -3, -4]<br>
 jspSource<br>
 java.lang.String<br>
 java.lang.String<br>
 add_container<br>
 UpdateContainer_Engine.JahiaContentContainerFacade<br>
 java.lang.String<br>
 org.jahia.data.containers.JahiaContentContainerFacade<br>
 org.jahia.data.containers.JahiaContentContainerFacade&amp;#64;6a16d4<br>
 theContainer<br>
 java.lang.String<br>
 org.jahia.data.containers.JahiaContainer<br>
 org.jahia.data.containers.JahiaContainer&amp;#64;100e398<br>
 flatCategoryList<br>
 java.lang.String<br>
 java.util.ArrayList<br>
 [org.jahia.services.categories.Category&amp;#64;1814dd0]<br>
 ManageCategories.fieldForm<br>
 java.lang.String<br>
 java.lang.String<br>
 <!-- FIXME : The following javascript file path are hardcoded. --> <script language=&amp;quot;javascript&amp;quot; src=&amp;quot;/jahia/javascript/selectbox.js&amp;quot;></script> <script language=&amp;quot;javascript&amp;quot; src=&amp;quot;/jahia/javascript/checkbox.js&amp;quot;></script> <!--#HELP# <script language=&amp;quot;javascript&amp;quot; src=&amp;quot;/jahia/javascript/help.js&amp;quot;></script> --> <script language=&amp;quot;javascript&amp;quot;> // Overide the previous check function function check() { return true; } function sendForm(method,params) { document.mainForm.method = &amp;quot;POST&amp;quot;; document.mainForm.action = &amp;apos;/jahia/Jahia/engineName/addcontainer/op/edit/pid/1?clistid=0&amp;cdefid=20&amp;cpid=1&amp;cparentid=0&amp;screen=categories&amp;apos; if ( params.charAt(0) == &amp;quot;&amp;&amp;quot; ){ document.mainForm.action += params; } else { document.mainForm.action += &amp;quot;&amp;&amp;quot; + params; } document.mainForm.submit(); } </script> <table class=&amp;quot;text&amp;quot; border=&amp;quot;0&amp;quot; cellspacing=&amp;quot;0&amp;quot; cellpadding=&amp;quot;0&amp;quot; width=&amp;quot;600&amp;quot; align=&amp;quot;center&amp;quot;> <tr> <td>&amp;nbsp;</td> <td>Please select the categories this object should be associated with.</td> </tr> <tr> <td>&amp;nbsp;</td> <td><br> <table class=&amp;quot;text&amp;quot; border=&amp;quot;0&amp;quot; cellspacing=&amp;quot;0&amp;quot; cellpadding=&amp;quot;0&amp;quot;> <tr class=&amp;quot;sitemap1&amp;quot;> <td>&amp;nbsp;</td> <td class=&amp;quot;text&amp;quot; nowrap> <img alt=&amp;quot;&amp;quot; border=&amp;quot;0&amp;quot; src=&amp;quot;/jahia/engines/images/pix.gif&amp;quot; height=&amp;quot;0&amp;quot; width=&amp;quot;14&amp;quot; align=&amp;quot;absmiddle&amp;quot;> <input type=&amp;quot;checkbox&amp;quot; name=&amp;quot;category_root&amp;quot;>(key=root) </td> <td>&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;</td> </tr> </table> </td> </tr> </table> <!-- --><br>
 addcontainer.fieldForms<br>
 java.lang.String<br>
 java.util.Map<br>
 addcontainer.theField<br>
 java.lang.String<br>
 org.jahia.data.fields.JahiaSmallTextSharedLangField<br>
 org.jahia.data.fields.JahiaSmallTextSharedLangField&amp;#64;15e10ab<br>
 theField<br>
 java.lang.String<br>
 org.jahia.data.fields.JahiaSmallTextSharedLangField<br>
 org.jahia.data.fields.JahiaSmallTextSharedLangField&amp;#64;15e10ab<br>
 fieldsEditCallingEngineName<br>
 java.lang.String<br>
 java.lang.String<br>
 ManageCategories<br>
 categoryTree<br>
 java.lang.String<br>
 javax.swing.JTree<br>
 javax.swing.JTree[,0,0,0x0,invalid,alignmentX=null,alignmentY=null,border=,flags=360,maximumSize=,minimumSize=,preferredSize=,editable=false,invokesStopCellEditing=false,largeModel=false,rootVisible=true,rowHeight=0,scrollsOnExpand=true,showsRootHandles=false,toggleClickCount=2,visibleRowCount=20]<br>
 addcontainer.org.jahia.data.fields.FieldsEditHelper.ContextID<br>
 java.lang.String<br>
 org.jahia.data.fields.ContainerFieldsEditHelper<br>
 org.jahia.data.fields.ContainerFieldsEditHelper&amp;#64;987c7d<br>
 engineOutputFile<br>
 java.lang.String<br>
 java.lang.String<br>
 /engines/engine.jsp<br>
 enableCategories<br>
 java.lang.String<br>
 java.lang.Boolean<br>
 true<br>
 selectedCategories<br>
 java.lang.String<br>
 java.util.ArrayList<br>
 []<br>
 imagesPath<br>
 java.lang.String<br>
 java.lang.String<br>
 /engines/engines/images/<br>
 addcontainer.theContainer<br>
 java.lang.String<br>
 org.jahia.data.containers.JahiaContainer<br>
 org.jahia.data.containers.JahiaContainer&amp;#64;100e398<br>
 renderType<br>
 java.lang.String<br>
 java.lang.Integer<br>
 2<br>
 org.jahia.engines.JahiaEngine.engineLanguageHelper<br>
 java.lang.String<br>
 org.jahia.engines.EngineLanguageHelper<br>
 org.jahia.engines.EngineLanguageHelper&amp;#64;115272a<br>
 org.jahia.engines.JahiaEngine.processingLocale<br>
 java.lang.String<br>
 java.util.Locale<br>
 en<br>
 addcontainer.fieldForm<br>
 java.lang.String<br>
 java.lang.String<br>
 <select class=&amp;quot;input&amp;quot; name=&amp;quot;listSelection__4&amp;quot; onChange=&amp;quot;handleListSelectionChange(document.mainForm.listSelection__4,document.mainForm.elements[&amp;apos;_-4&amp;apos;]);&amp;quot; > <option value=&amp;quot;&amp;lt;jahia-resource id=&amp;quot;myjahiasite_CORPORATE_PORTAL_TEMPLATES&amp;quot; key=&amp;quot;coloredBackground&amp;quot; default-value=&amp;quot;&amp;quot;/>&amp;quot; selected>Colored background</option> <option value=&amp;quot;&amp;lt;jahia-resource id=&amp;quot;myjahiasite_CORPORATE_PORTAL_TEMPLATES&amp;quot; key=&amp;quot;coloredBorder&amp;quot; default-value=&amp;quot;&amp;quot;/>&amp;quot;>Colored border</option> <option value=&amp;quot;&amp;lt;jahia-resource id=&amp;quot;myjahiasite_CORPORATE_PORTAL_TEMPLATES&amp;quot; key=&amp;quot;greyBackground&amp;quot; default-value=&amp;quot;&amp;quot;/>&amp;quot;>Grey background</option> <option value=&amp;quot;&amp;lt;jahia-resource id=&amp;quot;myjahiasite_CORPORATE_PORTAL_TEMPLATES&amp;quot; key=&amp;quot;greyBorder&amp;quot; default-value=&amp;quot;&amp;quot;/>&amp;quot;>Grey border</option> <option value=&amp;quot;&amp;lt;jahia-resource id=&amp;quot;myjahiasite_CORPORATE_PORTAL_TEMPLATES&amp;quot; key=&amp;quot;transparent&amp;quot; default-value=&amp;quot;&amp;quot;/>&amp;quot;>Transparent</option> </select> <input name=&amp;quot;_-4&amp;quot; type=&amp;quot;hidden&amp;quot; value=&amp;quot;&amp;lt;jahia-resource id=&amp;quot;myjahiasite_CORPORATE_PORTAL_TEMPLATES&amp;quot; key=&amp;quot;coloredBackground&amp;quot; default-value=&amp;quot;&amp;quot;/>&amp;quot;> <SCRIPT type=&amp;quot;text/javascript&amp;quot;> <!-- function concatMultipleFieldValues(selectBox){ var sep = &amp;quot;$$$&amp;quot;; var result = &amp;quot;&amp;quot;; for ( i=0 ;i<selectBox.options.length; i++ ){ if ( selectBox.options[i].selected ){ if ( result.length==0 ){ result = selectBox.options[i].value; } else { result = result + sep + selectBox.options[i].value; } } } return result; } function handleListSelectionChange(selectBox,fieldInput){ fieldInput.value = concatMultipleFieldValues(selectBox); //alert(fieldInput.value); } document.mainForm.elements[&amp;apos;_-4&amp;apos;].value = concatMultipleFieldValues(document.mainForm.listSelection__4); // --> </SCRIPT><br>
 jahiaBuild<br>
 java.lang.String<br>
 java.lang.Integer<br>
 7018<br>
 noApply<br>
 java.lang.String<br>
 java.lang.String<br>
 <br>
 addcontainer.isSelectedField<br>
 java.lang.String<br>
 java.lang.Boolean<br>
 false<br>
 addcontainer.fieldID<br>
 java.lang.String<br>
 java.lang.Integer<br>
 -1<br>
 dataSourceConnectUrl<br>
 java.lang.String<br>
 java.lang.String<br>
 /jahia/Jahia/engineName/selectdatasource/op/edit/pid/1?mode=displaywindow&amp;fid=-4<br>
 adminAccess<br>
 java.lang.String<br>
 java.lang.Boolean<br>
 true<br>
 dataSourceIDUrl<br>
 java.lang.String<br>
 java.lang.String<br>
 /jahia/Jahia/engineName/viewdatasourceid/op/edit/pid/1?mode=displayid&amp;fid=-4<br>
 engineName<br>
 java.lang.String<br>
 java.lang.String<br>
 addcontainer<br>
 localSwitchUrl<br>
 java.lang.String<br>
 java.lang.String<br>
 ReloadEngine(&amp;apos;localswitch***yes&amp;apos;)<br>
 writeAccess<br>
 java.lang.String<br>
 java.lang.Boolean<br>
 true<br>
 3_1_topMenu_filter_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerFilters<br>
 Container filters:[]<br>
 2_1_contentContainermain_1_filter_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerFilters<br>
 Container filters:[]<br>
 org.jahia.usermanager.jahiauser<br>
 org.jahia.services.usermanager.JahiaDBUser<br>
 Detail of user [root] - ID [0] - password [M6SFyxRuEVPGm1iMZxq0dPLluAA=] - properties : email -> [] lastname -> [Super Administrator] firstname -> [Jahia]<br>
 5_1_siteSettings_sort_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerSorterBean<br>
 <br>
 5_1_siteSettings_search_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.services.search.ContainerSearcher<br>
 Query=<br>
 3_1_topMenu_sort_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerSorterBean<br>
 <br>
 4_1_quickLinkContainer_search_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.services.search.ContainerSearcher<br>
 Query=<br>
 org.jahia.services.sites.jahiasite<br>
 org.jahia.services.sites.JahiaSite<br>
 org.jahia.services.sites.JahiaSite@e7a94c<br>
 2_1_contentContainermain_1_sort_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerSorterBean<br>
 <br>
 3_1_topMenu_filter_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerFilters<br>
 Container filters:[]<br>
 javax.servlet.jsp.jstl.fmt.locale.session<br>
 java.util.Locale<br>
 en
 <br>
 4_1_quickLinkContainer_filter_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerFilters<br>
 Container filters:[]<br>
 org.jahia.params.lastrequestedpageid<br>
 java.lang.Integer<br>
 1<br>
 6_1_linkContainer_filter_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerFilters<br>
 Container filters:[]<br>
 3_1_topMenu_sort_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerSorterBean<br>
 <br>
 6_1_linkContainer_search_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.services.search.ContainerSearcher<br>
 Query=<br>
 2_1_contentContainermain_1_search_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.services.search.ContainerSearcher<br>
 Query=<br>
 5_1_siteSettings_filter_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerFilters<br>
 Container filters:[]<br>
 6_1_linkContainer_sort_handler_[workflowState=2 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerSorterBean<br>
 <br>
 org.jahia.engines.lastenginename<br>
 java.lang.String<br>
 core<br>
 4_1_quickLinkContainer_sort_handler_[workflowState=1 versionID=0 languages=shared,en]<br>
 org.jahia.data.containers.ContainerSorterBean<br>
 <br>
 org.apache.struts.action.LOCALE<br>
 java.util.Locale<br>
 en<br>

 * </fieldset>
 * </attriInfo>"
 */

@SuppressWarnings("serial")
public class SessionViewerTag extends AbstractJahiaTag {

    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(SessionViewerTag.class);

    public SessionViewerTag () {
    }

    public int doStartTag () {

        try {
            JspWriter out = pageContext.getOut();
            HttpServletRequest request = (HttpServletRequest) pageContext.
                                         getRequest();
            HttpSession session = request.getSession();
            out.println("<div class=\"session\">");
            Enumeration<?> attrNameEnum = session.getAttributeNames();
            if (attrNameEnum.hasMoreElements()) {
                out.print(getPadding(2));
                out.println("<ol class=\"attribute\">");
                out.print(getPadding(4));
                out.println("<li>Name</li>");
                out.print(getPadding(4));
                out.println("<li>Type</li>");
                out.print(getPadding(4));
                out.println("<li>Value</li>");
                out.print(getPadding(2));
                out.println("</ol>");
            } while (attrNameEnum.hasMoreElements()) {
                String curAttrName = (String) attrNameEnum.nextElement();
                Object curAttrObject = session.getAttribute(curAttrName);
                handleAttrDisplay(out, curAttrName, curAttrObject);
            }
            out.println("</div>");
        } catch (IOException ioe) {
            logger.error("Error while displaying session content", ioe);
        }
        return SKIP_BODY;
    }

    /**
     * handleObjectDisplay
     *
     * @param curAttrName String
     * @param curAttrObject Object
     */
    private void handleAttrDisplay (JspWriter out, String curAttrName,
                                    Object curAttrObject)
        throws IOException {
        out.println("<ol class=\"attribute\">");
        out.print("  <li class=\"name\">");
        out.print(curAttrName);
        out.println("</li>");
        out.print("  <li class=\"type\">");
        out.print(curAttrObject.getClass().getName());
        out.println("</li>");
        out.print("  <li class=\"value\">");
        if (curAttrObject instanceof Map) {
            handleMapDisplay(out, (Map<Object, Object>) curAttrObject, 4);
        } else {
            out.print(curAttrObject.toString());
        }
        out.println("</li>");
        out.println("</ol>");
    }

    private void handleMapDisplay (JspWriter out, Map<Object, Object> map, int indent)
        throws IOException {
        Iterator<Map.Entry<Object, Object>> entryIter = map.entrySet().iterator();
        out.print(getPadding(indent));
        out.println("<div class=\"map\">");
        if (entryIter.hasNext()) {
            out.print(getPadding(indent + 2));
            out.println("<ol class=\"entry\">");
            out.print(getPadding(indent + 4));
            out.print("<li class=\"key\">");
            out.print("Key");
            out.println("</li>");
            out.print(getPadding(indent + 4));
            out.print("<li class=\"key-type\">");
            out.print("Key type");
            out.println("</li>");
            out.print(getPadding(indent + 4));
            out.print("<li class=\"value-type\">");
            out.print("Value type");
            out.println("</li>");
            out.print(getPadding(indent + 4));
            out.print("<li class=\"value\">");
            out.print("Value");
            out.println("</li>");
            out.print(getPadding(indent + 2));
            out.println("</ol>");
        } while (entryIter.hasNext()) {
            Map.Entry<Object, Object> curEntry = entryIter.next();
            Object key = curEntry.getKey();
            Object value = curEntry.getValue();
            out.print(getPadding(indent + 2));
            out.println("<ol class=\"entry\">");
            out.print(getPadding(indent + 4));
            out.print("<li class=\"key\">");
            out.print(key);
            out.println("</li>");
            out.print(getPadding(indent + 4));
            out.print("<li class=\"key-type\">");
            out.print(key.getClass().getName());
            out.println("</li>");
            out.print(getPadding(indent + 4));
            out.print("<li class=\"value-type\">");
            out.print(value.getClass().getName());
            out.println("</li>");
            out.print(getPadding(indent + 4));
            out.print("<li class=\"value\">");
            if (value instanceof Map) {
                handleMapDisplay(out, (Map<Object, Object>) value, indent + 4);
            } else {
                out.print(Util.escapeXml(value.toString()));
            }
            out.println("</li>");
            out.print(getPadding(indent + 2));
            out.println("</ol>");
        }
        out.print(getPadding(indent));
        out.println("</div>");
    }

    private String getPadding (int indent) {
        StringBuffer paddingBuf = new StringBuffer(indent);
        for (int i = 0; i < indent; i++) {
            paddingBuf.append(' ');
        }
        return paddingBuf.toString();
    }

}
