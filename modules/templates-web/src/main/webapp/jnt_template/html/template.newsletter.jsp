<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body id="body" style="background-color:#eaeaea;">
<div id="bodywrapper">
    <table style="font-size:80%;background-color:#eaeaea;width:100%;font-family: Arial, Helvetica, sans-serif;line-height:160%;"
           width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td align="center">

                <table width="579" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td style="padding: 8px 0 8px 0;">
                            <p style="font-size: 11px;font-weight: normal;font-style: italic;color: #333;text-align: center;">
                                <fmt:message key="web_templates_newsletter.intro.1">
                                    <fmt:param value="<a style='color: #0066ff;text-decoration: none;' href='/' name='top'>${renderContext.site.path}</a>"/>
                                </fmt:message>
                                <br/>
                                <fmt:message key="web_templates_newsletter.intro.2"/>
                                <a href="${currentNode.url}" target="_blank" style="color: #0066ff;text-decoration: none;">
                                    <fmt:message key="web_templates_newsletter.intro.3"/></a>.</p></td>
                    </tr>
                </table>

                <table width="579" border="0" cellspacing="0" cellpadding="0" style="background-color:#4f5c79">
                    <tr>
                        <td align="center">

                            <table width="579" height="108" border="0" cellspacing="0" cellpadding="0"
                                   style="background-color:#333333">
                                <tr>
                                    <td style="width:200px;"><template:area path="logo"/></td>
                                    <td align="right" style="height: 108px; padding-right: 20px;">
                                        <h3 style="color:#fff; font-weight:normal; margin:0; padding:5px 20px; font-size:30px;font-family:Georgia, 'Times New Roman', Times, serif">
                                            <em>${currentNode.properties["jcr:title"]}</em></h3>
                        <span style="color:#fff; padding:0 20px; font-size:14px;font-family:Georgia, 'Times New Roman', Times, serif">
                            <em>
                                <jsp:useBean id="startDate" class="java.util.Date" />
                                <fmt:formatDate value="${startDate}" dateStyle="long" type="date"/>
                            </em>
                        </span>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>

                <table width="579" border="0" cellspacing="0" cellpadding="0" style="background-color:#fff">
                    <tr>
                        <td width="254" align="center" valign="top"
                            style="background-color: #f8f8f8;border-right: 1px solid #ccc;">
                            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                <tr>
                                    <td align="left" style="padding: 10px;">

                                        <h2 style="font-size: 16px;font-weight: normal;color: #464646;margin: 0 0 10px 0;border-bottom: 3px solid #ccc;text-transform: uppercase;">
                                            In this issue</h2>
                                        <template:area path="contents"/>
                                        <h2 style="font-size: 16px;font-weight: normal;color: #464646;margin: 0 0 10px 0;border-bottom: 3px solid #ccc;text-transform: uppercase;">
                                            In short</h2>
                                        <template:area path="short"/>
                                        <%--<table width="100%" height="173" border="0" cellspacing="0" cellpadding="0"
                                               style="border-top: 1px solid #ccc;border-bottom: 1px solid #ccc;">
                                            <tr>
                                                <td align="center" valign="top"
                                                    style="border-bottom: 1px solid #ccc;padding: 16px 22px 16px 22px;">
                                                    <h3 style="font-size: 16px;font-weight: normal;color: #666;margin: 0 0 4px 0;padding: 0;">
                                                        UNSUBSCRIBE</h3>

                                                    <p style="font-size: 13px;font-weight: normal;color: #313131;margin: 0;	padding: 0;">
                                                        <a href="#" target="_blank"
                                                           style="color: #0066ff;text-decoration: none; border: none;margin: 0;padding: 0;">Click
                                                            to instantly unsubscribe from this email</a></p></td>
                                            </tr>
                                            <tr>
                                                <td align="center" valign="top"
                                                    style="padding: 16px 22px 16px 22px;">
                                                    <h3 style="font-size: 16px;font-weight: normal;color: #666;margin: 0 0 4px 0;padding: 0;">
                                                        FORWARD</h3>

                                                    <p style="font-size: 13px;font-weight: normal;color: #313131;margin: 0;	padding: 0;">
                                                        <a href="#" target="_blank"
                                                           style="color: #0066ff;text-decoration: none; border: none;margin: 0;padding: 0;">Click
                                                            to forward this email to a friend</a></p></td>
                                            </tr>
                                        </table>--%>
                                    </td>
                                </tr>
                            </table>
                        </td>

                        <td width="325" align="center" valign="top">
                            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                <tr>
                                    <td align="left" style=" padding: 20px;">
                                        <template:area path="lastNews"/>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>

                <table width="579" height="108" border="0" cellspacing="0" cellpadding="0"
                       style="padding: 10px;background-color: #333333;">
                    <tr>
                        <td align="center" valign="middle">

                            <table width="559" height="88" border="0" cellspacing="0" cellpadding="0"
                                   style="font-size: 11px;font-weight: normal;color: #ffffff;text-align: center;">
                                <tr>
                                    <td align="center" style="height: 88px;">
                                        <template:area path="logoFooter"/>
                                        <p>&copy; Copyright 2002-2011 - ACME International Corp.</p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td align="center">&nbsp;</td>
        </tr>
    </table>
</div>
</body>

</html>
