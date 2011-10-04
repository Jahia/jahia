<%@include file="/admin/include/header.inc" %>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.jahia.services.templates.JahiaTemplateManagerService"%>
<%@page import="org.jahia.data.templates.JahiaTemplatesPackage" %>
<%@page import="org.jahia.bin.Jahia,org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Locale" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%
    Locale selectedLocale = (Locale) request.getAttribute("selectedLocale");
    Locale currentLocale = Jahia.getThreadParamBean().getUILocale();
    JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
    stretcherToOpen = 0;
%>

<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit">
        <fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
    </h2>
</div>

<script type="text/javascript" language="javascript">
    function setCheckedValue(radioObj, newValue) {
        if (!radioObj)
            return;
        var radioLength = radioObj.length;
        if (radioLength == undefined) {
            radioObj.checked = (radioObj.value == newValue.toString());
            return;
        }
        for (var i = 0; i < radioLength; i++) {
            radioObj[i].checked = false;
            if (radioObj[i].value == newValue.toString()) {
                radioObj[i].checked = true;
            }
        }
    }
    function submitForm(operation) {
        showWorkInProgress();
        document.jahiaAdmin.operation.value = operation;
        document.jahiaAdmin.submit();
    }
</script>

<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
           cellspacing="0">
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
                                    <div class="head">
                                        <div class="object-title"><fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTemplateSet.label"/>
                                        </div>
                                        <div class="object-shared">
                                            <fmt:message key="label.step"/> 2 / 3
                                        </div>
                                    </div>


                                    <form name="jahiaAdmin"
                                          action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processtemplatesetchoice")%>'
                                          method="post" enctype="multipart/form-data">
                                        <table border="0" cellspacing="0" cellpadding="5">
                                            <tr>
                                                <c:if test="${not empty tmplSets}">
                                                    <td>
                                                        <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTemplateSet.label"/>:
                                                    </td>
                                                    <td>
                                                        <select id="selectTmplSet" name="selectTmplSet" onChange="submitForm('change');">
                                                            <c:forEach items="${tmplSets}" var="tmplPack">
                                                                <c:set var="displayName" value="" scope="request"/>
                                                                <option value="<c:out value='${tmplPack.name}'/>"
                                                                        <c:if test="${tmplPack.name == selectedTmplSet}">selected="selected"</c:if>>
                                                                    <c:out value="${tmplPack.name}"/></option>
                                                            </c:forEach>
                                                        </select>
                                                    </td>
                                                </c:if>
                                                <c:if test="${empty tmplSets}">
                                                    <td colspan="2">
                                                        <fmt:message key="org.jahia.admin.site.ManageSites.noTemplatesHasbeenFound.label"/>
                                                    </td>
                                                </c:if>
                                            </tr>
                                            <jcr:node path="${selectedPackage.path}/templates/files/template.jpg" var="thumbnail"/>
                                            <script type="text/javascript">
                                                function swapImage(imgId,imgToSwitch){
                                                    var image = document.getElementById(imgId);
                                                    var dropd = document.getElementById(imgToSwitch);
                                                    var themePreview = '${thumbnail.url}';
                                                    var themePreviewBegin = themePreview.substr(0,themePreview.lastIndexOf("."));
                                                    var themePreviewEnd = themePreview.substr(themePreview.lastIndexOf("."),themePreview.length);
                                                    if (image != null) {
                                                        if (dropd.value.length > 0) {
                                                            image.src = '<%=URL%>../${selectedPackage.name}/' + themePreviewBegin + '_' + dropd.value + themePreviewEnd;
                                                        } else {
                                                            image.src = '<%=URL%>../${selectedPackage.name}/' + themePreview;
                                                        }
                                                    }
                                                };
                                                swapImage('themePreview','selectTmplSet');
                                            </script>
                                            <tr>
                                                <c:if test="${not empty selectedPackage && not empty thumbnail.url}">
                                                    <td>&nbsp;</td>
                                                    <td>
                                                        <img id ="themePreview" src="${thumbnail.url}"
                                                             width="270" height="141" alt="">
                                                    </td>
                                                </c:if>
                                                <c:if test="${empty selectedPackage || empty thumbnail.url}">
                                                    <td>&nbsp;</td>
                                                    <td>
                                                        <img src="<%=URL%>/images/pictureNotAvailable.jpg" width="200" height="200" alt="<fmt:message key='org.jahia.admin.site.ManageSites.NoTemplatePreview.label'/>" title="<fmt:message key='org.jahia.admin.site.ManageSites.NoTemplatePreview.label'/>"/>
                                                    </td>
                                                </c:if>
                                            </tr>
                                            <c:if test="${not empty availableThemes}">
                                            <tr>
                                                    <td>
                                                        <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTheme.label"/>:
                                                    </td>
                                                    <td>
                                                        <select id="selectTheme" name="selectTheme"  onChange="swapImage('themePreview','selectTheme');">
                                                            <c:if test="${empty selectedTheme}">
                                                                <c:set var="selectedTheme" value="default"/>
                                                            </c:if>
                                                            <c:forEach items="${availableThemes}" var="theme">
                                                                <option value="<c:out value='${theme.key}'/>"
                                                                        <c:if test="${theme.key == selectedTheme}">selected="selected"</c:if>>
                                                                    <c:out value="${theme.value}"/></option>
                                                            </c:forEach>
                                                        </select>
                                                    </td>
                                            </tr>
                                            </c:if>
                                            <c:if test="${not empty jahiApps || not empty modules}">
                                            <tr>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseModules.label"/>:
                                                </td>
                                                <td style="vertical-align: top;">
                                                    <table width="100%">
                                                        <tr>
                                                            <td style="vertical-align: top;">
                                                                <c:forEach items="${jahiApps}" var="module">
                                                                    <input type="checkbox" id="module-${module.name}" name="selectedModules" value="${module.name}"${not empty selectedModules && functions:contains(selectedModules, module.name) ? ' checked="checked"' : ''}/>&nbsp;<label for="module-${module.name}">${fn:escapeXml(module.displayableName)}</label><br/>
                                                                </c:forEach>
                                                            </td>
                                                            <td style="vertical-align: top;">
                                                                ${paramValues.modules}
                                                                <c:forEach items="${modules}" var="module">
                                                                    <input type="checkbox" id="module-${module.name}" name="selectedModules" value="${module.name}"${not empty selectedModules && functions:contains(selectedModules, module.name) ? ' checked="checked"' : ''}/>&nbsp;<label for="module-${module.name}">${fn:escapeXml(module.displayableName)}</label><br/>
                                                                </c:forEach>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                            </c:if>
                                            <tr>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseLanguage.label"/>:
                                                </td>
                                                <td>
                                                    <select name="languageList">
                                                        <%
                                                            Iterator localeIter = LanguageCodeConverters.getSortedLocaleList(currentLocale).iterator();
                                                            while (localeIter.hasNext()) {
                                                                Locale curLocale = (Locale) localeIter.next();
                                                                String displayName = "";
                                                                displayName = curLocale.getDisplayName(currentLocale);
                                                        %>
                                                        <option value="<%=curLocale%>"
                                                                <% if ( curLocale.toString().equals(selectedLocale.toString()) ){%>selected="1"<%}%> ><%=displayName%>
                                                            (<%=curLocale.toString()%>)
                                                        </option>
                                                        <%
                                                            }
                                                        %>
                                                    </select>
                                                </td>
                                            </tr>

                                        </table>


                                        <input type="hidden" name="operation" value="">
                                    </form>


                                </div>
                            </div>

            </td>
        </tr>
        </tbody>
    </table>
