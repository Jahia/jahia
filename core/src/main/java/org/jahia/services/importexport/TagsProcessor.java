package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.tags.TaggingService;

import javax.jcr.RepositoryException;

/**
 * Attribute processor that handles <code>j:tags</code> attributes
 *
 * Created by kevan on 03/09/14.
 */
public class TagsProcessor implements AttributeProcessor{
    private TaggingService taggingService;

    @Override
    public boolean process(JCRNodeWrapper node, String name, String value) throws RepositoryException {
        if (name.equals("j:tags")) {
            for (String tagValue : value.split(" ")) {
                taggingService.tag(node, ISO9075.decode(StringUtils.substringAfterLast(tagValue, "/")));
            }
            return true;
        }
        return false;
    }

    public void setTaggingService(TaggingService taggingService) {
        this.taggingService = taggingService;
    }
}
