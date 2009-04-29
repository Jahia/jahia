<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

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
