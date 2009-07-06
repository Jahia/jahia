<%@ page import="javax.jcr.Node" %>
<%@ page import="org.jahia.taglibs.internal.gwt.GWTIncluder" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>

<%= GWTIncluder.generateGWTImport(pageContext,"org.jahia.ajax.gwt.template.general.edit.Edit") %> 

<template:module node="currentNode" />
