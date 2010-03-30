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
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/ckeditor/ckeditor.js"/>

<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<script>
    $(document).ready(function() {
        $("#ckeditorEditDescription").editable(function (value, settings) {
            var url = $(this).attr('jcr:url');
            var submitId = $(this).attr('jcr:id');
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post(url, data, null, "json");
            return(value);
        }, {
            type : 'ckeditor',
            onblur : 'ignore',
            submit : 'Ok',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });

        $("#actions").change(function() {
            if ($(this).val() == 'delete') {
                if (confirm("Do you really want to delete this file?")) {
                    var data = {};
                    data['methodToCall'] = 'delete';
                    $.post('${url.base}${currentNode.path}', data, function () {
                        window.location.href = '${url.base}${currentNode.parent.path}.html';
                    }, "json");
                } else {
                    $(this).val("");
                }
            }
        });
    });
</script>
<div class='grid_12'><!--start grid_12-->

    <div class="boxdocspace "><!--start boxdocspace -->
        <div class="boxdocspacepadding16 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border">
                    <div class="floatright">
                        <form action="#" method="post">
                            <select name="actions" id="actions">
                                <option value="">Actions</option>
                                <option value="delete">Suprimer</option>
                                <option>Demander a l'acces</option>
                                <option>Ajouter un Utilisateur</option>
                                <option>Partager</option>
                            </select>
                        </form>
                    </div>
                    <div class="imagefloatleft">
                        <div class="itemImage itemImageLeft"><a href="#"><img alt=""
                                                                              src="${url.currentModule}/css/img/docspacebig.png"/></a>
                        </div>
                    </div>
                    <h3>Espace : <jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>

                    <p class="clearMaringPadding docspacedate">Date de creation :
                        <jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
                        <fmt:formatDate value="${created.time}" pattern="yyyy/MM/dd HH:mm"/>
                    </p>

                    <p class="clearMaringPadding docspaceauthor"><a
                            href="#"><fmt:message key="docspace.label.document.createdBy"/> ${currentNode.properties['jcr:createdBy'].string}</a></p>

                    <p class="clearMaringPadding"><span jcr:id="jcr:description" id="ckeditorEditDescription"
                              jcr:url="${url.base}${currentNode.path}">
                        <c:if test="${not empty currentNode.properties['jcr:description'].string}">${currentNode.properties['jcr:description'].string}</c:if>
                        <c:if test="${empty currentNode.properties['jcr:description'].string}">Add a description (click here)</c:if>
                    </span></p>
                    <!--stop boxdocspace -->
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop boxdocspace -->
    <div class='clear'></div>
</div>
<!--stop grid_12-->
<%--list all users write write access to current node--%>

<div class='grid_4'><!--start grid_4-->
    <h4 class="boxdocspace-title">Users</h4><template:area path="searchUsers" forceCreation="true" areaType="jnt:searchUsers"/>

    <ul class="docspacelist docspacelistusers">
        <c:forEach items="${currentNode.aclEntries}" var="acls">
            <li>
                <c:set var="users" value="${fn:substringBefore(acls.key, ':')}"/>
                <c:choose>
                    <c:when test="${users eq 'u'}">
                        <c:set value="user_32" var="iconName"/>
                    </c:when>
                    <c:when test="${users eq 'g'}">
                        <c:set value="group-icon" var="iconName"/>
                    </c:when>
                </c:choose>
                <img class="floatleft" alt="user default icon" src="${url.currentModule}/images/${iconName}.png"/>
                <a class="floatleft" href="#"><c:out value="${fn:substringAfter(acls.key,':')}"/></a>

                <div class='clear'></div>
            </li>
        </c:forEach>
    </ul>
   
    <div class='clear'></div>
</div>
<!--stop grid_4-->
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

   <div class="boxdocspace"><!--start boxdocspace -->
	<div class="boxdocspacegrey boxdocspacepadding10 ">
		<div class="boxdocspace-inner">
			<div class="boxdocspace-inner-border">

            <form action="${currentNode.name}/*" method="POST" name="uploadFile" enctype="multipart/form-data">
                <input type="hidden" name="nodeType" value="jnt:file"/>
                <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
                <input type="hidden" name="targetDirectory" value="${currentNode.path}"/>
                <input type="file" name="file">
                <input type="hidden" name="jcr:mixinTypes" value="jmix:comments"/>
                <input type="hidden" name="jcr:mixinTypes" value="jmix:tagged"/>
                <input type="hidden" name="jcr:mixinTypes" value="jnt:docspaceFile"/>
                <input type="hidden" name="jcr:mixinTypes" value="jmix:rating"/>
                <input type="hidden" name="jcr:mixinTypes" value="mix:title"/>
                <input type="hidden" name="version" value="true"/>
                <input class="button" type="submit" id="upload" value="Upload"/>
            </form>

            </div>
		</div>
	</div>
</div>
    <script type="text/javascript">

        jQuery(document).ready(function() {

            // Masquer la div à slider
            jQuery(".AddNote1").hide();

            //Appliquer la classe active sur le bouton
            jQuery(".BtMore").toggle(function() {
                jQuery(this).addClass("active");
            }, function () {
                jQuery(this).removeClass("active");
            });

            // Slide down et up sur click
            jQuery(".BtMore").click(function() {
                jQuery(this).next(".AddNote1").slideToggle("slow");
            });

        });

    </script>
    <div class="boxdocspace">
        <div class="boxdocspacegrey boxdocspacepadding16 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border"><!--start boxdocspace -->
                    <template:area forcedTemplate="hidden.docspace" areaType="jnt:docFilesList" path="filesList"
                                   forceCreation="true"/>


                    <div class="clear"></div>

                </div>
            </div>
        </div>
    </div>
    <!--stop boxdocspace -->

    <div class='clear'></div>
</div>
<!--stop grid_16-->


