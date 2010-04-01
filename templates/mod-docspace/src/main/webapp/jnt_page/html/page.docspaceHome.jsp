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
<template:addResources type="css" resources="docspace.css,files.css,toggle-docspace.css,jquery.treeview.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.treeview.min.js"/>
<script type="text/javascript">
    function noAccent(chaine) {
        temp = chaine.replace(/[àâä]/gi, "a");
        temp = temp.replace(/[éèêë]/gi, "e");
        temp = temp.replace(/[îï]/gi, "i");
        temp = temp.replace(/[ôö]/gi, "o");
        temp = temp.replace(/[ùûü]/gi, "u");
        var t = "";
        for (var i = 0; i < temp.length; i++) {
            if (temp.charCodeAt(i) > 47 && temp.charCodeAt(i) < 123) t += temp.charAt(i);
        }
        return t;
    }

    $(document).ready(function() {
        $("#docspaceTree").treeview();
    });
</script>
<div class='grid_6'><!--start grid_6-->
    <h4 class="boxdocspace-title2"><fmt:message key="docspace.label.workspace"/></h4>

    <div class="boxdocspace"><!--start boxdocspace -->
        <div class="boxdocspacepadding16 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border">
                    <ul id="docspaceTree" class="filetree">
                        <c:forEach var="node" items="${jcr:getChildrenOfType(currentNode,'jnt:docspace')}">
                            <template:module node="${node}" forcedTemplate="hidden.tree"/>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <!--stop boxdocspace -->
    <div class="boxdocspace">
        <div class="boxdocspacegrey boxdocspacepadding16 boxdocspacemarginbottom16">

            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border"><!--start boxdocspace -->

                    <div class="formCreateDocspace">
                        <form method="post" action="${currentNode.name}/" name="newDocspace">
                            <input type="hidden" name="autoCheckin" value="true">
                            <input type="hidden" name="nodeType" value="jnt:docspace">

                            <h3 class="boxdocspacetitleh3"><fmt:message key="docspace.label.workspace.new"/></h3>
                            <fieldset>
                                <legend><fmt:message key="docspace.label.workspace.creation"/></legend>

                                <p><label for="docspacetitle" ><fmt:message key="docspace.label.title"/>:</label>
                                    <input type="text" name="jcr:title" id="docspacetitle" class="field" value=""
                                           tabindex="20"/></p>


                                <p><label for="docspacedesc" ><fmt:message
                                        key="docspace.label.description"/>:</label>
                                    <textarea name="jcr:description" id="docspacedesc" cols="45" rows="3"
                                              tabindex="21"></textarea></p>

                                <div>
                                    <input type="submit" class="button"
                                           value="<fmt:message key="docspace.label.workspace.create"/>" tabindex="28"
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

    <div class='clear'></div>
</div>
<!--stop grid_6-->

<div class='grid_10'><!--start grid_10-->
    <div class="boxdocspace"><!--start boxdocspace -->
        <div class="boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">

                <div id="search-docspace">
                    <h3 class="boxdocspacetitleh3">Search</h3>

                    <form method="get" action="#">
                        <fieldset>
                            <p class="field">
                                <input type="text" value="" name="search2" class="search docspacesearch" tabindex="4"/>
                                <input type="submit" value="Search" class="button searchbutton" tabindex="5"/>
                            </p>
                            <p>
                                <label class="formFloatLeft">
                                    <input type="radio" tabindex="10" id="RadioGroup2_0" value="radio"
                                           name="RadioGroup2"/>
                                    Tags, Categories</label>
                                <label class="formFloatLeft">
                                    <input type="radio" tabindex="11" id="RadioGroup2_1" value="radio"
                                           name="RadioGroup2"/>
                                </label>
                                all content</p>
                        </fieldset>
                    </form>
                </div>


            </div>
        </div>
    </div>
    <!--stop boxdocspace -->
    <div class='grid_5 alpha '><!--start grid_5-->
        <h4 class="boxdocspace-title"><fmt:message key="docspace.label.workspace.last.document"/></h4>
        <ul class="docspacelist">
            <jcr:sql var="result"
                     sql="select * from [jnt:file] as file where isdescendantnode(file, ['${currentNode.path}']) order by file.[jcr:lastModified] desc"/>
            <c:forEach items="${result.nodes}" var="document" end="10">
                <li>
                    <c:if test="${jcr:hasPermission(document, 'write')}">
                        <a class="${functions:fileIcon(document.name)}"
                           href="${url.basePreview}${document.path}.docspace.html"
                           title="${document.name}">${functions:abbreviate(document.name,20,30,'...')}</a>
                    </c:if>
                    <c:if test="${not jcr:hasPermission(document, 'write')}">
                        <a class="${functions:fileIcon(document.name)}"
                           href="${url.baseLive}${document.path}.docspace.html"
                           title="${document.name}">${functions:abbreviate(document.name,20,30,'...')}</a>
                    </c:if>
                    <span class="docspacelistinfo">${jcr:humanReadableFileLength(document)}</span>

                    <p class="docspacelistinfo2">${functions:abbreviate(functions:removeHtmlTags(document.properties['jcr:description'].string),100,150,'...')}</p>
                </li>
            </c:forEach>
        </ul>
        <!--stop boxdocspace -->
    </div>
    <!--stop grid_5-->
    <div class='grid_5 omega'><!--start grid_5-->
        <h4 class="boxdocspace-title"><fmt:message key="docspace.label.workspace.last"/></h4>
        <ul class="docspacelist">
            <jcr:sql var="result"
                     sql="select * from [jnt:docspace] as file where isdescendantnode(file, ['${currentNode.path}']) order by file.[jcr:lastModified] desc"/>
            <c:forEach items="${result.nodes}" var="docspace" end="10">
                <li>
                    <c:if test="${jcr:hasPermission(docspace, 'write')}">
                        <a class="adocspace" href="${url.basePreview}${docspace.path}.html"
                           title="${docspace.name}">${functions:abbreviate(docspace.name,20,30,'...')}</a>
                    </c:if>
                    <c:if test="${not jcr:hasPermission(docspace, 'write')}">
                        <a class="adocspace" href="${url.baseLive}${docspace.path}.html"
                           title="${docspace.name}">${functions:abbreviate(docspace.name,20,30,'...')}</a>
                    </c:if>
                    <span class="docspacelistinfo"><fmt:message
                            key="docspace.label.document.lastModification"/>&nbsp;<fmt:formatDate
                            value="${docspace.properties['jcr:lastModified'].time}" dateStyle="medium"/></span>

                    <p class="docspacelistinfo2">${functions:abbreviate(functions:removeHtmlTags(docspace.properties['jcr:description'].string),100,150,'...')}</p>
                </li>
            </c:forEach>
        </ul>
        <!--stop boxdocspace -->
    </div>
    <!--stop grid_5-->

    <div class='clear'></div>
</div>
<!--stop grid_10-->