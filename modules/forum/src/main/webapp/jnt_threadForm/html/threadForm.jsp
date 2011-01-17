<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="forum.css"/>
<template:linker property="j:bindedComponent"/>
<c:set var="linked" value="${uiComponents:getBindedComponentPath(currentNode, renderContext, 'j:bindedComponent')}"/>
<template:tokenizedForm>
<form action="${url.base}${linked}/*" method="post">
    <input type="hidden" name="nodeType" value="jnt:thread"/>
    <input type="hidden" name="autoAssignRole" value="owner"/>

    <div class="post-reply"><!--start post-reply-->
        <div class="forum-box forum-box-style2">
            <span class="forum-corners-top"><span></span></span>

            <div id="forum-Form"><!--start forum-Form-->
                <h4 class="forum-h4-first"><fmt:message key="create.new.thread"/></h4>

                <fieldset>
                    <p class="field">
                        <input value="New Thread Subject" type="text" size="35" id="forum_site" name="threadSubject"
                               tabindex="1"/>
                    </p>

                    <p class="forum_button">
                        <input type="reset" value="Reset" class="button" tabindex="3"/>

                        <input type="submit" value="Submit" class="button" tabindex="4"/>
                    </p>
                </fieldset>
            </div>
            <!--stop forum-Form-->
            <div class="clear"></div>
            <span class="forum-corners-bottom"><span></span></span>
        </div>
    </div>
</form>
</template:tokenizedForm>