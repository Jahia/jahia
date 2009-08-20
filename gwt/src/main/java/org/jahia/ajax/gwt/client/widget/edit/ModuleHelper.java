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
    public static Map<Element, Module> parse(LayoutContainer w, HTML html, EditManager editManager) {
        Map<Element, Module> m = new HashMap<Element,Module>();
        List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(html.getElement());
        for (Element divElement : el) {
            String jahiatype = DOM.getElementAttribute(divElement, JahiaType.JAHIA_TYPE);
            if ("placeholder".equals(jahiatype)) {
                String type = DOM.getElementAttribute(divElement, "type");
                String path = DOM.getElementAttribute(divElement, "path");
                //String template = DOM.getElementAttribute(divElement, "template");
                Module module = null;
                if (type.equals("list")) {
                    module = new ListModule(path, divElement.getInnerHTML(), editManager);
                } else if (type.equals("existingNode")) {
                    module = new SimpleModule(path, divElement.getInnerHTML(), editManager);
                } else if (type.equals("placeholder")) {
                    module  = new PlaceholderModule(path, editManager);
                }

                if (module != null) {
                    m.put(divElement, module);
                    divElement.setInnerHTML("");
                    w.add(module.getContainer());
                }
            }
        }

        return m;
    }

    public static void move(Map<Element, Module> m) {
        for (Element divElement : m.keySet()) {
            Element moduleElement = m.get(divElement).getContainer().getElement();
            divElement.setInnerHTML("");
            DOM.appendChild(divElement, moduleElement);
        }
    }

}
