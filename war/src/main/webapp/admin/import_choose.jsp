<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.services.SpringContextSingleton" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="org.jahia.services.importexport.SiteImportDefaults" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jahia.bin.JahiaAdministration" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    List importsInfos = (List) session.getAttribute("importsInfos");
    Map importInfosMap = new HashMap();
    for (Iterator iterator = importsInfos.iterator(); iterator.hasNext();) {
        Map infos = (Map) iterator.next();
        File file = (File) infos.get("importFile");
        importInfosMap.put(file, infos);
    }
    List importsInfosSorted = (List) session.getAttribute("importsInfosSorted");
    List tpls = (List) request.getAttribute("tmplSets");

    SiteImportDefaults siteImportDefaults = null;
    final Map<String, SiteImportDefaults> siteImportDefaultsMap = SpringContextSingleton.getInstance().getModuleContext().getBeansOfType(SiteImportDefaults.class);
    if (siteImportDefaultsMap != null && siteImportDefaultsMap.size() > 0) {
        if (siteImportDefaultsMap.size() > 1) {
            LoggerFactory.getLogger(JahiaAdministration.class).error("Found several beans of type org.jahia.services.importexport.SiteImportDefaults whereas only one is allowed, skipping");
        } else {
            siteImportDefaults = siteImportDefaultsMap.values().iterator().next();
        }
    }
    ProcessingContext jParams = null;
    if (jData != null) {
        jParams = jData.getProcessingContext();
    }
    final boolean isInstall = session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") != null;
    final String readmefilePath = response.encodeURL(new StringBuffer().append(request.getContextPath()).append("/html/startup/readme.html").toString());
    stretcherToOpen   = 0; %>
<script type="text/javascript">
    function sendForm(){
    	setWaitingCursor();
        document.main.submit();
    }

	function setWaitingCursor() {
    	if (typeof workInProgressOverlay != 'undefined') {
        	workInProgressOverlay.launch();
		}
	}

	function toggle(itemId) {
		var st = document.getElementById(itemId).style;
		st.display = st.display == 'none' ? 'block' : 'none';
	}
