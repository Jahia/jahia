<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="webprojectHandler" type="org.jahia.modules.serversettings.flow.WebprojectHandler"--%>
<jcr:node var="sites" path="/sites"/>
<jcr:nodeProperty name="j:defaultSite" node="${sites}" var="defaultSite"/>
<c:set var="defaultPrepackagedSite" value="acmespace.zip"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<jsp:useBean id="nowDate" class="java.util.Date" />
<fmt:formatDate value="${nowDate}" pattern="yyyy-MM-dd-HH-mm" var="now"/>
<script type="text/javascript">
    function submitSiteForm(act) {
    	$('#sitesFormAction').val(act);
    	$('#sitesForm').submit();
    }
    $(document).ready(function () {
    	$("a.sitesAction").click(function () {
    		var act=$(this).attr('id');
    		if (act != 'createSite' && $("#sitesForm input:checkbox[name='sites']:checked").length == 0) {
        		<fmt:message key="serverSettings.manageWebProjects.noWebProjectSelected" var="i18nNoSiteSelected"/>
        		alert("${functions:escapeJavaScript(i18nNoSiteSelected)}");
    			return false;
    		}
    		submitSiteForm(act);
    		return false;
    	});
        $("#exportSites").click(function (){
            var selectedSites = [];
            var checkedSites = $("input[name='sites']:checked");
            checkedSites.each(function(){
                selectedSites.push($(this).val());
            });
            if(selectedSites.length==0) {
                alert('you should select at least one site');
                return false;
            }
            var name = selectedSites.length>1?"sites":selectedSites;
            var sitebox = "";
            for (i = 0; i < selectedSites.length; i++) {
                sitebox = sitebox + "&sitebox=" + selectedSites[i];
            }
            $(this).target = "_blank";
            window.open("/cms/export/default/"+name+ '_export_${now}.zip?exportformat=site&live=true'+sitebox);
        });

        $("#exportStagingSites").click(function (){
            var selectedSites = [];
            var checkedSites = $("input[name='sites']:checked");
            checkedSites.each(function(){
                selectedSites.push($(this).val());
            });
            if(selectedSites.length==0) {
                alert('you should select at least one site');
                return false;
            }
            var name = selectedSites.length>1?"sites":selectedSites;
            var sitebox = "";
            for (i = 0; i < selectedSites.length; i++) {
                sitebox = sitebox + "&sitebox=" + selectedSites[i];
            }
            $(this).target = "_blank";
            window.open("/cms/export/default/"+name+ '_staging_export_${now}.zip?exportformat=site&live=false'+sitebox);
        });

    })
</script>
<form id="sitesForm" action="${flowExecutionUrl}" method="post">
    <fieldset>
        <legend><fmt:message key="label.virtualSitesManagement"/></legend>
            <input type="hidden" id="sitesFormAction" name="_eventId" value="" />
            <a href="#create" id="createSite" class="sitesAction"><fmt:message key="serverSettings.manageWebProjects.add"/></a>
            <a href="#export" id="exportSites" class="sitesAction-hide"><fmt:message key="serverSettings.manageWebProjects.export"/></a>
            <a href="#exportStaging" id="exportStagingSites" class="sitesAction-hide"><fmt:message key="serverSettings.manageWebProjects.exportStaging"/></a>
            <a href="#delete" id="deleteSites" class="sitesAction"><fmt:message key="serverSettings.manageWebProjects.delete"/></a>
    </fieldset>

    <fieldset>
        <legend><fmt:message key="org.jahia.admin.site.ManageSites.virtualSitesListe.label"/></legend>
        <table border="1" cellpadding="5" cellspacing="0">

            <tr>
                <th>&nbsp;</th>
                <th>#</th>
                <th>
                    <fmt:message key="serverSettings.manageWebProjects.webProject.title"/>
                </th>
                <th>
                    <fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/>
                </th>
                <th>
                    <fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/>
                </th>
                <th>
                    <fmt:message key="serverSettings.manageWebProjects.webProject.templateSet"/>
                </th>
                <th>
                    <fmt:message key="label.action"/>
                </th>
            </tr>

            <input name="_sites" type="hidden"/>
             <jcr:sql var="siteQuery" sql="select * from [jnt:virtualsite] where isdescendantnode('/sites') and localname()<>'systemsite' order by [j:title]"/>
            <c:forEach items="${siteQuery.nodes}" var="site" varStatus="loopStatus">
                <c:if test="${site.name ne 'systemsite'}">
                    <tr>
                        <td><input name="sites" type="checkbox" value="${site.name}"/></td>
                        <td>
                            ${loopStatus.index + 1}
                            <c:if test="${site.identifier == defaultSite.string}">
                                &nbsp;<img src="<c:url value='/css/images/andromeda/icons/accept.png'/>"
                                     title="<fmt:message key='serverSettings.manageWebProjects.webProject.isDefault'/>"
                                     width="10" height="10" border="0" alt="+"/>
                            </c:if>
                        </td>
                        <td>${fn:escapeXml(site.title)}</td>
                        <td>${fn:escapeXml(site.name)}</td>
                        <td>${fn:escapeXml(site.serverName)}</td>
                        <td title="${fn:escapeXml(site.templatePackageName)}">${fn:escapeXml(site.templateFolder)}</td>
                        <td>
                        &nbsp;
                        </td>
                    </tr>
                </c:if>
            </c:forEach>

        </table>
    </fieldset>

    <fieldset>
        <legend>System Site</legend>

        <a href="/cms/export/default/systemsite_export_${now}.zip?exportformat=site&live=true&sitebox=systemsite"><fmt:message key='serverSettings.manageWebProjects.export' /></a>
        <a href="/cms/export/default/systemsite_staging_export_${now}.zip?exportformat=site&live=false&sitebox=systemsite"><fmt:message key='serverSettings.manageWebProjects.exportStaging' /></a>

    </fieldset>
    
    <fieldset>
        <legend><fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.label"/></legend>
            <select name="selectedPrepackagedSite">
                <c:forEach items="${webprojectHandler.prepackagedSites}" var="file">
                    <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.${file.name}" var="label"/>
                    <c:set var="label" value="${fn:contains(label, '???') ? file.name : label}"/>
                    <option value="${file.absolutePath}"${file.name == defaultPrepackagedSite ? ' selected="selected"':''}>${fn:escapeXml(label)}</option>
                </c:forEach>
            </select>
    
            <input type="submit" name="importPrepackaged"
                   value="<fmt:message key='org.jahia.admin.site.ManageSites.importprepackaged.proceed' />" onclick="submitSiteForm('importPrepackaged'); return false;"/>
    </fieldset>
    
</form>
    <fieldset>
        <legend><fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.label"/></legend>
        <form action="${flowExecutionUrl}" method="post" enctype="multipart/form-data">
            <div>
                <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.fileselect"/>
                <input type="file" name="importFile"/>
            </div>
            <div>
                <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.fileinput"/> <input name="importPath"/>
            </div>
    
            <input type="submit" name="_eventId_import"
                   value="<fmt:message key='org.jahia.admin.site.ManageSites.fileImport.label'/>" onclick=""/>
        </form>
    </fieldset>
