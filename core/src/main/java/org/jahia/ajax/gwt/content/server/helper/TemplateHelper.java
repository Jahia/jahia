package org.jahia.ajax.gwt.content.server.helper;

import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.initializers.Templates;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:54:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class TemplateHelper {
    private static JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TemplateHelper.class);

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
    public static String getRenderedContent(String path, String workspace, Locale locale, String template, String templateWrapper, boolean editMode, ParamBean ctx) throws GWTJahiaServiceException {
        String res = null;
        try {
            if (locale == null) {
                locale = ctx.getLocale();
            }
            JCRSessionWrapper session = sessionFactory.getThreadSession(ctx.getUser(), workspace, locale);
            JCRNodeWrapper node = session.getNode(path);
            Resource r = new Resource(node, "html", null, template);
            ctx.getRequest().setAttribute("mode", "edit");
            RenderContext renderContext = new RenderContext(ctx.getRequest(), ctx.getResponse(), ctx.getUser());
            renderContext.setSite(ctx.getSite());
            renderContext.setEditMode(editMode);
            renderContext.setMainResource(r);
            renderContext.setTemplateWrapper(templateWrapper);
            res = RenderService.getInstance().render(r, renderContext);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return res;

    }

    public static List<String[]> getTemplatesSet(String path, ProcessingContext ctx) throws GWTJahiaServiceException {
        List<String[]> templatesPath = new ArrayList<String[]>();
        try {
            JCRNodeWrapper node = sessionFactory.getThreadSession(ctx.getUser()).getNode(path);
            String def = null;
            if (node.hasProperty("j:defaultTemplate")) {
                templatesPath.add(new String[]{"--unset--", "--unset--"});
                def = node.getProperty("j:defaultTemplate").getString();
            }

            SortedSet<Templates.Template> set = getTemplatesSet(node);
            for (Templates.Template s : set) {
                String tpl;
                if (s.getOwnerPackage() == null) {
                    tpl = "Default";
                } else {
                    tpl = s.getOwnerPackage().getName();
                }
                if (s.getKey().equals(def)) {
                    templatesPath.add(new String[]{s.getKey(), "* " + tpl + " : " + s.getKey()});
                } else {
                    templatesPath.add(new String[]{s.getKey(), tpl + " : " + s.getKey()});
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return templatesPath;
    }

    public static SortedSet<Templates.Template> getTemplatesSet(JCRNodeWrapper node) throws RepositoryException {
        ExtendedNodeType nt = node.getPrimaryNodeType();
        SortedSet<Templates.Template> set;
        if (node.getPrimaryNodeTypeName().equals("jnt:nodeReference")) {

            set = getTemplatesSet((JCRNodeWrapper) node.getProperty("j:node").getNode());
        } else if (node.isNodeType("jnt:contentList") || node.isNodeType("jnt:containerList")) {
            set = Templates.getTemplatesSet(nt);
            List<JCRNodeWrapper> l = node.getChildren();
            for (JCRNodeWrapper c : l) {
                set.addAll(getTemplatesSet(c));
            }
        } else {
            set = Templates.getTemplatesSet(nt);
        }
        return set;
    }
}
