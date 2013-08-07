<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<jcr:node var="sites" path="/sites"/>
<jcr:nodeProperty name="j:defaultSite" node="${sites}" var="defaultSite"/>
<c:set var="defaultPrepackagedSite" value="acmespace.zip"/>
<template:addResources type="javascript"
                       resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js,bootstrap-filestyle.min.js,jquery.tristate.js"/>

<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css,tristate.css"/>

<jsp:useBean id="nowDate" class="java.util.Date"/>
<fmt:formatDate value="${nowDate}" pattern="yyyy-MM-dd-HH-mm" var="now"/>


<script type="text/javascript">
    $(document).ready(function () {
        $("a.switchTab").click(function () {
            var tab = $(this).attr('tab');
            alert(tab);
            $("#switchTabForm").submit();
            return false;
        });

        $('#addContextField').keypress(function(e) {
            // Enter pressed?
            if(e.which == 10 || e.which == 13) {
                $("#addContextButton").click();
                return false;
            }

        });

        $("#form").submit(function() {
            selectedPermissions = [];
            $(".checkbox[class*='checked']").each(function(i) { selectedPermissions[i] = $(this).attr('path') })
            partialSelectedPermissions = [];
            $(".checkbox[class*='partial']").each(function(i) { partialSelectedPermissions[i] = $(this).attr('path') })

            $("#selectedPermissions").val(selectedPermissions)
            $("#partialSelectedPermissions").val(partialSelectedPermissions)

            window.alert(partial);
        })

        $(".checkbox").click(function() {

            if ($(this).hasClass('checked')) {
                uncheck(this)
            } else if ($(this).hasClass('partial')) {
                check(this)
            } else {
                check(this)
            }

            return false;
        })

        function uncheckthis() {
            uncheck(this)
        }

        function uncheck(obj) {
            $(obj).removeClass('checked')
            $(obj).removeClass('partial')
            $(":checkbox[name='selectedPermissions'][path='"+$(obj).attr('path')+"']").attr('checked',false)
            $(":checkbox[name='partialSelectedPermissions'][path='"+$(obj).attr('path')+"']").attr('checked',false)

            // Was checked, uncheck all
            selector = ".checkbox[parent='"+$(obj).attr('path')+"']"

            $(selector).each(uncheckthis)

            setParent($(obj).attr('parent'))
        }

        function checkthis() {
            check(this)
        }

        function check(obj) {
            $(obj).addClass('checked')
            $(obj).removeClass('partial')

            $(":checkbox[name='selectedPermissions'][path='"+$(obj).attr('path')+"']").attr('checked',true)
            $(":checkbox[name='partialSelectedPermissions'][path='"+$(obj).attr('path')+"']").attr('checked',false)

            // Was unchecked, check all
            selector = ".checkbox[parent='"+$(obj).attr('path')+"']"

            $(selector).each(checkthis)

            setParent($(obj).attr('parent'))
        }

        function setParent(parentPath) {
            selector = ".checkbox[path='"+parentPath+"']"

            if ($(".checkbox[parent='"+parentPath+"'][class*='checked']").size() ==  $(".checkbox[parent='"+parentPath+"']").size()) {
                $(selector).addClass('checked')
                $(selector).removeClass('partial')
                $(":checkbox[name='selectedPermissions'][path='"+parentPath+"']").attr('checked',true)
                $(":checkbox[name='partialSelectedPermissions'][path='"+parentPath+"']").attr('checked',false)
            } else if ($(".checkbox[parent='"+parentPath+"'][class*='checked']").size() > 0 || $(".checkbox[parent='"+parentPath+"'][class*='partial']").size() > 0) {
                $(selector).removeClass('checked')
                $(selector).addClass('partial')
                $(":checkbox[name='selectedPermissions'][path='"+parentPath+"']").attr('checked',false)
                $(":checkbox[name='partialSelectedPermissions'][path='"+parentPath+"']").attr('checked',true)
            } else {
                $(selector).removeClass('checked')
                $(selector).removeClass('partial')
                $(":checkbox[name='selectedPermissions'][path='"+parentPath+"']").attr('checked',false)
                $(":checkbox[name='partialSelectedPermissions'][path='"+parentPath+"']").attr('checked',false)
            }

            p = $(".checkbox[path='"+parentPath+"']").attr("parent")
            if (p) {
                setParent(p)
            }
        }
    });
</script>
<div class="clearfix">

    <h2>
        <form class="pull-left" action="${flowExecutionUrl}" method="POST" >
            <button class="btn" name="_eventId_rolesList" ><i class=" icon-chevron-left"></i>&nbsp;Back</button>
        </form>
        &nbsp;Roles and permissions : ${handler.roleBean.name}
    </h2>
