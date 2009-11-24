package org.jahia.services.render.filter;

import org.jahia.services.render.RenderService;

/**
 * Base filter, giving access to the RenderService
 *
 */
public abstract class AbstractFilter implements RenderFilter {

    protected RenderService service;

    public void setService(RenderService service) {
        this.service = service;
    }
}
