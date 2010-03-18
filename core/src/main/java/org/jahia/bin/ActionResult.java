package org.jahia.bin;

import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 18, 2010
 * Time: 3:14:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActionResult {

    private int resultCode;

    private String url;

    private JSONObject json;

    public ActionResult(int resultCode, String url, JSONObject json) {
        this.resultCode = resultCode;
        this.url = url;
        this.json = json;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }
}