</div>
<c:forEach var="msg" items="${flowRequestContext.messageContext.allMessages}">
    <div class="alert ${msg.severity == 'ERROR' ? 'validationError' : ''} ${msg.severity == 'ERROR' ? 'alert-error' : 'alert-success'}">
        <button type="button" class="close" data-dismiss="alert">&times;</button>
            ${fn:escapeXml(msg.text)}
    </div>
</c:forEach>






<form id="form" action="${flowExecutionUrl}" method="post">
    <input id="selectedPermissions" type="hidden" name="selectedPermissions"/>
    <input id="partialSelectedPermissions" type="hidden" name="partialSelectedPermissions"/>

    <div class="btn-group">
    <button class="btn btn-primary" type="submit" name="_eventId_saveRole" >
        <i class="icon-ok icon-white"></i>
        &nbsp;SAVE
    </button>
    </div>
    <p>

    </p>
    <input type="hidden" name="tab" id="tabField" value=""/>
    <div class="btn-group">
        <c:forEach items="${handler.roleBean.permissions}" var="permissionGroup">
            <button class="btn ${handler.currentContext eq permissionGroup.key ? 'btn-success':''}" type="submit" name="_eventId_switchContext" onclick="$('#tabField').val('${permissionGroup.key}')">
                    ${permissionGroup.key}
                    <%--<c:if test="${fn:startsWith(permissionGroup.key, 'context./')}">--%>
                            <%--<fmt:message key="rolesmanager.rolesAndPermissions.context"/> &nbsp; ${fn:substringAfter(permissionGroup.key, 'context.')}--%>
                            <%--</c:if>--%>
                            <%--<c:if test="${not fn:startsWith(permissionGroup.key, 'context./')}">--%>
                            <%--<fmt:message key="rolesmanager.rolesAndPermissions.${permissionGroup.key}"/>--%>
                            <%--</c:if>--%>
            </button>
            <button class="btn btn-danger" type="submit" name="_eventId_switchContext" onclick="$('#tabField').val('${permissionGroup.key}')">
            x
            </button>
        </c:forEach>
        <%--<c:forEach items="${handler.roleBean.externalPermissions}" var="permissionGroup">--%>
        <%--<div style="float:left;">--%>
        <%--<form style="margin: 0;" action="${flowExecutionUrl}" method="POST">--%>
                <%--<input type="hidden" name="tab" value="${permissionGroup.key}"/>--%>
                <%--<button class="btn btn-block" type="submit" name="_eventId_switchTab" onclick="">--%>
                        <%--${permissionGroup.key}--%>
                <%--</button>--%>
            <%--</form>--%>
        <%--</div>--%>
    <%--</c:forEach>--%>

            <input type="text" id="addContextField" name="newContext"/>
            <button class="btn btn-primary" type="submit" name="_eventId_addContext" id="addContextButton">
                <i class="icon-plus  icon-white"></i>
                &nbsp;Add
            </button>

    </div>

    <p>

    </p>

    <div class="btn-group">
        <div class="btn-group">
            <c:forEach items="${handler.roleBean.permissions[handler.currentContext]}" var="permissionGroup">
                <button class="btn ${handler.currentGroup eq permissionGroup.key ? 'btn-success':''}" type="submit" name="_eventId_switchGroup" onclick="$('#tabField').val('${permissionGroup.key}')">
                    ${permissionGroup.key}
                    <%--<c:if test="${fn:startsWith(permissionGroup.key, 'context./')}">--%>
                        <%--<fmt:message key="rolesmanager.rolesAndPermissions.context"/> &nbsp; ${fn:substringAfter(permissionGroup.key, 'context.')}--%>
                    <%--</c:if>--%>
                    <%--<c:if test="${not fn:startsWith(permissionGroup.key, 'context./')}">--%>
                        <%--<fmt:message key="rolesmanager.rolesAndPermissions.${permissionGroup.key}"/>--%>
                    <%--</c:if>--%>
                </button>
            </c:forEach>
        </div>
    </div>


    <div>
        <div class="box-1">
            <h3>Permissions :</h3>
            <c:forEach items="${handler.roleBean.permissions[handler.currentContext][handler.currentGroup]}" var="entry">
                <c:set value="${entry.value}" var="permission"/>
                <c:if test="${permission.set and not handler.roleBean.permissions[handler.currentContext][handler.currentGroup][permission.parentPath].set}">
                    <a href="#${permission.path}" > ${permission.name} </a>
                </c:if>
            </c:forEach>
        </div>
    </div>
    <fieldset>
        <h3>${permissionGroup.key}</h3>

        <c:if test="${not empty handler.roleBean.permissions[handler.currentContext][handler.currentGroup]}">
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th  width="3%">&nbsp;</th>
                <th width="82%">
                    <fmt:message key="label.name"/>
                </th>
                <th width="15%">
                    Scope
                </th>
            </tr>
            </thead>

            <tbody>

            <c:forEach items="${handler.roleBean.permissions[handler.currentContext][handler.currentGroup]}" var="entry">
                <c:set value="${entry.value}" var="permission"/>
                <tr>
                    <td ali>
                        <a name="${permission.path}"/>
                        <div class="triState" style="height:13px; overflow: hidden">

                            <c:choose>
                                <c:when test="${permission.set}"><a class="checkbox checked" path="${permission.path}" parent="${permission.parentPath}" href=""></a></c:when>
                                <c:when test="${permission.partialSet}"><a class="checkbox partial" path="${permission.path}" parent="${permission.parentPath}" href=""></a></c:when>
                                <c:otherwise><a class="checkbox" path="${permission.path}" parent="${permission.parentPath}" href=""></a></c:otherwise>
                            </c:choose>

                        </div>
                    </td>
                    <td>
                        <c:forEach var="i" begin="3" end="${permission.depth}" step="1" varStatus="status5">
                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        </c:forEach>
                            ${permission.depth == 2 ? '<h3>' : '' }${permission.name} ${permission.depth == 2 ? '</h3>' : '' }
                    </td>
                    <td>
                            ${permission.scope}
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        </c:if>

        <%--<c:if test="${not empty handler.roleBean.externalPermissions[handler.currentTab]}">--%>
            <%--<c:forEach items="${handler.roleBean.externalPermissions[handler.currentTab]}" var="permission">--%>
                <%--<c:set var="paths" value="${permission.targetPaths}"/>--%>
            <%--</c:forEach>--%>

            <%--<c:if test="${fn:length(paths) == 1}">--%>
                <%--<c:forEach items="${paths}" var="tmppath">--%>
                    <%--<c:set var="path" value="${tmppath}"/>--%>
                <%--</c:forEach>--%>

                <%--<table class="table table-bordered table-striped table-hover">--%>
                    <%--<thead>--%>
                    <%--<tr>--%>
                        <%--<th>&nbsp;</th>--%>
                        <%--<th>--%>
                            <%--<fmt:message key="label.name"/>--%>
                        <%--</th>--%>
                        <%--<th>--%>
                            <%--Scope--%>
                        <%--</th>--%>
                    <%--</tr>--%>
                    <%--</thead>--%>

                    <%--<tbody>--%>

                    <%--<c:forEach items="${handler.roleBean.externalPermissions[handler.currentTab]}" var="permission">--%>
                        <%--<tr>--%>
                            <%--<td><input name="selectedSites" type="checkbox"--%>
                                       <%--value="${permission.name}-${path}" ${permission.setForPath[path] ? 'checked="checked"':''} />--%>
                            <%--</td>--%>
                            <%--<td>--%>
                                <%--<c:forEach var="i" begin="4" end="${permission.depth}" step="1" varStatus="status5">--%>
                                    <%--&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;--%>
                                <%--</c:forEach>--%>
                                    <%--${permission.name}--%>
                            <%--</td>--%>
                            <%--<td>--%>
                                    <%--${permission.scope}--%>
                            <%--</td>--%>
                        <%--</tr>--%>
                    <%--</c:forEach>--%>
                    <%--</tbody>--%>
                <%--</table>--%>
            <%--</c:if>--%>
            <%--<c:if test="${fn:length(paths) > 1}">--%>
                <%--<table class="table table-bordered table-striped table-hover">--%>
                    <%--<thead>--%>
                    <%--<tr>--%>
                        <%--<c:forEach items="${handler.roleBean.externalPermissions[handler.currentTab]}" var="permission">--%>
                            <%--<th>--%>
                                <%--${permission.name}--%>
                            <%--</th>--%>
                        <%--</c:forEach>--%>
                        <%--<th>--%>
                            <%--Scope--%>
                        <%--</th>--%>
                    <%--</tr>--%>
                    <%--</thead>--%>

                    <%--<tbody>--%>

                    <%--<c:forEach items="${paths}" var="path">--%>
                        <%--<tr>--%>
                            <%--<c:forEach items="${handler.roleBean.externalPermissions[handler.currentTab]}" var="permission">--%>
                                <%--<td><input name="selectedSites" type="checkbox"--%>
                                           <%--value="${permission.name}-${path}" ${permission.setForPath[path] ? 'checked="checked"':''} />--%>
                                <%--</td>--%>
                            <%--</c:forEach>--%>
                            <%--<td>--%>
                                <%--${path}--%>
                            <%--</td>--%>
                        <%--</tr>--%>
                    <%--</c:forEach>--%>
                    <%--</tbody>--%>
                <%--</table>--%>
            <%--</c:if>--%>
        <%--</c:if>--%>

    </fieldset>

</form>
