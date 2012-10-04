<%@ page import="org.jahia.ajax.gwt.helper.CacheHelper" %>
<%@ page import="org.jahia.api.Constants" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.SpringContextSingleton" %>
<%@ page import="org.jahia.services.content.JCRCallback" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="org.jahia.services.content.JCRSessionWrapper" %>
<%@ page import="org.jahia.services.content.JCRTemplate" %>
<%@ page import="org.jahia.utils.RequestLoadAverage" %>
<%@ page import="org.quartz.JobDetail" %>
<%@ page import="org.quartz.SchedulerException" %>
<%@ page import="javax.jcr.*" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="workspace" value="${functions:default(param.workspace, 'default')}"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="tools.css" type="text/css"/>
    <title>JCR Integrity Tool</title>
    <script type="text/javascript">
        function toggleLayer(whichLayer) {
            var elem, vis;
            if (document.getElementById) // this is the way the standards work
                elem = document.getElementById(whichLayer);
            else if (document.all) // this is the way old msie versions work
                elem = document.all[whichLayer];
            else if (document.layers) // this is the way nn4 works
                elem = document.layers[whichLayer];
            vis = elem.style;
            // if the style.display value is blank we try to figure it out here
            if (vis.display == '' && elem.offsetWidth != undefined && elem.offsetHeight != undefined)
                vis.display = (elem.offsetWidth != 0 && elem.offsetHeight != 0) ? 'block' : 'none';
            vis.display = (vis.display == '' || vis.display == 'block') ? 'none' : 'block';
        }
    </script>
    <style type="text/css">
        div.hiddenDetails {
            margin: 0px 20px 0px 20px;
            display: none;
        }

        .error {
            color: #FF0000;
        }

        .warning {
            color: brown;
        }
    </style>
</head>
<body>
<h1>JCR Integrity Tools</h1>

<p>
    This tool will perform some integrity checks on the JCR repository, and also implements some fixes.
</p>

