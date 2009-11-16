package org.jahia.services.render;

import org.jahia.data.templates.JahiaTemplatesPackage;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 16, 2009
 * Time: 11:05:46 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Template {
    String getKey();

    JahiaTemplatesPackage getModule();

    String getDisplayName();

    /**
     * Return printable information about the script : type, localization, file, .. in order to help
     * template developer to find the original source of the script
     *
     * @return
     */
    String getInfo();

    /**
     * Return properties of the template
     *
     * @return
     */
    Properties getProperties();
}
