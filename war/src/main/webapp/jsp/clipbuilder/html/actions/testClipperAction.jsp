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

<%@taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="http://struts.apache.org/tags-nested" prefix="nested"%>
<%@taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@page import="org.jahia.clipbuilder.html.bean.*"%>
<%@page import="org.jahia.clipbuilder.html.*"%>
<%@page import="java.util.*"%>
<!-- javaScript-->
<script language="javascript" type="">
  // set the value of webClippinAction parameter
  function setAction(f, value) {
    if (value == "changeView") {
      f.action = '<%=response.encodeURL(request.getContextPath()+"/administration/testClipper?do=clipbuilder&webClippingAction=changeView")%>';
    }

    return;
  }

  //set action attribute of the given form
  function setActionAndSubmit(f, value) {
    setAction(f, value);
    f.submit();

    return;
  }
</script><!-- Main -->
<table class="principal" width="100%">
  <html:form action="testClipper?do=clipbuilder" method="post">
    <!-- Header -->
    <table align="center" width="100%">
      <!-- Title -->
      <table align="center" width="100%">
        <tr>
          <td>
            <bean:message key="testClipper.title"/>
          </td>
          <%/*!-- Help -->
          <td style="text-align: right">
            <a href="popitup('=response.encodeURL(request.getContextPath()+"/jsp/clipbuilder/html/help/testHelp.jsp")')">
              <bean:message key="wizard.help"/>
            </a>
          </td>
        </tr>*/%>
      </table>
      <!-- Buttons -->
      <table align="center" class="principal" width="100%">
        <table align="center" width="100%">
          <tr class="topmenubuttons">
            <td class="topmenubuttons">
              <html:submit styleClass="fancyButton" property="webClippingAction">
                <bean:message key="testClipper.button.go"/>
              </html:submit>
            </td>
          </tr>
        </table>
        <!-- Error-->
        <logic:messagesPresent>
          <table align="center" bgcolor="#800000" cellpadding="0" cellspacing="1" width="100%">
            <tbody>
              <tr>
                <td>
                  <table align="center" bgcolor="#ffffff" border="0" width="100%">
                    <tbody>
                      <tr>
                        <td align="center" bgcolor="#dcdcdc">
                          <font color="#cc0000">
                            <bean:message key="error.error"/>
                          </font>
                        </td>
                      </tr>
                      <tr>
                        <td>
                          <UL>
                            <li> <bean:message key="testClipper.error.parameter"/> </li>
                            <html:messages id="error">
                              <LI> <bean:write name="error"/> </LI>
                            </html:messages>
                          </UL>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </td>
              </tr>
            </tbody>
          </table>
        </logic:messagesPresent>
        <!-- Edit Information -->
      <%
        List urlListBean = SessionManager.getClipperBean(request).getUrlListBean();
        if (urlListBean.size() > 1) {
      %>
        <table>
          <tr valign="middle">
            <td>
              <bean:message key="testClipper.select.url"/>
              <html:select name="testClipperForm" property="selectedUrl" onchange="setActionAndSubmit(this.form,'changeView')">
                <!-- Get all the recorded url-->
              <%
                // strus bug
                String posStrg = request.getParameter("selectedUrl");
                if (posStrg == null) {
                  posStrg = "0";
                }
                for (int i = 0; i < urlListBean.size() - 1; i++) {
                  if (!posStrg.equalsIgnoreCase(String.valueOf(i))) {
              %>
                <option value="<%=i%>"><%=((UrlBean) urlListBean.get(i)).getLabelForStrut()%>                </option>
              <%} else { //if              %>
                <option selected value="<%=i%>"><%=((UrlBean) urlListBean.get(i)).getLabelForStrut()%>                </option>
              <%
                } //else
                    } //for
              %>
                %>
              </html:select>
              <html:checkbox property="resetCache" value="true">
                <bean:message key="testClipper.resetCache"/>
              </html:checkbox>
            </td>
          </tr>
        </table>
        <!-- Main -->
        <table width="100%">
          <!-- Edit Form parameters in tabled view -->
          <tr>
            <!-- All parameter-->
            <td>
              <table width="100%">
                <!-- Labels-->
                <tr>
                  <td class="topmenugreen bold waBG">
                    <bean:message key="testClipper.parameter"/>
                  </td>
                  <td class="topmenugreen bold waBG">
                    <bean:message key="testClipper.value"/>
                  </td>
                </tr>
                <logic:iterate name="testClipperForm" property="actifFormParamsList" id="currentParam">
                  <!-- visible parameter-->
                  <logic:equal name="currentParam" property="visibility" value="true">
                    <!-- new parameter-->
                    <tr>
                      <!-- Mapping -->
                      <td class="leftlevel2 waInput">
                        <bean:write name="currentParam" property="mapping"/>
                      </td>
                      <!-- Values-->
                      <logic:notEqual name="currentParam" property="type" value="password">
                        <td class="leftlevel2 waInput">
                          <html:text name="currentParam" property="usedValue"/>
                        </td>
                      </logic:notEqual>
                      <logic:equal name="currentParam" property="type" value="password">
                        <td class="leftlevel2 waInput">
                          <html:password name="currentParam" property="usedValue"/>
                        </td>
                      </logic:equal>
                    </tr>
                  </logic:equal>
                  <!-- hidden parameter-->
                  <logic:notEqual name="currentParam" property="visibility" value="true">
                    <!-- new parameter-->
                    <!-- make the input hidden-->
                    <html:hidden name="currentParam" property="usedValue"/>
                  </logic:notEqual>
                </logic:iterate>
              </table>


        </table>
      <%} else {      %>
        <table align="center" bgcolor="#800000" cellpadding="0" cellspacing="1" width="100%">
          <tbody>
            <tr>
              <td>
                <table align="center" bgcolor="#ffffff" border="0" width="100%">
                  <tbody>
                    <tr>
                      <td align="center" bgcolor="#dcdcdc">
                        <font color="#cc0000">
                          <bean:message key="error.warning"/>
                        </font>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <UL>
                          <LI> <bean:message key="information.noParameters"/> </LI>
                        </UL>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </td>
            </tr>
          </tbody>
        </table>
      <%}      %>


  </html:form>
</table>
</table>
