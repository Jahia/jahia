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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<jsp:useBean id="method" class="java.lang.String" scope="request"/>
</div>
<div id="buttons">
  <% if (! jsp.equals("welcome.jsp")) { %>
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-nav-previous" href="#previous" onclick="javascript:{submitFormular('<%=method%>','back'); return false;}" title="<fmt:message key="org.jahia.back.button"/>"><fmt:message key="org.jahia.back.button"/></a>
    </span>
  </span>
  <% } %>
  <% if (! jsp.equals("congratulations.jsp") && ! jsp.equals("values.jsp") && ! jsp.startsWith("error")) { %>
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-next ico-nav-next" href="#next" onclick="javascript:{setWaitingCursor(); submitFormular('<%=method%>','next'); return false;}" title="<fmt:message key="org.jahia.nextStep.button"/>"><fmt:message key="org.jahia.nextStep.button"/></a>
    </span>
  </span>
  <% } else if (jsp.equals("values.jsp")) { %>
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-nav-save" href="#" title="<fmt:message key="org.jahia.save.button"/>" onclick="javascript:{setWaitingCursor(); submitFormular('<%=method%>','next'); return false; }">&nbsp;<fmt:message key="org.jahia.save.button"/></a>
    </span>
  </span>
  <% } %>
</div>
<script type="text/javascript">
  
  /*
   // invalided for now: need QA test
   document.onkeydown = nextScreen;
   // get the elements of form to handle events
   var thelements = document.forms[0].elements;
   var thefocus=false;
   for (var i=0;i<thelements.length;i++)
   {
   thelements[i].onfocus = formFocus;
   thelements[i].onblur = formFocus;
   }
   */
  function formFocus(e){
  
      if (!e) {
          e = window.event;
          var targ = e.srcElement;
      }
      else {
          var targ = e.target;
      }
      if (e.type == "blur") 
          thefocus = false;
      if (e.type == "focus") 
          thefocus = true;
      //alert(e.type + ' on ' + targ.type + '\n');
      
      return true;
      
  }
  
  function nextScreen(e){
  
      if (!e) {
          e = window.event;
      }
      
      
      var keycode = e.keyCode;
      var thepage = "<%=jsp%>";
      //alert(keycode+" "+thepage);
      if (thefocus && (keycode == 37 || keycode == 39)) 
          return true;//if arrows and focus not handling events
      if ((keycode == 13 || keycode == 39) &&
      thepage != "values.jsp" &&
      thepage != "congratulations.jsp" &&
      thepage != "adv_settings.jsp" &&
      thepage.indexOf("error") == -1) {
          submitFormular('<%=method%>', 'next');
      }
      else 
          if ((keycode == 13 || keycode == 39) && thepage == "values.jsp") {
              submitFormular('<%=method%>', 'next');
          }
          else 
              if (keycode == 37 &&
              thepage != "welcome.jsp" &&
              thepage != "adv_settings.jsp") {
                  submitFormular('<%=method%>', 'back');
              }
              else 
                  return true;
      
  }
</script>
