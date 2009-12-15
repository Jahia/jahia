package org.jahia.services.search;

public interface SearchProvider {
    public abstract SearchResponse search(SearchCriteria criteria);
}
