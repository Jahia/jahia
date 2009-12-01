<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

<jcr:node var="rootPage" path="/sites/${renderContext.site.siteKey}/home"/>
<template:addResources type="css" resources="navigation.css" nodetype="jnt:navBar"/>
<template:addResources type="javascript" resources="jquery.min.js,ui.core.min.js" nodetype="jnt:navBar"/>
<script type="text/javascript">
    $(document).ready(function() {
      $('#shortcuts').children('ul').hide();
      $('#shortcuts').mouseover(function() {
        $(this).children('ul').show();
      }).mouseout(function() {
        $(this).children('ul').hide();
      });
    });
</script>

<!--start navigation-->
<div id="navigation">

   <div id="shortcuts">
        <h3><a title="Shortcuts" href="navBar.dropDown.jsp#">Shortcuts</a></h3>
                <ul>
                    <c:if test="${requestScope.currentRequest.logged}">
                        <li class="more-shortcuts">
                            <a class="loginFormTopLogoutShortcuts"
                               href="<template:composePageURL page="logout"/>"><span>logout</span></a>
                        </li>
                        <li>
                            <span class="currentUser"><utility:userProperty/></span>
                        </li>
                        <li class="more-shortcuts">
                            <a href="${url.userProfile}">my settings</a>
                        </li>
                        <li class="more-shortcuts">
                            <a href="${url.edit}"><fmt:message key="edit"/></a>
                        </li>
                    </c:if>
                    <li class="more-shortcuts"><a href="base.wrapper.bodywrapper.jsp#"
                                                      onclick="javascript:window.print()">
                        print</a>
                    </li>
                    <li class="more-shortcuts">
                        <a href="${url.base}${rootPage.path}.html">home</a>
                    </li>
                    <li class="more-shortcuts">
                        <a href="${url.base}${rootPage.path}.sitemap.html">sitemap</a>
                    </li>
                </ul>
       <div class="clear"></div>
    </div>
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