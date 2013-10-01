<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jcr:node var="sites" path="/sites"/>
<jcr:nodeProperty name="j:defaultSite" node="${sites}" var="defaultSite"/>
<c:set var="defaultPrepackagedSite" value="acmespace.zip"/>
<template:addResources type="javascript"
                       resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js,bootstrap-filestyle.min.js,jquery.blockUI.js,workInProgress.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>
<jsp:useBean id="nowDate" class="java.util.Date"/>
<fmt:formatDate value="${nowDate}" pattern="yyyy-MM-dd-HH-mm" var="now"/>
<template:addResources>
    <script type="text/javascript">
        $(document).ready(function() {
            $('#${currentNode.identifier}-deleteRolesConfirmed').click(function() {workInProgress('${i18nWaiting}');});
        });
    </script>
</template:addResources>
<div class="box-1">
    <h2><fmt:message key="rolesmanager.rolesAndPermissions.role.delete" /></h2>
    <fmt:message key="rolesmanager.rolesAndPermissions.role.delete.confirm" />
</div>

<c:forEach items="${roles}" var="entry" varStatus="loopStatus">
    <fieldset>

           <h3><fmt:message key="rolesmanager.rolesAndPermissions.roleType.${fn:replace(entry.key,'-','_')}"/></h3>

        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th width="25%">
                    <fmt:message key="label.name"/>
                </th>
                <th width="75%">
                    <fmt:message key="label.description"/>
                </th>
            </tr>
            </thead>

            <tbody>
            <c:forEach items="${entry.value}" var="role" varStatus="loopStatus2">
                <tr>
                    <td>
                        <c:forEach var="i" begin="3" end="${role.depth}" step="1" varStatus="loopStatus3">
                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        </c:forEach>
                        <strong>${role.title} (${role.name})</strong>
                    </td>
                    <td>
                        ${role.description}
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

    </fieldset>
</c:forEach>

<form style="margin: 0;" action="${flowExecutionUrl}" method="POST">
    <button class="btn btn-danger" type="submit" name="_eventId_deleteRolesConfirmed" id="${currentNode.identifier}-deleteRolesConfirmed">
        <i class="icon-remove icon-white"></i>
        <fmt:message key="rolesmanager.rolesAndPermissions.role.delete" />
    </button>
    <button class="btn" type="submit" name="_eventId_cancel">
        <i class="icon-ban-circle"></i>
        <fmt:message key="label.cancel" />
    </button>
</form>
