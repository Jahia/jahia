package org.jahia.portlet.sharedmapextr;

import javax.portlet.*;
import java.util.Map;
import java.io.IOException;


public class SharedMapExtR extends GenericPortlet {

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
    }

    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
        System.out.println("process action called");
    }

    public void render(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        System.out.println("render called");
        super.render(renderRequest, renderResponse);
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        System.out.println("doView called");
        response.setContentType("text/html");
        StringBuilder buf = new StringBuilder("<div>CONTENT FOUND IN MAP:</div><br />\n");
        Map m = (Map) request.getAttribute("jahiaSharedMap");
        if (m == null) {
            buf.append("<div>...no map found...</div>\n<br />\n");
        } else {
            if (m.size() == 0) {
                buf.append("<div>...map is empty...</div>\n<br />\n");
            } else {
                buf.append("<ul>");
                for (Object o: m.keySet()) {
                    Object entry = m.get(o);
                    buf.append("\n<li><span>").append(o.toString()).append("</span><span>:</span><span>").append(entry != null ? entry.toString() : "null").append("</span></li>");
                }
                buf.append("</ul><br />\n");
            }
        }
        response.getWriter().print(buf.toString());
    }

    public void destroy() {}
    
}
