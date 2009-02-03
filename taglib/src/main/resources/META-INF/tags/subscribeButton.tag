<%-- 
/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
--%>
<%@ tag body-content="empty"
        description="Renders subscribe/unsubscribe link, which allows current user to subscribe/unsubscribe to the notifications about changes in the content object."
        %>
<%@ attribute name="source" required="true" type="java.lang.String"
              description="The key of the content object to observe for changes."
        %>
<%@ attribute name="event" required="false" type="java.lang.String"
              description="The event type to catch. If not specified, the contentPublished event is used."
        %>
<%@ attribute name="user" required="false" type="java.lang.String"
              description="The username to use for a subscription. If not specified, the current user is used."
        %>
<%@ attribute name="confirmationRequired" required="false" type="java.lang.Boolean"
              description="Does the subscription require confirmation (an e-mail with the confirmation link will be sent first)?"
        %>
<%@ tag dynamic-attributes="attributes"
        %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
        %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"
        %>
<c:set target="${attributes}" property="jahiatype" value="subscription"/>
<c:set var="elementId" value="subscription_${source}"/>
<c:set target="${attributes}" property="id" value="${h:default(id, elementId)}"/>
<c:set target="${attributes}" property="source" value="${source}"/>
<c:set target="${attributes}" property="event" value="${h:default(event, 'contentPublished')}"/>
<c:set target="${attributes}" property="user" value="${h:default(user, jahia.user.username)}"/>
<c:set target="${attributes}" property="confirmationRequired" value="${h:default(confirmationRequired, 'false')}"/>
<span ${h:attributes(attributes)}></span>