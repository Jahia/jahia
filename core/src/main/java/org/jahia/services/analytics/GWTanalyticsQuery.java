package org.jahia.services.analytics;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 12 mars 2009
 * Time: 14:01:37
 * To change this template use File | Settings | File Templates.
 */
public class GWTanalyticsQuery {
    String user;
    String op;
    String objType;
    int objId;
    String uuid;
    int pid;
    int siteId;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }

    public int getObjId() {
        return objId;
    }

    public void setObjId(int objId) {
        this.objId = objId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }
}
