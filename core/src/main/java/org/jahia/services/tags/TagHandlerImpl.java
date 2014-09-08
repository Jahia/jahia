package org.jahia.services.tags;

import org.apache.commons.lang.StringUtils;

/**
 * Created by kevan on 04/09/14.
 */
public class TagHandlerImpl implements TagHandler{
    @Override
    public String execute(String tag) {
        return StringUtils.isNotEmpty(tag) ? tag.trim().toLowerCase() : "";
    }
}
