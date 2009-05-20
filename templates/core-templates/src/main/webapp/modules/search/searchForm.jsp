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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../../common/declarations.jspf" %>
<%@taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%-- search --%>
<div class="searchform">
    <s:form>
        <fieldset>
            <legend>&nbsp;<fmt:message key='search'/>&nbsp;</legend>
            <p><s:term class="field"/></p>
            <p><input type="submit" name="submit" class="button" value="<fmt:message key='search'/>"/></p>
        </fieldset>
    </s:form>
</div>