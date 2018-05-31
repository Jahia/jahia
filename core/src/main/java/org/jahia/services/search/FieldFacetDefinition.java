package org.jahia.services.search;

/**
 * Definition of a field facet.
 */
public class FieldFacetDefinition extends SearchCriteria.BaseFacetDefinition {

    private String fieldName;

    /**
     * Create a field facet definition instance.
     *
     * @param id the unique identifier for this facet definition
     * @param fieldName Facet field name
     * @param maxGroups The max number of result groups the facet should return
     */
    public FieldFacetDefinition(String id, String fieldName, int maxGroups) {
        super(id, maxGroups);
        this.fieldName = fieldName;
    }

    /**
     * @return Facet field name
     */
    public String getFieldName() {
        return fieldName;
    }
}
