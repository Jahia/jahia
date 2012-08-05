<%@ page import="javax.naming.InitialContext" %>
<%@ page import="javax.naming.Context" %>
<%@ page import="javax.naming.NamingException" %>
<%@ page import="javax.sql.DataSource" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Random" %>
<%@ page import="org.apache.jackrabbit.core.id.NodeId" %>
<%@ page import="java.io.*" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="tools.css" type="text/css"/>
    <title>Jahia Benchmark Tool</title>
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
    private long readBlob(long bytesRead, Blob currentBlob) throws SQLException, IOException {
        BufferedInputStream blobInputStream = new BufferedInputStream(currentBlob.getBinaryStream());
        while (blobInputStream.read() != -1) {
            bytesRead++;
        }
        blobInputStream.close();
        return bytesRead;
    }

    private Connection getConnection(JspWriter out) {
        String DATASOURCE_CONTEXT = "java:comp/env/jdbc/jahia";

        Connection result = null;
        try {
            Context initialContext = new InitialContext();
            DataSource datasource = (DataSource) initialContext.lookup(DATASOURCE_CONTEXT);
            if (datasource != null) {
                result = datasource.getConnection();
            } else {
                out.println("Failed to lookup datasource " + DATASOURCE_CONTEXT);
                return null;
            }
        } catch (NamingException ex) {
            ex.printStackTrace(new PrintWriter(out));
        } catch (SQLException ex) {
            ex.printStackTrace(new PrintWriter(out));
        } catch (IOException e) {
            e.printStackTrace(new PrintWriter(out));
        }
        return result;
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

    private void println(JspWriter out, String message, Throwable t) throws IOException {
        System.out.println(message);
        t.printStackTrace();
        out.println("<span class='error'>" + message + "</span>");
        errorCount++;
        out.println("<a href=\"javascript:toggleLayer('error" + errorCount + "');\" title=\"Click here to view error details\">Show/hide details</a>");
        out.println("<div id='error" + errorCount + "' class='hiddenDetails'><pre>");
        t.printStackTrace(new PrintWriter(out));
        out.println("</pre></div>");
        out.println("<br/>");
        out.flush();
    }

%>
<%
    String readTableName = "jr_default_bundle";
    String readIdColumn = "NODE_ID";
    String readBlobColumn = "BUNDLE_DATA";
    String readAllDataSQL = "SELECT " + readIdColumn + "," + readBlobColumn + " FROM " + readTableName;
    String readRowSQL = "SELECT " + readBlobColumn + " FROM " + readTableName + " WHERE " + readIdColumn + "=?";
    Set<NodeId> idCollection = new HashSet<NodeId>();
    long nbRandomLoops = 30000;
    long testFileSize = 100 * 1024 * 1024;
    int nbFileReadLoops = 10;

    Connection conn = null;

    try {
        Context ctx = new InitialContext();
        if (ctx == null)
            throw new Exception("Boom - No Context");

        DataSource ds =
                (DataSource) ctx.lookup(
                        "java:comp/env/jdbc/jahia");

        long bytesRead = 0;
        long rowsRead = 0;
        if (ds != null) {
            conn = ds.getConnection();

            if (conn != null) {
                long startTime = System.currentTimeMillis();
                PreparedStatement stmt = conn.prepareStatement(readAllDataSQL);
                ResultSet rst = stmt.executeQuery();
                while (rst.next()) {
                    byte[] byteId = rst.getBytes(1);
                    NodeId nodeId = new NodeId(byteId);
                    idCollection.add(nodeId);
                    Blob currentBlob = rst.getBlob(2);
                    long blobLength = currentBlob.length();
                    bytesRead = readBlob(bytesRead, currentBlob);
                    rowsRead++;
                }
                rst.close();
                stmt.close();
                long totalTime = System.currentTimeMillis() - startTime;
                println(out, "Total time to read " + rowsRead + " sequential rows : " + totalTime + "ms" );
                println(out, "Total bytes read sequentially: " + bytesRead);
                double sequentialReadSpeed = bytesRead / (1024.0 * 1024.0) / (totalTime / 1000.0);
                println(out, "Sequential read speed = " + sequentialReadSpeed + "MB/sec");

                NodeId[] idArray = idCollection.toArray(new NodeId[idCollection.size()]);
                println(out, "Now randomly picking rows out of " + idArray.length + "...");

                bytesRead = 0;
                rowsRead = 0;
                Random randomRow = new Random(System.currentTimeMillis());
                PreparedStatement randomRowReadStmt = conn.prepareStatement(readRowSQL);
                startTime = System.currentTimeMillis();
                for (long i=0; i < nbRandomLoops; i++) {
                    int rowPos = randomRow.nextInt(idArray.length);
                    randomRowReadStmt.setBytes(1, idArray[rowPos].getRawBytes());
                    ResultSet resultSet = randomRowReadStmt.executeQuery();
                    while (resultSet.next()) {
                        Blob currentBlob = resultSet.getBlob(1);
                        bytesRead = readBlob(bytesRead, currentBlob);
                        rowsRead++;
                    }
                }
                totalTime = System.currentTimeMillis() - startTime;
                println(out, "Total time to read " + rowsRead + " random rows : " + totalTime + "ms" );
                println(out, "Total bytes read randomly: " + bytesRead);
                double randomReadSpeed = bytesRead / (1024.0 * 1024.0) / (totalTime / 1000.0);
                println(out, "Random read speed = " + randomReadSpeed + "MB/sec");

            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (conn != null) {
            conn.close();
        }

    }
    
    File tempFile = File.createTempFile("benchmark", "tmp");

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
        println(out, "Total time to write " + bytesWritten + " bytes : " + totalTime + "ms" );
        double sequentialFileWriteSpeed = bytesWritten / (1024.0 * 1024.0) / (totalTime / 1000.0);
        println(out, "Sequential file write speed = " + sequentialFileWriteSpeed + "MB/sec");
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
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
    }
    long totalTime = System.currentTimeMillis() - startTime;
    println(out, "Total time to read " + bytesRead + " bytes : " + totalTime + "ms" );
    double sequentialFileReadSpeed = bytesRead / (1024.0 * 1024.0) / (totalTime / 1000.0);
    println(out, "Sequential file read speed = " + sequentialFileReadSpeed + "MB/sec");

    tempFile.delete();


%>
<h2>Benchmark completed.</h2>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>