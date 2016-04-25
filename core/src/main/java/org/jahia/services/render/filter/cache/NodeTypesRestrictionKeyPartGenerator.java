package org.jahia.services.render.filter.cache;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.Properties;

/**
 * Key part generator to store restrictions in cache key to be able to re add them in the request attributes
 * This restrictions are directly handle in the ModuleTag, we need to restore them to be able to recalculate the node that should'nt be display
 * by the ModuleTag
 *
 * Created by jkevan on 22/04/2016.
 */
public class NodeTypesRestrictionKeyPartGenerator implements CacheKeyPartGenerator, ContextModifierCacheKeyPartGenerator {
    @Override
    public String getKey() {
        return "restriction";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext, Properties properties) {
        Integer level = (Integer) renderContext.getRequest().getAttribute("org.jahia.modules.level");
        if(level != null) {
            String restrictions = (String) renderContext.getRequest().getAttribute("areaNodeTypesRestriction" + level);
            if(StringUtils.isNotEmpty(restrictions)) {
                return restrictions + "_" + level;
            }
        }
        return "";
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }


    @Override
    public Object prepareContentForContentGeneration(String keyValue, Resource resource, RenderContext renderContext) {
        if(StringUtils.isNotEmpty(keyValue)) {
            String[] keyValues = keyValue.split("_");
            Integer level = Integer.parseInt(keyValues[1]);
            renderContext.getRequest().setAttribute("org.jahia.modules.level", level);
            renderContext.getRequest().setAttribute("areaNodeTypesRestriction" + level, keyValues[0]);
        }
        return null;
    }

    @Override
    public void restoreContextAfterContentGeneration(String keyValue, Resource resource, RenderContext renderContext, Object previous) {
        // nothing to do
    }
}
