package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 7:25:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleContainer extends ContentPanel {

    public ModuleContainer(final String path) {
        super(new FlowLayout());

        setCollapsible(true);
        setBodyStyleName("pad-text");
        setHeading("Content : "+path);

//        Draggable d = new Draggable(cp);

        JahiaContentManagementService.App.getInstance().getRenderedContent(path, new AsyncCallback<String>() {
            public void onSuccess(String result) {
                HTML html = new HTML(result);
                   add(html);

                Map<Element, Widget> m = new HashMap();
                List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(html.getElement());
                for (Element element : el) {
                    String jahiatype = DOM.getElementAttribute(element, JahiaType.JAHIA_TYPE);
                    if ("placeholder".equals(jahiatype)) {
                        String type = DOM.getElementAttribute(element, "type");
                        String path = DOM.getElementAttribute(element, "path");
                        String template = DOM.getElementAttribute(element, "template");
                        if (type.equals("existingNode") && "null".equals(template)) {
                            m.put(element, new ModuleContainer(path));
                            add(m.get(element));
                        }
                        if (type.equals("placeholder")) {
                            m.put(element, new Text("--placeholder / put content here--"));
                            add(m.get(element));
                        }

                    }
                }

                layout();

                for (Element element : m.keySet()) {
                    Widget container = m.get(element);
                    Element moduleElement = container.getElement();
                    DOM.appendChild(element, moduleElement);
                }

            }

            public void onFailure(Throwable caught) {
                GWT.log("error", caught);
            }
        });

    }
}
