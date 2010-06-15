package org.jahia.ajax.gwt.client.data.toolbar;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 20, 2010
 * Time: 6:27:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTConfiguration implements IsSerializable, Serializable {
    protected String name;
    private List<GWTEngine> createEngines;
    private List<GWTEngine> editEngines;
    private List<GWTJahiaPermission> permissions;

    public GWTConfiguration() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GWTEngine> getCreateEngines() {
        return createEngines;
    }

    public void setCreateEngines(List<GWTEngine> createEngines) {
        this.createEngines = createEngines;
    }

    public List<GWTEngine> getEditEngines() {
        return editEngines;
    }

    public void setEditEngines(List<GWTEngine> editEngines) {
        this.editEngines = editEngines;
    }

    public List<GWTJahiaPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<GWTJahiaPermission> permissions) {
        this.permissions = permissions;
    }
}
