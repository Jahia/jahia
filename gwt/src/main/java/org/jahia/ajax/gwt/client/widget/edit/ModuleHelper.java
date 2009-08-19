package org.jahia.ajax.gwt.client.widget.edit;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTML;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;
import org.jahia.ajax.gwt.client.core.JahiaType;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:04:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleHelper {
    public static void parse(LayoutContainer w, HTML html) {
        Map<Element, Widget> m = new HashMap();
        List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(html.getElement());
        for (Element divElement : el) {
            String jahiatype = DOM.getElementAttribute(divElement, JahiaType.JAHIA_TYPE);
            if ("placeholder".equals(jahiatype)) {
                String type = DOM.getElementAttribute(divElement, "type");
                String path = DOM.getElementAttribute(divElement, "path");
                String template = DOM.getElementAttribute(divElement, "template");
                if (type.equals("list")) {
                    m.put(divElement, new ListModule(path, divElement.getInnerHTML()));
                    divElement.setInnerHTML("");
                    w.add(m.get(divElement));
                } else if (type.equals("existingNode")) {
                    m.put(divElement, new SimpleModule(path, divElement.getInnerHTML()));
                    divElement.setInnerHTML("");
                    w.add(m.get(divElement));
                } else if (type.equals("placeholder")) {
                    m.put(divElement, new PlaceholderModule(path));
                    w.add(m.get(divElement));
                }

            }
        }

        w.layout();

        for (Element divElement : m.keySet()) {
            Element moduleElement = m.get(divElement).getElement();
            String s = divElement.getInnerHTML();
            divElement.setInnerHTML("");
            DOM.appendChild(divElement, moduleElement);
        }
    }

}
