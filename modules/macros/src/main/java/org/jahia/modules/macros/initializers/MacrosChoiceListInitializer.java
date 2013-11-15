package org.jahia.modules.macros.initializers;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Damien GAILLARD
 * Date: 11/13/13
 * Time: 4:50 PM
 */
public class MacrosChoiceListInitializer implements ModuleChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(MacrosChoiceListInitializer.class);
    private String key;

    private String[] macroLookupPath;

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<ChoiceListValue> macrosNames = new ArrayList<ChoiceListValue>();
        JCRNodeWrapper node = null;

        if (context.containsKey("contextNode") && context.get("contextNode") != null) {
            node = (JCRNodeWrapper)context.get("contextNode");
        }else if (context.containsKey("contextParent") && context.get("contextParent") != null) {
            node = (JCRNodeWrapper)context.get("contextParent");
        }

        if (node != null){
            try{
                Set<JahiaTemplatesPackage> packages = new LinkedHashSet<JahiaTemplatesPackage>();

                for (String s : node.getResolveSite().getInstalledModules()){
                    JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(s);
                    packages.add(pack);
                    packages.addAll(pack.getDependencies());
                }

                for (JahiaTemplatesPackage aPackage : packages){
                    for (String path : macroLookupPath){
                        org.springframework.core.io.Resource[] resources = aPackage.getResources(path);
                        for (org.springframework.core.io.Resource resource : resources){
                            String macroNameSplit = "##" + StringUtils.substringBefore(resource.getFilename(), ".") + "##";
                            macrosNames.add(new ChoiceListValue(macroNameSplit, macroNameSplit));
                        }
                    }
                }
            }catch(RepositoryException e){
                logger.error("Cannot resolve site", e);
            }
        }
        return macrosNames;
    }

    public void setMacroLookupPath(String macroLookupPath) {
        this.macroLookupPath = macroLookupPath.split(",");
    }
}
