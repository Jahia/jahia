package org.jahia.services.search;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.search.jcr.JahiaJCRSearchProvider;

public class SearchServiceImpl extends SearchService {
    
    private static JahiaJCRSearchProvider searchProvider = new JahiaJCRSearchProvider(); 

    /**
     * The unique instance of this service *
     */
    protected static SearchServiceImpl theObject;    
    
    /**
     * Returns the unique instance of this service.
     */
    public static SearchServiceImpl getInstance() {
        if (theObject == null) {
            synchronized (SearchServiceImpl.class) {
                if (theObject == null) {
                    theObject = new SearchServiceImpl();
                }                
            }
        }
        return theObject;
    }    
    
    @Override
    public SearchResponse search(SearchCriteria criteria) {
        // @TODO add logic to pick the right search provider
        return searchProvider.search(criteria);
    }

    @Override
    public void start() throws JahiaInitializationException {

    }

    @Override
    public void stop() throws JahiaException {

    }

}
