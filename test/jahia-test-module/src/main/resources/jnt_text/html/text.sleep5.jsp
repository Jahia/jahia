<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
Very long to appear <% Thread.currentThread().sleep(5000); %>
${currentNode.properties['text'].string}
