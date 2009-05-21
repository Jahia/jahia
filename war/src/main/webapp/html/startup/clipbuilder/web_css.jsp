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
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ include file="colors_declaration.inc"%>
<%!

public boolean isNS4(HttpServletRequest req){
    String userAgent = req.getHeader( "user-agent" );
    if (userAgent != null) {
        if (userAgent.indexOf( "Mozilla/4" ) != -1) {
            if (userAgent.indexOf( "MSIE" ) == -1) {
                return true;
            }
        }
    }
    return false;
} // end isNS4

%><%
response.setContentType("text/css");
String theURL = request.getContextPath() + request.getServletPath() + "/..";

String colorSet = "blue";
String colorSetReq = request.getParameter("colorSet");
if (! "".equals(colorSetReq)){
    colorSet = colorSetReq;
}

String color1 = getColorCode(colorSet);
String topMenuOnPict = theURL + "/images/" + getColorTopPict(colorSet);
String leftMenuOnPict = theURL + "/images/" + getColorLeftPict(colorSet);

int ns4 = isNS4(request) ? -1 : 0;

String color2 = "#A8A8A8";

String darkcolor1 = "#666666";
String darkcolor2 = "#999999";
String lightcolor1 = "#ffffff";
String lightcolor2 = "#cccccc";

String backcolor1 = "#ffffff";
String backcolor2 = "#999999";

String leftMenuOff = "#F0F0F0";
String leftMenuOn = "#C5C5C5";
String leftMenuCurrent = color1;
String leftMenuBorder = "#E7E7E7";

String boxContentColor = "#E7E7E7";


%>body {
    margin: 0;
    background-color: <%=backcolor1%>;
}
.backcolor2 {
    background-color: <%=backcolor2%>;
}

body,p,h3,td,li {
    font-family: Verdana, Arial, Helvetica, sans-serif;
    font-size: 10px;
    color: #000000;
}
a {
	text-decoration: none;
	color: <%=color1%>;
}
h1 {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 24px;
	color: <%=darkcolor1%>;
}
h2 {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 14px;
	color: <%=darkcolor2%>;
}
h3 {
	font-size: 12px;
    background-image: url(<%=theURL%>/images/triangle.gif);
    background-repeat: no-repeat;
	padding-left: 20px;
}
.justify {
    text-align: justify;
}
.right {
    text-align: right;
    display:block;
}
.quicklinkon {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9px;
    color: <%=lightcolor1%>;
    background-color: <%=color1%>;
    padding-right: 15px;
    padding-left: 15px;
}
.quicklinkon a {
    color: <%=lightcolor1%>;
}
.quicklinkon a:hover {
    color: <%=lightcolor2%>;
}
.quicklink {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9px;
    color: <%=darkcolor1%>;
    padding-right: 15px;
    padding-left: 15px;
    border-top: 1px solid <%=color1%>;
    border-right: 0 none <%=color1%>;
    border-bottom: 1px solid <%=color1%>;
    border-left: 1px solid <%=color1%>;
}
.quicklink a {
    color: <%=darkcolor1%>;
}
.quicklink a:hover {
    color: <%=lightcolor2%>;
}
.quicklinklast {
    border-left: 1px solid <%=color1%>;
    font-size: 8px;
}
.tab_off a {
    color: <%=darkcolor1%>;
}
.tab_on a {
    color: <%=darkcolor1%>;
}
.adminmenu {
    vertical-align: middle;
    white-space: nowrap;
}
.adminmenu a {
    color: <%=darkcolor1%>;
}
.nowrap {
    white-space: nowrap;
}
input {
    border: 1px solid #c0c0c0;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10px;
    color: <%=darkcolor1%>;
    height: 15px;
}


