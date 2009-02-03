<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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

<display-el:setProperty name="basic.msg.empty_list"><internal:adminResourceBundle resourceName="net.sf.displaytag.basic.msg.empty_list"/></display-el:setProperty>
<display-el:setProperty name="basic.msg.empty_list_row"><internal:adminResourceBundle resourceName="net.sf.displaytag.basic.msg.empty_list_row"/></display-el:setProperty>
<display-el:setProperty name="export.banner"><internal:adminResourceBundle resourceName="net.sf.displaytag.export.banner"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.item_name"><internal:adminResourceBundle resourceName="net.sf.displaytag.paging.banner.item_name"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.items_name"><internal:adminResourceBundle resourceName="net.sf.displaytag.paging.banner.items_name"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.no_items_found"><internal:adminResourceBundle resourceName="net.sf.displaytag.paging.banner.no_items_found"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.one_item_found"><internal:adminResourceBundle resourceName="net.sf.displaytag.paging.banner.one_item_found"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.all_items_found"><internal:adminResourceBundle resourceName="net.sf.displaytag.paging.banner.all_items_found"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.some_items_found"><internal:adminResourceBundle resourceName="net.sf.displaytag.paging.banner.some_items_found"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.full"><internal:adminResourceBundle resourceName="net.sf.displaytag.paging.banner.full"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.first"><internal:adminResourceBundle resourceName="net.sf.displaytag.paging.banner.first"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.last"><internal:adminResourceBundle resourceName="net.sf.displaytag.paging.banner.last"/></display-el:setProperty>
<display-el:setProperty name="paging.banner.page.link"><internal:adminResourceBundle resourceName="net.sf.displaytag.paging.banner.page.link"/></display-el:setProperty>
