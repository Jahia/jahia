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
<template:addResources type="css" resources="docspace.css,files.css,toggle-docspace.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>

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
                    <option>Demander a l'acces</option>
                    <option>Ajouter un Utilisateur</option>
                    <option>Partager</option>
                    </select>
                  </form>
                </div>
                <div class="imagefloatleft">
                  <div class="itemImage itemImageLeft"><a href="#"><img alt="" src="${url.currentModule}/css/img/docspacebig.png"/></a></div>
                </div>
                <h3 >Espace : <jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
                      <p class="clearMaringPadding docspacedate">Date de creation :
                        <jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
                        <fmt:formatDate value="${created.time}" pattern="yyyy/MM/dd HH:mm"/>
                      </p>
  <p class="clearMaringPadding docspaceauthor"><a href="#">Par ${currentNode.propertiesAsString['jcr:createdBy']}</a></p>

                <p class="clearMaringPadding"><jcr:nodeProperty node="${currentNode}" name="jcr:description"/></p>
                <!--stop boxdocspace -->
                  <div class="clear"></div>
              </div>
        </div>
    </div>
</div><!--stop boxdocspace -->
<div class='clear'></div></div><!--stop grid_12-->
<%--list all users write write access to current node--%>
<jcr:sql var="users" sql="select "

<div class='grid_4'><!--start grid_4-->
    <h4 class="boxdocspace-title">Users</h4>

    <ul class="docspacelist docspacelistusers">
        <li>
            <img class="floatleft" alt="user default icon" src="${url.currentModule}/css/img/user_32.png" />
            <a class="floatleft" href="#" >le nom du user</a>
            <div class='clear'></div>
        </li>
    </ul>
    <div class='clear'></div></div><!--stop grid_4-->
<div class='grid_16'><!--start grid_16-->
    <!--<div class="boxdocspace">
<div class="edit"><a href="#" title="editer" ><span class="hidden">editer</span></a></div>
        <div class=" boxdocspaceyellow boxdocspacepadding16 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border">

                <h3 class="boxdocspacetitleh3 clearMaringPadding">Annonce :</h3>
                <p class="clearMaringPadding">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean scelerisque lorem sed metus vehicula non venenatis eros blandit. Donec molestie vestibulum nunc, ac molestie augue semper a. Quisque ut pharetra sem. Ut vitae urna ipsum.</p>
                  <div class="clear"></div>
              </div>
        </div>
    </div>
</div>-->

<h4 class="boxdocspace-title2">Espaces de travail</h4>
<div class="boxdocspace-title2">

                       <div class="TableActions"><!--start formSearchTop-->

                        <form action="#" method="post">
                          <p>
                            <a href="#" title="view details" ><img class="rightside" src="${url.currentModule}/css/img/view-details.png" alt="add user"/></a>
                            <a href="#" title="view thumbnails" ><img class="rightside" src="${url.currentModule}/css/img/view-thumbnails.png" alt="add user"/></a>
                            <label>Filtre : </label>

                            <select name="tagfilter">
                              <option>Mon tag 1</option>
                              <option>Mon tag 2</option>
                              <option>Mon tag 3</option>
                            </select>
                            <label> - Search: </label>
                            <input class="text" type="text" name="search"  value="Search..." tabindex="4"/>
                            <input class="gobutton" type="image" src="${url.currentModule}/css/img/search-button.png" tabindex="5"/>

                          </p>

                        </form>
                        <form action="${currentNode.name}/*" method="POST" name="uploadFile" enctype="multipart/form-data">
                            <input type="hidden" name="nodeType" value="jnt:file"/>
                            <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
                            <input type="hidden" name="targetDirectory" value="${currentNode.path}"/>
                            <input type="file" name="file">
                            <input type="hidden" name="jcr:mixinTypes" value="jmix:comments"/>
                            <input type="hidden" name="jcr:mixinTypes" value="jmix:tagged"/>
                            <input type="submit" name="upload"/>
                        </form>
                      </div><!--stop formSearchTop-->

</div>
<script type="text/javascript">

            jQuery(document).ready(function(){

                // Masquer la div à slider
                jQuery(".AddNote1").hide();

                //Appliquer la classe active sur le bouton
                jQuery(".BtMore").toggle(function(){
                    jQuery(this).addClass("active");
                }, function () {
                        jQuery(this).removeClass("active");
                    });

                // Slide down et up sur click
                jQuery(".BtMore").click(function(){
                    jQuery(this).next(".AddNote1").slideToggle("slow");
                });

            });

</script>
<div class="boxdocspace">
        <div class="boxdocspacegrey boxdocspacepadding16 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border"><!--start boxdocspace -->
                    <template:area forcedTemplate="hidden.docspace" areaType="jnt:docFilesList" path="filesList" forceCreation="true"/>


<div class="clear"></div>

              </div>
        </div>
    </div>
</div><!--stop boxdocspace -->

<div class='clear'></div></div><!--stop grid_16-->


