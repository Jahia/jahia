<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:node var="rootPage" path="/content/sites/${renderContext.site.siteKey}/home"/>
<template:addResources type="css" resources="navigation.css" nodetype="jnt:navBar"/>
<!--start navigation-->
<div id="navigation">
<%--
    // Not use yet
   <div id="shortcuts">
        <h3><a title="Shortcuts" href="navBar.dropDown.jsp#">Shortcuts</a></h3>
    </div>
--%>
    <div id="navbar">
        <jcr:JCRSimpleNavigation node="${rootPage}" var="menu"/>
        <c:if test="${not empty menu}">
            <ul class="main-nav">
                <li class="home"><a href="${url.base}${rootPage.path}.html">Home</a></li>
                <c:forEach items="${menu}" var="navNode">
                    <jcr:nodeProperty node="${navNode}" name="jcr:title" var="title"/>
                    <c:set var="submenu" value="submenu"/>
                    <c:if test="${empty navNode.nodes}">
                        <c:set var="submenu" value="nosubmenu"/>
                    </c:if>
                    <li class="${submenu}">
                        <a href="${url.base}${navNode.path}.html">${title.string}</a>
                        <jcr:JCRSimpleNavigation node="${navNode}" var="innerMenu"/>
                        <c:if test="${not empty innerMenu}">
                            <div class="box-inner">
                                <ul class="submenu">
                                    <c:forEach items="${innerMenu}" var="navInnerMenu">
                                        <jcr:nodeProperty node="${navInnerMenu}" name="jcr:title" var="title"/>
                                        <li class="box-inner-border">
                                            <a href="${url.base}${navInnerMenu.path}.html">${title.string}</a>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </c:if>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </div>
    <div class="clear"></div>
</div>
<!--stop navigation-->