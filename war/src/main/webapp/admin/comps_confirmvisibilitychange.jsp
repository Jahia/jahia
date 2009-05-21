<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@include file="/admin/include/header.inc"%>
<%@page import   = "java.util.*,org.jahia.data.applications.*"%>


<%

    String theURL = "";
    Iterator appsList = (Iterator)request.getAttribute("appsList");

    String requestURI = (String)request.getAttribute("requestURI");
    String contextRoot = (String)request.getContextPath();


%>


<script language="javascript">

        function sendForm()
        {
            document.mainForm.submit();
        }

</script>

<tr>
    <td align="center" class="text"><img name="component" src="<%=URL%>images/icons/admin/application.gif" width="48" height="48" border="0" align="middle"></td><td align="left" class="text"><h3><fmt:message key="org.jahia.admin.manageComponents.label"/></h3></td>
</tr>
</table>
<br>

<table cellpadding="2" cellspacing="0" border="0" width="100%">
<tr>
    <td colspan="2" width="530">&nbsp;</td>
</tr>
<tr>
    <td width="100">&nbsp;&nbsp;&nbsp;</td>
    <td width="100%">
        <table border="0" cellpadding="0" width="100%">
        <tr>
            <td>
                <font class="text"><br><br>
                <b><fmt:message key="org.jahia.admin.components.ManageComponents.confirmChangeStatus.label"/>&nbsp;:</b><br><br><br><br>
                </font>
            </td>
        </tr>
        <tr>
            <td width="100%">
                <form name="mainForm" action="<%=requestURI%>?do=components&sub=savevisibility" method="post">
                <table border="0" cellpadding="0" cellspacing="0" width="85%">
                <tr>
                    <td valign="top" align="left" class="text"><b><fmt:message key="org.jahia.admin.name.label"/></b></td>
                    <td	valign="top" align="right" class="text"><b><fmt:message key="org.jahia.admin.availableToUsers.label"/></b></td>
                </tr>
                <tr>
                    <td colspan="2"></td>
                </tr>
                <tr>
                    <td colspan="2" width="100%" height="2" background="${pageContext.request.contextPath}<fmt:message key="org.jahia.header.image"/>"><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image"/>" width="1" height="1"></td>
                </tr>
                <tr>
                    <td colspan="2"><br></td>
                </tr>
                <%
                    if ( appsList != null ){

                        ApplicationBean app = null;

                        while (appsList.hasNext()){
                            app = (ApplicationBean)appsList.next();
                            %>
                            <tr>
                                <td class="text" valign="top" nowrap><b><%=app.getName()%></b><br><br></td>
                                <td class="text" valign="top" align="right">
                                    <input type="hidden" name ="visible_status" value="<%=app.getID()%>">
                                    <%
                                    if (app.getVisibleStatus() == 1 ){
                                    %><b><fmt:message key="org.jahia.admin.no.label"/></b><%
                                    } else {
                                    %><b><fmt:message key="org.jahia.admin.yes.label"/></b><%
                                    } %>
                                </td>
                            </tr>
                            <%
                        }
                    } else {
                    %><tr><td colspan="2" class="text"><fmt:message key="org.jahia.admin.components.ManageComponents.noApplicationsFound.label"/></td></tr><%
                    }
                %>
                <tr>
                    <td colspan="2"><br><br><br><br></td>
                </tr>
                <tr>
                    <td align="right" colspan="2">
                    &nbsp;<br>
                        <a href="<%=requestURI%>?do=components&sub=display" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Cancel','','${pageContext.request.contextPath}<fmt:message key="org.jahia.cancelOn.button"/>',1)"><img name="Cancel" src="${pageContext.request.contextPath}<fmt:message key="org.jahia.cancelOff.button"/>" width="69" height="17" border="0" alt="<fmt:message key="org.jahia.admin.cancel.label"/>"></a>
                        <a href="javascript:document.mainForm.submit();" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('save','','${pageContext.request.contextPath}<fmt:message key="org.jahia.saveOn.button"/>',1)"><img name="save" src="${pageContext.request.contextPath}<fmt:message key="org.jahia.saveOff.button"/>" border="0" alt="<fmt:message key="org.jahia.admin.save.label"/>"></a>
                    </td>
                </tr>
                <tr>
                    <td colspan="2"><br></td>
                </tr>
                </table>
                </form>
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