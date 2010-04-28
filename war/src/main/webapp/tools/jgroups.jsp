<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">


<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Cluster View</title>
</head>
<%!
public class StringOutputStream extends OutputStream {
    
    StringWriter stringWriter;
 
    public StringOutputStream() {
        this.stringWriter = new StringWriter();
    }
 
    @Override
    public String toString() {
        return stringWriter.toString();
    }
 
    public StringBuffer toStringBuffer() {
        return stringWriter.getBuffer();
    }
 
    @Override
    public void write(int b) throws IOException {
        this.stringWriter.write(b);
    }
}
%>
<%@ page import="java.io.*,org.jgroups.tests.Probe"%>
<%
PrintStream oldOut = System.out;
StringOutputStream myOut = null;
try {
    myOut = new StringOutputStream();
    System.setOut(new PrintStream(myOut));
	Probe.main(new String[] {"-bind-addr=" + System.getProperty("cluster.tcp.start.ip_address")});
	pageContext.setAttribute("result", myOut.toString().replaceAll("\\r\\n", "<br/>").replaceAll("\\r", "<br/>"));
} finally {
    System.setOut(oldOut);
}
%>
<body>
<h1>Cluster View: <%= System.getProperty("cluster.tcp.start.ip_address") %></h1>
<p>${result}</p>
</body>
</html>