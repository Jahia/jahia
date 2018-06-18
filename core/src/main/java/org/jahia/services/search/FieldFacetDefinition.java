package org.jahia.services.search;

/**
 * Definition of a field facet.<br>
 * 
 * Note, please, if you are creating a sub-class for this class and adding fields, be sure to override {@link #hashCode()} and
 * {@link #equals(Object)} methods.
 */
public class FieldFacetDefinition extends SearchCriteria.BaseFacetDefinition {

    private static final long serialVersionUID = -8262925411007789117L;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FieldFacetDefinition other = (FieldFacetDefinition) obj;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        return true;
    }
}
