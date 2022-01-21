package org.jahia.bundles.provisioning.rest;

import org.apache.commons.io.IOUtils;
import org.jahia.settings.SettingsBean;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Interceptor that performs variable substitution
 */

public class VariableSubstitutionInterceptor implements ReaderInterceptor {
    private static final List<String> ALLOWED_MEDIA_TYPES = Arrays.asList(YamlProvider.APPLICATION_YAML, MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON);
    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        String contentType = context.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        if (contentType != null && ALLOWED_MEDIA_TYPES.contains(contentType)) {
            context.setInputStream(new ByteArrayInputStream(SettingsBean.getInstance().replaceBySubsitutor(IOUtils.toString(context.getInputStream())).getBytes()));
        }
        return context.proceed();
    }
}
