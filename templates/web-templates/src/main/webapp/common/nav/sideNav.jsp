<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ include file="../declarations.jspf" %>

<div id="navigationN2">
  <%--  navMenuBean properties :
        private String title="";
        private String url="";
        private boolean isLower=false;
        private boolean isUpper=false;
        private String actionMenu="";
        private int level=0;
        private boolean isFirst=false;
        private boolean isLast=false;
        private String actionMenuList="";
        private boolean isInPath=false;
        private boolean isSelected=false;
        private boolean isMarkedForDelete = false;
        private String displayLink = "";
        private int itemCount=0;
        private boolean actionMenuOnly = false;
--%><ui:navigationMenu labelKey="pages.add" kind="sideMenu" mockupClass="mockup-vmenu" display="false" var="navMenuBeans" usePageIdForCacheKey="true"
                       requiredTitle="true">
    <%--test if any items in menu (under 2 items because the first is the manage menu box)--%>
    <c:forEach var="navMenuBean" items="${navMenuBeans}">
        <c:if test="${fn:length(navMenuBeans) == 1 && navMenuBean.actionMenuOnly}">
          <div class="mockup-vmenu">
      </c:if>
        <c:if test="${navMenuBean.firstInLevel}">
            <ul class="level_${navMenuBean.level}">
        </c:if>
        <c:if test="${!navMenuBean.firstInLevel}">
            </li>
        </c:if>
        <c:set var="liCssNavItem" value=""/>
        <c:set var="aCssNavItem" value=""/>
        <c:if test="${navMenuBean.firstInLevel}"><c:set var="liCssNavItem" value="${liCssNavItem} first"/></c:if>
        <c:if test="${navMenuBean.lastInLevel}"><c:set var="liCssNavItem" value="${liCssNavItem} last"/></c:if>
        <c:if test="${navMenuBean.inPath}"><c:set var="aCssNavItem" value="${aCssNavItem} inpath"/></c:if>
        <c:if test="${navMenuBean.selected}"><c:set var="aCssNavItem" value="${aCssNavItem} selected"/></c:if>
        <c:if test="${navMenuBean.markedForDelete}"><c:set var="aCssNavItem" value="${aCssNavItem} markedForDelete"/></c:if>
        <li class="${liCssNavItem}">
        <c:if test="${!navMenuBean.actionMenuOnly}">
            <a class="${aCssNavItem}" href="${navMenuBean.url}" alt="${navMenuBean.title}"><span>${navMenuBean.displayLink}</span></a>${navMenuBean.actionMenu}
        </c:if>
        <c:if test="${navMenuBean.lastInLevel}">
            </li>
            <c:if test="${requestScope.currentRequest.editMode}">
                <li>${navMenuBean.actionMenuList}</li>
            </c:if>
            </ul>
        </c:if>
        <c:if test="${fn:length(navMenuBeans) == 1 && navMenuBean.actionMenuOnly}">
            </div>
        </c:if>
    </c:forEach>
    </ui:navigationMenu>


</div>
