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
<%@ taglib prefix="display-el" uri="http://displaytag.sf.net/el" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<% /*
     Standard properties for using display tag in Jahia administration with proper handling
     of resource bundles and locales
     
   */
%>
<display-el:setProperty name="locale.provider"
                     value="org.jahia.taglibs.html.controls.displaytag.AdminBundleProvider"/>
<display-el:setProperty name="locale.resolver"
                     value="org.jahia.taglibs.html.controls.displaytag.AdminBundleProvider"/>

<display-el:setProperty name="basic.msg.empty_list"><fmt:message key="net.sf.displaytag.basic.msg.empty_list"/></display-el:setProperty>
<display-el:setProperty name="basic.msg.empty_list_row"><fmt:message key="net.sf.displaytag.basic.msg.empty_list_row"/></display-el:setProperty>
<display-el:setProperty name="export.banner"><fmt:message key="net.sf.displaytag.export.banner"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.item_name"><fmt:message key="net.sf.displaytag.paging.banner.item_name"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.items_name"><fmt:message key="net.sf.displaytag.paging.banner.items_name"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.no_items_found"><fmt:message key="net.sf.displaytag.paging.banner.no_items_found"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.one_item_found"><fmt:message key="net.sf.displaytag.paging.banner.one_item_found"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.all_items_found"><fmt:message key="net.sf.displaytag.paging.banner.all_items_found"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.some_items_found"><fmt:message key="net.sf.displaytag.paging.banner.some_items_found"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.full"><fmt:message key="net.sf.displaytag.paging.banner.full"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.first"><fmt:message key="net.sf.displaytag.paging.banner.first"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.last"><fmt:message key="net.sf.displaytag.paging.banner.last"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.page.link"><fmt:message key="net.sf.displaytag.paging.banner.page.link"/></display-el:setProperty>
