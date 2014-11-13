package org.jahia.modules.serversettings.mount;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by kevan on 13/11/14.
 */
public class MountPoints implements Serializable{
    private static final long serialVersionUID = 7055743449410009286L;

    Map<String, String> providerFactoryTypes = new HashMap<String, String>();
    List<MountPoint> mountPoints = new LinkedList<MountPoint>();

    public MountPoints(Map<String, String> providerFactoryTypes, List<MountPoint> mountPoints) {
        this.providerFactoryTypes = providerFactoryTypes;
        this.mountPoints = mountPoints;
    }

    public MountPoints() {
    }

    public Map<String, String> getProviderFactoryTypes() {
        return providerFactoryTypes;
    }

    public void setProviderFactoryTypes(Map<String, String> providerFactoryTypes) {
        this.providerFactoryTypes = providerFactoryTypes;
    }

    public List<MountPoint> getMountPoints() {
        return mountPoints;
    }

    public void setMountPoints(List<MountPoint> mountPoints) {
        this.mountPoints = mountPoints;
    }
}
