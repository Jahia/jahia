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
<script type="text/javascript">
   function noAccent(chaine) {
      temp = chaine.replace(/[àâä]/gi,"a");
      temp = temp.replace(/[éèêë]/gi,"e");
      temp = temp.replace(/[îï]/gi,"i");
      temp = temp.replace(/[ôö]/gi,"o");
      temp = temp.replace(/[ùûü]/gi,"u");
       var t = "";
       for (var i = 0;i<temp.length;i++) {
           if (temp.charCodeAt(i) >47 && temp.charCodeAt(i) < 123) t +=temp.charAt(i);
       }
       return t;
   }
</script>
<div class='grid_6'><!--start grid_6-->
    <h4 class="boxdocspace-title2">Espaces de travail</h4>
    <div class="boxdocspace"><!--start boxdocspace -->
        <div class="boxdocspacepadding16 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border">
                    <c:forEach var="node" items="${currentNode.nodes}">
                        <c:if test="${jcr:isNodeType(node,'jnt:docspace')}">
                            <a href="${url.base}${node.path}.html">${node.path}</a>
                        </c:if>
                    </c:forEach>
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div><!--stop boxdocspace -->
    <div class="boxdocspace">
        <div class="boxdocspacegrey boxdocspacepadding16 boxdocspacemarginbottom16">

            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border"><!--start boxdocspace -->

                    <div class="Form formCreateDocspace">
                        <form method="post"  action="${currentNode.name}/" name="newDocspace">
                            <input type="hidden" name="autoCheckin" value="true">
                            <input type="hidden" name="nodeType" value="jnt:docspace">
                            <h3 class="boxdocspacetitleh3">Creer un nouvel espace de travail</h3>
                            <fieldset><legend>Creation d'un espace de travail</legend>

                                <p><label for="docspacetitle" class="left">Titre :</label>
                                    <input type="text" name="jcr:title" id="docspacetitle" class="field" value="" tabindex="20" /></p>


                                <p><label for="docspacedesc" class="left">Description :</label>
                                    <textarea name="jcr:description" id="docspacedesc" cols="45" rows="3" tabindex="21"></textarea></p>
 <%--                               <p>
                                    <label for="docspacecat" class="left">Categories :</label>
                                    <input type="text" name="docspacecat" id="docspacecat" class="field" value="" tabindex="22" /></p>
 --%>                               <div class="formMarginLeft">
                                    <input type="submit" class="button" value="Creer le docspace" tabindex="28"
                                           onclick="
                                                   if (document.newDocspace.elements['jcr:title'].value == '') {
                                                       alert('you must fill the title ');
                                                       return false;
                                                   }
                                                   document.newDocspace.action = '${currentNode.name}/'+noAccent(document.newDocspace.elements['jcr:title'].value.replace(' ',''));
                                                   document.newDocspace.submit();
                                               "
                                            />
                                </div>
                            </fieldset>
                        </form>
                    </div>
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop boxdocspace -->

    <div class='clear'></div></div><!--stop grid_6-->

<div class='grid_10'><!--start grid_10-->
    <div class="boxdocspace"><!--start boxdocspace -->
        <div class="boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">

                <div id="search-docspace">
                    <h3 class="boxdocspacetitleh3">Rechercher</h3>
                    <form method="get" action="#">
                        <fieldset>
                            <p class="field">
                                <input type="text" value="" name="search2" class="search docspacesearch" tabindex="4"/>
                                <input type="submit" value="Rechercher" class="button searchbutton" tabindex="5"/>
                            </p>
                            <p>
                                <label class="formFloatLeft">
                                    <input type="radio" tabindex="10" id="RadioGroup2_0" value="radio" name="RadioGroup2" />
                                    les tags, categories ou infos </label>
                                <label class="formFloatLeft">
                                    <input type="radio" tabindex="11" id="RadioGroup2_1" value="radio" name="RadioGroup2" />
                                </label>
                                tout le contenu</p>
                        </fieldset>
                    </form>
                </div>


            </div>
        </div>
    </div><!--stop boxdocspace -->
    <div class='grid_5 alpha '><!--start grid_5-->
        <h4 class="boxdocspace-title">Derniers documents publics</h4>
        <ul class="docspacelist">
            <li>
                <a class="file" href="#" >mon document </a><span class="docspacelistinfo">50ko</span>
                <p class="docspacelistinfo2">le resume de mon documentle resume de mon documentle resume de mon documentle resume de mon document </p>
            </li>
            <li>
                <a class="ppt" href="#" >mon document </a><span class="docspacelistinfo">50ko</span>
                <p class="docspacelistinfo2">le resume de mon documentle resume de mon documentle resume de mon documentle resume de mon document </p>
            </li>
            <li>
                <a class="doc" href="#" >mon document </a><span class="docspacelistinfo">50ko</span>
                <p class="docspacelistinfo2">le resume de mon documentle resume de mon documentle resume de mon documentle resume de mon document </p>
            </li>
            <li>
                <a class="rar" href="#" >mon document </a><span class="docspacelistinfo">50ko</span>
                <p class="docspacelistinfo2">le resume de mon documentle resume de mon documentle resume de mon documentle resume de mon document </p>
            </li>
            <li class="last">
                <a class="pdf" href="#" >mon document </a><span class="docspacelistinfo">50ko</span>
                <p class="docspacelistinfo2">le resume de mon documentle resume de mon documentle resume de mon documentle resume de mon document </p>
            </li>
        </ul>
        <!--stop boxdocspace -->
    </div><!--stop grid_5-->
    <div class='grid_5 omega'><!--start grid_5-->
        <h4 class="boxdocspace-title">Derniers Espaces crees</h4>
        <ul class="docspacelist">
            <li>
                <a class="adocspace" href="#" >Mon espace </a><span class="docspacelistinfo">cree le 08/02/2010</span>
                <p class="docspacelistinfo2">la description de mon espace la info2 de mon espace la description de mon espace la info2 de mon espace</p>
            </li>
            <li>
                <a class="adocspace" href="#" >Mon espace </a><span class="docspacelistinfo">cree le 08/02/2010</span>
                <p class="docspacelistinfo2">la description de mon espace la info2 de mon espace la description de mon espace la info2 de mon espace</p>
            </li>
            <li>
                <a class="adocspace" href="#" >Mon espace </a><span class="docspacelistinfo">cree le 08/02/2010</span>
                <p class="docspacelistinfo2">la description de mon espace la info2 de mon espace la description de mon espace la info2 de mon espace</p>
            </li>
            <li>
                <a class="adocspace" href="#" >Mon espace </a><span class="docspacelistinfo">cree le 08/02/2010</span>
                <p class="docspacelistinfo2">la description de mon espace la info2 de mon espace la description de mon espace la info2 de mon espace</p>
            </li>
            <li class="last">
                <a class="adocspace" href="#" >Mon espace </a><span class="docspacelistinfo">cree le 08/02/2010</span>
                <p class="docspacelistinfo2">la description de mon espace la info2 de mon espace la description de mon espace la info2 de mon espace</p>
            </li>
        </ul>
        <!--stop boxdocspace -->
    </div><!--stop grid_5-->

<div class='clear'></div></div><!--stop grid_10-->