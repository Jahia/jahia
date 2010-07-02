package org.jahia.bin;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * Result of the action handler execution.
 * User: toto
 * Date: Mar 18, 2010
 * Time: 3:14:41 PM
 */
public class ActionResult {
    
    public static final ActionResult OK = new ActionResult(HttpServletResponse.SC_OK);
    public static final ActionResult BAD_REQUEST = new ActionResult(HttpServletResponse.SC_BAD_REQUEST);


    private JSONObject json;

    private int resultCode;

    private String url;

    public ActionResult(int resultCode) {
        this(resultCode, null);
    }

    public ActionResult(int resultCode, String url) {
        this(resultCode, url, null);
    }

    public ActionResult(int resultCode, String url, JSONObject json) {
        super();
        this.resultCode = resultCode;
        this.url = url;
        this.json = json;
    }

    public JSONObject getJson() {
        return json;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getUrl() {
        return url;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
