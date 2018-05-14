package org.jahia.services.search;

/**
 * Definition of a field facet.
 */
public class FieldFacetDefinition extends SearchCriteria.BaseFacetDefinition {

    private String fieldName;

    /**
     * Create a field facet definition instance.
     *
     * @param fieldName Facet field name
     */
    public FieldFacetDefinition(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * @return Facet field name
     */
    public String getFieldName() {
        return fieldName;
    }
}
