package org.jahia.modules.serversettings.mount;

import org.jahia.services.content.decorator.JCRMountPointNode;

import java.io.Serializable;

/**
 * Created by kevan on 12/11/14.
 */
public class MountPoint implements Serializable{
    private static final long serialVersionUID = 4618846382714016491L;

    String name;
    String path;
    String displayStatusClass;
    JCRMountPointNode.MountStatus status;
    boolean showMountAction = false;
    boolean showUnmountAction = false;

    public MountPoint(JCRMountPointNode node) {
        this.name = node.getDisplayableName();
        this.path = node.getPath();
        this.status = node.getMountStatus();

        switch (node.getMountStatus()){
            case unmounted:
                displayStatusClass = "";
                showMountAction = true;
                break;
            case mounted:
                showUnmountAction = true;
                displayStatusClass = "badge-success";
                break;
            case error:
                displayStatusClass = "badge-important";
                break;
            case waiting:
                displayStatusClass = "badge-warning";
                break;
            case unknown:
                displayStatusClass = "badge-inverse";
                break;
        }
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public JCRMountPointNode.MountStatus getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setStatus(JCRMountPointNode.MountStatus status) {
        this.status = status;
    }

    public String getDisplayStatusClass() {
        return displayStatusClass;
    }

    public void setDisplayStatusClass(String displayStatusClass) {
        this.displayStatusClass = displayStatusClass;
    }

    public boolean isShowMountAction() {
        return showMountAction;
    }

    public void setShowMountAction(boolean showMountAction) {
        this.showMountAction = showMountAction;
    }

    public boolean isShowUnmountAction() {
        return showUnmountAction;
    }

    public void setShowUnmountAction(boolean showUnmountAction) {
        this.showUnmountAction = showUnmountAction;
    }
}
