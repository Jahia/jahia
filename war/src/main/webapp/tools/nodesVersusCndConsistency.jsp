<%@ page import="java.io.IOException" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="javax.jcr.*" %>
<%@ page import="javax.jcr.nodetype.ConstraintViolationException" %>
<%@ page import="javax.jcr.query.Query" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.jahia.ajax.gwt.helper.CacheHelper" %>
<%@ page import="org.jahia.api.Constants" %>
<%@ page import="org.jahia.services.content.*" %>
<%@ page import="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition" %>
<%@ page import="org.jahia.services.content.nodetypes.NodeTypeRegistry" %>
<%@ page import="org.jahia.services.SpringContextSingleton" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="tools.css" type="text/css"/>
    <title>Nodes consistency Tool</title>
</head>
<body>
<%@ include file="gotoIndex.jspf" %>
<h1>Nodes consistency Tool</h1>

<p>
    This tool will validates the content nodes against the cnd files. It will remove any property that would be
    found in the JCR but no more declared in the related CND. Ensure that you do not have any CND parsing issue
    before running this tool: if some definition is temporarly missing because of some syntax error, or any other
    reason, this tool may drop some content you want to keep. This operation cannot be reverted, unless from a backup.
    For this reason, it is highly recommended to perform such backup before using this tool!
</p>

<h2>Consistency checks</h2>

<c:choose>
    <c:when test="<%=hasEncounteredCndIssuesSinceStartup()%>">
        You have encountered some issues with your CND files since the last startup. To prevent unwanted removal of
        your content, this tool is deactivated. Please restart your Jahia application to make this tool available.
    </c:when>
    <c:when test="<%=isParameterActive(request, OPERATION, RUN_CONSISTENCY_TEST)%>">
        <%runConsistencyCheck(out, request, false);%>
    </c:when>
    <c:when test="<%=isParameterActive(request, OPERATION, FIX_JCR)%>">
        <%runConsistencyCheck(out, request, true);%>
    </c:when>
    <c:otherwise>
        <%
            out.println("<form>");
            renderWorkspaceSelector(out);
            renderRadio(out, RUN_CONSISTENCY_TEST, "Run the content nodes consistency check", true);
            renderRadio(out, FIX_JCR, "Fix the inconsistent content nodes (also performs check). DO NOT RUN IF PLATFORM IS ACTIVE (USERS, BACKGROUND JOBS ARE RUNNING !).", false);
            out.println("<input type=\"submit\" name=\"submit\" value=\"Submit\">");
            out.println("</form>");
        %>
    </c:otherwise>
</c:choose>

<%@ include file="gotoIndex.jspf" %>
</body>
</html>

