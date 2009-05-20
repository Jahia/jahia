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
package org.jahia.hibernate.manager;

import java.util.Date;
import java.util.List;

import org.jahia.bin.Jahia;
import org.jahia.hibernate.dao.JahiaInstalledPatchDAO;
import org.jahia.hibernate.model.JahiaInstalledPatch;

/**
 */
public class JahiaInstalledPatchManager {
    private JahiaInstalledPatchDAO jahiaInstalledPatchDao;

    public void setJahiaInstalledPatchDao(JahiaInstalledPatchDAO jahiaInstalledPatchDao) {
        this.jahiaInstalledPatchDao = jahiaInstalledPatchDao;
    }

    public List<JahiaInstalledPatch> getAllInstalledPatches() {
        return jahiaInstalledPatchDao.findAll();
    }

    public List<JahiaInstalledPatch> getPatchesForBuild(int build) {
        return jahiaInstalledPatchDao.findByBuildNumber(build);        
    }

    public boolean hasPatchBeenInstalled(String name) {
        List<JahiaInstalledPatch> l = jahiaInstalledPatchDao.findByName(name);
        for (JahiaInstalledPatch jahiaInstalledPatch : l) {
            if (jahiaInstalledPatch.getResultCode().intValue() == 0) {
                return true;
            }
        }
        return false;
    }

    public void afterPatchInstallation(String name, int code) {
        JahiaInstalledPatch p = new JahiaInstalledPatch(name, new Integer(Jahia.getBuildNumber()), new Integer(code), new Date());
        jahiaInstalledPatchDao.save(p);
    }
}