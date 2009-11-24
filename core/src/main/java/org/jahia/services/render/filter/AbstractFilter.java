package org.jahia.services.render.filter;

import org.jahia.services.render.RenderService;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 24, 2009
 * Time: 3:26:00 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractFilter implements RenderFilter {

    protected RenderService service;

    public void setService(RenderService service) {
        this.service = service;
    }
}
