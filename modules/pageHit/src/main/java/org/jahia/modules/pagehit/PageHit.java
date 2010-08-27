package org.jahia.modules.pagehit;

import org.apache.log4j.Logger;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dorth
 * Date: 26 août 2010
 * Time: 13:28:57
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table (name = "jahia_pagehit")
public class PageHit {

    private transient static Logger logger = Logger.getLogger(PageHit.class);
    private Long id = 0l;
    private Long hit;
    private String path;
    private String uuid;


    public PageHit(){

    }
    
    public PageHit(Long hit, String path, String uuid){
        this.hit = hit;
        this.path = path;
        this.uuid = uuid;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Basic
    public Long getHit(){
        return hit;
    }

    public void setHit(Long hit) {
        this.hit = hit;
    }

    @Lob
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
