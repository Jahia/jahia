package org.jahia.services.content.nodetypes.initializers;

import org.jahia.bin.Render;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RenderModesChoiceListInitializer implements ChoiceListInitializer, ServletContextAware {

    private ServletContext servletContext;

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<ChoiceListValue> list = new ArrayList<ChoiceListValue>();

        addMappings(list, (ApplicationContext) servletContext.getAttribute(
                        "org.springframework.web.servlet.FrameworkServlet.CONTEXT.RendererDispatcherServlet"));

        addMappings(list, SpringContextSingleton.getInstance().getContext());

        return list;
    }

    private void addMappings(List<ChoiceListValue> list, ApplicationContext ctx) {
        if (ctx != null) {
            for (SimpleUrlHandlerMapping mapping : ctx.getBeansOfType(SimpleUrlHandlerMapping.class).values()) {
                for (Map.Entry<String, ?> entry : mapping.getUrlMap().entrySet()) {
                    if (entry.getKey().endsWith("/**") && entry.getValue() instanceof Render) {
                        String key = entry.getKey().substring(1,entry.getKey().lastIndexOf('/'));
                        if (!key.equals("render")) {
                            list.add(new ChoiceListValue(key,key));
                        }
                    }
                }
            }
        }
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
