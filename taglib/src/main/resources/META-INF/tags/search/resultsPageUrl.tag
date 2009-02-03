<%-- 
/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
--%>
<%@ tag body-content="empty"
        description="Exposes the page path for the search results page into a page scope variable. Here the JSP page is used, which is configured in the search-results element of the template deployment descriptor (templates.xml) for the current template set. If not present the default search results page is used."
        %>
<%@ attribute name="var" required="true" rtexprvalue="false" type="java.lang.String"
              description="The name of the page scope attribute to expose the URL value."
        %>
<%@ variable name-from-attribute="var" variable-class="java.lang.String" alias="varName" scope="AT_END"
        %>
<%@ tag import="org.jahia.data.templates.JahiaTemplatesPackage"
        %>
<%@ tag import="org.jahia.registries.ServicesRegistry"
        %>
<%@ tag import="org.jahia.services.templates.JahiaTemplateManagerService"
        %>
<%@ tag import="org.jahia.taglibs.utility.Utils"
        %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
        %>
<%
    String path = null;
    JahiaTemplateManagerService templateMgr = ServicesRegistry.getInstance().getJahiaTemplateManagerService();

    JahiaTemplatesPackage templatePackage = templateMgr
            .getTemplatePackage(Utils.getProcessingContext((PageContext) jspContext).getSite().getTemplatePackageName());

    if (templatePackage.getSearchResultsPageName() != null) {
        path = templateMgr.resolveResourcePath(templatePackage.getSearchResultsPageName(), templatePackage.getName());
    }
    path = path != null ? path : "/jsp/jahia/engines/search/searchresult_v2.jsp";
%><c:set var="varName"><%= path %></c:set>