</div>
<div id="actionBar">
    <%
        if ("0".equals(((String) session.getAttribute("siteAdminOption")).trim())) {
    %>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-back"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=displaycreateadmin")%>' onclick="showWorkInProgress(); return true;"><fmt:message key="org.jahia.admin.site.ManageSites.backToPreviousStep.label"/></a>
            </span>
          </span>
    <% } else if ("1".equals(((String) session.getAttribute("siteAdminOption")).trim())) { %>
          <span class="dex-PushButton">
            <span class="first-child">
                <a class="ico-back"
                   href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=displayselectexistantadmin")%>' onclick="showWorkInProgress(); return true;"><fmt:message key="org.jahia.admin.site.ManageSites.backToPreviousStep.label"/></a>
           </span>
          </span>
    <% } else { %>
          <span class="dex-PushButton">
            <span class="first-child">
                <a class="ico-back"
                   href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=add")%>' onclick="showWorkInProgress(); return true;"><fmt:message key="org.jahia.admin.site.ManageSites.backToPreviousStep.label"/></a>
           </span>
          </span>
    <% } %>
    <c:if test="${not empty selectedPackage}">

                  <span class="dex-PushButton">
                    <span class="first-child">
                    <a class="ico-next" href="javascript:submitForm('save');" onclick="showWorkInProgress(); return true;">
                        <fmt:message key="label.nextStep"/>
                    </a>
                  </span>
                </span>
    </c:if>


</div>
</div>


<%@include file="/admin/include/footer.inc" %>