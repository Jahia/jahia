package org.jahia.portlet.sharedmaprw;

import javax.portlet.*;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;


public class SharedMapRW extends GenericPortlet {

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
    }

    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
        System.out.println("processAction called");
        Map m = (Map) actionRequest.getAttribute("jahiaSharedMap");
        System.out.println("retrieving shared map");
        String hello = null;
        if (m == null) {
            System.out.println("no map found, creating one...");
            m = new HashMap();
        } else {
            hello = (String) m.get("hello");
        }
        if (hello == null) {
            System.out.println("adding 'hello' entry");
            m.put("hello", "world");
        } else {
            System.out.println("updating 'hello' entry");
            m.put("hello", hello + "+");
        }
        actionRequest.setAttribute("jahiaSharedMap", m);
        System.out.println("shared map updated");
    }

    public void render(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        System.out.println("render called");
        super.render(renderRequest, renderResponse);
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        System.out.println("doView called");
        response.setContentType("text/html");
        PortletURL actionURL = response.createActionURL();
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
            try {
                m.put("write", "test");
            } catch (UnsupportedOperationException ex) {
                System.out.println("attempting to write in map while rendering : DENIED (map is readonly)");
            }
        }
        buf.append("<a href='").append(actionURL.toString()).append("'>Click here to write in map</a><br />");
        response.getWriter().print(buf.toString());
    }


    public void destroy() {}
    
}
