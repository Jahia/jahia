package org.jahia.services.search;

/**
 * Definition of a field facet.<br>
 * 
 * Note, please, if you are creating a sub-class for this class and adding fields, be sure to override {@link #hashCode()} and
 * {@link #equals(Object)} methods.
 */
public class FieldFacetDefinition extends SearchCriteria.BaseFacetDefinition {

    private static final long serialVersionUID = -8262925411007789117L;

    private static final String FIELD_NAME = "fieldName";

    /**
     * Create a field facet definition instance.
     *
     * @param id the unique identifier for this facet definition
     * @param fieldName Facet field name
     * @param maxGroups The max number of result groups the facet should return
     */
    public FieldFacetDefinition(String id, String fieldName, int maxGroups) {
        super(id, maxGroups);
        setField(FIELD_NAME, fieldName);
    }

    /**
     * @return Facet field name
     */
    public String getFieldName() {
        return (String) getField(FIELD_NAME);
    }
}
