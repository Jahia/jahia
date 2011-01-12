<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="wiki.css"/>

        <div class='grid_3'><!--start grid_3-->

        </div>
        <!--stop grid_3-->


        <div class='grid_10'><!--start grid_10-->

            <h2>${currentNode.name}</h2>

            <div class="idTabsContainer"><!--start idTabsContainer-->
                <c:choose>
                    <c:when test="${currentResource.resolvedTemplate == 'default'}">
                        <ul class="idTabs">
                            <li><a class="on selected" href="${url.base}${currentNode.path}.html"><span><fmt:message key="jnt_wiki.article"/></span></a></li>
                            <li><a class="off" href="${url.base}${currentNode.path}.contribute.html"><span><fmt:message key="jnt_wiki.contribute"/></span></a></li>
                            <li class="spacing"><a class="off" href="${url.base}${currentNode.path}.history.html"><span><fmt:message key="jnt_wiki.history"/> </span></a></li>
                        </ul>
                    </c:when>
                    <c:when test="${currentResource.resolvedTemplate == 'contribute'}">
                        <ul class="idTabs">
                            <li><a class="off" href="${url.base}${currentNode.path}.html"><span><fmt:message key="jnt_wiki.article"/></span></a></li>
                            <li><a class="on selected" href="${url.base}${currentNode.path}.contribute.html"><span><fmt:message key="jnt_wiki.contribute"/></span></a></li>
                            <li class="spacing"><a class="off" href="${url.base}${currentNode.path}.history.html"><span><fmt:message key="jnt_wiki.history"/> </span></a></li>
                        </ul>
                    </c:when>
                    <c:when test="${currentResource.resolvedTemplate == 'history'}">
                        <ul class="idTabs">
                            <li><a class="off" href="${url.base}${currentNode.path}.html"><span><fmt:message key="jnt_wiki.article"/></span></a></li>
                            <li><a class="off" href="${url.base}${currentNode.path}.contribute.html"><span><fmt:message key="jnt_wiki.contribute"/></span></a></li>
                            <li class="spacing"><a class="on selected" href="${url.base}${currentNode.path}.history.html"><span><fmt:message key="jnt_wiki.history"/> </span></a></li>
                        </ul>
                    </c:when>
                    <c:otherwise>
                        <ul class="idTabs">
                            <li><a class="off" href="${url.base}${currentNode.path}.html"><span><fmt:message key="jnt_wiki.article"/></span></a></li>
                            <li><a class="off" href="${url.base}${currentNode.path}.contribute.html"><span><fmt:message key="jnt_wiki.contribute"/></span></a></li>
                            <li class="spacing"><a class="off" href="${url.base}${currentNode.path}.history.html"><span><fmt:message key="jnt_wiki.history"/> </span></a></li>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="tabContainer"><!--start tabContainer-->
                ${wrappedContent}
            </div>
            <!--stop tabContainer-->
        </div>
        <!--stop grid_10-->
        <div class='grid_3'><!--start grid_3-->
            <img src="${url.currentModule}/images/jahia-apps-wiki.png" alt="jahia-apps-wiki"/>
            <h4 class="boxwiki-title"><fmt:message key="jnt_wiki.navigation"/></h4>

            <ul id="menuwiki">
                <li class="menuwikitop"><a href="${url.base}${currentNode.parent.path}.html"><fmt:message key="jnt_wiki.wikihome"/></a></li>
                <li class="menuwikitop"><a href="#"><fmt:message key="jnt_wiki.allwikis"/></a></li>
                <li class="menuwikitop"><a href="#"><fmt:message key="jnt_wiki.indexofpages"/></a></li>
            </ul>
            <div class="boxwiki">
                <div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">
                    <div class="boxwiki-inner">
                        <div class="boxwiki-inner-border"><!--start boxwiki -->
                            <h3 class="boxwikititleh3"><fmt:message key="jnt_wiki.languages"/></h3>

                            <ul class="listwiki">
                                <li><a href="#"><fmt:message key="jnt_wiki.Languages.french"/></a></li>
                                <li><a href="#"><fmt:message key="jnt_wiki.Languages.english"/></a></li>
                                <li><a href="#"><fmt:message key="jnt_wiki.Languages.chinese"/></a></li>
                                <li><a href="#"><fmt:message key="jnt_wiki.Languages.indian"/></a></li>
                            </ul>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop boxwiki -->
                <div class="boxwiki">
                <div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">
                    <div class="boxwiki-inner">
                        <div class="boxwiki-inner-border"><!--start boxwiki -->
                            <h3 class="boxwikititleh3"><fmt:message key="jnt_wiki.createPageSummary"/></h3>

									<p>{{box cssClass="summary"}}<br />

{{toc/}}<br />

{{/box}}</p>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop boxwiki -->
            <div class='clear'></div>
        </div>
        <!--stop grid_3-->