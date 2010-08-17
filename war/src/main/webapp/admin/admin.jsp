<%@include file="/admin/include/header.inc"%>
<%@page import = "java.util.*"%>
<%@page import = "org.jahia.bin.Jahia"%>

<%
    // String contenttypestr = (String) request.getAttribute("content-type");
    // response.setContentType(contenttypestr);

    String jspSource = (String)request.getAttribute("jspSource");
    String directMenu = null;  //(String)request.getAttribute("directMenu"); // Optional parameter
%>

<!----- - - -  -   -    -     header ends here     -    -   -  - - - ----->

<!-- jsp:include page="<%=directMenu%>" flush="true"/ -->
<jsp:include page="<%=jspSource%>" flush="true"/>
  
<!----- - - -  -   -    -    footer starts here    -    -   -  - - - ----->

<%@include file="/admin/include/footer.inc"%>