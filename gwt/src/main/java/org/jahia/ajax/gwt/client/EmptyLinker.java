package org.jahia.ajax.gwt.client;

import org.jahia.ajax.gwt.client.data.toolbar.GWTConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.List;
import java.util.Map;

public class EmptyLinker implements Linker {

    private GWTEditConfiguration configuration;
    private LinkerSelectionContext selectionContext = new LinkerSelectionContext();

    public EmptyLinker(GWTEditConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void loaded() {

    }

    @Override
    public void loading(String resource) {

    }

    @Override
    public void refresh(Map<String, Object> data) {

    }

    @Override
    public void select(Object o) {

    }

    @Override
    public void setSelectPathAfterDataUpdate(List<String> paths) {

    }

    @Override
    public LinkerSelectionContext getSelectionContext() {
        return selectionContext;
    }

    @Override
    public void syncSelectionContext(int context) {

    }

    @Override
    public GWTConfiguration getConfig() {
        return configuration;
    }

    @Override
    public boolean isDisplayHiddenProperties() {
        return false;
    }
}
