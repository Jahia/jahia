package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpSession;
import java.util.LinkedList;

/**
 * Filter that logs into the session main resources visited by the user
 */
public class HistoryTrackerFilter extends AbstractFilter {
    private HistoryTrackerBean historyTrackerBean;

    public void setHistoryTrackerBean(HistoryTrackerBean historyTrackerBean) {
        this.historyTrackerBean = historyTrackerBean;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        HttpSession session = renderContext.getRequest().getSession();
        historyTrackerBean.addHistoryNode(session, renderContext.getMainResource().getNode().getIdentifier());
        return super.execute(previousOut, renderContext, resource, chain);
    }

}
