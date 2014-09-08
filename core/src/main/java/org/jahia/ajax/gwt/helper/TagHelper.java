package org.jahia.ajax.gwt.helper;

import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.tags.TaggingService;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Map;

/**
 * Created by kevan on 01/07/14.
 */
public class TagHelper {
    private TaggingService taggingService;

    public Map<String, Long> getTags(String prefix, String startPath, Long minCount, Long limit, Long offset, boolean sortByCount, JCRSessionWrapper session)
            throws ItemNotFoundException, RepositoryException {
        return taggingService.getTagsSuggester().suggest(prefix, startPath, minCount, limit, offset, sortByCount, session);
    }

    public String convert(String tag){
        return taggingService.getTagHandler().execute(tag);
    }

    public void setTaggingService(TaggingService taggingService) {
        this.taggingService = taggingService;
    }
}
