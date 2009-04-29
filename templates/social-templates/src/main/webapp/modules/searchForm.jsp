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
<%@ include file="../common/declarations.jspf" %>
<div id="search">
    <h3><label for="search"><fmt:message key="blog.search"/> </label></h3>

    <form method="get" action="">
        <fieldset>
            <p>
                <input type="text" value="" name="search" class="search" tabindex="4"/>
                <input type="submit" value="GO" class="gobutton" tabindex="5"/>
            </p>
        </fieldset>
    </form>
</div>