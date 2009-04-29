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