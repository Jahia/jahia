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