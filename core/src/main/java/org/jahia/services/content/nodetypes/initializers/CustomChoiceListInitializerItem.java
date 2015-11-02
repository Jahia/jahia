package org.jahia.services.content.nodetypes.initializers;


/**
 * @author : faissah
 */
public class CustomChoiceListInitializerItem{
    private String displayName;
    private String value;
    private String mixin;

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setMixin(String mixin) {
        this.mixin = mixin;
    }

    public String getMixin() {
        return mixin;
    }
}
