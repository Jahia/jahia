/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.version;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.hibernate.manager.JahiaInstalledPatchManager;
import org.jahia.hibernate.manager.JahiaVersionManager;
import org.jahia.hibernate.model.JahiaInstalledPatch;
import org.jahia.hibernate.model.JahiaVersion;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 23 août 2007
 * Time: 17:55:25
 * To change this template use File | Settings | File Templates.
 */
public class VersionService extends JahiaService {

    private static VersionService instance;

    private static Logger logger = Logger.getLogger(VersionService.class);

    private JahiaVersionManager manager;
    private JahiaInstalledPatchManager patchManager;

    private SortedSet<Patch> patches = new TreeSet<Patch>();

    private List<Patcher> patchers = new ArrayList<Patcher>();

    private List<Status> status = new ArrayList<Status>();

    private SortedMap<Integer, String> versions = new TreeMap<Integer, String>();

    public static VersionService getInstance() {
        if (instance == null) {
            instance = new VersionService();
        }
        return instance;
    }

    private VersionService() {
    }


    private void addPatches(File dir) {
        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile()) {
                patches.add(new Patch(file));
            } else {
                addPatches(file);
            }
        }
    }

    public boolean isInitialized() {
        return !manager.getAllVersion().isEmpty();
    }

    public boolean installAllPatches() {
        status = new ArrayList<Status>();
        Jahia.setMaintenance(true);
        try {
            List<JahiaVersion> list = manager.getAllVersion();
            if (!list.isEmpty()) {
                Map<Patch, Patcher> patchesToInstall = getPatchesToInstall();

                for (Iterator<Patch> iterator = patchesToInstall.keySet().iterator(); iterator.hasNext();) {
                    Patch patch = iterator.next();
                    Patcher patcher = patchesToInstall.get(patch);
                    Status s = new Status();
                    s.scriptName = patch.getName();
                    status.add(s);

                    int result = patcher.execute(patch);
                    ServicesRegistry.getInstance().getCacheService().flushAllCaches();
                    JahiaBatchingClusterCacheHibernateProvider.flushAllCaches();

                    s.result = result;

                    if (result >= 0) {
                        patchManager.afterPatchInstallation(patch.getName(), result);
                    }
                }


                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            Jahia.setMaintenance(false);
        }
        return false;
    }

    public SortedMap<Patch, Patcher> getPatchesToInstall() {
        List<JahiaVersion> list = manager.getAllVersion();
        int firstVersion = 0;
        for (JahiaVersion version : list) {
            int b = version.getBuildNumber().intValue();
            if (firstVersion == 0 || b < firstVersion) {
                firstVersion = b;
            }
        }

        SortedMap<Patch, Patcher> patchesToInstall = new TreeMap<Patch, Patcher>();

        for (Iterator<Patch> iterator = patches.iterator(); iterator.hasNext();) {
            Patch patch = iterator.next();
            if (!patchManager.hasPatchBeenInstalled(patch.getName())) {
                for (Iterator<Patcher> it2 = patchers.iterator(); it2.hasNext();) {
                    Patcher patcher = it2.next();
                    if (patcher.canHandlePatch(patch, firstVersion, Jahia.getBuildNumber())) {
                        patchesToInstall.put(patch, patcher);
                        break;
                    }
                }
            }
        }
        return patchesToInstall;
    }

    public boolean setBuildNumber(int buildNumber) {
        if (buildNumber >= Jahia.getBuildNumber()) {
            return false;
        }

        SortedMap<Integer, String> headMap = versions.headMap(new Integer(buildNumber));
        String s = null;

        if (!headMap.isEmpty()) {
            s = headMap.get(headMap.lastKey());
        }

        manager.createOldVersion(buildNumber, s);

        if (!manager.isCurrentBuildDefined()) {
            manager.createVersion();
        }

        return true;
    }

    public SortedMap<JahiaVersion, List<JahiaInstalledPatch>> getInstalledPatchesByVersion() {
        SortedMap<JahiaVersion, List<JahiaInstalledPatch>> results = new TreeMap<JahiaVersion, List<JahiaInstalledPatch>>();
        List<JahiaVersion> versions = manager.getAllVersion();
        for (JahiaVersion jahiaVersion : versions) {
            results.put(jahiaVersion, patchManager.getPatchesForBuild(jahiaVersion.getBuildNumber().intValue()));
        }
        return results;
    }

    public List<Status> getScriptsStatus() {
        return status;
    }

    public Status getLastScriptStatus() {
        return status.get(status.size() - 1);
    }

    public void setSubStatus(String v) {
        getLastScriptStatus().setSubStatus(v);
    }

    public void setPercentCompleted(double d) {
        getLastScriptStatus().setPercentCompleted(d);
    }

    @Override
    public void start() throws JahiaInitializationException {
        logger.debug("Starting Jahia version service...");

        patchers.add(new GroovyPatcher());
        patchers.add(new SqlPatcher());

        File dir = new File(org.jahia.settings.SettingsBean.getInstance().getJahiaVarDiskPath() + File.separator + "patches");
        addPatches(dir);

        Properties v = new Properties();
        try {
            v.load(new FileInputStream(org.jahia.settings.SettingsBean.getInstance().getJahiaVarDiskPath() + File.separator + "versions.properties"));
            for (Iterator<?> it = v.keySet().iterator(); it.hasNext();) {
                String s = (String) it.next();
                versions.put(new Integer(s), v.getProperty(s));
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        if (isInitialized() && !manager.isCurrentBuildDefined()) {
            manager.createVersion();
        }
        logger.debug("...version service successfully started");
    }

    @Override
    public void stop() throws JahiaException {
        // nothing to do
    }

    public void setJahiaVersionManager(JahiaVersionManager manager) {
        this.manager = manager;
    }

    public void setPatchManager(JahiaInstalledPatchManager patchManager) {
        this.patchManager = patchManager;
    }
}