<h2>Integrity checks</h2>
<%!

    private void runJCRTest(final JspWriter out, HttpServletRequest request, JspContext pageContext, final boolean fix) throws IOException {

        if (running) {
            println(out, "ABORTING: check or fix already running, please wait for it to complete !");
            return;
        }
        running = true;
        mustStop = false;
        if (fix) {
            if (RequestLoadAverage.getInstance().getOneMinuteLoad() > 1) {
                println(out, "ABORTING: request load is above 1, users are using the platform and we cannot run a fix while this is the case !");
                return;                
            }
            try {
                List<JobDetail> activeJobs = ServicesRegistry.getInstance().getSchedulerService().getAllActiveJobs();
                if (activeJobs.size() > 0) {
                    println(out, "ABORTING: background jobs are executing, cannot run fix while background jobs are present !");
                    return;
                }
            } catch (SchedulerException se) {
                println(out, "ABORTING: error accessing scheduler service", se, false);
                return;
            }
        }

        long startTime;
        long bytesRead;
        long totalTime;
        final boolean referencesCheck = fix || !isParameterActive(request, "option", "noReferencesCheck");
        final boolean binaryCheck = fix || !isParameterActive(request, "option", "noBinariesCheck");
        if (fix) {
            printTestName(out, "JCR Integrity Fix");
        } else {
            StringBuilder exclusions = new StringBuilder();
            if (!binaryCheck) {
                exclusions.append("no binaries");
            }
            if (!referencesCheck) {
                if (exclusions.length() > 0) {
                    exclusions.append(", ");
                }
                exclusions.append("no references");
            }
            
            printTestName(out, "JCR Integrity Check " + (exclusions.length() > 0 ? "(" + exclusions.toString() + ")" : ""));
        }
        try {
            String chosenWorkspace = request.getParameter("workspace");
            final Map<String, Long> results = new HashMap<String, Long>();
            results.put("bytesRead", 0L);
            results.put("nodesRead", 0L);
            bytesRead = 0;
            startTime = System.currentTimeMillis();
            for (String workspaceName : workspaces) {
                if (chosenWorkspace == null || chosenWorkspace.isEmpty() || chosenWorkspace.equals(workspaceName)) {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspaceName, new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper sessionWrapper) throws RepositoryException {
                            JCRNodeWrapper jahiaRootNode = sessionWrapper.getRootNode();
                            Node jcrRootNode = jahiaRootNode.getRealNode();
                            Session jcrSession = jcrRootNode.getSession();

                            Workspace workspace = jcrSession.getWorkspace();
                            try {
                                println(out, "Traversing " + workspace.getName() + " workspace ...");
                                processNode(out, jcrRootNode, results, fix, binaryCheck, referencesCheck);
                            } catch (IOException e) {
                                throw new RepositoryException("IOException while running", e);
                            }
                            return null;
                        }
                    });
                }
            }
            if (fix) {
                CacheHelper cacheHelper = (CacheHelper) SpringContextSingleton.getInstance().getContext().getBean("CacheHelper");
                if (cacheHelper != null) {
                    println(out, "Flushing all caches...");
                    cacheHelper.flushAll();
                } else {
                    println(out, "Couldn't find cache helper, please flush all caches manually.");
                }
            }
            bytesRead = results.get("bytesRead");
            long nodesRead = results.get("nodesRead");
            totalTime = System.currentTimeMillis() - startTime;
            println(out, "Total time to process all JCR " + nodesRead + " nodes data" + (bytesRead == 0 ? "" : " (" + bytesRead + " bytes)") + " : " + totalTime + "ms");
            if (bytesRead > 0) {
                double jcrReadSpeed = bytesRead / (1024.0 * 1024.0) / (totalTime / 1000.0);
                println(out, "JCR processing speed = " + jcrReadSpeed + "MB/sec");
            }
        } catch (Throwable t) {
            println(out, "Error reading JCR ", t, false);
        } finally {
            running = false;
            mustStop = false;
        }

    }

    private void printTestName(JspWriter out, String testName) throws IOException {
        out.println("<h3>");
        println(out, testName);
        out.println("</h3>");
    }

    private static SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");

    private String generatePadding(int depth, boolean withNbsp) {
        StringBuffer padding = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            if (withNbsp) {
                padding.append("&nbsp;&nbsp;");
            } else {
                padding.append("  ");
            }
        }
        return padding.toString();
    }

    int errorCount = 0;

    private void print(JspWriter out, String message) throws IOException {
        System.out.print(message);
        out.print(message);
        out.flush();
    }

    private void println(JspWriter out, String message) throws IOException {
        System.out.println(message);
        out.println(message + "<br/>");
        out.flush();
    }

    private void depthPrintln(JspWriter out, int depth, String message) throws IOException {
        System.out.println(generatePadding(depth, false) + message);
        out.println(generatePadding(depth, true) + message + "<br/>");
        out.flush();
    }

    private void debugPrintln(JspWriter out, String message) throws IOException {
        System.out.println("DEBUG: " + message);
        out.println("<!--" + message + "-->");
        out.flush();
    }

    private void errorPrintln(JspWriter out, String message) throws IOException {
        System.out.println("ERROR: " + message);
        out.println("<span class='error'>" + message + "</span><br/>");
        out.flush();
    }

    private void depthErrorPrintln(JspWriter out, int depth, String message) throws IOException {
        System.out.println(generatePadding(depth, false) + "ERROR: " + message);
        out.println(generatePadding(depth, true) + "<span class='error'>" + message + "</span><br/>");
        out.flush();
    }

    private void println(JspWriter out, String message, Throwable t, boolean warning) throws IOException {
        System.out.println(message);
        if (t != null) {
            t.printStackTrace();
        }
        if (warning) {
            out.println("<span class='warning'>" + message + "</span>");
        } else {
            out.println("<span class='error'>" + message + "</span>");
        }
        errorCount++;
        if (t != null) {
        out.println("<a href=\"javascript:toggleLayer('error" + errorCount + "');\" title=\"Click here to view error details\">Show/hide details</a>");
        out.println("<div id='error" + errorCount + "' class='hiddenDetails'><pre>");
        t.printStackTrace(new PrintWriter(out));
        out.println("</pre></div>");
        }
        out.println("<br/>");
        out.flush();
    }

    protected boolean processPropertyValue(JspWriter out, Node node, Property property, Value propertyValue, Map<String, Long> results, boolean fix, boolean referencesCheck, boolean binaryCheck) throws RepositoryException, IOException {
        int propertyType = propertyValue.getType();
        switch (propertyType) {
            case PropertyType.BINARY:
                Binary binaryValue = propertyValue.getBinary();
                binaryValue.getSize();
                InputStream binaryInputStream = binaryValue.getStream();
                long bytesRead = results.get("bytesRead");
                while (binaryInputStream.read() != -1) {
                    bytesRead++;
                }
                results.put("bytesRead", bytesRead);
                break;
            case PropertyType.REFERENCE:
            case PropertyType.WEAKREFERENCE:
                String uuid = propertyValue.getString();
                try {
                    Node referencedNode = node.getSession().getNodeByIdentifier(uuid);
                } catch (ItemNotFoundException infe) {
                    println(out, "Couldn't find referenced node with UUID " + uuid + " referenced from property " + property.getPath(), null, true);
                    if (fix) {
                        if (mustRemoveParentNode(node)) {
                            println(out, "Fixing invalid reference by removing node " + node.getPath() + " from repository...");
                            Node parentNode = node.getParent();
                            Calendar originalLastModificationDate = null;
                            try {
                                originalLastModificationDate = parentNode.getProperty(Constants.JCR_LASTMODIFIED).getDate();
                            } catch (PathNotFoundException pnfe) {
                                originalLastModificationDate = null;
                            }
                            Session session = node.getSession();
                            if (!parentNode.isCheckedOut()) {
                                session.getWorkspace().getVersionManager().checkout(parentNode.getPath());
                            }
                            node.remove();
                            session.save();
                            // let's reload the node to make sure we don't have any cache issues.
                            parentNode = session.getNodeByIdentifier(parentNode.getIdentifier());
                            Calendar newLastModificationDate = null;
                            try {
                                newLastModificationDate = parentNode.getProperty(Constants.JCR_LASTMODIFIED).getDate();
                            } catch (PathNotFoundException pnfe) {
                                newLastModificationDate = null;
                            }
                            if (newLastModificationDate == null && originalLastModificationDate == null) {
                                // do nothing, they are equal
                            } else if (((newLastModificationDate != null) && (originalLastModificationDate == null)) ||
                                    ((newLastModificationDate == null) && (originalLastModificationDate != null)) ||
                                    (!newLastModificationDate.equals(originalLastModificationDate))) {
                                println(out, "Last modification date ("+originalLastModificationDate.getTime().toString()+") was changed by save operation (to "+newLastModificationDate.getTime().toString()+"), must reset to old value !");
                                parentNode.setProperty(Constants.JCR_LASTMODIFIED, originalLastModificationDate);
                                session.save();
                            }
                            return false;
                        } else {
                            println(out, "Fixing invalid reference by setting reference property " + property.getPath() + " to null...");
                            Calendar originalLastModificationDate = null;
                            try {
                                originalLastModificationDate = node.getProperty(Constants.JCR_LASTMODIFIED).getDate();
                            } catch (PathNotFoundException pnfe) {
                                originalLastModificationDate = null;
                            }
                            if (property.isMultiple()) {
                                Value[] oldValues = property.getValues();
                                Value[] newValues = new Value[oldValues.length-1];
                                int i=0;
                                for (Value oldValue : oldValues) {
                                    if (!oldValue.getString().equals(uuid)) {
                                        newValues[i] = oldValue;
                                    }
                                    if (i < (oldValues.length - 2)) {
                                        i++;
                                    } else {
                                        break;
                                    }
                                }
                            } else {
                                property.setValue((Value) null);
                            }
                            Session session = node.getSession();
                            session.save();
                            // let's reload the node to make sure we don't have any cache issues.
                            node = session.getNodeByIdentifier(node.getIdentifier());
                            Calendar newLastModificationDate = null;
                            try {
                                newLastModificationDate = node.getProperty(Constants.JCR_LASTMODIFIED).getDate();
                            } catch (PathNotFoundException pnfe) {
                                newLastModificationDate = null;
                            }
                            if (newLastModificationDate == null && originalLastModificationDate == null) {
                                // do nothing, they are equal
                            } else if (((newLastModificationDate != null) && (originalLastModificationDate == null)) ||
                                    ((newLastModificationDate == null) && (originalLastModificationDate != null)) ||
                                    (!newLastModificationDate.equals(originalLastModificationDate))) {
                                println(out, "Last modification date ("+originalLastModificationDate.getTime().toString()+") was changed by save operation (to "+newLastModificationDate.getTime().toString()+"), must reset to old value !");
                                node.setProperty(Constants.JCR_LASTMODIFIED, originalLastModificationDate);
                                session.save();
                            }
                        }
                    }
                }
                break;
            default:
        }
        return true;
    }

    private boolean mustRemoveParentNode(Node node) throws RepositoryException {
        for (String nodeTypeToTest : invalidReferenceNodeTypesToRemove) {
            if (node.isNodeType(nodeTypeToTest)) {
                return true;
            }
        }
        return false;
    }

    protected void processNode(JspWriter out, Node node, Map<String, Long> results, boolean fix, boolean referencesCheck, boolean binaryCheck) throws IOException, RepositoryException {
        long nodesRead = results.get("nodesRead");
        // first let's try to read all the properties
        try {
            // node let's recurse into subnodes.
            nodesRead++;
            if (nodesRead % 1000 == 0) {
                println(out, "Processed " + nodesRead + " nodes...");
            }
            results.put("nodesRead", nodesRead);
            if (fix || referencesCheck || binaryCheck) {
                PropertyIterator propertyIterator = node.getProperties();
                while (propertyIterator.hasNext() && !mustStop) {
                    Property property = propertyIterator.nextProperty();
                    int propertyType = property.getType();
                    if (property.isMultiple()) {
                        Value[] values = property.getValues();
                        for (int i = 0; i < values.length; i++) {
                            if (!processPropertyValue(out, node, property, values[i], results, fix, referencesCheck, binaryCheck)) {
                                return;
                            }
                        }
                    } else {
                        if (!processPropertyValue(out, node, property, property.getValue(), results, fix, referencesCheck, binaryCheck)) {
                            return;
                        }
                    }
                }
            }
            NodeIterator childNodeIterator = node.getNodes();
            while (childNodeIterator.hasNext() && !mustStop) {
                Node childNode = childNodeIterator.nextNode();
                if (childNode.getName().equals("jcr:system")) {
                    println(out, "Ignoring jcr:system node and it's child objects");
                } else {
                    processNode(out, childNode, results, fix, referencesCheck, binaryCheck);
                }
            }
        } catch (ValueFormatException vfe) {
            println(out, "ValueFormatException while processing node " + node.getPath(), vfe, false);
        } catch (RepositoryException re) {
            println(out, "RepositoryException while processing node " + node.getPath(), re, false);
        }
    }

    private void renderRadio(JspWriter out, String radioValue, String radioLabel, boolean checked) throws IOException {
        out.println("<input type=\"radio\" name=\"operation\" value=\"" + radioValue
                + "\" id=\"" + radioValue + "\""
                + (checked ? " checked=\"checked\" " : "")
                + "/><label for=\"" + radioValue + "\">"
                + radioLabel
                + "</label><br/>");
    }    
    
    private void renderCheckbox(JspWriter out, String checkboxValue, String checkboxLabel, boolean checked) throws IOException {
        out.println("<input type=\"checkbox\" name=\"option\" value=\"" + checkboxValue
                + "\" id=\"" + checkboxValue + "\""
                + (checked ? " checked=\"checked\" " : "")
                + "/><label for=\"" + checkboxValue + "\">"
                + checkboxLabel
                + "</label><br/>");
    }

    private void renderWorkspaceSelector(JspWriter out) throws IOException {
        out.println("<label for=\"workspaceSelector\">Choose workspace:</label>" +
            "<select id=\"workspaceSelector\" name=\"workspace\"><option value=\"\">All Workspaces</option>");
        for (String workspace : workspaces) {    
            out.println("<option value=\"" + workspace + "\">" + workspace + "</option>");
        }
        out.println("</select><br/>");
    }    
    
    private boolean isParameterActive(HttpServletRequest request, String parameterName, String operationName) {
        String[] operationValues = request.getParameterValues(parameterName);
        if (operationValues == null) {
            return false;
        }
        for (String operationValue : operationValues) {
            if (operationValue.equals(operationName)) {
                return true;
            }
        }
        return false;
    }

    // by default in the case of an invalid reference we will simply reset it to null, but if the node type is listed
    // in the following list, we will remove the parent node completely. This can be the case for group members, where
    // if the reference doesn't exist we want to remove the parent node completely. It is recommended that this list
    // be minimal as it does delete the node !
    static String[] invalidReferenceNodeTypesToRemove = new String[] {
            "nt:linkedFile",
            "jnt:member",
            "jnt:reference"
    };

    static boolean running = false;
    static boolean mustStop = false;
    static String[] workspaces = new String[]{"default", "live"};
