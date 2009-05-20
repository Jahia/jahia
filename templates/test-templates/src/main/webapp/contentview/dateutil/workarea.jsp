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


