package org.jahia.services.render;

import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRJahiaContentNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.JahiaService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.data.beans.TemplatePathResolverBean;
import org.jahia.data.beans.TemplatePathResolverFactory;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.JahiaData;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.content.ContentObject;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 12, 2009
 * Time: 7:06:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class RenderService extends JahiaService {
    private static volatile RenderService instance;

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

    public StringBuffer render(Resource resource, HttpServletRequest request, final HttpServletResponse response) throws RepositoryException, IOException {
        Script script = resolveScript(resource, request, response);


        Object old = request.getAttribute("currentNode");
        request.setAttribute("currentNode", resource.getNode());

        ProcessingContext threadParamBean = Jahia.getThreadParamBean();
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT,
                new LocalizationContext(new JahiaResourceBundle(threadParamBean
                        .getLocale(), threadParamBean.getSite()
                        .getTemplatePackageName()), threadParamBean.getLocale()));

        setJahiaAttributes(request, resource.getNode(), threadParamBean);

        String res = script.execute();

        request.setAttribute("currentNode",old);

        return new StringBuffer(res);
    }

    private Script resolveScript(Resource resource, HttpServletRequest request, final HttpServletResponse response) throws RepositoryException, IOException {
        return new RequestDispatcherScript(resource, request, response);
    }

    private void setJahiaAttributes(HttpServletRequest request, JCRNodeWrapper node, ProcessingContext threadParamBean) {
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
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