%>
<%
    if (request.getParameterMap().size() > 0) {

        if (isParameterActive(request, "operation", "runJCRTest")) {
            runJCRTest(out, request, pageContext, false);
        }

        if (isParameterActive(request, "operation", "fixJCR")) {
            runJCRTest(out, request, pageContext, true);
        }

        if (isParameterActive(request, "operation", "stop")) {
            mustStop = true;
        }

        out.println("<h2>Test completed.</h2>");
    } else {
        if (!running) {
            out.println("<form>");
            renderWorkspaceSelector(out);
            renderRadio(out, "runJCRTest", "Run Java Content Repository integrity check", true);
            renderCheckbox(out, "noReferencesCheck", "Do not check reference properties", false);
            renderCheckbox(out, "noBinariesCheck", "Do not check binary properties", false);
            renderRadio(out, "fixJCR", "Fix full Java Content Repository integrity (also performs check). DO NOT RUN IF PLATFORM IS ACTIVE (USERS, BACKGROUND JOBS ARE RUNNING !). Also this operation WILL DELETE node with invalid references so please backup your data before running this fix!", false);            
            out.println("<input type=\"submit\" name=\"submit\" value=\"Submit\">");
            out.println("</form>");
        } else {
            out.println("<form>");
            renderCheckbox(out, "stop", "Stop currently running check/fix", true);
            out.println("<input type=\"submit\" name=\"submit\" value=\"Submit\">");
            out.println("</form>");
        }
    }

%>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>