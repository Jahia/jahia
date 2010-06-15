package org.jahia.ajax.gwt.client.data;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jun 14, 2010
 * Time: 7:17:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaEditEngineInitBean extends GWTJahiaGetPropertiesResult {
    private List<GWTJahiaNodeType> mixin;
    private Map<String, List<GWTJahiaValueDisplayBean>> initializersValues;

    public GWTJahiaEditEngineInitBean() {
    }

    public GWTJahiaEditEngineInitBean(List<GWTJahiaNodeType> nodeTypes, Map<String, GWTJahiaNodeProperty> properties) {
        super(nodeTypes, properties);
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
