package org.jahia.services.render.webflow;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class JahiaFlowUrlHandler extends DefaultFlowUrlHandler {

    @Override
    public String getFlowExecutionKey(HttpServletRequest request) {
        JCRNodeWrapper n = (JCRNodeWrapper) request.getAttribute("currentNode");
        if (n != null) {
            try {
                return request.getParameter( "webflow-execution-" + n.getIdentifier());
            } catch (RepositoryException e) {
                e.printStackTrace();
                return super.getFlowExecutionKey(request);
            }
        }
        return super.getFlowExecutionKey(request);
    }

    @Override
    public String getFlowId(HttpServletRequest request) {
        WebflowDispatcherScript script = (WebflowDispatcherScript) request.getAttribute("script");
        if (script != null) {
            String path = script.getFlowPath();
            return StringUtils.substringAfter(path, "/flow/");
        }

        return super.getFlowId(request);
    }

    @Override
    public String createFlowExecutionUrl(String flowId, String flowExecutionKey, HttpServletRequest request) {
        WebflowDispatcherScript script = (WebflowDispatcherScript) request.getAttribute("script");
        JCRNodeWrapper n = (JCRNodeWrapper) request.getAttribute("currentNode");
        if (script != null && n != null) {
            StringBuffer path = new StringBuffer(request.getRequestURI());
            path.append('?');
            Map<String,String> params = new HashMap<String, String>();
            try {
                params.put("webflow-execution-" + n.getIdentifier(), flowExecutionKey);
                appendQueryParameters(path, params, getEncodingScheme(request));
            } catch (RepositoryException e) {
                e.printStackTrace();
                return super.createFlowExecutionUrl(flowId, flowExecutionKey, request);
            }
            return path.toString();
        }

        return super.createFlowExecutionUrl(flowId, flowExecutionKey, request);
    }

    @Override
    public String createFlowDefinitionUrl(String flowId, AttributeMap input, HttpServletRequest request) {
        return super.createFlowDefinitionUrl(flowId, input, request);
    }

}
