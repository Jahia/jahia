<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="javax.jcr.RepositoryException" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.json.JSONArray" %>
<%
    Logger logger = Logger.getLogger(this.getClass());
    try {
        JCRNodeWrapper node = (JCRNodeWrapper) pageContext.getAttribute("currentNode",PageContext.REQUEST_SCOPE);
        final Map<String, String> stringMap = node.getPropertiesAsString();
        Map<String, String> map = new HashMap<String, String>(stringMap.size());
        for (Map.Entry<String, String> stringStringEntry : stringMap.entrySet()) {
            map.put(stringStringEntry.getKey().replace(":", "_"), stringStringEntry.getValue());
        }
        JSONArray nodeJSON = new JSONArray(map);
        nodeJSON.write(pageContext.getOut());
    } catch (RepositoryException e) {
        logger.error(e.getMessage(), e);
    } catch (JSONException e) {
        logger.error(e.getMessage(), e);
    }
%>