<%
for (int  i = 0; i < colors.length; i++){
    String myColor = colors[i];
    String myColor1 = colorCodes[i];
    String myTopMenuOnPict = theURL + "/images/" + colorTopPicts[i];
    %>
    .topmenuon<%=myColor%> {
        background-color: <%=lightcolor2%>;
        background-image: url(<%=myTopMenuOnPict%>);
        background-repeat: no-repeat;
        background-position: 0% 100%;
        border-right: 1px solid <%=lightcolor1%>;
        padding-left: 15px;
        padding-right: 15px;
        white-space: nowrap;
    }
    .topmenuon<%=myColor%> a {
        color: #000000;
    }
    .topmenuon<%=myColor%> a:hover {
        color: <%=myColor1%>;
    }
    .topmenu<%=myColor%> {
        background-color: <%=lightcolor2%>;
        background-image: url(<%=theURL%>/images/top_menu_left_off.gif);
        background-repeat: no-repeat;
        background-position: 0% 100%;
        border-right: 1px solid <%=lightcolor1%>;
        padding-left: 15px;
        padding-right: 15px;
        font-family: Verdana, Arial, Helvetica, sans-serif;
        font-size: 10px;
        white-space: nowrap;
    }
    .topmenu<%=myColor%> a {
        color: #000000;
    }
    .topmenu<%=myColor%> a:hover {
        color: <%=myColor1%>;
    }

    .menu<%=myColor%> .options<%=myColor%> {
    	display:block;
    	padding:5px;
    	font-size: 10px;
    	line-height: 15px;
    	background: <%=lightcolor2%>;
    }

    .menu<%=myColor%> a {
    	color: <%=darkcolor1%>;
    	display :block;
        padding-right: 5px;
        padding-left: 5px;
    	text-decoration: none;
    }

    //.menu<%=myColor%> a:visited {
    //    color: <%=lightcolor1%>;
    //}

    .menu<%=myColor%> a:hover {
    	background: <%=myColor1%>;
        color: <%=lightcolor1%>;
    }

    <%
}
%>
.topmenubg {
    background-color: <%=lightcolor2%>;
}
.topmenubuttons {
    white-space: nowrap;
    background-color: <%=lightcolor2%>;
    padding-left: 2px;
    font-family: Verdana, Arial, Helvetica, sans-serif;
    font-size: 10px;
}

.color1bg {
    background-color: <%=color1%>;
}
.languages {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10px;
    color: <%=lightcolor1%>;
    background-color: <%=darkcolor1%>;
    padding-right: 5px;
    padding-left: 5px;
    height: 15px;
}
.languages a {
    color: <%=lightcolor1%>;
}
.languages a:hover {
    color: <%=lightcolor2%>;
}
.maintable {
    background-color: <%=backcolor1%>;
}
.maintable10 {
    background-color: <%=backcolor1%>;
    padding-left: 10px;
    padding-right: 10px;
}
.path {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10px;
    color: <%=darkcolor1%>;
    background-color: <%=backcolor1%>;
    padding-right: 20px;
    padding-left: 10px;
    padding-top: 10px;
    padding-bottom: 10px;
}
.path a {
    color: <%=darkcolor1%>;
}
.path a:hover {
    color: <%=darkcolor2%>;
}
.leftpict {
    background-image: url(<%=theURL%>/images/leftmenuoff.gif);
    background-repeat: no-repeat;
    width: 12px;
}
.leftpicton {
    background-image: url(<%=theURL%>/images/leftmenuoff.gif);
    background-repeat: no-repeat;
    width: 12px;
}
.leftpictcurrent {
    background-image: url(<%=leftMenuOnPict%>);
    background-repeat: no-repeat;
    width: 12px;
}
.left {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 11px;
	color: <%=darkcolor1%>;
	background-color: <%=leftMenuOff%>;
	border-top: 1px solid <%=leftMenuBorder%>;
	border-left: 1px solid <%=leftMenuBorder%>;
	border-right: 1px solid <%=leftMenuBorder%>;
}
.left:hover {
	background-color: <%=leftMenuOn%>;
}
.left a {
	color: <%=darkcolor1%>;
}
.lefton {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 11px;
	color: <%=darkcolor1%>;
	background-color: <%=leftMenuOn%>;
	border-top: 1px solid <%=leftMenuBorder%>;
	border-left: 1px solid <%=leftMenuBorder%>;
	border-right: 1px solid <%=leftMenuBorder%>;
}
.lefton a {
	color: <%=darkcolor1%>;
}
.lefton a:hover {
	color: <%=lightcolor1%>;
}
.leftcurrent {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 10px;
	color: <%=lightcolor1%>;
	background-color: <%=color1%>;
	border-top: 1px solid <%=leftMenuBorder%>;
	border-left: 1px solid <%=leftMenuBorder%>;
	border-right: 1px solid <%=leftMenuBorder%>;
}
.leftcurrent a {
	color: <%=lightcolor1%>;
}
.leftbottom {
	border-top: 1px solid <%=leftMenuBorder%>;
}
.leftlevel1 {
	padding-left: 5px;
	padding-top: 2px;
	padding-bottom: 2px;
}
.leftlevel2 {
	font-size: 10px;
	padding-left: 20px;
}
.leftlevel3 {
	font-size: 10px;
	padding-left: 35px;
}
.leftlevel4 {
	font-size: 10px;
	padding-left: 50px;
}
.leftlevel5 {
	font-size: 10px;
	padding-left: 65px;
}
.leftlevel6 {
	font-size: 10px;
	padding-left: 80px;
}
.verticaleline {
    background-image: url(<%=theURL%>/images/verticaleline.gif);
    background-repeat: repeat-y;
    width: 9px;
}
.horizontaleline {
    background-image: url(<%=theURL%>/images/horizontaleline.gif);
    background-repeat: repeat-x;
    padding-top: 8px;
	display:block;
    white-space: nowrap;
}
.footer {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10px;
    color: <%=darkcolor1%>;
    background-color: <%=backcolor1%>;
    text-align: center;
    padding-top: 10px;
    padding-bottom: 10px;
}

