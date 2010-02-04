<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="wiki.css"/>

        <div class='grid_3'><!--start grid_3-->
            <div class="boxwiki">
                <div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">

                    <div class="boxwiki-inner">
                        <div class="boxwiki-inner-border"><!--start boxwiki -->
                            <h3 class="boxwikititleh3"><fmt:message key="jnt_wiki.searchWiki"/></h3>

                            <div id="wikisearch">
                                <form method="get" action="#">
                                    <fieldset>
                                        <p class="field">
                                            <input name="search" type="text" class="search" tabindex="4"
                                                   value="Search..."/>

                                        </p>

                                        <div class="divButton">
                                            <a class="aButton" href="#"><span><fmt:message key="search"/> </span></a>

                                            <div class="clear"></div>
                                        </div>
                                    </fieldset>
                                </form>
                                <ul class="listwiki">
                                    <li>
                                        <a href="#"><fmt:message key="jnt_wiki.advancedSearch"/> </a>
                                    </li>

                                </ul>
                            </div>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop boxwiki -->
            <%--<div class="boxwiki">--%>
                <%--<div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">--%>
                    <%--<div class="boxwiki-inner">--%>
                        <%--<div class="boxwiki-inner-border"><!--start boxwiki -->--%>

                            <%--<h3 class="boxwikititleh3">Liste 4 </h3>--%>


                            <%--<ul class="listwiki">--%>

                                <%--<li><a href="#">Titre de mon lien</a></li>--%>
                                <%--<li><a href="#">Titre de mon lien</a></li>--%>
                                <%--<li><a href="#">Titre de mon lien</a></li>--%>
                                <%--<li><a href="#">Titre de mon lien</a></li>--%>
                                <%--<li><a href="#">Titre de mon lien</a></li>--%>
                                <%--<li><a href="#">Titre de mon lien</a></li>--%>


                            <%--</ul>--%>

                            <%--<div class="clear"></div>--%>
                        <%--</div>--%>
                    <%--</div>--%>
                <%--</div>--%>
            <%--</div>--%>
            <!--stop boxwiki -->
            <div class='clear'></div>
        </div>
        <!--stop grid_3-->


        <div class='grid_10'><!--start grid_10-->

            <h2><fmt:message key="jnt_wiki.wikiName"/> </h2>

            ${wrappedContent}
            <!--stop tabContainer-->
        </div>
        <!--stop grid_10-->
        <div class='grid_3'><!--start grid_3-->
            <img src="${url.currentModule}/images/jahia-apps-wiki.png" alt="jahia-apps-wiki"/>
            <h4 class="boxwiki-title">Navigation</h4>

            <ul id="menuwiki">
                <li class="menuwikitop"><a href="${url.base}${currentNode.path}.html">Wiki home</a></li>
                <li class="menuwikitop"><a href="#">All wikis</a></li>
                <li class="menuwikitop"><a href="#">Index</a></li>
            </ul>
            <%--<div class="boxwiki">--%>
                <%--<div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">--%>
                    <%--<div class="boxwiki-inner">--%>
                        <%--<div class="boxwiki-inner-border"><!--start boxwiki -->--%>
                            <%--<h3 class="boxwikititleh3">Langues</h3>--%>

                            <%--<ul class="listwiki">--%>
                                <%--<li><a href="#">Français</a></li>--%>
                                <%--<li><a href="#">Anglais</a></li>--%>
                                <%--<li><a href="#">Portugais</a></li>--%>
                                <%--<li><a href="#">Italien</a></li>--%>
                                <%--<li><a href="#">Chinois</a></li>--%>
                                <%--<li><a href="#">Indien</a></li>--%>
                            <%--</ul>--%>

                            <%--<div class="clear"></div>--%>
                        <%--</div>--%>
                    <%--</div>--%>
                <%--</div>--%>
            <%--</div>--%>
            <!--stop boxwiki -->
            <div class='clear'></div>
        </div>
        <!--stop grid_3-->
