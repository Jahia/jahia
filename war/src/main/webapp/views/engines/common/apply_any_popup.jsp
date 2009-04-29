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
<%@ page language="java" %>

<%@include file="/views/engines/common/taglibs.jsp" %>

<logic:notPresent name="engines.apply.new-url" >
	<% request.setAttribute("engines.apply.new-url",""); %>
</logic:notPresent>
<logic:notPresent name="engines.apply.opener-url-params" >
	<% request.setAttribute("engines.apply.opener-url-params",""); %>
</logic:notPresent>
<logic:notPresent name="engines.apply.refresh-opener" >
	<% request.setAttribute("engines.apply.refresh-opener","yes"); %>
</logic:notPresent>

<script language="javascript">
	applyPopupWindow("<bean:write filter="false" name="engines.apply.new-url" />","<bean:write filter="false" name="engines.apply.opener-url-params" />","<bean:write filter="false" name="engines.apply.refresh-opener" />");
</script>
