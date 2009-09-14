package org.jahia.services.render;

import org.jahia.api.Constants;
import org.jahia.bin.Edit;
import org.jahia.bin.Render;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.Transformer;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
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

    private Resource resource;
    private RenderContext context;

    public URLGenerator(RenderContext context, Resource resource) {
        this.context = context;
        this.resource = resource;
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
