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
<%@ tag body-content="empty" description="Displays a button to subscribe to a RSS feed" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ attribute name="targetURL" required="true" rtexprvalue="true" %>

<a title="<utility:resourceBundle resourceName="rssFeed" defaultValue="Subscribe to the RSS feed"/>"
   href="${targetURL}">
    <img title="<utility:resourceBundle resourceName="rssFeed" defaultValue="Subscribe to the RSS feed"/>"
         src="${pageContext.request.contextPath}/jsp/jahia/css/images/icones/rss_small.gif" alt="RSS"/>
</a>
