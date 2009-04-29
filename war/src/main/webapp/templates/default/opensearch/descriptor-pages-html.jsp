<%@ page contentType="application/opensearchdescription+xml;charset=UTF-8" %><%
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
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, proxy-revalidate, max-age=0");
    response.setHeader("Pragma", "No-Cache");
    response.setDateHeader("Expires", 295075800000L);
%><?xml version="1.0" encoding="UTF-8"?>
<!--

    
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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
<c:set var="title" value="${h:default(param.title, 'Jahia content')}"/>
    <ShortName>${fn:escapeXml(title)}</ShortName>
    <Description>${fn:escapeXml(param.description)}</Description>
    <InputEncoding>UTF-8</InputEncoding>
    <Image width="16" height="16">data:image/gif,GIF89a%1E%00%1E%00%F7S%00%FF%FF%FF%FD%F6%F8%F8%F6%F8%FB%F0%F2%F4%F2%F5%F1%F0%F3%EF%EE%F1%EA%EB%EE%EB%EA%EC%E8%E8%EB%E4%E4%E6%DF%E0%E4%DE%DD%E0%F2%D7%D8%D8%D8%DC%CE%CF%D3%CC%CC%CC%E8%BE%BF%C4%C4%C8%BD%BF%C3%B7%BB%BF%B4%B7%BB%E0%A7%A7%AF%B3%B6%AB%AF%B4%A7%AD%B3%DD%A0%9F%A7%AB%AF%A3%A7%AC%DB%99%98%D9%91%91%9C%A1%A6%9B%9F%A3%D6%8D%8D%97%9E%A3%D3%82%81%8D%94%99%D3%81%7F%8B%91%96%89%8F%95%86%8C%92%85%8B%90%80%85%8B%7B%82%88%7B%80%86y%7F%86t%7B%82qx%7FmszbjrZclT%60hV_fIRZEOVAKR%B3%2B(%B2'%248BK%B1%22%20%B0!%1E%AF%1F%1B2%3EG%2F%3BC%AD%19%16%2C9B)4%3E'2%3A%220%3A%1E-8%1A(2%19%24.%15!*%12%1F(%0F%1D'%0F%1B%24%0A%18%22%06%14%1E%02%10%1A%00%0B%15%00%09%14%00%04%0F%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00%00!%F9%04%01%00%00S%00%2C%00%00%00%00%1E%00%1E%00%00%08%DF%00%01%08%1CH%B0%A0%C1%83%08%13*%5C%C8%B0%A1%C3%87%10%23J%9CH%B1%A2E%83%1AB%5C%24h!G%0E%0F%0166%C0A%12H%84%85%02j%400Hb%06%C1%1C%40%1A%1E%F8%97%C2%A0%8E%26%04%3B%94p%C8%C1%01%80%05.%5C%08%ACq%04%C4%8B%90%1CH%084%F1%C2%E7A%01N.%3C%20bd%09%06%005%90T%CD%00%C0%08%11%00%22%92%1C%F1%C1%E0%E9%92%0A%00%0C%88%88%D1%02%2B%12%00%1F%84%0E%F9!%10%03%89%24h%0D%0A8%CB%C0%C6%90%20m%B3%82%95K%B7%C2%92%1FG%F2%16%DCK%E1%C5%93%03%2Cb%04%A8%91d0%80%B9%05t%60P%C0%E4%83%D9%0A%14%BC%0A%19%B2%E0%06N%120%00%1C%19%02%20%C5%11%22Jh%1CT%90d%E5F%81'b%08%B8%CD%BB%B7%EF%DF%C0%83%0B%1F.%3C%20%00%3B</Image>
    <c:url var="url" value="${currentSite.externalUrl}?src_mode=pages&src_terms[0].fields.content=true&src_terms[0].term={searchTerms}">
        <c:param name="template" value="${jahia.includes.templatePath['/opensearch/resultsHtml.jsp']}"/>
    </c:url>
    <Url type="text/html" template="${fn:escapeXml(url)}" title="${fn:escapeXml(title)}" />
</OpenSearchDescription>