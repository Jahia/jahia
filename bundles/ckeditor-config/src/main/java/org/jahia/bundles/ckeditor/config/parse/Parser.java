package org.jahia.bundles.ckeditor.config.parse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Parser {

    private static final String ALLOW_ELEMENTS_KEY = "allow";
    private static final String DISALLOW_ELEMENTS_KEY = "disallow";

    public abstract String parseToJsonString(JSONObject json);

    public static PolicyFactory parseToPolicy(JSONObject json) {
        HtmlPolicyBuilder builder = new HtmlPolicyBuilder();
        JSONObject filtering = json.getJSONObject("htmlFiltering");
        JSONArray allowed = filtering.has(ALLOW_ELEMENTS_KEY) ? filtering.getJSONArray(ALLOW_ELEMENTS_KEY) : null;
        JSONArray disallow = filtering.has(DISALLOW_ELEMENTS_KEY) ? filtering.getJSONArray(DISALLOW_ELEMENTS_KEY) : null;

        if (allowed != null) {
            allowed.forEach(jsonObject -> {
                String element = ((JSONObject)jsonObject).getString("name");
                JSONArray attr = ((JSONObject)jsonObject).has("attributes") ? ((JSONObject)jsonObject).getJSONArray("attributes") : null;

                if (attr != null) {
                    builder.allowAttributes(jsonArrayToArray(attr)).onElements(element);
                }

                builder.allowElements(element);
            });
        }

        if (disallow != null) {
            disallow.forEach(jsonObject -> {
                String element = ((JSONObject)jsonObject).getString("name");
                JSONArray attr = ((JSONObject)jsonObject).has("attributes") ? ((JSONObject)jsonObject).getJSONArray("attributes") : null;

                if (attr != null) {
                    builder.disallowAttributes(jsonArrayToArray(attr)).onElements(element);
                }

                builder.disallowElements(element);
            });
        }

        return builder.toFactory();
    };

    private static String[] jsonArrayToArray(JSONArray array) {
        String[] a = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            a[i] = array.getString(i);
        }
        return a;
    }
}