.boxtitletransparent {
    color: <%=darkcolor1%>;
    padding-left: 5px;
    padding-right: 5px;
    padding-top: 2px;
    padding-bottom: 2px;
    white-space: nowrap;
}
.boxtitleborder1 {
    color: <%=darkcolor1%>;
	border: 1px solid <%=color1%>;
    padding-left: 5px;
    padding-right: 5px;
    padding-top: 2px;
    padding-bottom: 2px;
    white-space: nowrap;
}
.boxtitleborder2 {
    color: <%=darkcolor1%>;
	border: 1px solid <%=color2%>;
    padding-left: 5px;
    padding-right: 5px;
    padding-top: 2px;
    padding-bottom: 2px;
    white-space: nowrap;
}
.boxtitlecolor1 {
    color: <%=lightcolor1%>;
    background-color: <%=color1%>;
    padding-left: 5px;
    padding-right: 5px;
    padding-top: 2px;
    padding-bottom: 2px;
    white-space: nowrap;
}
.boxtitlecolor2 {
    color: <%=lightcolor1%>;
    background-color: <%=color2%>;
    padding-left: 5px;
    padding-right: 5px;
    padding-top: 2px;
    padding-bottom: 2px;
    white-space: nowrap;
}

.boxcontenttransparent {
    padding: 5px;
}
.boxcontentborder1 {
	border-left: 1px solid <%=color1%>;
	border-right: 1px solid <%=color1%>;
	border-bottom: 1px solid <%=color1%>;
    padding: 5px;
}
.boxcontentborder2 {
	border-left: 1px solid <%=color2%>;
	border-right: 1px solid <%=color2%>;
	border-bottom: 1px solid <%=color2%>;
    padding: 5px;
}
.boxcontentcolor1 {
	border-left: 1px solid <%=color1%>;
	border-right: 1px solid <%=color1%>;
	border-bottom: 1px solid <%=color1%>;
    padding: 5px;
    background-color: <%=boxContentColor%>;
}
.boxcontentcolor2 {
	border-left: 1px solid <%=color2%>;
	border-right: 1px solid <%=color2%>;
	border-bottom: 1px solid <%=color2%>;
    padding: 5px;
    background-color: <%=boxContentColor%>;
}
.bold {
    font-weight: bold;
    font-size: 11px;
    color: <%=color1%>;
}
.boxtitleparticipant1 {
    color: #003399;
    text-align: left;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 12px;
    font-weight: bold;
    background-color: #C4D2FF;
    white-space: nowrap;
}
.boxtitleparticipant2 {
    color: #C4D2FF;
    text-align: left;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 12px;
    font-weight: bold;
    background-color: #003399;
    white-space: nowrap;
}
.boxEPcontentcolor1 {
	border-left: 1px solid <%=color1%>;
	border-right: 1px solid <%=color1%>;
	border-bottom: 1px solid <%=color1%>;
    	background-color: #C4D2FF;
}
.boxEPbordercolor1 {
	border-left: 1px solid #003399;
	border-right: 1px solid #003399;
	border-bottom: 1px solid #003399;
	border-top: 1px solid #003399;
}
.boxEPTitlecolor1 {
    color: #FFFFFF;
    text-align: left;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 11px;
    font-weight: bold;
    background-color: #003399;
    padding-top: 2px;
    padding-bottom: 2px;
    white-space: nowrap;
}
.boxEPLinkcolor1 {
    color: #FFFFFF;
    text-align: left;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 11px;
    font-weight: bold;
    white-space: nowrap;
}
.boxEPPaginationPos {
    color: #003399;
    text-align: left;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 11px;
    font-style: normal;
    white-space: nowrap;
}
.boxEPPaginationNav {
    color: #003399;
    text-align: right;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 11px;
    font-style: normal;
    white-space: nowrap;
}
.EPMetaDataTitle {
	color: #000000;
	text-align: left;
	vertical-align: top;
	font-family: Arial, Helvetica, sans-serif;
	font-size: 11px;
	font-weight: bold;
	white-space: nowrap;
}
.EPMetaDataValue {
	color: #000000;
	text-align: left;
	vertical-align: top;
	font-family: Arial, Helvetica, sans-serif;
	font-size: 11px;
}
## webapps

