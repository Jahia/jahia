package org.jahia.services.render.webflow;

import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.mvc.view.AbstractMvcView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Bundle view
 */
public class BundleMvcView extends AbstractMvcView {
    /**
     * Creates a new Servlet MVC view.
     * @param view the view to render
     * @param context the current flow request context.
     */
    public BundleMvcView(org.springframework.web.servlet.View view, RequestContext context) {
        super(view, context);
    }

    protected void doRender(Map model) throws Exception {
        RequestContext context = getRequestContext();
        ExternalContext externalContext = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getNativeRequest();
        HttpServletResponse response = (HttpServletResponse) externalContext.getNativeResponse();
        getView().render(model, request, response);
    }

}