</script>
<div id="topTitle">
    <h1>Jahia</h1>
    <h2 class="edit">
        <fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
    </h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td style="vertical-align: top;" align="left">
        <%@include file="/admin/include/tab_menu.inc"%>
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
            <div class="head">
                <div class="object-title">
                    <fmt:message key="label.virtualSitesManagement.configwizard.variables"/>
                </div>
            </div>
            <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                <thead>
                <tr>
                    <th<%if(importsInfos.size()==1){ %> style="display:none;"<%} %> width="5%">&nbsp;</th>
                    <th width="95%">
                        <fmt:message key="label.name"/>
                    </th>

                </tr>
                </thead>
                <form name="main" method="post" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processimport")%>'>
                    <input type="hidden" name="do" value="sites"/>
                    <input type="hidden" name="sub" value="processimport"/>
                    <tbody>
                    <%
                        int lineCounter = 0;
                        for (Iterator iterator = importsInfosSorted.iterator(); iterator.hasNext();) {
                            File file = (File) iterator.next();
                            Map infos = (Map) importInfosMap.get(file);
                            pageContext.setAttribute("infos", infos);
                            String filename = (String) infos.get("importFileName");
                            String fileType = (String) infos.get("type");
                            String siteKey = file.getName();
                            String oldSiteKey = (String) infos.get("oldsitekey");
                            String lineClass = "oddLine";
                            if (lineCounter % 2 == 0) {
                                lineClass = "evenLine";
                            }
                            lineCounter++;
                    %>
                    <tr class="<%=lineClass%>">
                        <td<%if(importsInfos.size()==1){ %> style="display:none;"<%} %> align="center">
                            <input type="checkbox" name="<%=file.getName()%>selected" value="on"${not empty infos.selected ? ' checked="checked"' : ''}/>
                        </td>
                        <td>
                            <% if ("site".equals(fileType)) { %>
                            <c:if test="${not empty infos.validationResult}">
                            <c:set var="validationErrorsPresent" value="true"/>
                            <%@ include file="/admin/import_validation.jspf" %>
                            </c:if>
                            <table border="0" cellpadding="0" width="100%">
                                <tr>
                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>*&nbsp;<c:if test="${infos.siteTitleInvalid}">
                                        <div class="error" style="font-weight: bold;">
                                            <fmt:message key="org.jahia.admin.warningMsg.completeRequestInfo.label"/>
                                        </div></c:if>
                                    </td>
                                    <td>
                                        <input class="input" type="text" name="<%=siteKey+"siteTitle"%>" value="${fn:escapeXml(infos.sitetitle)}" size="<%=inputSize%>" maxlength="100">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>*&nbsp;<c:if test="${infos.siteServerNameInvalid || infos.siteServerNameExists}">
                                        <div class="error" style="font-weight: bold;">
                                            <fmt:message key="${empty infos.siteservername ? 'org.jahia.admin.warningMsg.completeRequestInfo.label' : (infos.siteServerNameInvalid ? 'org.jahia.admin.warningMsg.invalidServerName.label' : 'org.jahia.admin.warningMsg.chooseAnotherServerName.label')}"/>
                                        </div></c:if>
                                    </td>
                                    <td>
                                        <input class="input" type="text" name="<%=siteKey+"siteServerName"%>" value="${fn:escapeXml(infos.siteservername)}" size="<%=inputSize%>" maxlength="200">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>*&nbsp;<c:if test="${infos.siteKeyInvalid || infos.siteKeyExists}">
                                        <div class="error" style="font-weight: bold;">
                                            <fmt:message key="${empty infos.sitekey ? 'org.jahia.admin.warningMsg.completeRequestInfo.label' : (infos.siteKeyInvalid ? 'org.jahia.admin.warningMsg.onlyLettersDigitsUnderscore.label' : 'org.jahia.admin.warningMsg.chooseAnotherSiteKey.label')}"/>
                                        </div></c:if>
                                    </td>
                                    <td>
                                        <input type="hidden" name="<%=siteKey+"oldSiteKey"%>" value="<%= oldSiteKey %>"><input class="input" type="text" name="<%=siteKey+"siteKey"%>" value="${fn:escapeXml(infos.sitekey)}" size="<%=inputSize%>" maxlength="50">
                                    </td>
                                </tr>
                                <tr>

                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTemplateSet.label"/>&nbsp;
                                    </td>
                                    <td>
                                        <%
                                            String selectedTmplSet;
                                            if (((Boolean) infos.get("legacyImport")) && siteImportDefaults != null) {
                                                selectedTmplSet = siteImportDefaults.getDefaultTemplateSet(oldSiteKey);
                                            } else {
                                                selectedTmplSet = (String) infos.get("templates");
                                            }
                                            pageContext.setAttribute("selectedTmplSet", selectedTmplSet);
                                        %>
                                        <select name="<%=siteKey%>templates">
                                            <c:if test="${not empty requestScope.templateSetsMissingCounts}">
                                                <c:forEach var="tmplSet" items="${requestScope.templateSetsMissingCounts}">
                                                    <c:if test="${tmplSet.value > 0}">
                                                        <fmt:message key="label.import.templatesMissing" var="i18nMissingTemplates">
                                                            <fmt:param value="${tmplSet.value}"/>
                                                        </fmt:message>
                                                        <c:set var="i18nMissingTemplates" value=" (${fn:escapeXml(i18nMissingTemplates)})"/>
                                                    </c:if>
                                                    <option value="${tmplSet.key}"${selectedTmplSet==tmplSet.key ? 'selected="selected"' : ''}${not empty i18nMissingTemplates ? 'style="color: #ADB7BD"' : ''}>${tmplSet.key}${i18nMissingTemplates}</option>
                                                </c:forEach>
                                            </c:if>
                                            <c:if test="${empty requestScope.templateSetsMissingCounts}">
                                            <% if (tpls != null)
                                                for (Iterator iterator1 = tpls.iterator(); iterator1.hasNext();) {
                                                    JCRNodeWrapper pack = (JCRNodeWrapper) iterator1.next();
                                            %>
                                            <option value="<%=pack.getName()%>"<% if (pack.getName().equals(selectedTmplSet)) { %>selected="selected"<% } %>><%=pack.getName() %></option>

                                            <%
                                                    } %>
                                            </c:if>
                                        </select>
                                    </td>
                                </tr>
                                <%if ((Boolean) infos.get("legacyImport")) { %>
                                <%
                                    List<java.io.File> legacyMappings = (List<File>) infos.get("legacyMappings");
                                    if (legacyMappings != null && !legacyMappings.isEmpty()) {
                                %>
                                <tr>
                                    <td>
                                        <fmt:message
                                                key="org.jahia.admin.site.ManageSites.selectDefinitionMapping"/>
                                    </td>
                                    <td>
                                        <select name="<%=siteKey%>legacyMapping">
                                            <option value="">No Mapping file or zip internal mapping file</option>
                                            <%
                                                String selectedMappingFile;
                                                if (siteImportDefaults != null) {
                                                    selectedMappingFile = siteImportDefaults.getDefaultMappingFile(oldSiteKey);
                                                } else {
                                                    selectedMappingFile = (String) infos.get("templates");
                                                }
                                                for (File legacyMapping : legacyMappings) {
                                            %>
                                            <option value="<%=legacyMapping.getAbsolutePath()%>" <% if (legacyMapping.getName().equals(selectedMappingFile)) { %>selected="selected"<% } %>><%=legacyMapping.getName()%>
                                            </option>
                                            <%
                                                }
                                            %>
                                        </select>

                                    </td>
                                </tr>
                                <%
                                    }
                                    List<java.io.File> legacyDefinitions = (List<File>) infos.get("legacyDefinitions");
                                    if (legacyDefinitions != null && !legacyDefinitions.isEmpty()) {
                                %>
                                <tr>
                                    <td>
                                        <fmt:message
                                                key="org.jahia.admin.site.ManageSites.selectLegacyDefinitions"/>
                                    </td>
                                    <td>
                                        <select name="<%=siteKey%>legacyDefinitions">
                                            <option value="">No definitions file or zip internal definitions file</option>
                                            <%
                                                String selectedSourceDefinitionsFile;
                                                if (siteImportDefaults != null) {
                                                    selectedSourceDefinitionsFile = siteImportDefaults.getDefaultSourceDefinitionsFile(oldSiteKey);
                                                } else {
                                                    selectedSourceDefinitionsFile = (String) infos.get("templates");
                                                }
                                                for (File legacyDefinition : legacyDefinitions) {
                                            %>
                                            <option value="<%=legacyDefinition.getAbsolutePath()%>" <% if (legacyDefinition.getName().equals(selectedSourceDefinitionsFile)) { %>selected<% } %>><%=legacyDefinition.getName()%>
                                            </option>
                                            <%
                                                }
                                            %>
                                        </select>

                                    </td>
                                </tr>
                                <%
                                    }
                                    }
                                %>
                            </table>
                            <% } else if ("files".equals(fileType))  { %>
                            <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.shared"/>: <%=filename %>
                            <% } else { %>
                            <fmt:message key='<%="org.jahia.admin.site.ManageSites.fileImport."+filename %>'/><% } %>
                        </td>
                    </tr>
                    <%} %>
                    <c:if test="${validationErrorsPresent}">
                    <tr>
                        <td colspan="2" class="error"><fmt:message key="failure.import.incomplete"/></td>
                    </tr>
                    </c:if>
                    </tbody>
                </form>
            </table>
        </div>
    </div>
</td>
</tr>
</tbody>
</table>
</div>
<div id="actionBar">
    <%
        if (session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") == null) { %>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
            </span>
          </span><%} %>
          <span class="dex-PushButton">
            <span class="first-child">
                <c:if test="${not validationErrorsPresent}">
                    <a class="ico-ok" href='javascript:sendForm();'><fmt:message key="label.doImport"/></a>
                </c:if>
                <c:if test="${validationErrorsPresent}">
                    <fmt:message key="failure.import.incomplete" var="i18nValidationError"/>
                    <fmt:message key="org.jahia.admin.site.ManageSites.doYouWantToContinue.label" var="i18nContinue"/>
                    <c:set var="i18nConfirm" value="${functions:escapeJavaScript(i18nValidationError)}\n${functions:escapeJavaScript(i18nContinue)}"/>
                    <a class="ico-ok" href="#import" onclick="if (confirm('${i18nConfirm}')) { sendForm(); } return false;"><fmt:message key="label.doImport"/></a>
                </c:if>
            </span>
          </span>
</div>

</div>
<%@include file="/admin/include/footer.inc" %>