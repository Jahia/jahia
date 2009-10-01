package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Template;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:54:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class TemplateHelper {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TemplateHelper.class);

    private JCRSessionFactory sessionFactory;
    private RenderService renderService;

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }

    /**
     * Get rendered content
     *
     * @param path
     * @param workspace
     * @param locale
     * @param template
     * @param templateWrapper
     * @param editMode
     * @param ctx             @return   @throws GWTJahiaServiceException
     */
    public String getRenderedContent(String path, String workspace, Locale locale, String template, String templateWrapper, boolean editMode, ParamBean ctx) throws GWTJahiaServiceException {
        String res = null;
        try {
            if (locale == null) {
                locale = ctx.getLocale();
            }
            JCRSessionWrapper session = sessionFactory.getCurrentUserSession(workspace, locale);
            JCRNodeWrapper node = session.getNode(path);
            Resource r = new Resource(node, "html", null, template);
            ctx.getRequest().setAttribute("mode", "edit");
            RenderContext renderContext = new RenderContext(ctx.getRequest(), ctx.getResponse(), ctx.getUser());
            renderContext.setSite(ctx.getSite());
            renderContext.setEditMode(editMode);
            renderContext.setMainResource(r);
            r.pushWrapper(templateWrapper);
//            renderContext.setTemplateWrapper(templateWrapper);
            res = renderService.render(r, renderContext);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return res;

    }

    public List<String[]> getTemplatesSet(String path, ProcessingContext ctx) throws GWTJahiaServiceException {
        List<String[]> templatesPath = new ArrayList<String[]>();
        try {
            JCRNodeWrapper node = sessionFactory.getCurrentUserSession().getNode(path);
            String def = null;
            if (node.hasProperty("j:defaultTemplate")) {
                templatesPath.add(new String[]{"--unset--", "--unset--"});
                def = node.getProperty("j:defaultTemplate").getString();
            }

            SortedSet<Template> set = getTemplatesSet(node);
            for (Template s : set) {
                String tpl;
                if (s.getOwnerPackage() == null) {
                    tpl = "Default";
                } else {
                    tpl = s.getOwnerPackage().getName();
                }
                if (s.getKey().equals(def)) {
                    templatesPath.add(new String[]{s.getKey(), "* " + s.getKey() + "(" + tpl + ")"});
                } else {
                    templatesPath.add(new String[]{s.getKey(), s.getKey() + "(" + tpl + ")"});
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return templatesPath;
    }

    public SortedSet<Template> getTemplatesSet(JCRNodeWrapper node) throws RepositoryException {
        ExtendedNodeType nt = node.getPrimaryNodeType();
        SortedSet<Template> set;
        SortedSet<Template> result = new TreeSet<Template>();
        if (node.getPrimaryNodeTypeName().equals("jnt:nodeReference")) {

            set = getTemplatesSet((JCRNodeWrapper) node.getProperty("j:node").getNode());
        } else if (node.isNodeType("jnt:contentList") || node.isNodeType("jnt:containerList")) {
            set = renderService.getTemplatesSet(nt);
            List<JCRNodeWrapper> l = node.getChildren();
            for (JCRNodeWrapper c : l) {
                set.addAll(getTemplatesSet(c));
            }
        } else {
            set = renderService.getTemplatesSet(nt);
        }
        for (Template template : set) {
            if (!template.getKey().startsWith("wrapper") && !template.getKey().startsWith("skin")) {
                result.add(template);
            }
        }

        return result;
    }
}
