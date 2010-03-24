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

<ul class="docspacelistcomment">
<li class="docspaceitemcomment">
<span class="public floatright"><input name="" type="checkbox" value="" /> public</span>
<div class="image">
		<div class="itemImage itemImageLeft"><a href="#"><img alt="" src="${url.currentModule}/css/img/userbig.png"/></a></div>
</div>
                <h5 class="title">Validation</h5><span class="docspacedate"> le 10/02/2010</span>

                <p><span class="author">Regis mora</span> dis :
                 <span>Ok Clem je valide ta version </span></p>

		         <div class='clear'></div>
</li>
<li class="docspaceitemcomment">
<span class="public floatright"><input name="" type="checkbox" value="" /> public</span>
<div class="image">
		<div class="itemImage itemImageLeft"><a href="#"><img alt="" src="${url.currentModule}/css/img/userbig2.png"/></a></div>
</div>

                <h5  class="title">Correction</h5><span class="docspacedate"> le 10/02/2010</span>
                <p><span class="author">Clem</span> dis : Voici les corrections du document. J'ai revu la structure du document et des differents shemas</p>
                <div class='clear'></div>
</li>
<li class="docspaceitemversion">
<span class="public floatright"><input name="" type="checkbox" value="" /> public</span>
<div class="image">
		<div class="itemImage itemImageLeft"><a href="#" title="Télécharger la version" ><img alt="" src="${url.currentModule}/css/img/versionmedium.png"/></a></div>
</div>
                <h5 class="title"><a title="Télécharger la version" href="#">Titre de mon document Version 2</a></h5>
                <span class="docspacedate"> le 11/02/2010</span>
                <p><span class="author">Clem</span> a uploade une nouvelle version (versions en jaune)</p>


		<div class='clear'></div>
</li>
<li class="docspaceitemsystem last">
<span class="public floatright"><input name="" type="checkbox" value="" /> public</span>
                <span class="author">Regis mora</span> a cree le document (action  système en vert) <span class="docspacedate"> le 10/02/2010</span>
		<div class='clear'></div>
</li>
</ul>
                      <div class="pagination"><!--start pagination-->
                  <div class="paginationPosition"> <span>Page 2 of 2 - 450 Comments</span> - Show
                    <select name="paginationShow" id="paginationShow">
                      <option>20</option>
                      <option>50</option>
                      <option>100</option>
                    </select>
                  </div>
                  <div class="paginationNavigation"> <a href="#" class="previousLink">Previous</a> <span><a href="#" class="paginationPageUrl">1</a></span> <span><a href="#" class="paginationPageUrl">2</a></span> <span><a href="#" class="paginationPageUrl">3</a></span> <span><a href="#" class="paginationPageUrl">4</a></span> <span><a href="#" class="paginationPageUrl">5</a></span> <span class="currentPage">6</span> <a href="#" class="nextLink">Next</a> </div>


               <div class="clear"></div>
</div><!--stop pagination-->
<div class="clear"></div>

<div class="boxdocspace"><!--start boxdocspace -->
	<div class="boxdocspacegrey boxdocspacepadding10 boxdocspacemarginbottom16">
		<div class="boxdocspace-inner">
			<div class="boxdocspace-inner-border">
              <a name="formdocspaceupload" id="formdocspaceupload"></a>

           <div class="Form formdocspaceupload"><!--start formdocspaceupload-->
                        <form action="#" method="post">
							<label for="uploadfile" class="left">Soumettre version : </label><input id="uploadfile" name="uploadfile" tabindex="1" type="file" />
                        </form>
			</div>
            </div>
		</div>
	</div>
</div><!--stop boxdocspace -->

<div class="boxdocspace"><!--start boxdocspace -->
	<div class="boxdocspacegrey boxdocspacepadding10">
		<div class="boxdocspace-inner">
			<div class="boxdocspace-inner-border">
<a name="formdocspacecomment" id="formdocspacecomment"></a>
<div class="Form formdocspacecomment"><!--start formdocspacecomment-->
   <fieldset>
<p>
	<label for="commenttitle" class="left">Tire : </label>
	<input type="text" size="35" id="commenttitle" name="commenttitle" tabindex="1"/>
</p>
<p>
	<label for="comment" class="left">Comment : </label>
    <textarea rows="4" cols="35" id="comment" name="comment" tabindex="2"></textarea>
</p>
  <div class="formMarginLeft">
     <input type="submit" value="Submit" name="Save" class="button" tabindex="3" />
</div>
</fieldset>






</div>
			</div>
		</div>
	</div>
</div><!--stop boxdocspace -->
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

