package org.jahia.ajax.gwt.client.widget.content;

import java.io.Serializable;

public class CustomPickerConfiguration implements Serializable {
    private String initMethodName;
    private String loadFieldValueMethodName;
    private String getFieldValueFromPickerMethodName;

    public CustomPickerConfiguration() {
    }

    public String getInitMethodName() {
        return initMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    public String getLoadFieldValueMethodName() {
        return loadFieldValueMethodName;
    }

    public void setLoadFieldValueMethodName(String loadFieldValueMethodName) {
        this.loadFieldValueMethodName = loadFieldValueMethodName;
    }

    public String getGetFieldValueFromPickerMethodName() {
        return getFieldValueFromPickerMethodName;
    }

    public void setGetFieldValueFromPickerMethodName(String getFieldValueFromPickerMethodName) {
        this.getFieldValueFromPickerMethodName = getFieldValueFromPickerMethodName;
    }
}
