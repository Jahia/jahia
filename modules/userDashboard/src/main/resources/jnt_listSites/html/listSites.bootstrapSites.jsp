<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.blockUI.js,workInProgress.js,admin-bootstrap.js"/>
<template:addResources type="javascript" resources="managesitesbootstrap.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>

<template:addResources type="css" resources="datatables/css/bootstrap-theme.css,tablecloth.css"/>

<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>

<template:include view="hidden.header"/>

<c:set var="currentLocale">${currentResource.locale}</c:set>

<template:addResources>
    <script type="text/javascript">
        $(document).ready(function () {
            $('#userSites_table').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "iDisplayLength":10,
                "sPaginationType": "bootstrap",
                "aaSorting": [] //this option disable sort by default, the user steal can use column names to sort the table
            });

            $(".checkAll").click(function () {
                $(".sitecheckbox").each(function (index) {
                    if ($(".checkAll").attr("checked") == "checked") {
                        $(this).attr("checked","checked");
                    } else {
                        $(this).removeAttr("checked");
                    }
                });
            });
        });
    </script>
</template:addResources>

<fieldset>
    <jcr:node var="root" path="/"/>
    <table cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered" id="userSites_table">
        <thead>
            <tr>
                <th>
                    <img src="<c:url value='/icons/siteManager.png'/>" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key='label.site'/>
                </th>
                <c:if test="${currentNode.properties.administrationlink.boolean}">
                    <th>
                        <img src="<c:url value='/icons/admin.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative;"/>
                        <fmt:message key="label.administration"/>
                    </th>
                </c:if>
                <c:if test="${currentNode.properties.edit.boolean}">
                    <th>
                        <img src="<c:url value='/icons/editMode.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative;"/>
                        <fmt:message key="label.edit"/>
                    </th>
                </c:if>
                <c:if test="${currentNode.properties.contribute.boolean}">
                    <th>
                        <img src="<c:url value='/icons/contribute.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative;"/>
                        <fmt:message key="label.contribute"/>
                    </th>
                </c:if>
                <c:if test="${currentNode.properties.preview.boolean}">
                    <th>
                        <img src="<c:url value='/icons/preview.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative;"/>
                        <fmt:message key="label.preview"/>
                    </th>
                </c:if>
                <c:if test="${currentNode.properties.live.boolean}">
                    <th>
                        <img src="<c:url value='/icons/live.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative;"/>
                        <fmt:message key="label.live.version"/>
                    </th>
                </c:if>
                <th>
                    <img src="<c:url value='/icons/files-manager-1616.png'/>" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key="myWebProjects.files"/>
                </th>
                <th>
                    <img src="<c:url value='/icons/content-manager-1616.png'/>" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key="label.content"/>
                </th>
                <c:if test="${currentNode.properties.editproperties.boolean}">
                    <th>
                        <img src="<c:url value='/icons/admin.png'/>" width="16" height="16" alt=" "
                             role="presentation" style="position:relative;"/>
                        <fmt:message key="label.manageSite.changeProperties"/>
                    </th>
                </c:if>
            </tr>
        </thead>
        <tbody>
            <%@include file="sitesTableRow.jspf" %>
        </tbody>
    </table>
    <c:if test="${moduleMap.end > 0 and moduleMap.end > moduleMap.begin}">
        <c:if test="${currentNode.properties.export.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
            <c:url var="stagingExportUrl" value="/cms/export/default/sites_staging_export_${now}.zip"/>
            <fmt:message key="label.manageSite.exportStaging" var="exportStagingTitle"/>
            <fmt:message key="label.manageSite.exportLive" var="exportLiveTitle"/>
            <button class="btn btn-primary exportStagingButton" id="exportStagingButton" onclick="exportSite('${stagingExportUrl}',false, '${exportStagingTitle}')">
                ${exportStagingTitle}
            </button>
            <c:url var="exportUrl" value="/cms/export/default/sites_export_${now}.zip"/>
            <button class="btn btn-primary exportLiveButton" id="exportLiveButton" onclick="exportSite('${exportUrl}',true, '${exportLiveTitle}')">
                ${exportLiveTitle}
            </button>
        </c:if>

        <c:if test="${currentNode.properties.delete.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
            <button class="btn btn-danger deleteSiteButton" id="deleteSiteButton" onclick="deleteSiteBootstrap()">
                <fmt:message key="label.manageSite.deleteSite"/>
            </button>
        </c:if>
    </c:if>
</fieldset>

<c:if test="${currentNode.properties.delete.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
    <script>
        $(document).ready(function(){
            $('#confirmDeleteSite').on('click', function (){
                $('#dialog-delete-confirm').modal('hide');

                workInProgress('${i18nWaiting}');

                $('#deleteSiteForm').ajaxSubmit(function() {
                    window.location.reload();
                });
            });
        });
    </script>

    <div id="dialog-delete-confirm" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="modalDeleteSite" aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h3 id="modalDeleteSite"><fmt:message key="label.manageSite.deleteSite"/></h3>
        </div>
        <div class="modal-body">
            <p>
                <fmt:message key="label.delete.confirm" />
                <ol id="dialog-delete-confirm-body"></ol>
            </p>
        </div>
        <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">
                <fmt:message key="cancel" />
            </button>
            <button class="btn btn-danger" id="confirmDeleteSite" type="submit">
                <fmt:message key="label.manageSite.deleteSite"/>
            </button>
        </div>
    </div>

    <form  class="deleteSiteForm ajaxForm" id="deleteSiteForm" action="<c:url value='${url.basePreview}/sites.adminDeleteSite.do'/>">
    </form>

    <div id="nothing-selected" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="modal-nothing-selected" aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h3 id="modal-nothing-selected"><fmt:message key="label.manageSite.deleteSite"/></h3>
        </div>
        <div class="modal-body">
            <p>
                <fmt:message key="label.manageSites.noSiteSelected"/>
            </p>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">Ok</button>
        </div>
    </div>
</c:if>

<c:if test="${currentNode.properties.export.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
    <form class="exportForm ajaxForm"  name="export" id="exportForm" method="POST">
        <input type="hidden" name="exportformat" value="site"/>
        <input type="hidden" name="live" value="true"/>
    </form>
</c:if>

<c:if test="${jcr:hasPermission(root, 'adminVirtualSites')}">
    <a href="<c:url value='/cms/admin/default/en/settings.webProjectSettings.html'/>" class="btn btn-primary pull-right">
        <fmt:message key="myWebProjects.goToCreateNewSite"/>&nbsp;<i class="icon-arrow-right icon-white"></i>
    </a>
</c:if>

<template:include view="hidden.footer"/>
