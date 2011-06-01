<%@page import   = "java.util.*" %>
<%@page import="org.jahia.bin.*"%>


<%

    //String      myVar            = (String)      request.getAttribute("myVar");

%>

<%@include file="/admin/include/header.inc"%>

<tr>
    <td align="right" class="text" colspan="2"><b>::&nbsp;&nbsp;<fmt:message key="org.jahia.engines.sitemap.SiteMap_Engines.siteMap.label"/>&nbsp;&nbsp;--&nbsp;&nbsp;::</b>&nbsp;&nbsp;&nbsp;&nbsp;</td>
</tr>
</table>
<br>&nbsp;<br>
<table cellpadding="2" cellspacing="0" border="0" width="530">
<tr>
    <td colspan="2" width="530">&nbsp;</td>
</tr>
    <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"processsitemap","")%>' method="post">
    <input type="hidden" name="myname" value="toto">
<tr>
    <td width="100">&nbsp;</td>
    <td width="430">
        <table border="0">
        <tr>
            <td>
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            <!--  CODE GOES HERE -->
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;<br>
                <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td nowrap width="145" valign="top"><font class="text"><b><fmt:message key="org.jahia.admin.otherOperations.label"/>&nbsp;:&nbsp;&nbsp;&nbsp;</b></font></td>
                    <td valign="top">
                        <font class="text">
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a><br>
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
        <br>&nbsp;<br>
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td align="left">
                    <font class="text2"><%=jahiaDisplayMessage%></font>
                </td>
                <td>&nbsp;&nbsp;&nbsp;</td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td colspan="2">&nbsp;</td>
</tr>
</table>
<%@include file="/admin/include/footer.inc"%>