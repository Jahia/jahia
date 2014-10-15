/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.modules.serversettings.forge;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.jahia.bin.Jahia;
import org.jahia.commons.Version;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.notification.HttpClientService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service to manage Forges
 */

public class ForgeService {

    static Logger logger = LoggerFactory.getLogger(ForgeService.class);

    private HttpClientService httpClientService;
    private Set<Forge> forges = new HashSet<Forge>();

    private static List<Module> modules = new ArrayList<Module>();
    private long loadModulesDelay;
    private static long lastModulesLoad = new Date().getTime();
    private static boolean flushModules = true;

    public ForgeService() {
        loadForges();
    }

    public Set<Forge> getForges() {
        return forges;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void addForge(Forge forge) {
        for (Forge f : forges) {
            if (StringUtils.equals(forge.getId(), f.getId())) {
                f.setUser(forge.getUser());
                f.setUrl(forge.getUrl());
                f.setPassword(forge.getPassword());
                return;
            }
        }
        forges.add(forge);
    }

    public void removeForge(Forge forge) {
        for (Forge f : forges) {
            if (StringUtils.equals(forge.getId(), f.getId())) {
                forges.remove(f);
                return;
            }
        }
    }

    public void loadForges() {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    if (session.itemExists("/settings/forgesSettings")) {
                        Node forgesRoot = session.getNode("/settings/forgesSettings");
                        if (forgesRoot != null) {
                            NodeIterator ni = forgesRoot.getNodes();
                            while (ni.hasNext()) {
                                Node n = ni.nextNode();
                                Forge f = new Forge();
                                f.setId(n.getIdentifier());
                                f.setUrl(n.getProperty("j:url").getString());
                                f.setUser(n.getProperty("j:user").getString());
                                f.setPassword(n.getProperty(JCRUserNode.J_PASSWORD).getString());
                                forges.add(f);
                            }
                        }
                    }
                    return null;
                }
            });

        } catch (RepositoryException e) {
            logger.error(e.getMessage(),e);
        }
    }

    public void saveForges() {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                    Node forgesRoot;

                    // delete all previous nodes
                    try {
                        forgesRoot = session.getNode("/settings/forgesSettings");
                        forgesRoot.remove();
                    } catch (PathNotFoundException e) {
                        // do nothing
                    }

                    if (!session.getNode("/").hasNode("settings")) {
                        session.getNode("/").addNode("settings", "jnt:globalSettings");
                        session.save();
                    }
                    if (!session.getNode("/settings").hasNode("forgesSettings")) {
                        session.getNode("/settings").addNode("forgesSettings", "jnt:forgesServerSettings");
                        session.save();
                    }

                    forgesRoot = session.getNode("/settings/forgesSettings");
                    // write all forges
                    for (Forge forge : forges) {
                        Node forgeNode = forgesRoot.addNode(JCRContentUtils.generateNodeName(forge.getUrl()), "jnt:forgeServerSettings");
                        forgeNode.setProperty("j:url", forge.getUrl());
                        forgeNode.setProperty("j:user", forge.getUser());
                        forgeNode.setProperty(JCRUserNode.J_PASSWORD, forge.getPassword());
                        forge.setId(forgeNode.getIdentifier());
                    }
                    session.save();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(),e);
        }
    }

    public Module findModule(String name, String groupId) {
        for (Module m : modules) {
            if (StringUtils.equals(name, m.getId()) && m.getGroupId().equals(groupId)) {
                return m;
            }
        }
        return null;
    }


    public List<Module> loadModules() {
        if(flushModules || (lastModulesLoad + loadModulesDelay) < new Date().getTime()){
            modules.clear();
            for (Forge forge : forges) {
                String url = forge.getUrl() + "/contents/modules-repository.moduleList.json";
                Map<String, String> headers = new HashMap<String, String>();
                if (!StringUtils.isEmpty(forge.getUser())) {
                    headers.put("Authorization", "Basic " + Base64.encode((forge.getUser() + ":" + forge.getPassword()).getBytes()));
                }
                headers.put("accept", "application/json");

                String jsonModuleList = httpClientService.executeGet(url, headers);
                try {
                    JSONArray modulesRoot = new JSONArray(jsonModuleList);

                    JSONArray moduleList = modulesRoot.getJSONObject(0).getJSONArray("modules");
                    for (int i = 0; i < moduleList.length(); i++) {
                        boolean add = true;

                        final JSONObject moduleObject = moduleList.getJSONObject(i);
                        for (Module m : modules) {
                            if (StringUtils.equals(m.getId(), moduleObject.getString("name")) && StringUtils.equals(m.getGroupId(), moduleObject.getString("groupId"))) {
                                add = false;
                                break;
                            }
                        }
                        if (add) {
                            final JSONArray moduleVersions = moduleObject.getJSONArray("versions");

                            SortedMap<Version, JSONObject> sortedVersions = new TreeMap<Version, JSONObject>();

                            final Version jahiaVersion = new Version(Jahia.VERSION);

                            for (int j = 0; j < moduleVersions.length(); j++) {
                                JSONObject object = moduleVersions.getJSONObject(j);
                                Version version = new Version(object.getString("version"));
                                Version requiredVersion = new Version(StringUtils.substringAfter(object.getString("requiredVersion"), "version-"));
                                if (requiredVersion.compareTo(jahiaVersion) <= 0) {
                                    sortedVersions.put(version, object);
                                }
                            }
                            if (!sortedVersions.isEmpty()) {
                                Module module = new Module();
                                JSONObject versionObject = sortedVersions.get(sortedVersions.lastKey());
                                module.setRemoteUrl(moduleObject.getString("remoteUrl"));
                                module.setRemotePath(moduleObject.getString("path"));
                                if (moduleObject.has("icon")) {
                                    module.setIcon(moduleObject.getString("icon"));
                                }
                                module.setVersion(versionObject.getString("version"));
                                module.setName(moduleObject.getString("title"));
                                module.setId(moduleObject.getString("name"));
                                module.setGroupId(moduleObject.getString("groupId"));
                                module.setDownloadUrl(versionObject.getString("downloadUrl"));
                                module.setForgeId(forge.getId());
                                modules.add(module);
                            }
                        }
                    }
                } catch (JSONException e) {
                    logger.error("unable to parse JSON return string for " + url);
                } catch (Exception e) {
                    logger.error("unable to get store information" + e.getMessage());
                }
            }
            Collections.sort(modules);
            lastModulesLoad = new Date().getTime();
            flushModules = false;
        }

        return modules;
    }

    public long getLastUpdateTime(){
        return lastModulesLoad;
    }

    public void flushModules(){
        flushModules = true;
    }

    public File downloadModuleFromForge(String forgeId, String url) {
        for (Forge forge : forges) {
            if (forgeId.equals(forge.getId())) {
                GetMethod httpMethod = new GetMethod(url);
                httpMethod.addRequestHeader("Authorization", "Basic " + Base64.encode((forge.getUser() + ":" + forge.getPassword()).getBytes()));
                HttpClient httpClient = httpClientService.getHttpClient();
                try {
                    int status = httpClient.executeMethod(httpMethod);
                    if (status == HttpServletResponse.SC_OK) {
                        File f = File.createTempFile("module", "." + StringUtils.substringAfterLast(url, "."));
                        FileUtils.copyInputStreamToFile(httpMethod.getResponseBodyAsStream(), f);
                        return f;
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);  //To change body of catch statement use File | Settings | File Templates.
                }


            }
        }
        return null;
    }

    public void setHttpClientService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    public void setLoadModulesDelay(long loadModulesDelay) {
        this.loadModulesDelay = loadModulesDelay;
    }
}
