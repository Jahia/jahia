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
                       resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js,bootstrap-filestyle.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<jsp:useBean id="nowDate" class="java.util.Date"/>
<fmt:formatDate value="${nowDate}" pattern="yyyy-MM-dd-HH-mm" var="now"/>
<template:addResources>
    <script type="text/javascript">
        function getUuids() {
            var uuids = new Array();
            i = 0;
            $(".roleCheckbox:checked").each(function (index) {
                uuids[i++] = $(this).val();
            });
            return uuids;
        }

        function addSubRole() {
            var uuids = getUuids();
            if (uuids.length == 0) {
                alert('<fmt:message key="rolesmanager.rolesAndPermissions.subRole.selectParent" />');
                return false;
            }
            if (uuids.length > 1) {
                alert('<fmt:message key="rolesmanager.rolesAndPermissions.subRole.selectOnlyOneParent" />');
                return false;
            }
            $('#roleType').val($("#"+uuids[0]).attr("roleType"));
            $('#uuid').val(uuids[0]);
            $('#eventId').val('addRole');
            $('#roleForm').submit();
        }

        function deleteRoles() {
            var uuids = getUuids();
            if (uuids.length == 0) {
                return false;
            }
            $('#uuid').val(uuids.join(","));
            $('#eventId').val('deleteRoles');
            $('#roleForm').submit();
        }

        function viewRole(uuid) {
            $('#uuid').val(uuid);
            $('#eventId').val('viewRole');
            $('#roleForm').submit();
        }
    </script>
</template:addResources>
<div class="box-1">
    <fieldset>
        <h2>Roles and permissions</h2>
        <form style="margin: 0;" action="${flowExecutionUrl}" method="POST" id="roleForm">
        <select id="roleType" name="roleType">
            <c:forEach items="${handler.roleTypes.values}" var="roleType">
                <option value="${roleType.name}">
                <fmt:message key="rolesmanager.rolesAndPermissions.roleType.${fn:replace(roleType.name,'-','_')}"/>
                </option>
            </c:forEach>
        </select>
        <input type="text" id="addRoleField" name="newRole"/>
        <input type="hidden" id="uuid" name="uuid"/>
        <input type="hidden" id="eventId" name="_eventId" value="addRole" />
        <button class="btn btn-primary" type="submit" onclick="${'#parentRoleId'}.val('')">
            <i class="icon-plus  icon-white"></i>
            <fmt:message key="rolesmanager.rolesAndPermissions.role.add" />
        </button>
        <button class="btn btn-primary" type="button" onclick="addSubRole()">
            <i class="icon-plus  icon-white"></i>
            <fmt:message key="rolesmanager.rolesAndPermissions.subRole.add" />
        </button>
            <button class="btn btn-danger" type="button" onclick="deleteRoles()">
                <i class="icon-remove  icon-white"></i>
                <fmt:message key="rolesmanager.rolesAndPermissions.role.delete" />
            </button>
        </form>
    </fieldset>
</div>
<c:forEach var="msg" items="${flowRequestContext.messageContext.allMessages}">
    <div class="alert ${msg.severity == 'ERROR' ? 'validationError' : ''} ${msg.severity == 'ERROR' ? 'alert-error' : 'alert-success'}">
        <button type="button" class="close" data-dismiss="alert">&times;</button>
            ${fn:escapeXml(msg.text)}
    </div>
</c:forEach>

<c:forEach items="${roles}" var="entry" varStatus="loopStatus">
    <fieldset>

           <h3><fmt:message key="rolesmanager.rolesAndPermissions.roleType.${fn:replace(entry.key,'-','_')}"/></h3>

        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th width="3%">&nbsp;</th>
                <th width="25%">
                    <fmt:message key="label.name"/>
                </th>
                <th width="72%">
                    <fmt:message key="label.description"/>
                </th>
            </tr>
            </thead>

            <tbody>
            <c:forEach items="${entry.value}" var="role" varStatus="loopStatus2">
                <tr>
                    <td><input id="${role.uuid}" name="selectedRoles" class="roleCheckbox" type="checkbox" value="${role.uuid}" roleType="${entry.key}" /></td>
                    <td>
                        <c:forEach var="i" begin="3" end="${role.depth}" step="1" varStatus="loopStatus3">
                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        </c:forEach>
                        <strong><a href="#" onclick="viewRole('${role.uuid}')">${role.title} (${role.name})</a></strong>
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