<%@ include file="functions.jspf" %>
<%!

    private static final String RUN_CONSISTENCY_TEST = "runConsistencyTest";
    private static final String FIX_JCR = "fixJCR";
    private static final String OPERATION = "operation";
    private static boolean running = false;

    private boolean hasEncounteredCndIssuesSinceStartup() {

        return ServicesRegistry.getInstance().getJahiaTemplateManagerService().hasEncounteredIssuesWithDefinitions();
    }

    private void runConsistencyCheck(final JspWriter out, HttpServletRequest request, final boolean fix) throws IOException {
        if (running) {
            println(out, "ABORTING: check or fix already running, please wait for it to complete !");
            return;
        }
        running = true;
        errorCount = 0;
        final long startTime = System.currentTimeMillis();

        final String chosenWorkspace = request.getParameter("workspace");
        for (String workspaceName : workspaces) {
            if (StringUtils.isBlank(chosenWorkspace) || chosenWorkspace.equals(workspaceName)) {
                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspaceName, new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper sessionWrapper) throws RepositoryException {
                            try {
                                return runConsistencyCheck(sessionWrapper, fix, out);
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    });
                } catch (RepositoryException e) {
                    println(out, "Error occured", e, false);
                }
            }
        }
        if (fix) {
            println(out, "");
            final CacheHelper cacheHelper = (CacheHelper) SpringContextSingleton.getInstance().getContext().getBean("CacheHelper");
            if (cacheHelper != null) {
                println(out, "Flushing all caches...");
                cacheHelper.flushAll();
            } else {
                println(out, "Couldn't find cache helper, please flush all caches manually.");
            }
        }
        final long totalTime = System.currentTimeMillis() - startTime;
        println(out, "Total time to process all JCR : " + totalTime + "ms");
        running = false;
    }

    private Object runConsistencyCheck(JCRSessionWrapper session, boolean fix, final JspWriter out) throws IOException {
        try {
            final Map<String, Map<String, Set<String>>> invalidProps = new HashMap<String, Map<String, Set<String>>>();

            final String stmt = "select * from [jnt:content] where isdescendantnode('/sites/')";
            final NodeIterator nodeIterator = session.getWorkspace().getQueryManager().createQuery(stmt, Query.JCR_SQL2).execute().getNodes();
            final ArrayList<Property> propertiesToDelete = new ArrayList<Property>();

            while (nodeIterator.hasNext()) {
                final JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
                final Node node = nodeWrapper.getRealNode();
                final PropertyIterator nodeProperties = node.getProperties();

                while (nodeProperties.hasNext()) {
                    final Property property = nodeProperties.nextProperty();
                    final String pName = property.getName();
                    final ExtendedPropertyDefinition pDef = nodeWrapper.getApplicablePropertyDefinition(pName);
                    final String fullType = nodeWrapper.getNodeTypes().toString();
                    if (pDef == null) {
                        println(out, MessageFormat.format("Undeclared property [{0}] on the node {1} of type {2}",
                                pName, nodeWrapper.getPath(), fullType));
                        trackInvalidDeclarations(invalidProps, fullType, pName, "missing");
                        if (fix) {
                            try {
                                property.remove();
                            } catch (ConstraintViolationException cve) {
                                propertiesToDelete.add(property);
                            }
                        }
                    } else if (pDef.isInternationalized()) {
                        println(out, MessageFormat.format("Not i18n property [{0}] on the node {1} of type {2}, i18n property expected",
                                pName, nodeWrapper.getPath(), nodeWrapper.getNodeTypes().toString()));
                        trackInvalidDeclarations(invalidProps, fullType, pName, "untranslated");
                        if (fix) {
                            try {
                                property.remove();
                            } catch (ConstraintViolationException cve) {
                                propertiesToDelete.add(property);
                            }
                        }
                    }
                }


                final Map<String, ExtendedPropertyDefinition> jntTranslationProps = NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_TRANSLATION).getPropertyDefinitionsAsMap();
                final NodeIterator translationIterator = node.getNodes();
                while (translationIterator.hasNext()) {
                    final Node translation = translationIterator.nextNode();
                    if (!translation.isNodeType(Constants.JAHIANT_TRANSLATION)) continue;

                    final PropertyIterator translatedProperties = translation.getProperties();
                    while (translatedProperties.hasNext()) {
                        final Property property = translatedProperties.nextProperty();
                        final String pName = property.getName();
                        if (jntTranslationProps.containsKey(pName)) continue;
                        final ExtendedPropertyDefinition pDef = nodeWrapper.getApplicablePropertyDefinition(pName);
                        final String fullType = nodeWrapper.getNodeTypes().toString();
                        if (pDef == null) {
                            println(out, MessageFormat.format("Undeclared property [{0}] on the node {1} of type {2}",
                                    pName, nodeWrapper.getPath(), fullType));
                            trackInvalidDeclarations(invalidProps, fullType, pName, "missing");
                            if (fix) {
                                try {
                                    property.remove();
                                } catch (ConstraintViolationException cve) {
                                    propertiesToDelete.add(property);
                                }
                            }
                        } else if (!pDef.isInternationalized()) {
                            println(out, MessageFormat.format("i18n property [{0}] on the node {1} of type {2}, not i18n property expected",
                                    pName, nodeWrapper.getPath(), fullType));
                            trackInvalidDeclarations(invalidProps, fullType, pName, "translated");
                            if (fix) {
                                try {
                                    property.remove();
                                } catch (ConstraintViolationException cve) {
                                    propertiesToDelete.add(property);
                                }
                            }
                        }
                    }
                }
            }

            if (fix) {
                for (Property property : propertiesToDelete) {
                    final String propertyPath;
                    try {
                        propertyPath = property.getPath();
                    } catch (InvalidItemStateException e) {
                        println(out, MessageFormat.format("Skipping a property:{0}", e.getMessage()),e, true);
                        continue;
                    }
                    final Node node = property.getParent();
                    node.addMixin("jmix:unstructured");
                    try {
                        property.remove();
                        //println(out, "Deleted the property " + propertyPath);
                    } catch (Throwable t) {
                        int pType;
                        try {
                            pType = property.getType();
                        } catch (InvalidItemStateException iise) {
                            pType = PropertyType.UNDEFINED;
                        }
                        println(out, MessageFormat.format("Impossible to deleted the property [{0}] of type {1}\n   {2}",
                                propertyPath, PropertyType.nameFromValue(pType), t.getMessage()));
                    }
                    node.removeMixin("jmix:unstructured");
                }
            }

            printInvalidProps(invalidProps, out, session.getWorkspace().getName());

            if (fix) {
                session.save();
            }
        } catch (Throwable throwable) {
            println(out, "Error occured ", throwable, false);
        }

        return null;
    }

    private void trackInvalidDeclarations(Map<String, Map<String, Set<String>>> map, String type, String prop, String errorType) {
        Map<String, Set<String>> propsMap = map.get(type);
        if (propsMap == null) {
            propsMap = new HashMap<String, Set<String>>();
            map.put(type, propsMap);
        }
        Set<String> props = propsMap.get(errorType);
        if (props == null) {
            props = new HashSet<String>();
            propsMap.put(errorType, props);
        }
        props.add(prop);
    }

    private void printInvalidProps(Map<String, Map<String, Set<String>>> map, final JspWriter out, String workspace) throws IOException {
        if (map == null || map.isEmpty()) return;

        println(out, "");
        println(out, "**** Sum up of the invalid definitions in workspace " + workspace);
        for (String nodeType : map.keySet()) {
            depthPrintln(out, 1, nodeType);
            final Map<String, Set<String>> errorsForCurrentType = map.get(nodeType);
            for (String errorType : errorsForCurrentType.keySet()) {
                final String label;
                if ("missing".equals(errorType)) {
                    label = "Undefined properties";
                } else if ("untranslated".equals(errorType)) {
                    label = "i18n properties found on the master node";
                } else if ("translated".equals(errorType)) {
                    label = "Non i18n properties found on a translation subnode";
                } else {
                    label = "Unknown errors";
                }
                final StringBuilder sb = new StringBuilder(label);
                sb.append(": ");
                for (String prop : errorsForCurrentType.get(errorType)) {
                    sb.append(prop).append(", ");
                }
                depthPrintln(out, 2, sb.toString());
            }
        }
    }
%>