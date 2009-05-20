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
<%@page import = "java.util.*"%>
<%@page import="org.jahia.bin.*"%>

<%
    Integer      jahiaBackupCount  =  (Integer)     request.getAttribute("jahiaBackupCount");
    Iterator  backupsList       =  (Iterator) request.getAttribute("jahiaBackupList");
%>

<%@include file="/admin/include/header.inc"%>

<tr>
    <td align="center" class="text"><img name="db" src="<%=URL%>images/icons/admin/data.gif" width="48" height="48" border="0" align="middle"></td><td align="left" class="text"><h3><fmt:message key="org.jahia.admin.database.ManageDatabase.manageBackups.label"/></h3>&nbsp;&nbsp;&nbsp;&nbsp;</td>
</tr>
</table>
<br><br>

<table cellpadding="2" cellspacing="0" border="0" width="530">
<tr>
    <td colspan="2" width="530">&nbsp;</td>
</tr>
<tr>
    <td width="40">&nbsp;</td>
    <td width="490">
        <table border="0">
        <tr>
            <td nowrap>
                <br><br>
                <% if(jahiaBackupCount.intValue() > 0) { %>
                    <table border="0" cellpadding="0" cellspacing="2">
                        <%
                            while(backupsList.hasNext()) {
                                Map backupHash = (Map) backupsList.next();
                            %>
                    <tr>
                        <td class="text" valign="top"><li></td>
                        <td class="text" valign="top"><%=backupHash.get("backup.name")%></td>
                        <td width="15">&nbsp;</td>
                        <td class="text" nowrap valign="top"><font size="1">[<%=backupHash.get("backup.date")%>, <%=backupHash.get("backup.type")%>, b=<%=backupHash.get("backup.build")%>, rel=<%=backupHash.get("backup.release")%>]</font></td>
                        <td width="15">&nbsp;</td>
                        <td class="text" valign="top"><a href='<%=JahiaAdministration.composeActionURL(request,response,"database","&sub=restore&epoch=" + backupHash.get("backup.epoch") + "&build=" + backupHash.get("backup.build") + "&release=" + backupHash.get("backup.release"))%>' border="0"><font size="1"><fmt:message key="org.jahia.admin.restore.label"/></font></a></td>
                        <td width="10">&nbsp;</td>
                        <td class="text" valign="top"><a href='<%=JahiaAdministration.composeActionURL(request,response,"database","&sub=flush&epoch=" + backupHash.get("backup.epoch"))%>' border="0"><font size="1"><fmt:message key="org.jahia.admin.delete.label"/></font></a></td>
                    </tr>
                    <tr>
                        <td><font size="5">&nbsp;</font></td>
                        <td class="text" valign="top" colspan="5"><%=backupHash.get("backup.desc")%></td>
                    </tr>
                            <%
                            }
                        %>
                    </table>
                <% } else { %>
                    <font class="text"><fmt:message key="org.jahia.admin.database.ManageDatabase.noBackup.label"/></font>
                <% } %>
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
<tr>
    <td colspan="2" align="right">
        <table border="0" width="100%"><tr><td width="48"><img name="logo" src="../css/images/logo/logo-jahia.gif" border="0" width="45" height="34"></td><td><img src="<%=URL%>images/pix.gif" border="0" width="1" height="10">
<div id="copyright"><%=copyright%></div><span class="version">Jahia <%=Jahia.VERSION%>.<%=Jahia.getPatchNumber()%> r<%=Jahia.getBuildNumber()%></span>
</td></tr></table>
    </td>
</tr>

</table>

<%@include file="/admin/include/footer.inc"%>