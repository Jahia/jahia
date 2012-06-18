<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.bin.JahiaAdministration" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@page import="org.jahia.settings.SettingsBean" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.admin.sites.ManageSites" %>
<%@ page import="java.io.File" %>
<%@ page import="org.jahia.bin.Export" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="defaultSet" value="acmespace.zip"/>
<%
    Iterator sitesList = (Iterator) request.getAttribute("sitesList");
    String warningMsg = (String) request.getAttribute("warningMsg");
    String sub = (String) request.getParameter("sub");
    boolean hasTemplateSets = Boolean.valueOf(request.getAttribute("hasTemplateSets").toString());
    JahiaSite newJahiaSite = (JahiaSite) session.getAttribute(JahiaAdministration.CLASS_NAME + "newJahiaSite");
    ProcessingContext jParams = null;
    if (jData != null) {
        jParams = jData.getProcessingContext();
    }
    stretcherToOpen = 0;
    pageContext.setAttribute("exportPath", Export.getExportServletPath());%>
<jsp:useBean id="nowDate" class="java.util.Date" />
<fmt:message key="org.jahia.admin.site.ManageSites.exportsites.label" var="i18nExport"/>
<fmt:message key="org.jahia.admin.site.ManageSites.doYouWantToContinue.label" var="i18nContinue"/>
<c:set var="i18nConfirmExport" value="${functions:escapeJavaScript(i18nExport)}${fn:endsWith(i18nExport, '.') ? '' : '.'} ${functions:escapeJavaScript(i18nContinue)}"/>
<fmt:formatDate value="${nowDate}" pattern="yyyy-MM-dd-HH-mm" var="now"/>
<% if (sitesList != null && sitesList.hasNext()) { %>
<script type="text/javascript">
    function forSites(callback) {
        if (document.main.sitebox.length) {
            for (var i = 0; i < document.main.sitebox.length; i++) {
            	if (callback(document.main.sitebox[i])) {
            		return;
            	} 
            }
        } else {
        	callback(document.main.sitebox); 
        }
    }
    function selectSite(selectedSite, doSelect) {
    	doSelect = typeof doSelect != 'undefined' ? doSelect : true;
    	forSites(function(site) {
			site.checked =  ('<all>' == selectedSite || site.value == selectedSite) ? doSelect : !doSelect;
    	});
    	updateSelectedSites();
    }
    function checkAnySelected() {
    	var anySelected = false;
    	forSites(function(site) {
    		if (site.checked) {
    			anySelected = true;
        		return true;
    		}
    	});
        if (!anySelected) {
    		<fmt:message key="org.jahia.admin.site.ManageSites.noSiteSpecified.label" var="i18nNoSiteSelected"/>
    		alert("${functions:escapeJavaScript(i18nNoSiteSelected)}");
        }
		return anySelected;
    }
    function copyTemplateToVarFolder(selectedTemplate) {
    }

    function sendExportForm(stagingOnly) {
    	if (!checkAnySelected()) {
    		return;
    	}
    	document.getElementById('live').value = stagingOnly ? 'false' : 'true';
    	var name = null;
    	forSites(function(site) {
    		if (site.checked) {
        		if (name != null) {
        			name = 'sites';
        			return true;
        		}
        		name = site.value;
    		}
    	});
    	if (stagingOnly) {
    		name += '_staging';
    	}
        document.main.action = '<%=request.getContextPath() %>${exportPath}/default/' + name + '_export_${now}.zip';
        document.main.submit();
    }

    function sendDeleteForm() {
        document.main.action = '<%=request.getContextPath()+JahiaAdministration.getServletPath()%>';
        document.main.submit();
    }

    function sendForm(){
        document.jahiaAdmin.submit();
    }

    function updateSelectedSites() {
    	var allSelected = true;
    	if (document.main.sitebox.length) {
            for (var i = 0; i < document.main.sitebox.length; i++) {
            	if (!document.main.sitebox[i].checked) {
            		allSelected = false;
            		break;
            	}
            }
        } else {
        	allSelected = document.main.sitebox.checked;
        }
    	document.getElementById('allsitebox').checked = allSelected;
    }
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.site.ManageSites.virtualSitesListe.label"/></h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td style="vertical-align: top;" align="left">
        <%@include file="/admin/include/tab_menu.inc" %>
    </td>
</tr>
<tr>
<td style="vertical-align: top;" align="left" height="100%">
<div class="dex-TabPanelBottom">
<div class="tabContent">
<jsp:include page="/admin/include/left_menu.jsp">
    <jsp:param name="mode" value="server"/>
</jsp:include>
<div id="content" class="fit">
<div class="head headtop">
    <div class="object-title"><fmt:message key="label.virtualSitesManagement"/>
    </div>
</div>
<div class="content-body">
    <div id="operationMenu">
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-siteAdd"
                           href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=add")%>'><fmt:message key="org.jahia.admin.site.ManageSites.addSite.label"/></a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-export" href="#export" onclick="if (checkAnySelected() && confirm('${i18nConfirmExport}')) { sendExportForm(); } return false;"><fmt:message key="label.export"/></a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-export-staging" href="#exportStaging" onclick="if (checkAnySelected() && confirm('${i18nConfirmExport}')) { sendExportForm(true); } return false;"><fmt:message key="label.export"/> (<fmt:message key="label.stagingContent"/>)</a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-siteDelete" class="operationLink"
                           href="#delete" onclick="if (checkAnySelected()) { sendDeleteForm(); } return false;"><fmt:message key="label.delete"/></a>
                      </span>
                    </span>
    </div>
</div>
<div class="head headtop">
    <div class="object-title"><fmt:message key="org.jahia.admin.site.ManageSites.virtualSitesListe.label"/>&nbsp;(${sitesListSize})</div>
</div>
<div  class="content-item-noborder">
    <% if (warningMsg != "" && !sub.equals("prepareimport")) { %>
    <p class="errorbold">
        <%=warningMsg %>
    </p>
    <% } %>
    <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
        <thead>
        <tr>
            <th width="5%" style="text-align: center;">
                <input type="checkbox" name="allsitebox" id="allsitebox" value="true" onchange="selectSite('&lt;all&gt;', this.checked)"/>
            </th>
            <th width="3%">
                &nbsp;
            </th>
            <th width="28%">
                <fmt:message key="label.name"/>
            </th>
            <th width="7%">
                <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>
            </th>
            <th width="20%">
                <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>
            </th>
            <th width="23%">
                <fmt:message key="org.jahia.admin.site.ManageSites.templateSet.label"/>
            </th>
            <th width="14%" class="lastCol">
                <fmt:message key="label.action"/>
            </th>
        </tr>
        </thead>
        <form name="main" method="POST">
            <input type="hidden" name="do" value="sites"/><input type="hidden" name="sub" value="multipledelete"/><input
                type="hidden" name="exportformat" value="site"/> <input
                type="hidden" name="live" id="live" value="true"/>
            <tbody>
            <%
                JahiaSite site = null;
                String lineClass = "oddLine";
                int lineCounter = 0;
                while (sitesList.hasNext()) {
                    site = (JahiaSite) sitesList.next();
                    if (lineCounter % 2 == 0) {
                        lineClass = "evenLine";
                    }
                    lineCounter++; %>
            <tr class="<%=lineClass%>">
                <td align="center">
                    <input type="checkbox" name="sitebox" value="<%=site.getSiteKey()%>" onchange="updateSelectedSites();"/>
                </td>
                <td align="center">
                    <%
                        if (site.isDefault()) {
                    %>
                    <img
                            src="${pageContext.request.contextPath}/css/images/andromeda/icons/accept.png"
                            alt="+"
                            title="<fmt:message key='org.jahia.admin.site.ManageSites.isTheDefaultSite.label'/>" width="10"
                            height="10" border="0"/>
                    <%
                    } else {
                    %>&nbsp;<% } %>

                </td>
                <td>
                    <a href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=edit&siteid=" + site.getID())%>'
                       title="<fmt:message key='label.edit'/>"><%=site.getTitle() %>
                    </a>
                </td>
                <td>
                    <%=site.getSiteKey() %>
                </td>
                <td>
                    <%=site.getServerName() %>
                </td>
                <td>
                    <%=site.getTemplatePackageName() %>
                </td>
                <td class="lastCol">
                    <a href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=edit&siteid=" + site.getID())%>'
                       title="<fmt:message key='label.edit'/>"><img
                            src="<%=URL%>images/icons/admin/adromeda/edit.png"
                            alt="<fmt:message key='label.edit'/>"
                            title="<fmt:message key='label.edit'/>" width="16"
                            height="16" border="0" style="cursor: pointer;"/></a>&nbsp;<a href="#delete"
                                                                                          onclick="selectSite('<%=site.getSiteKey()%>'); sendDeleteForm(); return false;"
                                                                                          title="<fmt:message key='label.delete'/>"><img
                        src="<%=URL%>images/icons/admin/adromeda/delete.png"
                        alt="<fmt:message key='label.delete'/>"
                        title="<fmt:message key='label.delete'/>" width="16"
                        height="16" border="0" style="cursor: pointer;"/></a>&nbsp;
                    <fmt:message var="i18nExport" key="label.export"/>
                    <c:set var="i18nExport" value="${fn:escapeXml(i18nExport)}"/>
                    <a href="#export" onclick="selectSite('<%=site.getSiteKey()%>'); if (confirm('${i18nConfirmExport}')) { sendExportForm(); } return false;"
                       title="${i18nExport}"><img
                            src="<c:url value='/css/images/andromeda/icons/export1.png'/>"
                            alt="${i18nExport}"
                            title="${i18nExport}"
                            width="16" height="16" border="0" style="cursor: pointer;"/></a>
                    <c:set var="i18nExportStaging"><fmt:message key="label.export"/> (<fmt:message key="label.stagingContent"/>)</c:set>
                    <c:set var="i18nExportStaging" value="${fn:escapeXml(i18nExportStaging)}"/>
                    <a href="#export" onclick="selectSite('<%=site.getSiteKey()%>'); if (confirm('${i18nConfirmExport}')) { sendExportForm(true); } return false;"
                       title="${i18nExportStaging}"><img
                            src="<c:url value='/css/images/andromeda/icons/export2.png'/>"
                            alt="${i18nExportStaging}"
                            title="${i18nExportStaging}"
                            width="16" height="16" border="0" style="cursor: pointer;"/></a>
                </td>
            </tr>
            <%
                } %>
            </tbody>
        </form>

    </table>

    <div class="head headtop">
        <div class="object-title">${fn:escapeXml(systemSite.title)}</div>
    </div>
    <div  class="content-item">
        <p><fmt:message key="label.export"/>&nbsp;
        <fmt:message var="i18nExport" key="label.export"/>
        <c:set var="i18nExport" value="${fn:escapeXml(i18nExport)}"/>
        <c:url var="urlExportSystem" value="${exportPath}/default/systemsite_export_${now}.zip">
            <c:param name="exportformat" value="site"/>
            <c:param name="sitebox" value="systemsite"/>
        </c:url>
        <c:url var="urlExportSystemStaging" value="${exportPath}/default/systemsite_staging_export_${now}.zip">
            <c:param name="exportformat" value="site"/>
            <c:param name="sitebox" value="systemsite"/>
            <c:param name="live" value="false"/>
        </c:url>
        <a href="${urlExportSystem}" onclick="return confirm('${i18nConfirmExport}');" target="_blank"
           title="${i18nExport}"><img
                src="<c:url value='/css/images/andromeda/icons/export1.png'/>"
                alt="${i18nExport}"
                title="${i18nExport}"
                width="16" height="16" border="0" style="cursor: pointer;"/></a>
        <c:set var="i18nExportStaging"><fmt:message key="label.export"/> (<fmt:message key="label.stagingContent"/>)</c:set>
        <c:set var="i18nExportStaging" value="${fn:escapeXml(i18nExportStaging)}"/>
        <a href="${urlExportSystemStaging}" onclick="return confirm('${i18nConfirmExport}');" target="_blank"
           title="${i18nExportStaging}"><img
                src="<c:url value='/css/images/andromeda/icons/export2.png'/>"
                alt="${i18nExportStaging}"
                title="${i18nExportStaging}"
                width="16" height="16" border="0" style="cursor: pointer;"/></a>
        </p>
    </div>

    <!-- prepackaged site -->
    <% pageContext.setAttribute("files", new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "prepackagedSites").listFiles()); %>
    <c:if test="${not empty files}">
    <div class="head headtop">
        <div class="object-title">
            <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.label"/>
        </div>
    </div>
    <div  class="content-item">

        <form name="siteImportPrepackaged"
              action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=prepareimport")%>'
              method="post"
              enctype="multipart/form-data">
            <input type="hidden" name="validityCheckOnImport" value="true"/>
            <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                <tr>
                    <td>
                        <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.fileselect"/>&nbsp;
                    </td>
                    <td>
                        &nbsp;<select name="importpath">
                        <c:forEach var="file" items="${files}">
                            <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.${file.name}" var="label"/>
                            <c:set var="label" value="${fn:contains(label, '???') ? file.name : label}"/>
                            <option value='${file.path}'${file.name == defaultSet ? ' selected="selected"' : ''}>${fn:escapeXml(label)}</option>
                        </c:forEach>
                    </select>
                    </td>
                    <td>
                        <span class="dex-PushButton">
                            <span class="first-child">
                                <a class="ico-import"
                                    href="javascript:{showWorkInProgress();document.siteImportPrepackaged.submit();}"><fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.proceed"/></a>
                            </span>
                        </span>
                    </td>
                </tr>
            </table>
        </form>
    </div>
    </c:if>

    <!--   import backup -->
    <div class="head headtop">
        <div class="object-title">
            <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.label"/>
        </div>
    </div>
    <div  class="content-item">
        <% if (warningMsg != "" && sub.equals("prepareimport")) { %>
        <p class="errorbold">
            <%=warningMsg %>
        </p>
        <% } %>
        <form name="siteImport"
              action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=prepareimport")%>'
              method="post"
              enctype="multipart/form-data">
            <input type="hidden" name="validityCheckOnImport" value="true"/>
            <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                <tr>
                    <td>
                        <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.fileselect"/>&nbsp;
                    </td>
                    <td>
                        :&nbsp;<input type="file" name="import" onclick="setCheckedValue(document.forms['siteImport'].elements['siteImport'], 'siteImport'); setCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged'], '');setCheckedValue(document.forms['blank'].elements['blank'], '');">
                    </td>
                </tr><tr>
                <td>
                    <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.fileinput"/>&nbsp;
                </td>
                <td>
                    :&nbsp;<input name="importpath" size="<%=inputSize%>">
                </td>
                <td>
                        <span class="dex-PushButton">
                            <span class="first-child">
                                <a class="ico-import"
                                   href="javascript:{showWorkInProgress(); document.siteImport.submit();}"><fmt:message key="org.jahia.admin.site.ManageSites.fileImport.label"/></a>
                            </span>
                        </span>
                </td>

            </tr>
            </table>
        </form>
    </div>
