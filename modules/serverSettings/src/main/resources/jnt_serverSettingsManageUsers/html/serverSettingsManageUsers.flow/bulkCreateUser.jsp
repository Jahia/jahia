<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="mailSettings" type="org.jahia.services.mail.MailSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="flowExecutionUrl" type="java.lang.String"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.blockUI.js,admin-bootstrap.js,bootstrap-filestyle.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources>
    <script type="text/javascript">
        $(document).ready(function() {
            $('#${currentNode.identifier}-confirm').click(workInProgress)
        });
        $(document).ready(function () {
            $(":file").filestyle({classButton: "btn",classIcon: "icon-folder-open"/*,buttonText:"Translation"*/});
        });
    </script>
</template:addResources>

<c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
    <c:if test="${message.severity eq 'ERROR'}">
        <div class="alert alert-danger">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
                ${message.text}
        </div>
    </c:if>
    <c:if test="${message.severity eq 'INFO'}">
        <div class="alert alert-success">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
                ${message.text}
        </div>
    </c:if>
</c:forEach>
<h2><fmt:message key="serverSettings.users.bulk.create"/></h2>
<div class="box-1">
    <form action="${flowExecutionUrl}" method="post" enctype="multipart/form-data" autocomplete="off">

            <fieldset>
                <div class="alert alert-info">
                    <label for="csvFile"><fmt:message key="label.csvFile"/></label>
                    <input type="file" name="csvFile" id="csvFile"/>
                </div>
                <label for="csvSeparator"><fmt:message key="label.csvSeparator"/></label>
                <input class="span6" type="text" name="csvSeparator" value="${csvFile.csvSeparator}" id="csvSeparator"/>
            </fieldset>

        <fieldset>
            <button class="btn btn-primary" type="submit" name="_eventId_confirm" id="${currentNode.identifier}-confirm">
                <i class="icon-ok icon-white"></i>
                &nbsp;<fmt:message key='label.ok'/>
            </button>
            <button class="btn" type="submit" name="_eventId_cancel">
                <i class="icon-ban-circle"></i>
                &nbsp;<fmt:message key='label.cancel'/>
            </button>
        </fieldset>
    </form>
    <p>
        <fmt:message key="serverSettings.users.batch.file.format"/>
    </p>
</div>