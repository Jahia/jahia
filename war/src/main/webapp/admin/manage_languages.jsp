<%@page import="org.jahia.bin.Jahia,org.jahia.utils.LanguageCodeConverters,java.util.Iterator,java.util.Locale,java.util.Set" %>
<%@include file="/admin/include/header.inc" %>
<%
    Set<String> languageSet = (Set<String>) request.getAttribute("languageSet");
    Set<String> inactiveLanguageSet = (Set<String>) request.getAttribute("inactiveLanguageSet");
    Set<String> inactiveLiveLanguageSet = (Set<String>) request.getAttribute("inactiveLiveLanguageSet");
    Set<String> mandatoryLanguageSet = (Set<String>) request.getAttribute("mandatoryLanguageSet");
    Boolean mixLanguages = (Boolean) request.getAttribute("mixLanguages");
    Boolean allowsUnlistedLanguages = (Boolean) request.getAttribute("allowsUnlistedLanguages");
    String defaultLanguage = (String) request.getAttribute("defaultLanguage");
    Locale currentLocale = Jahia.getThreadParamBean().getUILocale();
%>
<script type="text/javascript" language="javascript">
    <!--

    function sendForm() {
        showWorkInProgress();
        document.mainForm.submit();
    }

    //-->
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message
            key="label.manageLanguages"/>: <% if (currentSite != null) { %><fmt:message
            key="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getTitle() %>&nbsp;&nbsp;<%} %></h2>
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
                            <jsp:param name="mode" value="site"/>
                        </jsp:include>
                        <div id="content" class="fit">
                            <div class="head">
                                <div class="object-title">
                                    <fmt:message
                                            key="org.jahia.admin.languages.ManageSiteLanguages.configuredLanguages.label"/>&nbsp;
                                </div>
                            </div>
                            <form name="mainForm"
                                  action='<%=JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=commit")%>'
                                  method="post">
                                <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0"
                                       style="width: 100%">
                                    <thead>
                                    <tr>
                                        <th>
                                            <fmt:message
                                                    key="label.language"/>
                                        </th>
                                        <th style="text-align:center">
                                            <fmt:message
                                                    key="org.jahia.admin.languages.ManageSiteLanguages.default.label"/>
                                        </th>
                                        <th style="text-align:center">
                                            <fmt:message
                                                    key="org.jahia.admin.languages.ManageSiteLanguages.mandatory.label"/>
                                        </th>
                                        <th style="text-align:center">
                                            <fmt:message key="label.active"/> (<fmt:message key="label.edit"/>)
                                        </th>
                                        <th style="text-align:center">
                                            <fmt:message key="label.active"/> (<fmt:message key="label.live"/>)
                                        </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%
                                        int count = 0;
                                        if (languageSet.size() == 0) { %>
                                    <tr>
                                        <td colspan="5">
                                            <b><fmt:message
                                                    key="org.jahia.admin.languages.ManageSiteLanguages.noLanguageDefined.label"/></b>
                                        </td>
                                    </tr>
                                    <%
                                    } else {
                                        for (String lang : languageSet) {
                                            count++;
                                            Locale curLocale = LanguageCodeConverters.languageCodeToLocale(lang);
                                            pageContext.setAttribute("lang", lang);
                                            pageContext.setAttribute("even", (count % 2 == 0));
                                            pageContext.setAttribute("defaultLang", lang.equals(defaultLanguage));
                                            pageContext.setAttribute("mandatory", mandatoryLanguageSet.contains(lang));
                                            pageContext.setAttribute("inactive", inactiveLanguageSet.contains(lang));
                                            pageContext.setAttribute("inactiveLive", inactiveLiveLanguageSet.contains(lang));
                                            
                                            String style = null;
                                            if (inactiveLanguageSet.contains(lang)) {
                                                style = "color: #666666";
                                            }
                                            pageContext.setAttribute("style", style != null ? " style=\"" + style + "\"" : null);
                                            %>
                                    <tr class="${even ? 'evenLine' : 'oddLine'}">
                                        <td align="left"${not empty style ? style : ''}>
                                            <%=curLocale.getDisplayName(currentLocale) %> (<%=curLocale.toString() %>)
                                            <input type="hidden" name="languages" value="${lang}"/>
                                        </td>
                                        <td align="center">
                                            <input type="radio" name="defaultLanguage" value="${lang}"
                                                    ${defaultLang ? 'checked="checked"' : ''}
                                                    ${!defaultLang && inactive ? 'disabled="disabled"' : ''}/>
                                        </td>
                                        <td align="center">
                                            <input type="checkbox" name="mandatoryLanguages" value="${lang}"
                                            ${mandatory ? 'checked="checked"' : ''}
                                            ${inactive ? 'disabled="disabled"' : ''}/>
                                        </td>
                                        <td align="center">
                                            <c:if test="${defaultLang}">
                                                <input type="checkbox" name="activeLanguages_display" value="${lang}" checked="checked" disabled="disabled"/>
                                                <input type="hidden" name="activeLanguages" value="${lang}"/>
                                            </c:if>
                                            <c:if test="${!defaultLang}">
                                            <input type="checkbox" name="activeLanguages" value="${lang}"${!inactive ? 'checked="checked"' : ''}/>
                                            </c:if>
                                        </td>
                                        <td align="center">
                                            <c:if test="${defaultLang}">
                                                <input type="checkbox" name="activeLiveLanguages_display" value="${lang}" checked="checked" disabled="disabled"/>
                                                <input type="hidden" name="activeLiveLanguages" value="${lang}"/>
                                            </c:if>
                                            <c:if test="${!defaultLang}">
                                            <input type="checkbox" name="activeLiveLanguages" value="${lang}"
                                                ${!inactive && !inactiveLive? 'checked="checked"' : ''}
                                                ${inactive ? 'disabled="disabled"' : ''}/>
                                            </c:if>
                                        </td>
                                    </tr>
                                    <%
                                            }
                                        } %>
                                    </tbody>
                                </table>
                                <div class="head headtop">
                                    <div class="object-title">
                                        <fmt:message key="label.options"/>
                                    </div>
                                </div>
                                <input type="checkbox" name="mixLanguages" id="mixLanguages" value="true"${mixLanguages ? ' checked="checked"' : ''} />
                                <label for="mixLanguages">&nbsp;<fmt:message
                                        key="org.jahia.admin.languages.ManageSiteLanguages.mixLanguages.label"/></label>
                                <br>
                                <input type="checkbox" name="allowsUnlistedLanguages" id="allowsUnlistedLanguages" value="true"${allowsUnlistedLanguages ? ' checked="checked"' : ''} />
                                <label for="allowsUnlistedLanguages">&nbsp;<fmt:message
                                        key="org.jahia.admin.languages.ManageSiteLanguages.allowsUnlistedLanguages.label"/></label>
                                <br>
                                <% if (request.getAttribute("jahiaErrorMessage") != null) { %>
                                <br>

                                <div class="text2" style="text-align:center">
                                    <%=request.getAttribute("jahiaErrorMessage") %>
                                </div>
                                <%
                                    } %>
                                <br>
                                <br>

                                <div style="text-align:center">
                                    <table border="0" cellpadding="5" cellspacing="0">
                                        <tr>
                                            <td>
                                                <b><fmt:message
                                                        key="org.jahia.admin.languages.ManageSiteLanguages.availableLanguages.label"/></b><br/>
                                                <select name="language_list" id="language_list" multiple="multiple" size="10">
                                                    <%
                                                        Iterator localeIter = LanguageCodeConverters.getSortedLocaleList(
                                                                currentLocale).iterator();
                                                        while (localeIter.hasNext()) {
                                                            Locale curLocale = (Locale) localeIter.next();
                                                            // we must now check if this language wasn't already inserted in
                                                            // the site.
                                                            if (!languageSet.contains(curLocale.toString())) {
                                                                String displayName = "";
                                                                displayName = curLocale.getDisplayName(
                                                                        currentLocale); %>
                                                    <option value="<%=curLocale%>"><%=displayName %>&nbsp;(<%=curLocale.toString() %>)</option>
                                                    <%
                                                            }
                                                        } %>
                                                </select>
                                            </td>
                                            <td>
                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="add-lang" href="javascript:sendForm();"
                                           title="<fmt:message key='org.jahia.admin.languages.ManageSiteLanguages.addLanguages.label'/>"><fmt:message
                                                key="org.jahia.admin.languages.ManageSiteLanguages.addLanguages.label"/></a>
                                    </span>
                                </span>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                            </form>
                            <br/>
                        </div>
                    </div>
                </div>
            </td>
        </tr>
        </tbody>
    </table>
</div>
</div>
<div id="actionBar">
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-back"
                       href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message
                            key="label.backToMenu"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-ok" href="#save" onclick="document.getElementById('language_list').selectedIndex=-1; sendForm(); return false;"><fmt:message key='label.save'/></a>
                  </span>
                </span>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>
