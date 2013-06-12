<%@ page contentType="text/html;charset=UTF-8" language="java"
        %><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.apache.jackrabbit.core.id.NodeId"%>
<%@ page import="org.jahia.services.usermanager.jcr.JCRUserManagerProvider" %>
<%@ page import="javax.jcr.*" %>
<%@ page import="javax.sql.DataSource" %>
<%@ page import="java.io.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.jahia.settings.SettingsBean" %>
<%@ page import="org.jahia.services.SpringContextSingleton" %>
<%@ page import="org.hibernate.*" %>
<%@ page import="javax.jcr.Session" %>
<%@ page import="org.jahia.services.content.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:set var="workspace" value="${functions:default(param.workspace, 'default')}"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="tools.css" type="text/css"/>
    <title>Jahia Benchmark Tool</title>
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
            color: #FFDD00;
        }
    </style>
</head>
<body>
<h1>Jahia System Benchmark Tool</h1>

<p>
    This tool will benchmark the database read performance as well as perform both read and write performance
    checks for the filesystem. This is in now way a exhaustive tool but will allow to check if the performance is
    nominal or not.
</p>

<p>
    It is recommended to execute the tests multiple times to make sure that the results are stable and significant.
    You can re-run a test simply by reloading the JSP.
</p>

<h2>Running tests...</h2>
<%!

    // the following code was copied from DatabaseUtils class since we need to be able to use it with 6.6.0.1 that
    // didn't include this class.

    private static String dbType;

    public static String getDatabaseType() {
        if (dbType == null) {
            dbType = StringUtils.substringBefore(
                    StringUtils.substringBefore(SettingsBean.getInstance().getPropertiesFile()
                            .getProperty("db_script").trim(), "."), "_");
        }
        return dbType;
    }

    public static DataSource getDatasource() {
        return (DataSource) SpringContextSingleton.getBean("dataSource");
    }

    public static void closeQuietly(Object closable) {
        if (closable == null) {
            return;
        }
        try {
            if (closable instanceof Connection) {
                ((Connection) closable).close();
            } else if (closable instanceof Statement) {
                ((Statement) closable).close();
            } else if (closable instanceof ResultSet) {
                ((ResultSet) closable).close();
            } else if (closable instanceof ScrollableResults) {
                ((ScrollableResults) closable).close();
            } else if (closable instanceof org.hibernate.Session) {
                ((org.hibernate.Session) closable).close();
            } else if (closable instanceof StatelessSession) {
                ((StatelessSession) closable).close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // end of copied code.

    private void runWorkspaceDBTest(JspWriter out, String readAllDataSQL, String readRowSQL, String tableName, long nbRandomLoops, Connection conn, boolean keyTypeLongLong) throws SQLException, IOException {
        Set<NodeId> idCollection = new HashSet<NodeId>();
        out.println("<h4>Table " + tableName + "</h4>");
        long bytesRead = 0;
        long rowsRead = 0;
        long startTime = System.currentTimeMillis();
        PreparedStatement stmt = conn.prepareStatement(readAllDataSQL);
        ResultSet rst = null;
        try {
            rst = stmt.executeQuery();
            while (rst.next()) {
                NodeId nodeId = null;
                if (keyTypeLongLong) {
                    nodeId = new NodeId(rst.getLong("NODE_ID_HI"), rst.getLong("NODE_ID_LO"));
                } else {
                    byte[] byteId = rst.getBytes("NODE_ID");
                    nodeId = new NodeId(byteId);
                }
                idCollection.add(nodeId);
                bytesRead += readBlob(rst, keyTypeLongLong ? 3 : 2);
                rowsRead++;
            }
        } finally {
            closeQuietly(rst);
            closeQuietly(stmt);
        }
        long totalTime = System.currentTimeMillis() - startTime;
        println(out, "Total time to read " + rowsRead + " sequential rows : " + totalTime + "ms");
        println(out, "Total bytes read sequentially: " + bytesRead);
        double sequentialReadSpeed = bytesRead / (1024.0 * 1024.0) / (totalTime / 1000.0);
        println(out, "Sequential read speed = " + sequentialReadSpeed + "MB/sec");

        NodeId[] idArray = idCollection.toArray(new NodeId[idCollection.size()]);
        println(out, "Now randomly picking rows out of " + idArray.length + "...");

        bytesRead = 0;
        rowsRead = 0;
        Random randomRow = new Random(System.currentTimeMillis());
        PreparedStatement randomRowReadStmt = conn.prepareStatement(readRowSQL);
        try {
            startTime = System.currentTimeMillis();
            for (long i = 0; i < nbRandomLoops; i++) {
                int rowPos = randomRow.nextInt(idArray.length);
                if (keyTypeLongLong) {
                    randomRowReadStmt.setLong(1, idArray[rowPos].getMostSignificantBits());
                    randomRowReadStmt.setLong(2, idArray[rowPos].getLeastSignificantBits());
                } else {
                    randomRowReadStmt.setBytes(1, idArray[rowPos].getRawBytes());
                }
                ResultSet resultSet = randomRowReadStmt.executeQuery();
                try {
                    while (resultSet.next()) {
                        bytesRead += readBlob(resultSet, 1);
                        rowsRead++;
                    }
                } finally {
                    closeQuietly(resultSet);
                }
            }
        } finally {
            closeQuietly(randomRowReadStmt);
        }
        totalTime = System.currentTimeMillis() - startTime;
        println(out, "Total time to read " + rowsRead + " random rows : " + totalTime + "ms");
        println(out, "Total bytes read randomly: " + bytesRead);
        double randomReadSpeed = bytesRead / (1024.0 * 1024.0) / (totalTime / 1000.0);
        println(out, "Random read speed = " + randomReadSpeed + "MB/sec");

        idCollection.clear();
        idArray = null;
    }

    private void runDBTest(JspWriter out) throws IOException, SQLException {
        printTestName(out, "Database");

        String dbType = getDatabaseType();
        println(out, "Database type:" + dbType);
        boolean keyTypeLongLong = dbType.equals("derby") || dbType.equals("postgresql");

        String readEditTableName = "jr_default_bundle";
        String readLiveTableName = "jr_live_bundle";
        String readIdColumn = "NODE_ID";
        String readBlobColumn = "BUNDLE_DATA";
        String readAllEditDataSQL = "SELECT " + readIdColumn + "," + readBlobColumn + " FROM " + readEditTableName;
        String readEditRowSQL = "SELECT " + readBlobColumn + " FROM " + readEditTableName + " WHERE " + readIdColumn + "=?";
        String readAllLiveDataSQL = "SELECT " + readIdColumn + "," + readBlobColumn + " FROM " + readLiveTableName;
        String readLiveRowSQL = "SELECT " + readBlobColumn + " FROM " + readLiveTableName + " WHERE " + readIdColumn + "=?";

        if (keyTypeLongLong) {
            readAllEditDataSQL = "SELECT NODE_ID_HI,NODE_ID_LO,BUNDLE_DATA FROM " + readEditTableName;
            readEditRowSQL = "SELECT BUNDLE_DATA FROM "+readEditTableName+" WHERE NODE_ID_HI=? AND NODE_ID_LO=?";
            readAllLiveDataSQL = "SELECT NODE_ID_HI,NODE_ID_LO,BUNDLE_DATA FROM " + readLiveTableName;
            readLiveRowSQL = "SELECT BUNDLE_DATA FROM "+readLiveTableName+" WHERE NODE_ID_HI=? AND NODE_ID_LO=?";
        }

        long nbRandomLoops = 30000;

        Connection conn = null;

        try {
            DataSource ds = getDatasource();

            if (ds != null) {
                conn = ds.getConnection();

                if (conn != null) {
                    runWorkspaceDBTest(out, readAllEditDataSQL, readEditRowSQL, readEditTableName, nbRandomLoops, conn, keyTypeLongLong);
                    runWorkspaceDBTest(out, readAllLiveDataSQL, readLiveRowSQL, readLiveTableName, nbRandomLoops, conn, keyTypeLongLong);
                }
            }
        } catch (Throwable t) {
            println(out, "Error while accessing database", t, false);
        } finally {
            closeQuietly(conn);
        }
    }

    private void runFileSystemTest(JspWriter out) throws IOException {
        printTestName(out, "File system");
        long testFileSize = 100 * 1024 * 1024;
        int nbFileReadLoops = 10;
        File tempFile = File.createTempFile("benchmark", "tmp" + System.getProperty("cluster.node.serverId", ""));

        BufferedOutputStream fileOut = null;
        Random randomValue = new Random(System.currentTimeMillis());
        try {
            long bytesWritten = 0;
            long startTime = System.currentTimeMillis();

            fileOut = new BufferedOutputStream(new FileOutputStream(tempFile));
            for (long l = 0; l < testFileSize; l++) {
                fileOut.write(randomValue.nextInt());
                bytesWritten++;
            }
            fileOut.flush();
            long totalTime = System.currentTimeMillis() - startTime;
            println(out, "Total time to write " + bytesWritten + " bytes : " + totalTime + "ms");
            double sequentialFileWriteSpeed = bytesWritten / (1024.0 * 1024.0) / (totalTime / 1000.0);
            println(out, "Sequential file write speed = " + sequentialFileWriteSpeed + "MB/sec");
        } catch (Throwable t) {
            println(out, "Error writing to file " + tempFile, t, false);
        } finally {
            if (fileOut != null) {
                fileOut.close();
            }
        }

        long bytesRead = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < nbFileReadLoops; i++) {
            BufferedInputStream fileIn = null;
            try {
                fileIn = new BufferedInputStream(new FileInputStream(tempFile));
                while (fileIn.read() != -1) {
                    bytesRead++;
                }
            } catch (Throwable t) {
                println(out, "Error reading file " + tempFile, t, false);
            } finally {
                if (fileIn != null) {
                    fileIn.close();
                }
            }
        }
        long totalTime = System.currentTimeMillis() - startTime;
        println(out, "Total time to read " + bytesRead + " bytes : " + totalTime + "ms");
        double sequentialFileReadSpeed = bytesRead / (1024.0 * 1024.0) / (totalTime / 1000.0);
        println(out, "Sequential file read speed = " + sequentialFileReadSpeed + "MB/sec");

        tempFile.delete();
    }

    private void runJCRTest(JspWriter out, HttpServletRequest request, JspContext pageContext) throws IOException {
        long startTime;
        long bytesRead;
        long totalTime;
        printTestName(out, "Java Content Repository");
        try {
            JCRSessionFactory.getInstance().setCurrentUser(JCRUserManagerProvider.getInstance().lookupRootUser());
            JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession((String) pageContext.getAttribute("workspace"));

            JCRWorkspaceWrapper workspaceWrapper = sessionWrapper.getWorkspace();
            Locale locale = sessionWrapper.getLocale();
            println(out, "Traversing " + workspaceWrapper.getName() + " workspace with locale " + locale + "...");
            JCRNodeWrapper rootNode = sessionWrapper.getRootNode();
            startTime = System.currentTimeMillis();
            bytesRead = 0;
            Map<String, Long> results = new HashMap<String, Long>();
            results.put("bytesRead", 0L);
            results.put("nodesRead", 0L);
            processNode(out, rootNode, results);
            bytesRead = results.get("bytesRead");
            long nodesRead = results.get("nodesRead");
            totalTime = System.currentTimeMillis() - startTime;
            println(out, "Total time to read JCR " + nodesRead + " nodes data (" + bytesRead + " bytes) : " + totalTime + "ms");
            double jcrReadSpeed = bytesRead / (1024.0 * 1024.0) / (totalTime / 1000.0);
            println(out, "JCR read speed = " + jcrReadSpeed + "MB/sec");

        } catch (Throwable t) {
            println(out, "Error reading JCR ", t, false);
        }
    }

    private void runJCRWriteTest(final JspWriter out, HttpServletRequest request, JspContext pageContext) throws IOException {
        long startTime;
        long bytesRead;
        long totalTime;
        printTestName(out, "Java Content Repository Write");
        try {
            JCRSessionFactory.getInstance().setCurrentUser(JCRUserManagerProvider.getInstance().lookupRootUser());
            JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession((String) pageContext.getAttribute("workspace"));

            JCRWorkspaceWrapper workspaceWrapper = sessionWrapper.getWorkspace();
            Locale locale = sessionWrapper.getLocale();
            println(out, "Creating nodes in " + workspaceWrapper.getName() + " workspace with 100 threads...");
            JCRNodeWrapper rootNode = sessionWrapper.getRootNode();
            String tmpName = "tmp" + System.getProperty("cluster.node.serverId", "");
            if (rootNode.hasNode(tmpName)) {
                rootNode.getNode(tmpName).remove();
                sessionWrapper.save();
            }
            final JCRNodeWrapper tmp = rootNode.addNode(tmpName, "nt:unstructured");
            sessionWrapper.save();

            startTime = System.currentTimeMillis();
            final Map<String, Long> results = new HashMap<String, Long>();
            results.put("nodesWrite", 0L);

            try {
                List<Thread> l = new ArrayList<Thread>();
                for (int i = 0; i < 100; i++) {
                    final int threadId = i;
                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            try {
                                String path = "/tmp" + System.getProperty("cluster.node.serverId", "");
                                for (int j = 0; j < 100; j++) {
                                    final int iterationId = j;
                                    final String fPath = path;
                                    String nodePath = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {

                                        public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                            JCRNodeWrapper n = session.getNode(fPath).addNode("benchmark"+threadId+"_"+iterationId);
                                            synchronized (results) {
                                                long nodesWrite = results.get("nodesWrite");
                                                nodesWrite++;
                                                if (nodesWrite % 1000 == 0) {
                                                    try {
                                                        println(out, "Processed " + nodesWrite + " nodes...");
                                                    } catch (IOException e) {
                                                    }
                                                }
                                                results.put("nodesWrite", nodesWrite);
                                            }
                                            session.save();
                                            return n.getPath();
                                        }
                                    });
                                    if (j % 50 == 0) {
                                        path = nodePath;
                                    }
                                }
                            } catch (RepositoryException e) {
                                e.printStackTrace();
                            }
                            try {
                                println(out, "  ..  benchmark thread "+threadId + " ended.");
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }

                    }, "benchmark"+i);
                    l.add(t);
                    t.start();
                }

                for (Thread thread : l) {
                    thread.join();
                }

                long nodesWrite = results.get("nodesWrite");
                totalTime = System.currentTimeMillis() - startTime;
                println(out, "Total time to write JCR " + nodesWrite + " : " + totalTime + "ms");

            } finally {
                tmp.remove();
                sessionWrapper.save();
            }

        } catch (Throwable t) {
            println(out, "Error writing JCR ", t, false);
        }
    }

    private void printTestName(JspWriter out, String testName) throws IOException {
        out.println("<h3>");
        println(out, testName);
        out.println("</h3>");
    }

    private long readBlob(ResultSet rs, int columnIndex) throws SQLException, IOException {
        long bytesRead = 0;
        BufferedInputStream blobInputStream = new BufferedInputStream(rs.getMetaData().getColumnType(columnIndex) == Types.BLOB ? rs.getBlob(columnIndex).getBinaryStream() : rs.getBinaryStream(columnIndex));
        try {
            while (blobInputStream.read() != -1) {
                bytesRead++;
            }
        } finally {
            blobInputStream.close();
        }
        return bytesRead;
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
        t.printStackTrace();
        if (warning) {
            out.println("<span class='warning'>" + message + "</span>");
        } else {
            out.println("<span class='error'>" + message + "</span>");
        }
        errorCount++;
        out.println("<a href=\"javascript:toggleLayer('error" + errorCount + "');\" title=\"Click here to view error details\">Show/hide details</a>");
        out.println("<div id='error" + errorCount + "' class='hiddenDetails'><pre>");
        t.printStackTrace(new PrintWriter(out));
        out.println("</pre></div>");
        out.println("<br/>");
        out.flush();
    }

    protected void processPropertyValue(JspWriter out, JCRNodeWrapper node, Property property, Value propertyValue, Map<String, Long> results) throws RepositoryException, IOException {
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
                    JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) node.getSession().getNodeByUUID(uuid);
                } catch (ItemNotFoundException infe) {
                    println(out, "Couldn't find referenced node with UUID " + uuid + " referenced from " + property.getPath(), infe, true);
                }
                break;
            default:
        }

    }

    protected boolean processNode(JspWriter out, JCRNodeWrapper node, Map<String, Long> results) throws RepositoryException, IOException {
        long nodesRead = results.get("nodesRead");
        // first let's try to read all the properties
        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();
            int propertyType = property.getType();
            if (property.isMultiple()) {
                Value[] values = property.getValues();
                for (int i = 0; i < values.length; i++) {
                    processPropertyValue(out, node, property, values[i], results);
                }
            } else {
                processPropertyValue(out, node, property, property.getValue(), results);
            }
        }
        // node let's recurse into subnodes.
        nodesRead++;
        if (nodesRead % 1000 == 0) {
            println(out, "Processed " + nodesRead + " nodes...");
        }
        if (nodesRead > 10000) {
            println(out, "Reached 10000 nodes, stopping benchmark test normally.");
            return false;
        }
        results.put("nodesRead", nodesRead);
        NodeIterator childNodeIterator = node.getNodes();
        while (childNodeIterator.hasNext()) {
            JCRNodeWrapper childNode = (JCRNodeWrapper) childNodeIterator.nextNode();
            if (childNode.getName().equals("jcr:system") || childNode.getPath().equals("/modulesFileSystem")) {
                println(out, "Ignoring " + childNode.getName() + " node and it's child objects");
            } else {
                if (!processNode(out, childNode, results)) {
                    break;
                }
            }
        }
        return true;
    }

    private void renderCheckbox(JspWriter out, String checkboxValue, String checkboxLabel, boolean checked) throws IOException {
        out.println("<input type=\"checkbox\" name=\"operation\" value=\"" + checkboxValue
                + "\" id=\"" + checkboxValue + "\""
                + (checked ? " checked=\"checked\" " : "")
                + "/><label for=\"" + checkboxValue + "\">"
                + checkboxLabel
                + "</label><br/>");
    }

    private boolean isParameterActive(HttpServletRequest request, String operationName) {
        String[] operationValues = request.getParameterValues("operation");
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

%>
<%
    if (request.getParameterMap().size() > 0) {

        if (isParameterActive(request, "runDBTest")) {
            runDBTest(out);
        }

        if (isParameterActive(request, "runFileSystemTest")) {
            runFileSystemTest(out);
        }

        if (isParameterActive(request, "runJCRTest")) {
            runJCRTest(out, request, pageContext);
        }

        if (isParameterActive(request, "runJCRWriteTest")) {
            runJCRWriteTest(out, request, pageContext);
        }

        out.println("<h2>Benchmark completed.</h2>");
    } else {
        out.println("<form>");
        renderCheckbox(out, "runDBTest", "Run database benchmark", true);
        renderCheckbox(out, "runFileSystemTest", "Run file system benchmark", true);
        renderCheckbox(out, "runJCRTest", "Run Java Content Repository read benchmark", true);
        renderCheckbox(out, "runJCRWriteTest", "Run Java Content Repository write benchmark", true);
        out.println("<input type=\"submit\" name=\"submit\" value=\"Submit\">");
        out.println("</form>");
    }

%>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>