<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

<jcr:node var="rootPage" path="/sites/${renderContext.site.siteKey}/home"/>
<template:addResources type="css" resources="navigation.css" nodetype="jnt:navBar"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.core.min.js" nodetype="jnt:navBar"/>
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
<div id="navbar">
    <jcr:simpleNavigationMenu node="${rootPage}" var="menu"/>
    <ul class="main-nav">
        <li class="home"><a href="${url.base}${rootPage.path}.html">Home</a></li>
        <c:forEach items="${menu}" var="navNode">
            <jcr:nodeProperty node="${navNode}" name="jcr:title" var="title"/>
            <c:set var="submenu" value="submenu"/>
            <c:if test="${empty navNode.nodes}">
                <c:set var="submenu" value="nosubmenu"/>
            </c:if>
            <li class="${submenu}">
                <a href='<c:url value="${navNode.path}.html" context="${url.base}"/>'>${title.string}</a>
                <jcr:simpleNavigationMenu node="${navNode}" var="innerMenu"/>
                <c:if test="${not empty innerMenu}">
                    <div class="box-inner">
                        <ul class="submenu">
                            <c:forEach items="${innerMenu}" var="navInnerMenu">
                                <jcr:nodeProperty node="${navInnerMenu}" name="jcr:title" var="title"/>
                                <li class="box-inner-border">
                                    <a href='<c:url value="${navInnerMenu.path}.html" context="${url.base}"/>'>${title.string}</a>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>
            </li>
        </c:forEach>
    </ul>
</div>
<!--stop navigation-->