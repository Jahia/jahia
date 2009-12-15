package org.jahia.services.search;

import org.jahia.services.JahiaService;

public abstract class SearchService extends JahiaService {
    public abstract SearchResponse search(SearchCriteria criteria);
}
