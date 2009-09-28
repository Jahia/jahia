package org.jahia.services.render;

import org.jahia.services.content.nodetypes.initializers.Templates;
import org.jahia.data.templates.JahiaTemplatesPackage;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 28, 2009
* Time: 7:20:38 PM
* To change this template use File | Settings | File Templates.
*/
public class Template implements Comparable<Template> {
    private String path;
    private String key;
    private JahiaTemplatesPackage ownerPackage;
    private String displayName;

    public Template(String path, String key, JahiaTemplatesPackage ownerPackage, String displayName) {
        this.path = path;
        this.key = key;
        this.ownerPackage = ownerPackage;
        this.displayName = displayName;
    }

    public String getPath() {
        return path;
    }

    public String getKey() {
        return key;
    }

    public JahiaTemplatesPackage getOwnerPackage() {
        return ownerPackage;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Template template = (Template) o;

        if (displayName != null ? !displayName.equals(template.displayName) : template.displayName != null) {
            return false;
        }
        if (key != null ? !key.equals(template.key) : template.key != null) {
            return false;
        }
        if (ownerPackage != null ? !ownerPackage.equals(template.ownerPackage) : template.ownerPackage != null) {
            return false;
        }
        if (path != null ? !path.equals(template.path) : template.path != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (ownerPackage != null ? ownerPackage.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        return result;
    }

    public int compareTo(Template template) {
        if (ownerPackage == null) {
            if (template.ownerPackage != null ) {
                return 1;
            } else {
                return key.compareTo(template.key);
            }
        } else {
            if (template.ownerPackage == null ) {
                return -1;
            } else if (!ownerPackage.equals(template.ownerPackage)) {
                return ownerPackage.getName().compareTo(template.ownerPackage.getName());
            } else {
                return key.compareTo(template.key);
            }
        }
    }
}
