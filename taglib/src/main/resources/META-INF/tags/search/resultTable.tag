<%--


    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ tag body-content="empty" %>
<%@ tag import="org.jahia.taglibs.search.ResultsTag" %>
<%@ tag import="org.jahia.taglibs.search.ResultTableSettingsTag" %>
<%@ attribute name="contextId" description="Unique ID to distinguish this result table." %>
<%@ attribute name="columns"
              description="Comma-separated list of column names to be displayed. The order will be respected." %>
<%@ attribute name="iconsPath" description="The path to the icons folder, related to the template set root folder." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%
    ResultsTag resultTag = (ResultsTag) findAncestorWithClass(this, ResultsTag.class);
    if (resultTag == null) {
        throw new JspTagException("Parent tag not found. This tag must be enclosed into the 'results' tag");
    }
%>
<c:set var="parent" value="<%= resultTag %>"/><c:set var="count" value="${parent.count}"/>
<c:if test="${count > 0}">
    <c:set var="hits" value="${parent.hits}"/>
    <c:set var="iconsPath" value="${empty iconsPath ? 'images' : iconsPath}"/>
    <%-- generate a unique 'uid' --%>
    <c:set var="org.jahia.taglibs.search.resultTable.index"
           value="${not empty requestScope['org.jahia.taglibs.search.resultTable.index'] ? requestScope['org.jahia.taglibs.search.resultTable.index'] + 1 : '1'}"
           scope="request"/>
    <c:choose>
        <c:when test="${not empty contextId}"><c:set var="contextId" value="${contextId}"/></c:when>
        <c:otherwise><c:set var="contextId"
                            value="resultTable${requestScope['org.jahia.taglibs.search.resultTable.index']}"/></c:otherwise>
    </c:choose>
    <%-- get column names to display --%>
    <c:if test="${empty columns}">
        <%-- if columns are not specified, check for parent 'resultTableSettings' tag --%>
        <c:set var="settingsTag" value="<%= findAncestorWithClass(this, ResultTableSettingsTag.class) %>"/>
        <%-- parent tag found; get settings --%>
        <c:if test="${not empty settingsTag}">
            <c:set var="columns" value="${settingsTag.viewSettings.selectedFieldsOrder}"/>
        </c:if>
    </c:if>
    <%-- columns still empty? set default --%>
    <c:if test="${empty columns}">
        <c:set var="columns"
               value="name,score,contentType,creationUser,creationDate,modificationUser,lastModified,size,info,folder"/>
    </c:if>
    <c:set var="columnNames" value="${fn:split(fn:replace(columns, ' ', ''), ',')}"/>
    <%-- render the table --%>
    <display:table name="${hits}" uid="${contextId}" decorator="org.jahia.taglibs.search.FileSearchTableDecorator"
                   requestURI="${jahia.page.url}" pagesize="10" excludedParams="name authorizedTypes"
                   class="searchResultListing" style="width: 100%">
        <c:set var="row" value="${pageScope[contextId]}"/>
        <c:forEach items="${columnNames}" var="column">
            <c:if test="${'name' == column}">
                <display:column title="Name" sortable="true" sortProperty="name">
                    <a href="<c:url value='${row.link}' context='/'/>" class="${row.iconType}"><c:out
                            value="${row.name}"/></a>
                </display:column>
            </c:if>
            <c:if test="${'score' == column}">
                <display:column property="score" title="Score" sortable="true"/>
            </c:if>
            <c:if test="${'contentType' == column}">
                <display:column property="contentType" title="Type" sortable="true"/>
            </c:if>
            <c:if test="${'creationUser' == column}">
                <display:column property="createdBy" title="Author" sortable="true"/>
            </c:if>
            <c:if test="${'modificationUser' == column}">
                <display:column property="lastModifiedBy" title="Last editor" sortable="true"/>
            </c:if>
            <c:if test="${'creationDate' == column}">
                <display:column property="created" title="Creation date" sortable="true"/>
            </c:if>
            <c:if test="${'lastModified' == column}">
                <display:column property="lastModified" title="Last modified" sortable="true"/>
            </c:if>
            <c:if test="${'sizeKb' == column}">
                <display:column property="sizeKb" title="Size" format="{0} KB" sortable="true"
                                sortProperty="contentLength"/>
            </c:if>
            <c:if test="${'info' == column && jahia.requestInfo.logged}">
                <display:column>
                    <c:set var="aboutImage" value="${iconsPath}/about.gif"/>
                    <input type="image" src="${jahia.includes.webPath[aboutImage]}" onclick="${row.infoPopupLauncher}"
                           title="${fn:escapeXml(row.name)}"/>
                </display:column>
            </c:if>
            <c:if test="${'folder' == column}">
                <display:column>
                    <c:set var="webdavPath"
                           value="<%= org.jahia.registries.ServicesRegistry.getInstance().getJCRStoreService().getMainStoreProvider().getAbsoluteContextPath(request)%>"/>
                    <a href="${webdavPath}${row.folderPath}"
                       folder="${webdavPath}${row.folderPath}"
                       target="_blank"
                       style="behavior:url(#default#AnchorClick)"
                       title="<utility:resourceBundle resourceBundle='JahiaEnginesResources' resourceName='org.jahia.engines.filemanager.Filemanager_Engine.openIEfolder.label'/>"><img
                            src="${pageContext.request.contextPath}/jsp/jahia/javascript/zimbra/complexTree/IEFolder.gif"
                            alt="IE folder" border="0"
                            title="<utility:resourceBundle resourceBundle='JahiaEnginesResources' resourceName='org.jahia.engines.filemanager.Filemanager_Engine.openIEfolder.label'/>"/></a>
                </display:column>
            </c:if>
        </c:forEach>
    </display:table>
</c:if>