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
<template:addResources type="inlinejavascript">
    function noAccent(chaine) {
    temp = chaine.replace(/[àâä]/gi, "a");
    temp = temp.replace(/[éèêë]/gi, "e");
    temp = temp.replace(/[îï]/gi, "i");
    temp = temp.replace(/[ôö]/gi, "o");
    temp = temp.replace(/[ùûü]/gi, "u");
    var t = "";
    for (var i = 0; i < temp.length; i++) {
    if (temp.charCodeAt(i) > 47 && temp.charCodeAt(i) < 123)
    t += temp.charAt(i);
    }
    return t;
    }

</template:addResources>
<c:set var="pageNode" value="${jcr:getParentOfType(currentNode, 'jnt:page')}"/>
<c:if test="${jcr:hasPermission(pageNode, 'write')}">
    <div class="boxdocspace">
        <div class="boxdocspacegrey boxdocspacepadding16 boxdocspacemarginbottom16">

            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border"><!--start boxdocspace -->
                    <div class="formDocspace">
                        <form method="post" action="${pageNode.name}/" name="newDocspace">
                            <input type="hidden" name="autoCheckin" value="true">
                            <input type="hidden" name="nodeType" value="jnt:folder">
                            <input type="hidden" name="jcr:mixinTypes" value="jmix:tagged"/>
                            <input type="hidden" name="jcr:mixinTypes" value="mix:title"/>

                            <h3 class="boxdocspacetitleh3"><fmt:message key="docspace.label.docspace.new"/></h3>
                            <fieldset>
                                <legend><fmt:message key="docspace.label.workspace.creation"/></legend>

                                <p><label for="docspacetitle"><fmt:message key="docspace.label.title"/></label>
                                    <input type="text" name="jcr:title" id="docspacetitle" class="field" value=""
                                           tabindex="20"/></p>


                                <p><label for="docspacedesc"><fmt:message
                                        key="docspace.label.description"/></label>
                                    <textarea name="jcr:description" id="docspacedesc" cols="45" rows="3"
                                              tabindex="21"></textarea></p>

                                <div>
                                    <input type="submit" class="button"
                                           value="<fmt:message key="docspace.label.docspace.create"/>" tabindex="28"
                                           onclick="if (document.newDocspace.elements['jcr:title'].value == '') {
                                                       alert('you must fill the title ');
                                                       return false;
                                                   }
                                                   document.newDocspace.action = '${url.basePreview}${pageNode.path}/'+noAccent(document.newDocspace.elements['jcr:title'].value.replace(' ',''));
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
</c:if>