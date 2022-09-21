/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.bundles.provisioning.rest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.provisioning.ProvisioningManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.jahia.bundles.provisioning.rest.YamlProvider.APPLICATION_YAML;

/**
 * Provisioning resource
 */
@Path("/api/provisioning")
public class ProvisioningResource {
    private static final Logger logger = LoggerFactory.getLogger(ProvisioningResource.class);

    private ProvisioningManager getService() {
        return BundleUtils.getOsgiService(ProvisioningManager.class, null);
    }

    /**
     * Execute a single script
     *
     * @param script the script
     * @return result
     */
    @Consumes({APPLICATION_YAML, APPLICATION_JSON})
    @POST
    public Response execute(List<Map<String, Object>> script) {
        try {
            getService().executeScript(script);
        } catch (Exception e) {
            logger.error("Cannot execute script", e);
            return Response.serverError().entity(e.getMessage()).build();
        }

        return Response.ok().build();
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response executeMultipart(@FormDataParam("script") FormDataBodyPart script, @FormDataParam("file") List<FormDataBodyPart> files) {
        List<File> tmpFiles = new ArrayList<>();
        List<String> result = new ArrayList<>();
        try {
            String scriptAsString = IOUtils.toString(script.getEntityAs(BodyPartEntity.class).getInputStream(), StandardCharsets.UTF_8);
            List<Map<String,Object>> sc = getService().parseScript(scriptAsString, script.getMediaType().getSubtype());
            Map<String, FileSystemResource> resources = new HashMap<>();
            if (files != null) {
                for (FormDataBodyPart file : files) {
                    //Creating temp file in order to define a specific name
                    final BodyPartEntity entity = file.getEntityAs(BodyPartEntity.class);
                    File tmpFile = File.createTempFile("tmp-", "." + StringUtils.substringAfterLast(file.getFormDataContentDisposition().getFileName(), "."));
                    tmpFiles.add(tmpFile);
                    entity.moveTo(tmpFile);
                    resources.put(file.getFormDataContentDisposition().getFileName(), new FileSystemResource(tmpFile));
                }
            }
            Map<String, Object> context = new HashMap<>();
            context.put("resources", resources);
            context.put("result", result);
            getService().executeScript(sc, context);
        } catch (Exception e) {
            logger.error("Cannot execute script", e);
            return Response.serverError().entity(e.getMessage()).build();
        }finally {
            for (File tmpFile : tmpFiles) {
                FileUtils.deleteQuietly(tmpFile);
            }
        }
        return Response.status(Response.Status.OK)
                .entity(result)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
