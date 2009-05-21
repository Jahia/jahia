<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ include file="../../common/declarations.jspf" %>


<h3><fmt:message key='dateutil.calculateDates'/></h3>
<% java.text.SimpleDateFormat sf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");%>
<c:set var="now1" value="<%=sf.format(new java.util.Date())%>"/>

<p>
    <utility:dateUtil currentDate="${now1}" var="dateU" datePattern="dd.MM.yyyy HH:mm"/> <br/>
    <b><fmt:message key='dateutil.now'/>:</b> <c:out value="${dateU}"/> -
    <fmt:formatDate pattern="dd.MM.yyyy HH:MM" value="${dateU}"/></p>

<p><utility:dateUtil currentDate="${now1}" var="dateU1" datePattern="dd.MM.yyyy HH:mm" days="7"/> <br/>
    <b><fmt:message key='dateutil.week'/>:</b> <c:out value="${dateU1}"/>
    -
    <fmt:formatDate pattern="dd.MM.yyyy HH:MM" value="${dateU1}"/></p>

<p><utility:dateUtil currentDate="${now1}" var="dateU1" datePattern="dd.MM.yyyy HH:mm" months="1"/> <br/>
    <b><fmt:message key='dateutil.month'/>:</b> <c:out
            value="${dateU1}"/> -
    <fmt:formatDate pattern="dd.MM.yyyy HH:MM" value="${dateU1}"/></p>

<p><utility:dateUtil currentDate="${now1}" var="dateU1" datePattern="dd.MM.yyyy HH:mm" years="1"/> <br/>
    <b><fmt:message key='dateutil.year'/>:</b> <c:out value="${dateU1}"/>
    -
    <fmt:formatDate pattern="dd.MM.yyyy HH:MM" value="${dateU1}"/></p>

<p><utility:dateUtil currentDate="${now1}" var="dateU1" datePattern="dd.MM.yyyy HH:mm" years="-1"/> <br/>
    <b><fmt:message key='dateutil.lastyear'/>:</b><c:out
            value="${dateU1}"/> -
    <fmt:formatDate pattern="dd.MM.yyyy HH:MM" value="${dateU1}"/></p>

<p><utility:dateUtil currentDate="${now1}" var="dateU1" datePattern="dd.MM.yyyy HH:mm" years="1" hours="12"/>
    <br/>
    <b><fmt:message key='dateutil.yearhours'/>:</b> <c:out
            value="${dateU1}"/> -
    <fmt:formatDate pattern="dd.MM.yyyy HH:MM" value="${dateU1}"/></p>

<p><utility:dateUtil currentDate="${now1}" var="dateU1" datePattern="dd.MM.yyyy HH:mm" years="2" days="1"
                         months="1" hours="12" minutes="30" seconds="30"/> <br/>
    <b><fmt:message key='dateutil.complex'/>:</b>
    <br/> <c:out value="${dateU1}"/> -
    <fmt:formatDate pattern="dd.MM.yyyy HH:MM" value="${dateU1}"/></p>


