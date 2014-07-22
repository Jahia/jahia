package org.jahia.services.search;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Created by kevan on 21/07/14.
 */
public class SearchSettings implements Serializable {

    private static final long serialVersionUID = -7260490373906189126L;

    private String currentProvider;

    public String getCurrentProvider() {
        return currentProvider;
    }

    public void setCurrentProvider(String currentProvider) {
        this.currentProvider = currentProvider;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
