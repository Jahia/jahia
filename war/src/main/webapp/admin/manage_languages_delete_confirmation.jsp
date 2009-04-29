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
<%@page import   = "org.jahia.bin.*,java.util.*,org.jahia.services.sites.*,org.jahia.registries.*,org.jahia.utils.*"%>
<%
    List languagesToDelete = (List) request.getAttribute("languagesToDelete");
%>

<%@include file="/admin/include/header.inc"%>

<tr>
    <td align="center" class="text"><img name="language" src="<%=URL%>images/icons/admin/signpost.gif" width="48" height="48" border="0" align="middle"></td><td align="left" class="text"><h3><fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.deleteLanguages.label"/></h3></td>
</tr>
</table>
<br><br>

<table cellpadding="2" cellspacing="0" border="0" width="530">
<tr>
    <td colspan="2" width="530">&nbsp;</td>
</tr>
    <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=reallyDelete")%>' method="post">
<tr>
    <td width="100">&nbsp;</td>
    <td width="430">
        <table cellpadding="2" cellspacing="0" border="0">
        <tr>
            <td nowrap class="text">
                <b><font color="#FF0000"><fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.pleaseBeCareful.label"/>&nbsp;:</font></b>
                <br><br>
                <fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.definitelyDelete.label"/><br>
                <br>
<%
    Iterator languagesToDeleteEnum = languagesToDelete.iterator();
    while (languagesToDeleteEnum.hasNext()) {
        SiteLanguageSettings curSetting = (SiteLanguageSettings) languagesToDeleteEnum.next();
        Locale curLocale = LanguageCodeConverters.languageCodeToLocale(curSetting.getCode());
%>
                <%=curLocale.getDisplayName()%> (<%=curLocale.toString()%>)<br>
<%
    }
%>
                <br><fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.reallyWantContinue.label"/>
                <br><br>
            </td>
        </tr>
        <tr>
            <td align="right">
                &nbsp;<br>
                <a href='<%=JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=display")%>' onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Cancel','','${pageContext.request.contextPath}<fmt:message key="org.jahia.cancelOn.button"/>',1)"><img name="Cancel" src="${pageContext.request.contextPath}<fmt:message key="org.jahia.cancelOff.button"/>" width="69" height="17" border="0"></a>
                <a href="javascript:document.jahiaAdmin.submit();" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('delete','','${pageContext.request.contextPath}<fmt:message key="org.jahia.deleteOn.button"/>',1)"><img name="delete" src="${pageContext.request.contextPath}<fmt:message key="org.jahia.deleteOff.button"/>" width="69" height="17" border="0" alt="<fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.delete.label"/>"></a>
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;<br><br><br>
                <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td nowrap width="145" valign="top"><font class="text"><b><fmt:message key="org.jahia.admin.otherOperations.label"/>&nbsp;:&nbsp;&nbsp;&nbsp;</b></font></td>
                    <td valign="top">
                        <font class="text">
                            <li> <a href='<%=JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=display")%>'><fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.backToLanguageList.label"/></a><br>
                            <li> <a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a><br>
                        </font>
                    </td>
                </tr>
                </table>
            </td>
        </tr>
        </table>
    </td>
</tr>
   </form>
<tr>
    <td colspan="2" align="right">
        <table border="0" width="100%"><tr><td width="48"><img name="logo" src="../css/images/logo/logo-jahia.gif" border="0" width="45" height="34"></td><td><img src="<%=URL%>images/pix.gif" border="0" width="1" height="10">
<div id="copyright"><%=copyright%></div><span class="version">Jahia <%=Jahia.VERSION%>.<%=Jahia.getPatchNumber()%> r<%=Jahia.getBuildNumber()%></span>
</td></tr></table>
    </td>
</tr>

</table>

<%@include file="/admin/include/footer.inc"%>