<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>

<template:addResources type="javascript" resources="jquery.min.js,admin-bootstrap.js,jquery.blockUI.js,bootstrap-filestyle.min.js,jquery.metadata.js,workInProgress.js"/>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>
<template:addResources type="javascript" resources="moment-with-langs.min.js"/>
<template:addResources type="css" resources="admin-bootstrap.css,datatables/css/bootstrap-theme.css,tablecloth.css"/>
<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>

<template:addResources>
    <script type="text/javascript">
        $(document).ready(function () {
            $(":file").filestyle({classButton: "btn",classIcon: "icon-folder-open"/*,buttonText:"Translation"*/});
        });
    </script>
    <script type="text/javascript">
        jQuery.extend( jQuery.fn.dataTableExt.oSort, {
            "date-pages-pre": function ( a ) {
                var ukDatea = $(a).text().split('by');
                var momentread = moment(ukDatea[0].trim(), "DD, MMMM YYYY HH:mm");
                return  momentread;
            },

            "date-pages-asc": function ( a, b ) {
                return (a.diff(b) < 0) ? -1 : ((a.diff(b)>0)?1:0);
            },

            "date-pages-desc": function ( a, b ) {
                return (a.diff(b) < 0) ? 1 : ((a.diff(b)>0)?-1:0);
            }
        } );

        $(document).ready(function () {
            $('#userContent_table').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "iDisplayLength":25,
                "sPaginationType": "bootstrap",
                "bFilter":true,
                "aaSorting": [],
                "aoColumns": [
                    null,
                    null,
                    {"sType":"date-pages" },
                    {"sType":"date-pages" },
                    {"sType":"date-pages" }
                ]
            });
        });
    </script>
</template:addResources>

<template:include view="hidden.header"/>

<c:if test="${not empty moduleMap.currentList}">
    <fieldset class="well">
        <table cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered" id="userContent_table">
            <thead>
            <tr>
                <th>
                    <img src="/icons/siteManager.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key='label.site'/>
                </th>
                <th>
                    <img src="/icons/copy.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key='jnt_imageReferenceLink.j_linknode'/>
                </th>
                <th>
                    <img src="/icons/editMode.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key='mix_created'/>
                </th>
                <th>
                    <img src="/icons/contribute.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key='jmix_contentmetadata.j_lastModificationDate'/>
                </th>
                <th>
                    <img src="/icons/publicationAction.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key='jmix_contentmetadata.j_lastPublishingDate'/>
                </th>
            </tr>
            </thead>
            <tbody>
                <%@include file="userContentTableRow.jspf" %>
            </tbody>
        </table>
    </fieldset>
</c:if>

<c:if test="${functions:length(moduleMap.currentList) == 0 and not empty moduleMap.emptyListMessage}">
    ${moduleMap.emptyListMessage}
</c:if>

<c:if test="${moduleMap.editable and renderContext.editMode && !resourceReadOnly}">
    <template:module path="*"/>
</c:if>
<template:include view="hidden.footer"/>