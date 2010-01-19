package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Label;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.GWTLanguageSwitcherLocaleBean;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 6:34:40 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PropertiesTabItem extends EditEngineTabItem {
    protected PropertiesEditor propertiesEditor;
    protected Map<String, PropertiesEditor> langPropertiesEditorMap;
    protected String dataType;
    protected List<String> excludedTypes;
    protected boolean multiLang = false;


    protected PropertiesTabItem(String title, AbstractContentEngine engine, String dataType) {
        super(title, engine);
        this.dataType = dataType;
        langPropertiesEditorMap = new HashMap<String, PropertiesEditor>();
    }

    /**
     * Get properties editor of the default lang
     *
     * @return
     */
    public PropertiesEditor getPropertiesEditor() {
        return propertiesEditor;
    }

    /**
     * Get properties editor by langCode
     *
     * @param locale
     * @return
     */
    public PropertiesEditor getPropertiesEditorByLang(GWTLanguageSwitcherLocaleBean locale) {
        if (langPropertiesEditorMap == null || locale == null) {
            return null;
        }
        return langPropertiesEditorMap.get(locale.getCountryIsoCode());
    }

    /**
     * set properties editor by lang
     *
     * @param locale
     */
    private void setPropertiesEditorByLang(GWTLanguageSwitcherLocaleBean locale) {
        if (langPropertiesEditorMap == null || locale == null) {
            return;
        }
        langPropertiesEditorMap.put(locale.getCountryIsoCode(), propertiesEditor);
    }

    @Override
    public void create(GWTLanguageSwitcherLocaleBean locale) {
        if (engine.getMixin() != null) {
            if (propertiesEditor != null) {
                Log.debug("remove old properties editor from parents");
                propertiesEditor.removeFromParent();
            }
            if (!isMultiLang()) {
                setProcessed(true);
            }
            propertiesEditor = getPropertiesEditorByLang(locale);

            if (propertiesEditor == null) {
                if (engine.isExistingNode() && engine.getNode().getNodeTypes().contains("jmix:shareable")) {
                    Label label = new Label("Important : This is a shared node, editing it will modify its value for all its usages");
                    label.setStyleAttribute("color", "rgb(200,80,80)");
                    label.setStyleAttribute("font-size", "14px");
                    add(label);
                }

                propertiesEditor = new PropertiesEditor(engine.getNodeTypes(), engine.getMixin(), engine.getProperties(), false, true, dataType, null, excludedTypes, !engine.isExistingNode() || engine.getNode().isWriteable(), true);

                setPropertiesEditorByLang(locale);
            }

            postCreate();

            layout();
        }
    }


    /**
     * call after created
     */
    public void postCreate() {
        add(propertiesEditor);
    }

    @Override
    public boolean isProcessed() {
        // is processed is handled only for non-multilang engines
        return !isMultiLang() && super.isProcessed();
    }

    public boolean isMultiLang() {
        return multiLang;
    }

    public void setMultiLang(boolean multiLang) {
        this.multiLang = multiLang;
    }


    /**
     * Get lang properties per map
     *
     * @return
     */
    public Map<String, List<GWTJahiaNodeProperty>> getLangPropertiesMap() {
        Map<String, List<GWTJahiaNodeProperty>> mapProperties = new HashMap<String, List<GWTJahiaNodeProperty>>();
        Iterator<String> langCodes = langPropertiesEditorMap.keySet().iterator();
        while (langCodes.hasNext()) {
            String langCode = langCodes.next();
            mapProperties.put(langCode, langPropertiesEditorMap.get(langCode).getProperties());
        }
        return mapProperties;
    }


}
