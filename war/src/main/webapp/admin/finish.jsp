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
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>" ><fmt:message key="label.ok"/></a>
          </div>

        </div>
        <%}else{%>
            &nbsp;&nbsp;<a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.bin.JahiaAdministration.ok.label"/> >></a>
        <%}%>

    </td>

</tr>


</table>

<%@include file="/admin/include/footer.inc"%>