package org.jahia.services.render;

import org.jahia.api.Constants;
import org.jahia.bin.Edit;
import org.jahia.bin.Render;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.content.JCRStoreService;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.Transformer;

import java.util.Map;
import java.util.HashMap;

/**
 * Main URL generation class. This class is exposed to the template developers to make it easy to them to access
 * basic URLs such as <code>${url.edit}</code>, <code>${url.userProfile}</code>.
 * User: toto
 * Date: Sep 14, 2009
 * Time: 11:13:37 AM
 */
public class URLGenerator {

    private String base;

    private String live;
    private String edit;
    private String preview;

    private String userProfile;

    private Resource resource;
    private RenderContext context;
    private JCRStoreService jcrStoreService;
    
    private Map<String, String> languages;

    private Map<String, String> templates;
    
    private String templatesPath;

    public URLGenerator(RenderContext context, Resource resource, JCRStoreService jcrStoreService) {
        this.context = context;
        this.resource = resource;
        this.jcrStoreService = jcrStoreService;
        initURL();
        context.setURLGenerator(this);
    }

    /**
     * Set workspace url as attribute of the current request
     */
    protected void initURL() {
        if (context.isEditMode()) {
            base = getContext() + Edit.getEditServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        } else {
            base = getContext() + Render.getRenderServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        }

        final String resourcePath = context.getMainResource().getNode().getPath()  + ".html";

        live = getContext() + Render.getRenderServletPath() + "/" + Constants.LIVE_WORKSPACE + "/" + resource.getLocale() + resourcePath;
        edit = getContext() + Edit.getEditServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale() + resourcePath;
        preview = getContext() + Render.getRenderServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale() + resourcePath;
        
        templatesPath = getContext() + "/templates";
    }

    public String getContext() {
        return context.getRequest().getContextPath();
    }

    public String getFiles() {
        return getContext() + "/files";
    }

    public String getBase() {
        return base;
    }

    public String getLive() {
        return live;
    }

    public String getEdit() {
        return edit;
    }

    public String getPreview() {
        return preview;
    }

    public String getUserProfile() {
        if (userProfile == null) {
            if (!JahiaUserManagerService.isGuest(context.getUser())) {
                userProfile = buildURL(jcrStoreService.getUserFolders(null, context.getUser()).iterator().next(), null);
            }
        }
        return userProfile;
    }

    public String getCurrentModule() {
        return getTemplatesPath() + "/" + ((Script) context.getRequest().getAttribute("script")).getTemplate().getModule().getRootFolder();
    }

    public String getCurrent() {
        return buildURL(resource.getNode(), resource.getForcedTemplate());
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getLanguages() {
        if (languages == null) {
            languages = LazyMap.decorate(new HashMap(), new Transformer() {
                public Object transform(Object lang) {
                    String servletPath;
                    if (context.isEditMode()) {
                        servletPath = Edit.getEditServletPath();
                    } else {
                        servletPath = Render.getRenderServletPath();
                    }
                    return getContext() + servletPath + "/" + resource.getWorkspace() + "/" + lang + resource.getNode().getPath() + ".html";
                }
            });
        }
        
        return languages;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getTemplates() {
        if (templates == null) {
            templates = LazyMap.decorate(new HashMap(), new Transformer() {
                public Object transform(Object template) {
                    return buildURL(resource.getNode(), (String) template);
                }
            });
        }
        return templates;
    }

    /**
     * Returns the path to the templates folder.
     * @return the path to the templates folder
     */
    public String getTemplatesPath() {
        return templatesPath;
    }

    /**
     * Returns the URL of the main resource (normally, page), depending on the
     * current mode.
     * 
     * @return the URL of the main resource (normally, page), depending on the
     *         current mode
     */
    public String getMainResource() {
        if (context.isEditMode()) {
            return getEdit();
        } else {
            return Constants.LIVE_WORKSPACE.equals(resource.getWorkspace()) ? live : preview;
        }
    }
    
    public String buildURL(JCRNodeWrapper node, String template) {
        return base + node.getPath() + (template!=null?"."+template:"") + ".html";
    }
}
