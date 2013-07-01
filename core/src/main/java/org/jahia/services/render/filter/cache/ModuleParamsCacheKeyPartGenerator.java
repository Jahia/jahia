package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.json.JSONObject;

import java.util.Properties;

public class ModuleParamsCacheKeyPartGenerator implements CacheKeyPartGenerator {
    @Override
    public String getKey() {
        return "moduleParams";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext, Properties properties) {
        return encodeString(new JSONObject(resource.getModuleParams()).toString()).replaceAll("\"","'");
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

    private String encodeString(String toBeEncoded) {
        return toBeEncoded != null ? toBeEncoded.replaceAll("#", "@@") : toBeEncoded;
    }

}
