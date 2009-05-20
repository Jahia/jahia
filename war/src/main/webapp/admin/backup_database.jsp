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
<%@page import   = "java.util.*"%>
<%@page import="org.jahia.bin.*"%>

<%
    String jahiaBackupName  = (String)  request.getAttribute("jahiaBackupName");
    String jahiaBackupDesc  = (String)  request.getAttribute("jahiaBackupDesc");
    String jahiaBackupType  = (String)  request.getAttribute("jahiaBackupType");
%>

<%@include file="/admin/include/header.inc"%>

<tr>
    <td align="center" class="text"><br><br><img name="db" src="<%=URL%>images/icons/admin/data.gif" width="48" height="48" border="0" align="middle"></td><td align="left" class="text"><h3><fmt:message key="org.jahia.admin.database.ManageDatabase.backupCurrentDatabase.label"/></h3></td>
</tr>
</table>
<br><br>

<table cellpadding="2" cellspacing="0" border="0" width="530">
<tr>
    <td colspan="2" width="530">&nbsp;</td>
</tr>
    <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"database","&sub=backup")%>' method="post">
    <input type="hidden" name="backuptype" value="<%=jahiaBackupType%>">
<tr>
    <td width="100">&nbsp;</td>
    <td width="430">
        <table border="0">
        <tr>
            <td nowrap>
                <br><br>
                <font class="text"><fmt:message key="org.jahia.admin.database.ManageDatabase.backupName.label"/>&nbsp;:</font><br>
                <input class="input" type="text" name="backupname" size="<%=inputSize%>" maxlength="250" value="<%=jahiaBackupName%>">
                <br>&nbsp;<br>
                <font class="text"><fmt:message key="org.jahia.admin.database.ManageDatabase.backupDesc.label"/>&nbsp;:</font><br>
                <input class="input" type="text" name="backupdesc" size="<%=inputSize%>" maxlength="250" value="<%=jahiaBackupDesc%>">
            </td>
        </tr>
        <tr>
            <td align="right">
                &nbsp;<br>
                <%if(!isLynx){%>
                    <a href="javascript:document.jahiaAdmin.submit();" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('next','','${pageContext.request.contextPath}<fmt:message key="org.jahia.nextSteepOn.button"/>',1)"><img name="next" src="${pageContext.request.contextPath}<fmt:message key="org.jahia.nextSteepOff.button"/>" width="114" height="17" border="0"></a>
                <%}else{%>
                    &nbsp;<br>
                    <input type="submit" name="submit" value="<fmt:message key="org.jahia.admin.database.ManageDatabase.saveChanges.label"/> >>">
                <%}%>
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;<br><br>
                <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td nowrap width="145" valign="top"><font class="text"><b><fmt:message key="org.jahia.admin.otherOperations.label"/>&nbsp;:&nbsp;&nbsp;&nbsp;</b></font></td>
                    <td valign="top">
                        <font class="text">
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"database","&sub=display")%>'><fmt:message key="org.jahia.admin.previousStep.label"/></a><br>
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a><br>
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