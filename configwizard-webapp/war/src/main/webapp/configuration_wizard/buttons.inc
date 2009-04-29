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
<jsp:useBean id="method" class="java.lang.String" scope="request"/>
</div>
<div id="buttons">
  <% if (! jsp.equals("welcome.jsp")) { %>
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-nav-previous" href="#previous" onclick="javascript:{setWaitingCursor(); submitFormular('<%=method%>','back'); return false;}" title="<fmt:message key="org.jahia.back.button"/>"><fmt:message key="org.jahia.back.button"/></a>
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
