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
        <form class="pull-left" action="${flowExecutionUrl}" method="POST">
            <button class="btn" name="_eventId_rolesList"><i class=" icon-chevron-left"></i>&nbsp;Back</button>
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
    <%@include file="viewRoleHeader.jspf"%>


    <p>
    </p>
    <div>
        <div class="box-1">
            <c:forEach items="${handler.roleBean.permissions[handler.currentContext]}" var="gentry" varStatus="status">
                <c:forEach items="${gentry.value}" var="entry">
                    <c:set value="${entry.value}" var="permission"/>
                    <c:if test="${permission.set and not handler.roleBean.permissions[handler.currentContext][gentry.key][permission.parentPath].set}">
                        <c:if test="${gentry.key eq handler.currentGroup}">
                            <a href="#${permission.path}" > ${permission.title} </a>
                        </c:if>
                        <c:if test="${gentry.key ne handler.currentGroup}">
                            <a href="#" onclick="$('#form').attr('action',$('#form').attr('action') + '#${permission.path}');$('#switchToGroup${status.index}').click()"> ${permission.title} </a>
                        </c:if>
                    </c:if>
                </c:forEach>
            </c:forEach>
        </div>
    </div>

    <div class="btn-group">
        <div class="btn-group">
            <c:forEach items="${handler.roleBean.permissions[handler.currentContext]}" var="permissionGroup" varStatus="status">
                <button class="btn ${handler.currentGroup eq permissionGroup.key ? 'btn-success':''}" id="switchToGroup${status.index}" type="submit" name="_eventId_switchGroup" onclick="$('#tabField').val('${permissionGroup.key}')">
                    <c:set var="key" value="${fn:replace(permissionGroup.key,',','_')}"/>
                    <c:set var="key" value="${fn:replace(key,'-','_')}"/>
                    <fmt:message key="rolesmanager.rolesAndPermissions.group.${key}"/>
                </button>
            </c:forEach>
        </div>
    </div>


    <fieldset>
        <c:if test="${not empty handler.roleBean.permissions[handler.currentContext][handler.currentGroup]}">
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th  width="3%">&nbsp;</th>
                <th width="40%">
                    <fmt:message key="label.name"/>
                </th>
                <th width="42%">
                    <fmt:message key="label.description"/>
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
                    <td>
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
                            ${permission.depth == 2 ? '<h3>' : '' }${permission.title} ${permission.depth == 2 ? '</h3>' : '' }
                    </td>
                    <td>
                            ${permission.description}
                    </td>
                    <td>
                            ${permission.scope}
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        </c:if>
    </fieldset>

</form>
