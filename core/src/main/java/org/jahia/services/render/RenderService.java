package org.jahia.services.render;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRJahiaContentNode;
import org.jahia.services.JahiaService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.JahiaData;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaException;
import org.jahia.operations.valves.EngineValve;
import org.jahia.params.ParamBean;
import org.jahia.content.ContentObject;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * Service to render node
 *  
 * @author toto
 *
 */
public class RenderService extends JahiaService {

    private static volatile RenderService instance;
    
    private static final Logger logger = Logger.getLogger(RenderService.class);

    public synchronized static RenderService getInstance() {
        if (instance == null) {
            instance = new RenderService();
        }
        return instance;
    }

    private JCRStoreService storeService;

    public JCRStoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(JCRStoreService storeService) {
        this.storeService = storeService;
    }

    public void start() throws JahiaInitializationException {

    }

    public void stop() throws JahiaException {

    }

    /**
     * Render a specific resource and returns it as a StringBuffer.
     *
     * @param resource Resource to display
     * @param request Servlet request
     * @param response Servlet response
     * @return The rendered result
     * @throws RepositoryException
     * @throws IOException
     */
    public StringBuffer render(Resource resource, HttpServletRequest request, final HttpServletResponse response) throws RepositoryException, IOException {
        Script script = resolveScript(resource, request, response);


        Object old = request.getAttribute("currentNode");
        request.setAttribute("currentNode", resource.getNode());

        Object oldResource = request.getAttribute("currentResource");
        request.setAttribute("currentResource", resource);

        String res;
        try {
            setJahiaAttributes(request, resource.getNode(), (ParamBean) Jahia.getThreadParamBean());
            res = script.execute();
        } finally {
            request.setAttribute("currentNode",old);
            request.setAttribute("currentResource",oldResource);
        }

        return new StringBuffer(res);
    }

    /**
     * This resolves the executable script from the resource object. This should be able to find the proper script
     * depending of the template / template type. Currently resolves only simple RequestDispatcherScript.
     *
     * If template cannot be resolved, fall back on default template
     *
     * @param resource The resource to display
     * @param request Serlvet request
     * @param response Servlet response
     * @return An executable script
     * @throws RepositoryException
     * @throws IOException
     */
    private Script resolveScript(Resource resource, HttpServletRequest request, final HttpServletResponse response) throws RepositoryException, IOException {
//        try {
            return new RequestDispatcherScript(resource, request, response);
//        } catch (IOException e) {
//            if (resource.getTemplate() != null) {
//                return new RequestDispatcherScript(new Resource(resource.getNode(), resource.getTemplateType() ,null), request,response);
//            } else {
//                throw e;
//            }
//        }
    }

    /**
     * This set Jahia context attributes, so that legacy jahia tags can still be used in the templates
     * @param request Request where the attributes will be set
     * @param node Node to display
     * @param threadParamBean The "param bean"
     */
    private void setJahiaAttributes(HttpServletRequest request, JCRNodeWrapper node, ParamBean threadParamBean) {
        try {
            if (node instanceof JCRJahiaContentNode) {
                ContentObject obj = ((JCRJahiaContentNode)node).getContentObject();
                if (obj instanceof ContentContainer) {
                    ContentContainer c = (ContentContainer) obj;

                    ContainerBean bean = new ContainerBean(c.getJahiaContainer(threadParamBean, threadParamBean.getEntryLoadRequest()), threadParamBean);
                    request.setAttribute("container", bean);
                }
            }
            if (request.getAttribute(JahiaData.JAHIA_DATA) == null) {
                request.setAttribute(JahiaData.JAHIA_DATA,new JahiaData(threadParamBean, false));
            }
            if (request.getAttribute("jahia") == null) {
                // expose beans into the request scope  
                EngineValve.setContentAccessBeans(threadParamBean);
            }
            
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
