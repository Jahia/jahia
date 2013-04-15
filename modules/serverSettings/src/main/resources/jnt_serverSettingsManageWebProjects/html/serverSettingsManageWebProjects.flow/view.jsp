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
    function submitSiteForm(act, site) {
    	if (typeof site != 'undefined') {
    		$("<input type='hidden' name='sites' />").attr("value", site).appendTo('#sitesForm');
    	} else {
    		$("#sitesForm input:checkbox[name='selectedSites']:checked").each(function() {
    			$("<input type='hidden' name='sites' />").attr("value", $(this).val()).appendTo('#sitesForm');
    		});
    	}
    	$('#sitesFormAction').val(act);
    	$('#sitesForm').submit();
    }
    $(document).ready(function () {
    	$("a.sitesAction").click(function () {
    		var act=$(this).attr('id');
    		if (act != 'createSite' && $("#sitesForm input:checkbox[name='selectedSites']:checked").length == 0) {
        		<fmt:message key="serverSettings.manageWebProjects.noWebProjectSelected" var="i18nNoSiteSelected"/>
        		alert("${functions:escapeJavaScript(i18nNoSiteSelected)}");
    			return false;
    		}
    		submitSiteForm(act);
    		return false;
    	});
        $("#exportSites").click(function (){
            var selectedSites = [];
            var checkedSites = $("input[name='selectedSites']:checked");
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
            window.open("${url.context}/cms/export/default/"+name+ '_export_${now}.zip?exportformat=site&live=true'+sitebox);
        });

        $("#exportStagingSites").click(function (){
            var selectedSites = [];
            var checkedSites = $("input[name='selectedSites']:checked");
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
            window.open("${url.context}/cms/export/default/"+name+ '_staging_export_${now}.zip?exportformat=site&live=false'+sitebox);
        });

    })
