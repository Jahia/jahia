package org.jahia.services.importexport;

/**
 * Generates some default values to be preselected in the import UI.
 *
 * @since 6.6.1.2
 */
public interface SiteImportDefaults {

    public String getDefaultTemplateSet (String sitekey);

    public String getDefaultMappingFile (String sitekey);

    public String getDefaultSourceDefinitionsFile (String sitekey);
}