.waMenu a ,a.waMenu{color: #000000;  }
.waMenu a:hover, a.waMenu:hover  {color: <%=color1%>;  background-color: <%=lightcolor1%>; }

a.waMenuHighlight { color: #be0c2b;  }
a.waMenuHighlight:hover { background-color: <%=lightcolor1%>; }

.waUnderline:link {color: #000000 }
.waUnderline:visited {color: #000000 }
.waUnderline:active {color: #000000 }
.waUnderline:hover {color: #100E80;  background-color: <%=lightcolor2%>}

.waTree:link {color: #000000;  }
.waTree:visited {color: #000000;  }
.waTree:active {color: #000000;  }
.waTree:hover {color: <%=color1%>;  }

.waSelected:link {color: <%=lightcolor1%>;  background-color: <%=color1%>}
.waSelected:visited {color: <%=lightcolor1%>;  background-color: <%=color1%>}
.waSelected:active {color: <%=lightcolor1%>;  background-color: <%=color1%>}
.waSelected:hover {color: <%=lightcolor1%>;  background-color: <%=color1%>}

.waNormal { ; color: <%=color1%> }
.waSmall { font-size: 9px; color:  <%=color1%> }
.waRed { color: #be0c2b; font-size: 9px; }
.waLargeRed { ; color: #be0c2b }

.waSmallBoldItalic { font-weight: bold; font-style: italic; font-size: 9px; color: <%=color1%> }
.waSmallItalic { font-style: italic; font-size: 9px; color: <%=color1%> }

.waBG { background-color: <%=boxContentColor%> }
.waTrpBG { background-color: #C1C7D0 }
.waBoxBG { background-color: <%=lightcolor2%> }
.waBorder { background-color: <%=darkcolor2%> }

.waInput { color: #000000; font-size: 9px; font-family: Arial, Helvetica, sans-serif; }
.waSelect { color: #000000; font-size: 11px; font-family: Arial, Helvetica, sans-serif; }
.waSelect:hover { color: #000000;  }
.waTextarea { font-size: 9px; font-family: Arial, Helvetica, sans-serif; }

.nfLink:link {  font-style: normal; color: <%=color1%>;  }
.nfLink:visited {  font-style: normal; color: <%=color1%>;  }
.nfLink:visited {  font-style: normal; color: <%=color1%>;  }
.nfLink:hover {  font-style: normal; color: #100E80; background-color: <%=lightcolor2%>;  }

.nfTitle { font-size: 11px; font-style: normal; font-weight: bold; color: <%=color1%> }
.nfDate { font-size: 9px; font-style: normal; font-weight: bold; color: <%=color1%> }
.nfSource { font-size: 9px; font-style: normal; color: <%=color1%> }
.nfMedia { font-size: 9px; font-style: italic; color: <%=color1%> }