</script>
<form id="sitesForm" action="${flowExecutionUrl}" method="post">
    <fieldset>
        <h2><fmt:message key="label.virtualSitesManagement"/></h2>
            <input type="hidden" id="sitesFormAction" name="_eventId" value="" />

        <div class="btn-group">
            <a href="#create" id="createSite" class="btn sitesAction"><fmt:message key="serverSettings.manageWebProjects.add"/></a>
            <a href="#export" id="exportSites" class="btn sitesAction-hide"><fmt:message key="label.export"/></a>
            <a href="#exportStaging" id="exportStagingSites" class="btn sitesAction-hide"><fmt:message key="label.export"/> (<fmt:message key="label.stagingContent"/>)</a>
            <a href="#delete" id="deleteSites" class="btn sitesAction"><fmt:message key="label.delete"/></a>
        </div>
    </fieldset>

    <fieldset>
        <h2><fmt:message key="serverSettings.manageWebProjects.virtualSitesListe"/></h2>
        
        <c:forEach var="msg" items="${flowRequestContext.messageContext.allMessages}">
            <div class="${msg.severity == 'ERROR' ? 'validationError' : ''}" style="color: ${msg.severity == 'ERROR' ? 'red' : 'blue'};">${fn:escapeXml(msg.text)}</div>
        </c:forEach>
    
        <table class="table table-bordered table-striped table-hover">
            <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th>#</th>
                    <th>
                        <fmt:message key="label.name"/>
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
            </thead>

            <tbody>
                <jcr:sql var="siteQuery" sql="select * from [jnt:virtualsite] where isdescendantnode('/sites') and localname()<>'systemsite' order by [j:title]"/>
                <c:forEach items="${siteQuery.nodes}" var="site" varStatus="loopStatus">
                    <c:if test="${site.name ne 'systemsite'}">
                        <tr>
                            <td><input name="selectedSites" type="checkbox" value="${site.name}"/></td>
                            <td>
                                ${loopStatus.index + 1}
                                <c:if test="${site.identifier == defaultSite.string}">
                                    &nbsp;<img src="<c:url value='/css/images/andromeda/icons/accept.png'/>"
                                         title="<fmt:message key='serverSettings.manageWebProjects.webProject.isDefault'/>"
                                         width="10" height="10" border="0" alt="+"/>
                                </c:if>
                            </td>
                            <td><a href="#edit" onclick="submitSiteForm('editSite', '${site.name}'); return false;">${fn:escapeXml(site.title)}</a></td>
                            <td>${fn:escapeXml(site.name)}</td>
                            <td>${fn:escapeXml(site.serverName)}</td>
                            <td title="${fn:escapeXml(site.templatePackageName)}">${fn:escapeXml(site.templateFolder)}</td>
                            <td>
                                <c:set var="i18nExportStaging"><fmt:message key="label.export"/> (<fmt:message key="label.stagingContent"/>)</c:set>
                                <c:set var="i18nExportStaging" value="${fn:escapeXml(i18nExportStaging)}"/>
                                <c:url var="editUrl" value="/cms/edit/default/${site.defaultLanguage}${site.home.path}.html"/>
                                <a href="${editUrl}"><img
                                        src="<c:url value='/css/images/andromeda/icons/arrow_right_green.png'/>"
                                        alt="<fmt:message key='serverSettings.manageWebProjects.exitToEdit'/>" title="<fmt:message key='serverSettings.manageWebProjects.exitToEdit'/>" width="16" height="16" border="0"/></a>
                                <a href="#edit" onclick="submitSiteForm('editSite', '${site.name}'); return false;"><img
                                        src="<c:url value='/engines/images/icons/admin/adromeda/edit.png'/>"
                                        alt="<fmt:message key='label.edit'/>" title="<fmt:message key='label.edit'/>" width="16" height="16" border="0"/></a>
                                <%--
                                <a href="#edit" onclick="submitSiteForm('exportSites', '${site.name}'); return false;"><img
                                        src="<c:url value='/css/images/andromeda/icons/export1.png'/>"
                                        alt="<fmt:message key='label.export'/>" title="<fmt:message key='label.export'/>" width="16" height="16" border="0"/></a>
                                <a href="#edit" onclick="submitSiteForm('exportStagingSites', '${site.name}'); return false;"><img
                                        src="<c:url value='/css/images/andromeda/icons/export2.png'/>"
                                        alt="${i18nExportStaging}" title="${i18nExportStaging}" width="16" height="16" border="0"/></a>
                                 --%>
                                <a href="#delete" onclick="submitSiteForm('deleteSites', '${site.name}'); return false;"><img
                                        src="<c:url value='/engines/images/icons/admin/adromeda/delete.png'/>"
                                        alt="<fmt:message key='label.delete'/>" title="<fmt:message key='label.delete'/>" width="16" height="16" border="0"/></a>
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
            </tbody>
        </table>
    </fieldset>

    <fieldset>
        <h2>System Site</h2>
        <div class="btn-group">
            <a class="btn" href="<c:url value='/cms/export/default/systemsite_export_${now}.zip?exportformat=site&live=true&sitebox=systemsite' />"><fmt:message key='label.export' /></a>
            <a class="btn" href="<c:url value='/cms/export/default/systemsite_staging_export_${now}.zip?exportformat=site&live=false&sitebox=systemsite' />"><fmt:message key="label.export"/> (<fmt:message key="label.stagingContent"/>)</a>
        </div>
    </fieldset>
    
    <fieldset>
        <h2><fmt:message key="serverSettings.manageWebProjects.importprepackaged"/></h2>
            <select name="selectedPrepackagedSite">
                <c:forEach items="${webprojectHandler.prepackagedSites}" var="file">
                    <fmt:message key="serverSettings.manageWebProjects.importprepackaged.${file.name}" var="label"/>
                    <c:set var="label" value="${fn:contains(label, '???') ? file.name : label}"/>
                    <option value="${file.absolutePath}"${file.name == defaultPrepackagedSite ? ' selected="selected"':''}>${fn:escapeXml(label)}</option>
                </c:forEach>
            </select>
    
            <input class="btn" type="submit" name="importPrepackaged"
                   value="<fmt:message key='serverSettings.manageWebProjects.importprepackaged.proceed' />" onclick="submitSiteForm('importPrepackaged'); return false;"/>
    </fieldset>
    
</form>
    <fieldset>
        <h2><fmt:message key="serverSettings.manageWebProjects.multipleimport"/></h2>
        <form action="${flowExecutionUrl}" method="post" enctype="multipart/form-data">
            <div>
                <p><strong><fmt:message key="serverSettings.manageWebProjects.multipleimport.fileselect"/></strong></p>
                <input type="file" name="importFile"/>
            </div>
            <div>
                <p><strong><fmt:message key="serverSettings.manageWebProjects.multipleimport.fileinput"/></strong></p>
                <input type="text"  name="importPath"/>
            </div>
    
            <input class="btn btn-primary" type="submit" name="_eventId_import"
                   value="<fmt:message key='serverSettings.manageWebProjects.fileImport'/>" onclick=""/>
        </form>
    </fieldset>
