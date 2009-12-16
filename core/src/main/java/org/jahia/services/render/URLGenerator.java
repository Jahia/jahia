package org.jahia.services.render;

import org.jahia.api.Constants;
import org.jahia.bin.Edit;
import org.jahia.bin.Render;
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

    public URLGenerator(RenderContext context, Resource resource, JCRStoreService jcrStoreService) {
        this.context = context;
        this.resource = resource;
        this.jcrStoreService = jcrStoreService;
        initURL();
    }

    /**
     * Set workspace url as attribute of the current request
     */
    protected void initURL() {
        if (context.isEditMode()) {
            base = context.getRequest().getContextPath() + Edit.getEditServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        } else {
            base = context.getRequest().getContextPath() + Render.getRenderServletPath() + "/" + resource.getWorkspace() + "/" + resource.getLocale();
        }

        final String resourcePath = context.getMainResource().getNode().getPath()  + ".html";

        live = context.getRequest().getContextPath() + Render.getRenderServletPath() + "/" + Constants.LIVE_WORKSPACE + "/" + resource.getLocale() + resourcePath;
        edit = context.getRequest().getContextPath() + Edit.getEditServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale() + resourcePath;
        preview = context.getRequest().getContextPath() + Render.getRenderServletPath() + "/" + Constants.EDIT_WORKSPACE + "/" + resource.getLocale() + resourcePath;
    }

    public String getContext() {
        return context.getRequest().getContextPath();
    }

    public String getFiles() {
        return context.getRequest().getContextPath() + "/files";
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
                userProfile = base + jcrStoreService.getUserFolders(null, context.getUser()).iterator().next().getPath() + ".html";
            }
        }
        return userProfile;
    }

    public String getCurrentModule() {
        return context.getRequest().getContextPath() + "/templates/" + ((Script) context.getRequest().getAttribute("script")).getTemplate().getModule().getRootFolder();
    }

    public String getCurrent() {
        if (resource.getForcedTemplate() != null) {
            return base + resource.getNode().getPath() + "." + resource.getForcedTemplate() + ".html";
        } else {
            return base + resource.getNode().getPath() + ".html";
        }
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
                    return context.getRequest().getContextPath() + servletPath + "/" + resource.getWorkspace() + "/" + lang + resource.getNode().getPath() + ".html";
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
                    return base + resource.getNode().getPath() + "." + template + ".html";
                }
            });
        }
        return templates;
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

}