</div>
<% }
else { %>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.site.ManageSites.virtualSitesListe.label"/></h2>
</div>
<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
        <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/admin/include/tab_menu.inc" %>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">


                <div class="dex-TabPanelBottom">
                    <div class="tabContent">
                        <jsp:include page="/admin/include/left_menu.jsp">
                            <jsp:param name="mode" value="server"/>
                        </jsp:include>
                        <div id="content" class="fit">
                            <div class="head headtop">
                                <div class="object-title"><fmt:message key="label.virtualSitesManagement"/>
                                </div>
                            </div>

                            <%if (hasTemplateSets) {%>
                            <!-- adding blank site -->
                            <div class="content-body">
                                <div id="operationMenu">
                                    <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                                        <tr>
                                            <td>
                                                <fmt:message key="org.jahia.admin.site.ManageSites.addSite.label"/>&nbsp;
                                            </td>

                                            <td>
                    <span class="dex-PushButton">
                                          <span class="first-child">

                                            <a class="ico-siteAdd"
                                               href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=add")%>'><fmt:message key="org.jahia.admin.site.ManageSites.addSite.label"/></a>
                                          </span>
                                        </span>
                                            </td>
                                        </tr>
                                    </table>
                                </div>

                                <div class="head headtop">
                                    <div class="object-title">${fn:escapeXml(systemSite.title)}</div>
                                </div>
                                <div  class="content-item">
                                    <p><fmt:message key="label.export"/>&nbsp;
                                    <fmt:message var="i18nExport" key="label.export"/>
                                    <c:set var="i18nExport" value="${fn:escapeXml(i18nExport)}"/>
                                    <c:url var="urlExportSystem" value="${exportPath}/default/systemsite_export_${now}.zip">
                                        <c:param name="exportformat" value="site"/>
                                        <c:param name="sitebox" value="systemsite"/>
                                    </c:url>
                                    <c:url var="urlExportSystemStaging" value="${exportPath}/default/systemsite_staging_export_${now}.zip">
                                        <c:param name="exportformat" value="site"/>
                                        <c:param name="sitebox" value="systemsite"/>
                                        <c:param name="live" value="false"/>
                                    </c:url>
                                    <a href="${urlExportSystem}" onclick="return confirm('${i18nConfirmExport}');" target="_blank"
                                       title="${i18nExport}"><img
                                            src="<c:url value='/css/images/andromeda/icons/export1.png'/>"
                                            alt="${i18nExport}"
                                            title="${i18nExport}"
                                            width="16" height="16" border="0" style="cursor: pointer;"/></a>
                                    <c:set var="i18nExportStaging"><fmt:message key="label.export"/> (<fmt:message key="label.stagingContent"/>)</c:set>
                                    <c:set var="i18nExportStaging" value="${fn:escapeXml(i18nExportStaging)}"/>
                                    <a href="${urlExportSystemStaging}" onclick="return confirm('${i18nConfirmExport}');" target="_blank"
                                       title="${i18nExportStaging}"><img
                                            src="<c:url value='/css/images/andromeda/icons/export2.png'/>"
                                            alt="${i18nExportStaging}"
                                            title="${i18nExportStaging}"
                                            width="16" height="16" border="0" style="cursor: pointer;"/></a>
                                    </p>
                                </div>

                                <% pageContext.setAttribute("files", new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/prepackagedSites").listFiles()); %>
                                <c:if test="${not empty files}">
                                <!-- prepackaged site -->
                                <div class="head headtop">
                                    <div class="object-title">
                                        <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.label"/>&nbsp;<fmt:message key="label.virtualSitesManagement.default"/>
                                    </div>
                                </div>
                                <div  class="content-item">
                                    <form name="siteImportPrepackaged"
                                          action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=prepareimport")%>'
                                          method="post"
                                          enctype="multipart/form-data">
                                        <input type="hidden" name="validityCheckOnImport" value="true"/>
                                        <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                                            <tr>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.fileselect"/>&nbsp;
                                                </td>
                                                <td>
                                                    &nbsp;<select name="importpath">
                                                    <c:forEach var="file" items="${files}">
                                                        <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.${file.name}" var="label"/>
                                                        <c:set var="label" value="${fn:contains(label, '???') ? file.name : label}"/>
                                                        <option value='${file.path}'${file.name == defaultSet ? ' selected="selected"' : ''}>${fn:escapeXml(label)}</option>
                                                    </c:forEach>
                                                </select>
                                                </td>
                                                <td>
                                            
                	                    <span class="dex-PushButton">
                                          <span class="first-child">
                                            <a class="ico-import"
                                               href="javascript:{showWorkInProgress();document.siteImportPrepackaged.submit();}"><fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.proceed"/></a>
                                          </span>
                                        </span>

                                                </td>
                                            </tr>
                                        </table>
                                    </form>
                                </div>
                                    </c:if>
                                <!--   import backup -->
                                <div class="head headtop">
                                    <div class="object-title">
                                        <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.label"/>
                                    </div>
                                </div>
                                <div  class="content-item">
                                    <% if (warningMsg != "" && sub.equals("prepareimport")) { %>
                                    <p class="errorbold">
                                        <%=warningMsg %>
                                    </p>
                                    <% } %>
                                    <form name="siteImport"
                                          action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=prepareimport")%>'
                                          method="post"
                                          enctype="multipart/form-data">
                                        <input type="hidden" name="validityCheckOnImport" value="true"/>
                                        <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                                            <tr>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.fileselect"/>&nbsp;
                                                </td>
                                                <td>
                                                    :&nbsp;<input type="file" name="import" onclick="setCheckedValue(document.forms['siteImport'].elements['siteImport'], 'siteImport'); setCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged'], '');setCheckedValue(document.forms['blank'].elements['blank'], '');">
                                                </td>
                                            </tr><tr>
                                            <td>
                                                <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.fileinput"/>&nbsp;
                                            </td>
                                            <td>
                                                :&nbsp;<input name="importpath" size="<%=inputSize%>">
                                            </td>
                                            <td>
                                            <span class="dex-PushButton">
                                              <span class="first-child">
                                                <a class="ico-import"
                                                   href="javascript:{showWorkInProgress(); document.siteImport.submit();}"><fmt:message key="org.jahia.admin.site.ManageSites.fileImport.label"/></a>
                                              </span>
                                            </span>
                                            </td>

                                        </tr>
                                        </table>
                                    </form>
                                </div>
                                <%} else {%>
                                <div  class="content-item">
                                    <fmt:message key="org.jahia.admin.site.ManageSites.noTemplatesSets"/>
                                </div>
                                <%}%>
                            </div>
                            <% } %>
                        </div>
                    </div>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<br class="clear"/>
<div id="actionBar">
    <%
        if (session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") == null) { %>
                            <span class="dex-PushButton">
                              <span class="first-child">
                                <a class="ico-back"
                                   href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
                              </span>
                            </span>


    <%
        } %>
</div>
</div>


<SCRIPT language="JavaScript">

    // return the value of the radio button that is checked
    // return an empty string if none are checked, or
    // there are no radio buttons
    function getCheckedValue(radioObj) {
        if(!radioObj)
            return "";
        var radioLength = radioObj.length;
        if(radioLength == undefined)
            if(radioObj.checked)
                return radioObj.value;
            else
                return "";
        for(var i = 0; i < radioLength; i++) {
            if(radioObj[i].checked) {
                return radioObj[i].value;
            }
        }
        return "";
    }

    // set the radio button with the given value as being checked
    // do nothing if there are no radio buttons
    // if the given value does not exist, all the radio buttons
    // are reset to unchecked
    function setCheckedValue(radioObj, newValue) {
        if(!radioObj)
            return;
        var radioLength = radioObj.length;
        if(radioLength == undefined) {
            //window.alert("setting new checked value for ");
            radioObj.checked = (radioObj.value == newValue.toString());
            return;
        }
        for(var i = 0; i < radioLength; i++) {
            radioObj[i].checked = false;
            if(radioObj[i].value == newValue.toString()) {
                //window.alert("setting new checked value for "+newValue.toString());
                radioObj[i].checked = true;
            }
        }
    }

    function submitform()
    {

        if(getCheckedValue(document.forms['blank'].elements['blank'])=="blank"){
            //window.alert("radio button checked   "+getCheckedValue(document.forms['blank'].elements['blank']));
            document.blank.submit();
        }
        if(getCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged'])=="siteImportPrepackaged"){
            //window.alert("radio button checked   "+getCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged']));
            document.siteImportPrepackaged.submit();
        }
        if(getCheckedValue(document.forms['siteImport'].elements['siteImport'])=="siteImport"){
            //window.alert("radio button checked   "+getCheckedValue(document.forms['siteImport'].elements['siteImport']));
            document.siteImport.submit();

        }
    }
</SCRIPT>
<%@include file="/admin/include/footer.inc" %>