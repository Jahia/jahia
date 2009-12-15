package org.jahia.services.search;

import java.util.Collections;
import java.util.List;

public class SearchResponse {
    private List<Hit> results = Collections.emptyList();

    public SearchResponse() {
        super();
    }

    public List<Hit> getResults() {
        return results;
    }

    public void setResults(List<Hit> results) {
        this.results = results;
    }
}
