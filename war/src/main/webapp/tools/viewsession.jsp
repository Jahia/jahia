<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
    <style type="text/css">
        <!--

        ol.attribute {
        /* border: 1px solid #CFD9E1; */
            display: block;
            padding: 2px;
            clear: both;
        }

        ol.attribute li {
            background: #CFD9E1;
            display: block;
            width: 100%;
        }

        div.map ol.entry {
            background: #CFD9E1;
            display: block;
            padding: 0;
            width: 100%;
            clear: both;
        }

        div.map ol.entry li {
            background: #CFD9E1;
            display: inline;
            float: left;
        }

        div.map ol.entry li.key {
            width: 12%;
        }

        div.map ol.entry li.key-type {
            width: 10%;
        }

        div.map ol.entry li.value-type {
            width: 20%;
        }

        div.map ol.entry li.value {
            width: 55%;
        }

        -->
    </style>
    <title>Session Viewer JSP</title>
</head>
<body>

<utility:sessionViewer/>
</body>
</html>