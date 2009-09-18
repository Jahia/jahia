package org.jahia.services.render;

import org.jahia.api.Constants;
import org.jahia.bin.Edit;
import org.jahia.bin.Render;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.Transformer;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Main URL generation class. This class is exposed to the template developers to make it easy to them to access
 * basic URLs such as
 * ${url.edit}
 * ${url.userProfile}
 * User: toto
 * Date: Sep 14, 2009
 * Time: 11:13:37 AM
 * To change this template use File | Settings | File Templates.
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

    public URLGenerator(RenderContext context, Resource resource, JCRStoreService jcrStoreService) {
        this.context = context;
        this.resource = resource;
        this.jcrStoreService = jcrStoreService;
        initURL();
    }

    /**
     * Set worksapce url as attribute of the current request
     */
    protected void initURL() {
        if (context.isEditMode()) {
            base = context.getRequest().getContextPath()+Edit.getEditServletPath()+ "/"+resource.getWorkspace()+"/"+resource.getLocale();
        } else {
            base = context.getRequest().getContextPath()+Render.getRenderServletPath()+ "/"+resource.getWorkspace()+"/"+resource.getLocale();
        }

        live = context.getRequest().getContextPath()+ Render.getRenderServletPath()+ "/"+ Constants.LIVE_WORKSPACE +"/"+resource.getLocale();
        edit = context.getRequest().getContextPath()+ Edit.getEditServletPath()+ "/"+Constants.EDIT_WORKSPACE+"/"+resource.getLocale();
        preview = context.getRequest().getContextPath()+ Render.getRenderServletPath()+ "/"+Constants.EDIT_WORKSPACE+"/"+resource.getLocale();

        final String resourcePath = resource.getNode().getPath();

        live += resourcePath + ".html";
        edit += resourcePath + ".html";
        preview += resourcePath + ".html";
        if (!context.getUser().getName().equals(JahiaUserManagerService.GUEST_USERNAME)) {
            List<JCRNodeWrapper> userFolders = jcrStoreService.getUserFolders(null,context.getUser());
            String userHomePath = userFolders.iterator().next().getPath();
            userProfile = base + userHomePath + ".html";
        }
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
        return userProfile;
    }

    public String getCurrent() {
        return base + resource.getNode().getPath() +".html";
    }

    public Map<String,String> getLanguages() {
        return LazyMap.decorate(new HashMap(), new Transformer() {
            public Object transform(Object lang) {
                return context.getRequest().getContextPath()+Edit.getEditServletPath()+ "/"+resource.getWorkspace()+"/"+lang+ resource.getNode().getPath() +".html";
            }
        });
    }

    public Map<String,String> getTemplates() {
        return LazyMap.decorate(new HashMap(), new Transformer() {
            public Object transform(Object template) {
                return base + resource.getNode().getPath() +"."+template+".html";
            }
        });
    }
}
