package org.jahia.modules.serversettings.forge;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.jahia.utils.i18n.Messages;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.io.Serializable;

public class Forge implements Serializable {

    private static final long serialVersionUID = 2031426003900898977L;
    String url;
    String user;
    String password;
    String id;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void validateView(ValidationContext context) {
        if (!StringUtils.equals((String) context.getUserValue("actionType"),"delete")) {
            // try basic http connexion
            try {
                GetMethod httpMethod = new GetMethod(url + "/contents/forge-modules-repository.forgeModuleList.json");
                httpMethod.addRequestHeader("Authorization", "Basic " + Base64.encode((user + ":" + password).getBytes()));
                HttpClient httpClient = new HttpClient();
                int i = httpClient.executeMethod(httpMethod);
                if (i != 200) {
                    context.getMessageContext().addMessage(new MessageBuilder()
                            .error()
                            .source("testUrl")
                            .defaultText(
                                    Messages.get("resources.JahiaServerSettings",
                                            "serverSettings.manageForges.error.cannotVerify", LocaleContextHolder.getLocale()))
                            .build());
                }
            } catch (Exception e) {
                context.getMessageContext().addMessage(new MessageBuilder()
                        .error()
                        .source("testUrl")
                        .defaultText(
                                Messages.get("resources.JahiaServerSettings",
                                        "serverSettings.manageForges.error.cannotVerify", LocaleContextHolder.getLocale()))
                        .build());
            }
        }
    }
}
