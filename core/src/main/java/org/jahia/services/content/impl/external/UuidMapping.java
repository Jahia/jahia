package org.jahia.services.content.impl.external;

import javax.persistence.*;

@Entity
@Table(name = "jahia_external_mapping")
public class UuidMapping {

    private String internalUuid;
    private String providerKey;
    private String externalId;
//    private String extendingNodeUuid;

    public UuidMapping() {
    }

    @Id
    @Column(length = 36, nullable = false)
    public String getInternalUuid() {
        return internalUuid;
    }

    public void setInternalUuid(String internalUuid) {
        this.internalUuid = internalUuid;
    }

    @Column(nullable = false)
    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    @Column(nullable = false)
    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

//    @Column(length = 36)
//    public String getExtendingNodeUuid() {
//        return extendingNodeUuid;
//    }
//
//    public void setExtendingNodeUuid(String extendingNodeUuid) {
//        this.extendingNodeUuid = extendingNodeUuid;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UuidMapping that = (UuidMapping) o;

        if (externalId != null ? !externalId.equals(that.externalId) : that.externalId != null) return false;
        if (providerKey != null ? !providerKey.equals(that.providerKey) : that.providerKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = providerKey != null ? providerKey.hashCode() : 0;
        result = 31 * result + (externalId != null ? externalId.hashCode() : 0);
        return result;
    }
}
