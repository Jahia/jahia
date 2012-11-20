package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.utils.Patterns;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryStringCacheKeyPartGenerator implements CacheKeyPartGenerator {

    protected static final Pattern QUERYSTRING_REGEXP = Pattern.compile("(.*)(_qs\\[([^\\]]+)\\]_)(.*)");

    @Override
    public String getKey() {
        return "queryString";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        HttpServletRequest request = renderContext.getRequest();
        String[] params = (String[]) request.getAttribute("cache.requestParameters");
        if (params != null && params.length > 0) {
            return "_qs" + Arrays.toString(params) + "_";
        } else {
            return "";
        }
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        Matcher m = QUERYSTRING_REGEXP.matcher(keyPart);
        if (m.matches()) {
            Map parameterMap = renderContext.getRequest().getParameterMap();
            String qsString = m.group(2);
            String[] params = Patterns.COMMA.split(m.group(3));

            SortedMap<String, String> qs = new TreeMap<String, String>();
            for (String param : params) {
                param = param.trim();
                if (param.endsWith("*")) {
                    param = param.substring(0, param.length() - 1);
                    for (Map.Entry o : (Iterable<? extends Map.Entry>) parameterMap.entrySet()) {
                        String k = (String) o.getKey();
                        if (k.startsWith(param)) {
                            qs.put(k, Arrays.toString((String[]) o.getValue()));
                        }
                    }
                } else if (parameterMap.containsKey(param)) {
                    qs.put(param, Arrays.toString((String[]) parameterMap.get(param)));
                }
            }
            keyPart = keyPart.replace(qsString, qs.toString());
        }
        return keyPart;
    }
}
