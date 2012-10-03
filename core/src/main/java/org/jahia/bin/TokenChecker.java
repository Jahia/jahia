package org.jahia.bin;

import org.apache.commons.collections.CollectionUtils;
import org.jahia.settings.SettingsBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenChecker {

    public static final int NO_TOKEN = 0;
    public static final int VALID_TOKEN = 1;
    public static final int INVALID_TOKEN = 2;
    public static final int INVALID_HIDDEN_FIELDS = 3;
    public static final int INVALID_CAPTCHA = 4;

    public static int checkToken(HttpServletRequest req, HttpServletResponse resp, Map<String, List<String>> parameters) throws UnsupportedEncodingException {
        String token = parameters.get("form-token")!=null?parameters.get("form-token").get(0):null;
        if (token != null) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, List<String>>> toks = (Map<String, Map<String, List<String>>>) req.getSession().getAttribute("form-tokens");
            if (toks != null && toks.containsKey(token)) {
                Map<String, List<String>> m = toks.remove(token);
                if (m == null) {
                    return INVALID_TOKEN;
                }
                Map<String, List<String>> values = new HashMap<String, List<String>>(m);

                // Validate form token
                List<String> stringList1 = values.remove("form-action");
                String formAction = stringList1.isEmpty()?null:stringList1.get(0);
                String characterEncoding = SettingsBean.getInstance().getCharacterEncoding();
                if (formAction == null ||
                        (!URLDecoder.decode(req.getRequestURI(), characterEncoding).equals(URLDecoder.decode(formAction, characterEncoding)) &&
                        !URLDecoder.decode(resp.encodeURL(req.getRequestURI()), characterEncoding).equals(URLDecoder.decode(formAction, characterEncoding)))
                        ) {
                    return INVALID_HIDDEN_FIELDS;
                }
                if (!req.getMethod().equalsIgnoreCase(values.remove("form-method").get(0))) {
                    return INVALID_HIDDEN_FIELDS;
                }
                for (Map.Entry<String, List<String>> entry : values.entrySet()) {
                    List<String> stringList = entry.getValue();
                    List<String> parameterValues = parameters.get(entry.getKey());
                    if (parameterValues == null || !CollectionUtils.isEqualCollection(stringList, parameterValues)) {
                        if (entry.getKey().equals(Render.CAPTCHA)) {
                            return INVALID_CAPTCHA;
                        }
                        return INVALID_HIDDEN_FIELDS;
                    }
                }
                return VALID_TOKEN;
            }
        }
        return NO_TOKEN;
    }
}
