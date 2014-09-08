package org.jahia.services.tags;

/**
 * Tag handler transform a tag before store it
 *
 * Created by kevan on 04/09/14.
 */
public interface TagHandler {
    /**
     *
     * @param tag the tag to transform
     * @return the transformed tag
     */
    public String execute(String tag);
}
