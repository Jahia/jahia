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
<%@ page language="java" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.data.applications.ApplicationBean" %>
<%@ page import="org.jahia.engines.applications.Application_Engine" %>
<%@ page import="org.jahia.data.applications.EntryPointDefinition" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.commons.collections.FastHashMap" %>
<%@ page import="org.apache.pluto.descriptors.portlet.UserAttributeDD" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String warningMsg = (String) request.getAttribute("Template_Engine.warningMsg");

    final ApplicationBean theTempoApplicationBean = (ApplicationBean) engineMap.get(Application_Engine.TEMPORARY_APPLICATION_SESSION_NAME);

    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
    final String fieldForm = (String) engineMap.get(fieldsEditCallingEngineName + "." + "fieldForm");
    final String logForm = (String) engineMap.get("logForm");
    final String engineUrl = (String) engineMap.get("engineUrl");
    final String theScreen = (String) engineMap.get("screen");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final JahiaData jData = (JahiaData) jParams.getRequest().getAttribute("org.jahia.data.JahiaData");

    final int inputSize = jData.gui().isIE() ? 65 : 40;
    final boolean showEditMenu = (theScreen.equals("edit") || theScreen.equals("rightsMgmt"));
    request.setAttribute("showEditMenu", new Boolean(showEditMenu));
%>

<script type="text/javascript">
    <!--
    function setVisible(who) {

        if (who.checked) {
            document.mainForm.templateAvailable.checked = true;
        }
    }
    //-->
</script>
<div id="header">
  <h1>Jahia</h1>
  <h2><internal:message key="org.jahia.engines.application.applicationSettings.label"/></h2>
</div>
<div id="mainContent">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <div class="dex-TabBar">
            <jsp:include page="../menuBar.jsp" flush="true" />
          </div>
        </td>
        <td style="vertical-align: top;" align="right" nowrap="nowrap">
          <jsp:include page="../multilanguage_links.jsp" flush="true" />
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%" colspan="2">
        <% if (theScreen.equals("edit")) { %>
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
              <% if (showEditMenu) { %>
                <%@ include file="../menu.inc" %>
              <% } else { %>
                <%@ include file="../tools.inc" %>
              <% } %>
              <div id="content" class="fit w2">
                <% if (!warningMsg.equals("")) { %>
                  <p class="errorbold">
                    <fmt:message key="org.jahia.warning.label"/>
                  </p>
                  <p class="error"><%=warningMsg%></p>
                <% } %>
                <div class="head">
                  <div class="object-title">
                    <a href="#informations"><fmt:message key="org.jahia.engines.application.portlets.informations"/></a>
                  </div>
                </div>
                <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
                  <tr>
                    <th>
                      <fmt:message key="org.jahia.engines.name.label"/>
                    </th>
                    <td>
                      <%=theTempoApplicationBean.getName()%>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <fmt:message key="org.jahia.engines.application.context.label"/>
                    </th>
                    <td>
                      <%=theTempoApplicationBean.getContext()%>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <fmt:message key="org.jahia.engines.application.description.label"/>
                    </th>
                    <td>
                      <input type="text" name="applicationDescription" value="<%=theTempoApplicationBean.getdesc()%>" size="<%=inputSize%>">
                    </td>
                  </tr>
                </table>
                <div class="head">
                  <div class="object-title">
                    <% final boolean isPortlet = theTempoApplicationBean.getType().equals("portlet");
                        if (isPortlet) { %>
                      <fmt:message key="org.jahia.engines.application.portlets.label"/>
                    <% } else { %>
                      <fmt:message key="org.jahia.engines.application.entries.label"/>
                    <% } %>
                  </div>
                </div>
                <!-- table portlet -->
                <%@ include file="/engines/application/application_displaytag.inc" %>
                <%if (theTempoApplicationBean.getUserAttributes() != null) {%>
                <div class="head">
                  <div class="object-title">
                    <fmt:message key="org.jahia.engines.application.portlets.user.attributes.label"/>
                  </div>
                </div>
                <!-- table user mapping (only for portlet JSR168)-->
                <table style="display:none" class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                  <tbody>                 
                      <tr>
                        <td colspan="2">
                            <table>
                              <%
                              Collection userAttributes = theTempoApplicationBean.getUserAttributes();
                              Map jahiaUserPreferencesNames = jParams.getUser().getProperties();
                              Collection userAttributeRefs = new ArrayList();
                              // Build Map for fatser retrieving
                              Iterator userIterator = userAttributes.iterator();
                              Map alreadyExistingMapping = new FastHashMap(userAttributes.size());
                              while (userIterator.hasNext()) {
                                  UserAttributeDD userAttribute = (UserAttributeDD) userIterator.next();
                                  Iterator userRefsIterator = userAttributeRefs.iterator();
                                  while (userRefsIterator.hasNext()) {
                                      /** todo implement mapping */
                                  /* here is the old Jetspeed mapping code
                                      UserAttributeRef userAttributeRef = (UserAttributeRef) userRefsIterator.next();
                                      if (userAttribute.getName().equals(userAttributeRef.getNameLink())) {
                                          alreadyExistingMapping.put(userAttribute.getName(), userAttributeRef.getName());
                                      }
                                  */
                                  }
                              }
                              %>
                              <tr>
                                <th colspan="2" align="center">
                                  <fmt:message key="org.jahia.engines.application.portlets.user.attributes.jahia.prefs.labels"/>
                                </th>
                              </tr>

                              <tr>
                                <th>
                                  <fmt:message key="org.jahia.engines.application.portlets.user.attributes.jahia.prefs.labels.keyname"/>
                                </th>
                                <th>
                                  <fmt:message key="org.jahia.engines.application.portlets.user.attributes.jahia.prefs.labels.value"/>
                                </th>
                              </tr>
                              <%
                              Iterator iterator = jahiaUserPreferencesNames.entrySet().iterator();
                              while (iterator.hasNext()) {
                                  Map.Entry entry = (Map.Entry) iterator.next();
                              %>
                                <tr>
                                    <td><%=entry.getKey()%>
                                    </td>
                                    <td><%=entry.getValue()%>
                                    </td>
                                </tr>
                              <%}%>
                            </table>
                            <table>
                              <%
                              iterator = userAttributes.iterator();
                              while (iterator.hasNext()) {
                                UserAttributeDD attribute = (UserAttributeDD) iterator.next();
                                String name = attribute.getName();
                                String existingMapping = (String) (alreadyExistingMapping.get(name) != null ? alreadyExistingMapping.get(name) : "");
                                %>
                                <tr>
                                  <td title="<%=attribute.getDescription()%>">
                                    <%=name%>
                                  </td>
                                  <td>
                                    <input name="portlet_ua_<%=name%>" value="<%=existingMapping%>">
                                  </td>
                                </tr>
                              <%}%>
                            </table>
                        </td>
                      </tr>
                    <%}%>          
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        <% } else if (theScreen.equals("rightsMgmt")) { %>
          <%=fieldForm%>
        <% } else if (theScreen.equals("logs")) { %>
          <%=logForm%>
        <% } %>
        </td>
      </tr>
    </tbody>
  </table>
  <jsp:include page="../buttons.jsp" flush="true" />
</div>
