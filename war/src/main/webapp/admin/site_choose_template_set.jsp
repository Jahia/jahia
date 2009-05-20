<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@page import="org.jahia.data.templates.JahiaTemplatesPackage" %>
<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.params.ProcessingContext,org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Locale" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    Locale selectedLocale = (Locale) request.getAttribute("selectedLocale");
    Locale currentLocale = (Locale) request.getAttribute("currentLocale");
    stretcherToOpen = 0;
%>

<%if (!isConfigWizard) {%>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit">
        <%if (!isConfigWizard) {%>
        <fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
        <%} else {%>
        <internal:message key="org.jahia.createSite.siteFactory"/>
        <%}%>
    </h2>
</div>
<% } %>

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
        <%if (!isConfigWizard) {%>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/admin/include/tab_menu.inc" %>
            </td>
        </tr>
        <% } %>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
                <%if (!isConfigWizard) {%>
                <div class="dex-TabPanelBottom">
                    <div class="tabContent">
                        <jsp:include page="/admin/include/left_menu.jsp">
                            <jsp:param name="mode" value="server"/>
                        </jsp:include>

                        <div id="content" class="fit">
                                <% } else { %>

                            <div class="dex-TabPanelBottom-full">

                                <div id="content" class="full">
                                    <% } %>
                                    <div class="head">
                                        <div class="object-title"><fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTemplateSet.label"/>
                                        </div>
                                        <%if (!isConfigWizard) {%>
                                        <div class="object-shared">
                                            <fmt:message key="org.jahia.step.label"/> 2 / 3
                                        </div>
                                        <% } %>
                                    </div>


                                    <form name="jahiaAdmin"
                                          action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processtemplatesetchoice")%>'
                                          method="post" enctype="multipart/form-data">
                                        <table border="0" cellspacing="0" cellpadding="5">
                                            <tr>
                                                <c:if test="${not empty tmplSets}">
                                                    <td>
                                                        <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTemplateSet.label"/>&nbsp;:
                                                    </td>
                                                    <td>
                                                        <select id="selectTmplSet" name="selectTmplSet" onChange="submitForm('change');">
                                                            <c:forEach items="${tmplSets}" var="tmplPack">
                                                                <c:set var="displayName" value="" scope="request"/>
                                                                <c:forEach items="${tmplPack.invertedHierarchy}"
                                                                           var="parent">
                                                                    <c:set var="displayName"
                                                                           value="${functions:stringConcatenation(displayName, ' / ', parent)}"
                                                                           scope="request"/>
                                                                </c:forEach>
                                                                <option value="<c:out value='${tmplPack.name}'/>"
                                                                        <c:if test="${tmplPack.name == selectedTmplSet}">selected="selected"</c:if>>
                                                                    <c:out value="${fn:substring(displayName, 3, -1)}"/></option>
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
                                            <script type="text/javascript">
                                                function swapImage(imgId,imgToSwitch){
                                                    var image = document.getElementById(imgId);
                                                    var dropd = document.getElementById(imgToSwitch);
                                                    var themePreview = '${selectedPackage.thumbnail}';
                                                    var themePreviewBegin = themePreview.substr(0,themePreview.lastIndexOf("."));
                                                    var themePreviewEnd = themePreview.substr(themePreview.lastIndexOf("."),themePreview.length);
                                                    if (image != null) {
                                                        if (dropd.value.length > 0) {
                                                            image.src = '<%=URL%>../${selectedPackage.rootFolderPath}/' + themePreviewBegin + '_' + dropd.value + themePreviewEnd;
                                                        } else {
                                                            image.src = '<%=URL%>../${selectedPackage.rootFolderPath}/' + themePreview;
                                                        }
                                                    }
                                                };
                                                swapImage('themePreview','selectTmplSet');
                                            </script>
                                            <tr>
                                                <c:if test="${not empty selectedPackage && not empty selectedPackage.thumbnail}">
                                                    <td>&nbsp;</td>
                                                    <td>
                                                        <img id ="themePreview" src="<%=URL%>../templates/<c:out value="${selectedPackage.rootFolder}"/>/<c:out value="${selectedPackage.thumbnail}"/>"
                                                             width="270" height="141" alt="">
                                                    </td>
                                                </c:if>
                                                <c:if test="${empty selectedPackage || empty selectedPackage.thumbnail}">
                                                    <td>&nbsp;</td>
                                                    <td>
                                                        <fmt:message key="org.jahia.admin.site.ManageSites.NoTemplatePreview.label"/>
                                                    </td>
                                                </c:if>
                                            </tr>
                                            <tr>
                                                <c:if test="${not empty availableThemes}">
                                                    <td>
                                                        <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTheme.label"/>&nbsp;:
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
                                                </c:if>
                                            </tr>
                                            <tr>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseLanguage.label"/>&nbsp;:
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
    <%if (!isConfigWizard) {%>


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
                    <a class="ico-ok" href="javascript:submitForm('save');" onclick="showWorkInProgress(); return true;">
                        <fmt:message key="org.jahia.admin.save.label"/>
                    </a>
                  </span>
                </span>
    </c:if>


    <% } else { %>
    <c:if test="${not empty selectedPackage}">
               <span class="dex-PushButton">
                    <span class="first-child">
                    <a class="ico-back"
                       href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=add")%>' onclick="showWorkInProgress(); return true;">
                        <internal:message key="org.jahia.back.button"/>
                    </a>
                   </span>
                </span>

                   <span class="dex-PushButton">
                    <span class="first-child">
                    <a class="ico-next" href="javascript:submitForm('save');" onclick="showWorkInProgress(); return true;">

                        <internal:message key="org.jahia.nextStep.button"/>

                    </a>
                   </span>
                </span>
    </c:if>
    <% } %>
</div>
</div>


<%@include file="/admin/include/footer.inc" %>