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
package org.jahia.bundles.jcrcommands.rest;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.jahia.bundles.jcrcommands.executor.KarafCommandExecutor;
import org.jahia.osgi.BundleUtils;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Path("/api/commands")
@Produces({ MediaType.APPLICATION_JSON })
public class CommandResource {

    @POST
    @Path("{command}")
    public Response executeCommandFromPath(@PathParam("command") String command, @Context SecurityContext securityContext) {
        return executeCommand(command, securityContext);
    }

    @POST
    public Response executeCommandFromBody(String content, @Context SecurityContext securityContext) {
        return executeCommand(content, securityContext);
    }

    private Response executeCommand(String command, @Context SecurityContext securityContext) {
        try {
            String output = getKarafCommand().executeCommand(command, 1000L, securityContext.getUserPrincipal(), new RolePrincipal("manager"), new RolePrincipal("admin"));

            return Response.ok(parseResults(output)).build();
        } catch (Exception e) {
            return Response.serverError().entity((e.getMessage() != null ? e.getMessage() : e.toString()) + '\n').build();
        }
    }

    private Map<String, Object> parseResults(String output) throws IOException {
        Map<String,Object> result = new HashMap<>();

        BufferedReader reader = new BufferedReader(new StringReader(output));
        List<Map> table = null;
        List<String> raw = new ArrayList<>();
        List<String> headers = null;
        String nextLine;
        while ((nextLine = reader.readLine()) != null) {
            if (table == null && nextLine.contains("|")) {
                headers = Arrays.stream(StringUtils.split(nextLine, '|')).map(String::trim).collect(Collectors.toList());
            } else if (table == null && headers != null && nextLine.matches("^-+$")) {
                table = new ArrayList<>();
                result.put("resultsTable", table);
            } else if (table != null && nextLine.contains("|")) {
                List<String> contents = Arrays.stream(StringUtils.split(nextLine, '|')).map(String::trim).collect(Collectors.toList());
                if (contents.size() == headers.size()) {
                    table.add(IntStream.range(0, headers.size()).boxed().collect(Collectors.toMap(headers::get,contents::get)));
                }
            } else {
                table = null;
                raw.add(nextLine);
            }
        }
        if (!raw.isEmpty()) {
            result.put("output", raw);
        }
        return result;
    }

    private KarafCommandExecutor getKarafCommand() {
        return BundleUtils.getOsgiService(KarafCommandExecutor.class, null);
    }

}
