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

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="javascript" resources="managesites.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>

<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>

<template:addResources type="css" resources="admin-bootstrap.css,datatables/css/bootstrap-theme.css,tablecloth.css"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<template:addResources type="css" resources="listsites.css"/>

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

            $("a.changePropertiesButton").fancybox();
            $("a.detailsButton").fancybox({
                margin : 50,
                scrolling : 'auto',
                width : 600,
                height : 400,
                autoDimensions : false,
                type : 'ajax'
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

<fieldset class="well">
    <table cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered" id="userSites_table">
        <thead>
            <tr>
                <th><img src="/icons/siteManager.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key='label.site'/></th>
                <th><img src="/icons/editMode.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key="label.editMode"/></th>
                <th><img src="/icons/contribute.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key="label.contribute"/></th>
                <th><img src="/icons/preview.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key="label.preview"/></th>
                <th><img src="/icons/live.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key="label.live.version"/></th>
                <th><img src="/icons/files-manager-1616.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key="label.filemanager"/></th>
                <th><img src="/icons/content-manager-1616.png" width="16" height="16" alt=" "
                         role="presentation" style="position:relative;"/>
                    <fmt:message key="label.contentmanager"/></th>
            </tr>
        </thead>
        <tbody>
            <%@include file="sitesTableRow.jspf" %>
        </tbody>
    </table>
</fieldset>

<div style="display:none">
    <div id="dialog-delete-confirm" title=" ">
        <p><span class="ui-icon ui-icon-alert"
                 style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="label.delete.confirm" /></p>
    </div>
    <div id="nothing-selected" >
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="label.manageSites.noSiteSelected"/></p>
    </div>
</div>
<div style="display:none; position:fixed; left:0; top:0; width:100%; height:100%; z-index:9999" class="loading">
    <h1><fmt:message key="label.workInProgressTitle"/></h1>
</div>

<c:if test="${currentNode.properties.delete.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
    <form class="deleteSiteForm ajaxForm" id="deleteSiteForm" action="<c:url value='${url.base}/sites.adminDeleteSite.do'/>" >
    </form>
</c:if>
<c:if test="${currentNode.properties.export.boolean && jcr:hasPermission(root,'adminVirtualSites')}">
    <form class="exportForm ajaxForm"  name="export" id="exportForm" method="POST">
        <input type="hidden" name="exportformat" value="site"/>
        <input type="hidden" name="live" value="true"/>
    </form>
</c:if>

<template:include view="hidden.footer"/>
