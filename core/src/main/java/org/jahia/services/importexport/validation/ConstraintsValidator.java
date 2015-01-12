package org.jahia.services.importexport.validation;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.xml.sax.Attributes;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.*;

/**
 * Validator that checks if all mandatory property constraints are satisfied or not before the import
 * @author Serge Huber
 */
public class ConstraintsValidator implements ImportValidator {

    private Map<String, Set<String>> missingMandatoryProperties = new TreeMap<String, Set<String>>();
    private Map<String, Set<String>> missingMandatoryI18NProperties = new TreeMap<String, Set<String>>();

    @Override
    public ValidationResult getResult() {
        return new ConstraintsValidatorResult(missingMandatoryProperties, missingMandatoryI18NProperties);
    }

    @Override
    public void validate(String decodedLocalName, String decodedQName, String currentPath, Attributes atts) {
        String pt = atts.getValue(Constants.JCR_PRIMARYTYPE);
        if (pt != null) {
            checkTypeConstraints(pt, currentPath, false, atts);
        }
        String m = atts.getValue(Constants.JCR_MIXINTYPES);
        if (m != null) {
            StringTokenizer st = new StringTokenizer(m, " ,");
            while (st.hasMoreTokens()) {
                String mixin = st.nextToken();
                checkTypeConstraints(mixin, currentPath, true, atts);
            }
        }
    }

    private boolean checkTypeConstraints(String type, String currentPath, boolean mixin, Attributes atts) {
        try {
            ExtendedNodeType extendedNodeType = NodeTypeRegistry.getInstance().getNodeType(type);
            ExtendedPropertyDefinition[] extendedPropertyDefinitions = extendedNodeType.getPropertyDefinitions();
            if (Constants.JAHIANT_TRANSLATION.equals(extendedNodeType.getName())) {
                // let's retrieve the missing I18N properties from the parent, and remove them if we find at least
                // a property value that matches
                String parentPath = StringUtils.substringBeforeLast(currentPath, "/");
                Set<String> parentMissingMandatoryI18NProperties = missingMandatoryI18NProperties.get(parentPath);
                if (parentMissingMandatoryI18NProperties != null && parentMissingMandatoryI18NProperties.size() > 0) {
                    boolean valuesFound = false;
                    for (int i=0; i < atts.getLength(); i++) {
                        String attValue = atts.getValue(i);
                        String attName = atts.getQName(i);
                        if (attValue != null && parentMissingMandatoryI18NProperties.contains(attName)) {
                            parentMissingMandatoryI18NProperties.remove(attName);
                            valuesFound = true;
                        }
                    }
                    if (valuesFound) {
                        if (parentMissingMandatoryI18NProperties.size() == 0) {
                            missingMandatoryI18NProperties.remove(parentPath);
                        } else {
                            missingMandatoryI18NProperties.put(parentPath, parentMissingMandatoryI18NProperties);
                        }
                    }
                }
            }
            for (ExtendedPropertyDefinition extendedPropertyDefinition : extendedPropertyDefinitions) {
                if (extendedPropertyDefinition.isMandatory() &&
                        !extendedPropertyDefinition.isAutoCreated() &&
                        !extendedPropertyDefinition.isProtected() &&
                        !extendedPropertyDefinition.hasDynamicDefaultValues()) {
                    if (extendedPropertyDefinition.isInternationalized()) {
                        Set<String> missingProperties = missingMandatoryI18NProperties.get(currentPath);
                        if (missingProperties == null) {
                            missingProperties = new TreeSet<String>();
                        }
                        missingProperties.add(extendedPropertyDefinition.getName());
                        missingMandatoryI18NProperties.put(currentPath, missingProperties);
                    } else {
                        if (atts.getValue(extendedPropertyDefinition.getName()) == null) {
                            if (Constants.JCR_DATA.equals(extendedPropertyDefinition.getName())) {
                                // @todo for jcr:data property we need to check if a file exists in the import, not yet implemented
                            } else {
                                Set<String> constraintsViolatedOnPath = missingMandatoryProperties.get(currentPath);
                                if (constraintsViolatedOnPath == null) {
                                    constraintsViolatedOnPath = new TreeSet<String>();
                                }
                                constraintsViolatedOnPath.add("Property [" + extendedPropertyDefinition.getName() + "] is mandatory but has no value");
                                missingMandatoryProperties.put(currentPath, constraintsViolatedOnPath);
                            }
                        }
                    }
                }
            }
            if (missingMandatoryProperties.size() > 0) {
                return false;
            }
            return true;
        } catch (NoSuchNodeTypeException e) {
            Set<String> constraintsViolatedOnPath = missingMandatoryProperties.get(currentPath);
            if (constraintsViolatedOnPath == null) {
                constraintsViolatedOnPath = new TreeSet<String>();
            }
            if (!mixin) {
                constraintsViolatedOnPath.add("Couldn't find node type definition " + type);
            } else {
                constraintsViolatedOnPath.add("Couldn't find mixin type definition " + type);
            }
            missingMandatoryProperties.put(type, constraintsViolatedOnPath);
            return false;
        }
    }
}
