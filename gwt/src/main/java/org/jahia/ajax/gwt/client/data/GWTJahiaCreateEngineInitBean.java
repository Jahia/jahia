package org.jahia.ajax.gwt.client.data;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jun 14, 2010
 * Time: 7:17:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaCreateEngineInitBean implements Serializable {
    private List<GWTJahiaLanguage> languages;
    private List<GWTJahiaNodeType> mixin;
    private Map<String, List<GWTJahiaValueDisplayBean>> initializersValues;

    public GWTJahiaCreateEngineInitBean() {
    }

    public List<GWTJahiaLanguage> getLanguages() {
        return languages;
    }

    public void setLanguages(List<GWTJahiaLanguage> languages) {
        this.languages = languages;
    }

    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    public void setMixin(List<GWTJahiaNodeType> mixin) {
        this.mixin = mixin;
    }

    public Map<String, List<GWTJahiaValueDisplayBean>> getInitializersValues() {
        return initializersValues;
    }

    public void setInitializersValues(Map<String, List<GWTJahiaValueDisplayBean>> initializersValues) {
        this.initializersValues = initializersValues;
    }
}
