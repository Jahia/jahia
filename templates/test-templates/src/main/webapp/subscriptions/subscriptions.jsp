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
<%@ include file="../common/declarations.jspf"%>


<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.jahia.data.beans.JahiaBean"%>
<%@page import="org.jahia.registries.ServicesRegistry"%>
<%@page import="org.jahia.utils.LanguageCodeConverters"%>
<%@page import="org.jahia.services.notification.Subscription"%>
<%@page import="org.jahia.services.notification.SubscriptionService"%>
<%@page import="org.jahia.services.notification.templates.TemplateUtils"%>
<%@page import="org.jahia.services.notification.NotificationEvent"%>

<h3>Simple subscription management</h3>
<%--
    We use 'testSubscriptionEvent' as the event name and site's home page
    as the source object for the sake of simplicity.  
--%>
<%
  String event = "testSubscriptionEvent";
  JahiaBean jBean = (JahiaBean) request.getAttribute("jahia");
  String objectKey = jBean.getSite().getHomeContentPage().getObjectKey().getKey();
  SubscriptionService service = SubscriptionService.getInstance();
  
  
  String action = request.getParameter("action");
  if (action != null) {
      if ("add".equals(action)) {
          // add new
          String newSubscriber = request.getParameter("newSubscriber");
          if (newSubscriber != null && newSubscriber.length() > 0) {
              // registered user
              service.subscribe(objectKey, true, event, newSubscriber, jBean.getSite().getId());
          } else {
              // unregistered user
              Map<String, String> properties = new HashMap<String, String>();
              //properties.put("email", request.getParameter("username"));
              properties.put("firstname", request.getParameter("firstname"));
              properties.put("lastname", request.getParameter("lastname"));
              properties.put("preferredLanguage", request.getParameter("preferredLanguage"));
              properties.put("comment", request.getParameter("comment"));
              service.subscribe(objectKey, true, event, request.getParameter("username"), false, jBean.getSite().getId(), true, properties);
          }
      } else if ("suspend".equals(action)) {
          // suspend
          String subscriptionId = request.getParameter("subscription");
          service.suspendSubscription(Integer.valueOf(subscriptionId));
      } else if ("resume".equals(action)) {
          // suspend
          String subscriptionId = request.getParameter("subscription");
          service.resumeSubscription(Integer.valueOf(subscriptionId));
      } else if ("delete".equals(action)) {
          // delete
          String subscriptionId = request.getParameter("subscription");
          service.unsubscribe(Integer.valueOf(subscriptionId));
      } else if ("notify".equals(action)) {
          // fire an event
            NotificationEvent evt = new NotificationEvent(jBean.getSite().getHomeContentPage().getObjectKey().getKey(), event);
            evt.setSiteId(jBean.getSite().getId());
            evt.setPageId(jBean.getSite().getHomepageID());
            ServicesRegistry.getInstance().getJahiaEventService().fireNotification(evt);
      }
  }
  
  Subscription searchPattern = new Subscription();
  searchPattern.setObjectKey(objectKey);
  searchPattern.setEventType(event);
  searchPattern.setSiteId(jBean.getSite().getId());
  
  pageContext.setAttribute("subscriptions", service.getSubscriptionsByCriteria(searchPattern, "includeChildren", "userRegistered", "enabled", "suspended"));
%>
<script type="text/javascript">
function doIt(action, subscription) {
    document.getElementById('action').value = action;
    document.getElementById('subscription').value = subscription;
    document.subscriptionForm.submit();
}
</script>
<template:jahiaPageForm name="subscriptionForm">
<input type="hidden" name="action" id="action" value=""/>
<input type="hidden" name="subscription" id="subscription" value=""/>
<table border="1" width="95%">
    <caption>${fn:length(subscriptions)} subsription(s) found</caption>
    <thead>
        <tr>
            <th>#</th>
            <th>User</th>
            <th>Profile</th>
            <th>E-mail</th>
            <th>Suspended</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
<c:forEach items="${subscriptions}" var="subscription" varStatus="status">
    <% pageContext.setAttribute("subscriber", TemplateUtils.getSubscriber((Subscription) pageContext.getAttribute("subscription"))); %>
        <tr>
            <td>${status.count}</td>
            <td>
                ${subscription.username}<br/>
                ${subscriber.userProperties.properties.firstname}&nbsp;${subscriber.userProperties.properties.lastname} 
            </td>
            <td>
                <c:if test="${subscription.userRegistered}">
                    <a href="<c:url value='/administration/?do=users&sub=edit&searchString=&searchIn=allProps&storedOn=providers&selectedUsers=u{jahia_db}${subscription.username}'/>" target="_blank">profile</a>
                </c:if>
                <c:if test="${not subscription.userRegistered}">
                    unregistered
                </c:if>
            </td>
            <td>${not empty subscriber.userProperties.properties.email ? subscriber.userProperties.properties.email : (not subscription.userRegistered ? subscription.username : '')}</td>
            <td>${subscription.suspended ? 'yes' : 'no'}</td>
            <c:set var="suspendAction" value="${subscription.suspended ? 'resume' : 'suspend'}"/>
            <td><button type="button" onclick="doIt('delete', '${subscription.id}')">delete</button><br/><button type="button" onclick="doIt('${suspendAction}', '${subscription.id}')">${suspendAction}</button></td>
        </tr>
</c:forEach>        
    </tbody>
</table>
    
<fieldset>
    <legend>Add subscription for a registered user:</legend>
    <input name="newSubscriber" id="newSubscriber" readonly="readonly"/>&nbsp;<ui:userGroupSelector mode="users" fieldId="newSubscriber"/>
    &nbsp;<button type="button" onclick="doIt('add')">Subscribe</button>
</fieldset>

<fieldset>
    <legend>Add subscription for an unregistered user:</legend>
    E-mail: <input name="username" value=""/><br/> 
    Firstname: <input name="firstname" value=""/><br/> 
    Lastname: <input name="lastname" value=""/><br/>
    <% pageContext.setAttribute("locales", LanguageCodeConverters.getAvailableBundleLocalesSorted(jBean.getProcessingContext().getLocale())); %>    
    Preferred language:
    <select name="preferredLanguage">
        <c:forEach items="${locales}" var="locale">
            <option value="${locale}">${locale.displayName}</option>
        </c:forEach>
    </select><br/> 
    Comment: <input name="comment" value=""/><br/>
    <button type="button" onclick="doIt('add')">Subscribe</button>
</fieldset>

<fieldset>
    <legend>Trigger notification:</legend>
    <button type="button" onclick="doIt('notify')">Fire an event and notify subscribers</button>
</fieldset>
</template:jahiaPageForm>