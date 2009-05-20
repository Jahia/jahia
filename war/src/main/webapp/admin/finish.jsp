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
<%@page import="org.jahia.bin.*"%>
<%
    String processMessage                = (String) request.getAttribute("processMessage");
%>

<%@include file="/admin/include/header.inc"%>

<tr>
    <td align="left" class="text" colspan="2"><h3><fmt:message key="org.jahia.bin.JahiaAdministration.confirmation.label"/></h3></td>
</tr>
</table>
&nbsp;<br>&nbsp;<br>

<table cellpadding="2" cellspacing="0" border="0" width="530">
<tr>
    <td colspan="2" width="530">&nbsp;</td>
</tr>
<tr>
    <td width="100">&nbsp;</td>
    <td width="430">
        <font class="text"><%=processMessage%></font>
        &nbsp;<br>&nbsp;<br>
        <%if(!isLynx){%>
         <div class="buttonList" style="padding-top: 8px; padding-bottom: 8px">
          <div class="button" title="<fmt:message key='org.jahia.admin.users.ManageUsers.applyModif.label'/>">
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>" ><fmt:message key="org.jahia.admin.ok.label"/></a>
          </div>

        </div>
        <%}else{%>
            &nbsp;&nbsp;<a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.bin.JahiaAdministration.ok.label"/> >></a>
        <%}%>

    </td>

</tr>


</table>

<%@include file="/admin/include/footer.inc"%>