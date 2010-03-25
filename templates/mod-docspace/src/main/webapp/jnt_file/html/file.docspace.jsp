<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="docspace.css,files.css,toggle-docspace.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.cuteTime.js"/>

<div class='grid_12'><!--start grid_12-->
<div class="boxdocspace "><!--start boxdocspace -->
            <div class="boxdocspacepadding16 boxdocspacemarginbottom16">
                <div class="boxdocspace-inner">
                  <div class="boxdocspace-inner-border">
                   <div class="floatright">
                        <form action="#" method="post">
                        <select name="actions">
                        <option>Actions</option>
                        <option>Informations</option>
                        <option>Suprimer</option>
                        <option>Telecharger</option>
                         <option>Telecharger en pdf</option>
                        </select>
                        </form>
                                            <p>Votes : <img src="${url.currentModule}/css/img/rating.png" alt=" " /> </p>
                    </div>
                    <div class="imagefloatleft">
		<div class="itemImage itemImageLeft"><a href="#"><img alt="" src="${url.currentModule}/css/img/documentbig.png"/></a></div>
</div>
                    <h3>Le nom de mon document : Version 3</h3>
                    <p class="clearMaringPadding docspacedate ">Cree le : 10/02/2010, Par : <a href="#">Regis Mora</a></p>
                    <p class="clearMaringPadding docspacedate">Dernière modif le : 12/02/2010, Par : <a href="#">Jean Dupond</a></p>
<br class="clear"/>
                    <img src="${url.currentModule}/css/img/tags.png" alt=" " />
        <!--stop boxdocspace -->
                      <div class="clear"></div>
                  </div>
			</div>
		</div>
</div><!--stop boxdocspace -->

    <h4 class="boxdocspace-title2">Discution et Versions </h4>
    <div class="boxdocspace-title2">

                           <div class="TableActions"><!--start Form-->

                            <form action="#" method="post">
                              <p>
                                <a href="#formdocspaceupload" title="upload" ><img class="rightside" src="${url.currentModule}/css/img/upload.png" alt="upload"/></a>
                            	<a href="#formdocspacecomment" title="comment" ><img class="rightside" src="${url.currentModule}/css/img/comment.png" alt="comment"/></a>
                                <label>Filtre : </label>

                                <select name="tagfilter">
                                   <option>All</option>
                                  <option>Comments</option>
                                  <option>Versions</option>

                                </select>
                                <label> - Search: </label>
                                <input class="text" type="text" name="search"  value="Search..." tabindex="4"/>
                                <input class="gobutton" type="image" src="${url.currentModule}/css/img/search-button.png" tabindex="5"/>

                              </p>

                            </form>

                          </div><!--stop Form-->

    </div>




<div class="boxdocspace">
            <div class="boxdocspacepadding10 boxdocspacemarginbottom16">
                <div class="boxdocspace-inner">
                    <div class="boxdocspace-inner-border"><!--start boxdocspace -->
                        <template:option nodetype="jmix:comments" template="hidden.options.wrapper" node="${currentNode}"/>

                  </div>
			</div>
		</div>
</div><!--stop boxdocspace -->

<div class='clear'></div></div><!--stop grid_12-->




<div class='grid_4'><!--start grid_4-->
<h4 class="boxdocspace-title">Description</h4>
<div class="boxdocspace"><!--start boxdocspace -->
	<div class="boxdocspacepadding16 boxdocspacemarginbottom16">
		<div class="boxdocspace-inner">
			<div class="boxdocspace-inner-border">
            <p class="clearMaringPadding">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean scelerisque lorem sed metus vehicula non venenatis eros blandit. Donec molestie vestibulum nunc, ac molestie augue semper a. Quisque ut pharetra sem. Ut vitae urna ipsum. Mauris condimentum lobortis turpis, eu porttitor neque ultricies nec.</p>
            <div class="clear"></div>
            </div>
        </div>
    </div>
</div><!--stop boxdocspace -->

    <h4 class="boxdocspace-title">Versions</h4>
    <ul class="docspacelist docspacelistversion">
        <c:forEach items="${functions:reverse(currentNode.versionInfos)}" var="version">
            <li>
                <span class="public floatright">
                    <input name="" type="checkbox" value="" /> restore
                </span>
                <img class="floatleft" alt="user default icon" src="${url.currentModule}/css/img/version.png" />
                <a href="#" >Version ${version.version.name}</a><p class="docspacedate"><span class="timestamp"><fmt:formatDate
                    value="${version.checkinDate.time}" pattern="yyyy/MM/dd HH:mm"/></span></p>
                <div class='clear'></div></li>
        </c:forEach>
    </ul>
</div><!--stop grid_4-->

