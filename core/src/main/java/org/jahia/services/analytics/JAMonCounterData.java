package org.jahia.services.analytics;

/**
 * Created by IntelliJ IDEA.
 * Date: 27 févr. 2009
 * Time: 17:02:35
 *
 * @author Ibrahim El Ghandour
 */
public class JAMonCounterData {
    private String oid;
    private String pid;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    private String timestamp;
    private double avgtime;
    private double hits;
    private double maxtime;
    private String name;
    private String user;
    private String uuid;
    private String objectId;
    private String objectType;
    private String operation;
    private String siteId;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setUser(name.split("#")[1].split("::")[0]);
        setOperation(name.split("#")[1].split("::")[1]);
        setObjectType(name.split("#")[1].split("::")[2]);
        setObjectId(name.split("#")[1].split("::")[3]);
        setUuid(name.split("#")[1].split("::")[4]);
        setPid(name.split("#")[1].split("::")[5]);
        setSiteId(name.split("#")[1].split("::")[6]);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }



    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getAvgtime() {
        return avgtime;
    }

    public void setAvgtime(double avgtime) {
        this.avgtime = avgtime;
    }

    public double getHits() {
        return hits;
    }

    public void setHits(double hits) {
        this.hits = hits;
    }

    public double getMaxtime() {
        return maxtime;
    }

    public void setMaxtime(double maxtime) {
        this.maxtime = maxtime;
    }
}
