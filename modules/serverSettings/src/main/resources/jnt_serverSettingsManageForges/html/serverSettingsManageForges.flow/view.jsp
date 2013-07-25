<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
<%--@elvariable id="forge" type="org.jahia.modules.serversettings.forge.Forge"--%>
<script>
    function populateForgeForm(id,url,user) {
        $("#forge input[name=url]").val(url);
        $("#forge input[name=id]").val(id);
        $("#forge input[name=user]").val(user);
        $("#forgeTitle").html('<fmt:message key="serverSettings.manageForges.title.edit"/> '+url);
    }
    function resetForgeForm() {
        $("#forge input[name=id]").val('');
        $("#forge input[name=oldUrl]").val('');
        $("#forge input[name=user]").val('');
        $("#forge input[name=actionType]").val('add');
        $("#forgeTitle").html('<fmt:message key="serverSettings.manageForges.title.add"/>');
    }
    function deleteForge(id) {
        $("#forge input[name=id]").val(id);
        $("#forge input[name=actionType]").val('delete');
        $('#submit').click();

    }
</script>
<table class="table table-bordered table-striped table-hover">
    <thead>
    <tr>
        <th><fmt:message key="serverSettings.manageForges.url"/></th>
        <th>
            <fmt:message key="serverSettings.manageForges.user"/>
        </th>
        <th>

        </th>
    </tr>
    </thead>

    <tbody>

    <c:forEach items="${requestScope.forges}" var="forge">
        <tr>
            <td>${forge.url}</td>
            <td> ${forge.user}</td>
            <td><a href="#" onclick="populateForgeForm('${forge.id}','${forge.url}','${forge.user}');">edit</a>
            <a href="#" onclick="deleteForge('${forge.id}')">
                <i class="icon-remove"></i>
                <fmt:message key="label.delete"/></a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<c:forEach var="msg" items="${flowRequestContext.messageContext.allMessages}">
    <div class="${msg.severity == 'ERROR' ? 'validationError' : ''} alert ${msg.severity == 'ERROR' ? 'alert-error' : 'alert-success'}"><button type="button" class="close" data-dismiss="alert">&times;</button>${fn:escapeXml(msg.text)}</div>
</c:forEach>
<div class="box-1">
    <form:form modelAttribute="forge" cssClass="form" autocomplete="off">
        <input type="hidden" name="actionType" value="add"/>
        <h3 id="forgeTitle"><fmt:message key="serverSettings.manageForges.title.add"/></h3>
        <div class="container-fluid">
            <div class="row-fluid">
                <div class="span8">
                    <label for="url"><fmt:message key="serverSettings.manageForges.url"/></label>
                    <form:input class="span12" type="text" id="url" path="url"/>
                    <form:input path="id" type="hidden"/>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span4">
                    <label for="user"><fmt:message key="serverSettings.manageForges.user"/></label>
                    <form:input class="span12" type="text" id="user" path="user"/>
                </div>
                <div class="span4">
                    <label for="password"><fmt:message key="serverSettings.manageForges.password"/></label>
                    <form:password class="span12" type="password" id="password" path="password"/>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span4">
                    <button class="btn btn-primary" id="submit" type="submit" name="_eventId_submit"><i class="icon-ok icon-white"></i>&nbsp;<fmt:message key='label.save'/></button>
                </div>
                <div class="span4">
                    <button class="btn btn-primary" onclick="resetForgeForm();" type="reset"><i class="icon-ok icon-white"></i>&nbsp;<fmt:message key='label.reset'/></button>
                </div>
            </div>
        </div>
    </form:form>
</div>
