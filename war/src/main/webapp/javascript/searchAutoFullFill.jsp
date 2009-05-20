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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%
    final ProcessingContext processingContext = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    String searchForm = request.getParameter("searchForm");
    if (searchForm == null || searchForm.length() == 0) {
        searchForm = "searchForm";
    }

    String searchResult = request.getParameter("searchResult");
    if (searchResult == null || searchResult.length() == 0) {
        searchResult = "searchResult";
    }
%>
<script type="text/javascript">
function keyDownSearch(e) {
    var code;
    if (window.event)
        code = window.event.keyCode;
    else if (e)
        code = e.which;
    else
        code = null;

    document.getElementById("<%=searchResult%>").className = "auto_completeHidden";

    if (code == 13 || code == 38) {     // Enter, Up_Arrow
        return false;
    } else if (code == 40) {            // Down_Arrow
        var divElement = document.getElementById("element0");
        if (! divElement) {
            return false;
        }
        divElement.className = "getFocus";
        divElement.focus();
        return false;
    }
    delete code;
    var searchString = document.getElementById("s").value;
    if (searchString && searchString.length > 3) {
        getSearchFullfill("<%=request.getContextPath()%>/ajaxaction/SearchAutoFullFill", searchString);
    }
    return true;
}

function getSearchFullfill(actionUrl, searchString) {
    try {
        // correct values are "POST" or "GET" (HTTP methods).
        var method = "POST" ;
        var data = "searchString=" + searchString + "&params=/op/edit/lang/" + "<%=processingContext.getLocale()%>" +
                   "/pid/" + "<%=processingContext.getPageID()%>";

        if (method == "GET") {
            actionUrl += "?" + data;
            data = null;
        }

		// Create new XMLHttpRequest request
        if (window.XMLHttpRequest) {
            req = new XMLHttpRequest();

        } else if (window.ActiveXObject) {
            req = new ActiveXObject("Microsoft.XMLHTTP");

        } else {
            alert("Error: Your Browser does not support XMLHTTPRequests, please upgrade...");
            return;
        }

        req.open(method, actionUrl, true);

        req.onreadystatechange = displayResult;

        if (method == "POST") {
            req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        }
        req.send(data);

    } catch (e) {
        alert("Exception sending the Request: " + e);
    }
}

function displayResult() {
    if (req.readyState == 4) {
        // alert ("resp: " + req.responseText);
        if (req.status == 200) {
            try {
                var response = req.responseText;

                var values = response.split(",");
                if (values.length > 0) {
                    var htmlText = "<ol>\n";
                    for (var i = 0; i < values.length; i++) {
                        if (values[i].length == 0) continue;
                        htmlText += "<li><a id='element" + i + "' href='javascript:copyValue(\"" + values[i] + "\");'>" +
                                    values[i] + "</a></li>\n";
                    }
                    htmlText += "</ol>\n";

                    var resultList = document.getElementById("<%=searchResult%>");
                    resultList.innerHTML = htmlText;
                    resultList.className = "auto_complete";
                }

            } catch (e) {
                alert("Exception displaying result: " + e);
            }

        } else {
            alert("There was a problem processing the request. Status: " +
                  req.status + ", msg: " + req.statusText);
        }
        document.body.style.cursor = "default";
    }
}

function copyValue(value) {
    var inputElem = document.getElementById("s");
    document.getElementById("<%=searchResult%>").innerHTML = "";
    document.getElementById("<%=searchResult%>").className = "auto_completeHidden";
    inputElem.value = value;
    document.getElementById("<%=searchForm%>").submit();
}
</script>