package org.jahia.services.importexport;

import java.io.File;

/**
 * A XMLContentTransformer performs some modifications in a Jahia exported content xml file.
 * It can be used when the content structure does not fit the set of templates used to import it. To use this feature,
 * the first step is to define one or more implementations of this interface, and then to declare it in the
 * WEB-INF/etc/spring/applicationcontext-services.xml configuration file of the Jahia application.
 * <p/>
 * &lt;bean id="ImportExportService" parent="proxyTemplate"&gt;
 * &lt;property name="target"&gt;
 * &lt;bean class="org.jahia.services.importexport.ImportExportBaseService" parent="jahiaServiceTemplate" factory-method="getInstance"&gt;
 * <p/>
 * ...
 * <p/>
 * &lt;property name="xmlContentTransformers"&gt;
 * &lt;list&gt;
 * &lt;bean class="org.myProject.AnImplementation" /&gt;
 * &lt;bean class="org.myProject.AnOtherImplementation" /&gt;
 * &lt;/list&gt;
 * &lt;/property&gt;
 * &lt;/bean&gt;
 * &lt;/property&gt;
 * &lt;/bean&gt;
 * <p/>
 * This interface provides the transform() method that will be called by the import feature. Several implementations
 * can be used to simplify the code, for example focusing on a single modification in the xml file. In this case,
 * the different declared implementations will be processed sequencially. It is very important to keep in mind that
 * the output of the last processed Object has to be a Jahia compliant content export file, as it will be processed
 * by the import feature, instead of the initial file.
 */
public interface XMLContentTransformer {
    /**
     * Performs some custom modifications in an Jahia exported content xml file, and returns the updated file.
     *
     * @param input A Jahia exported content xml file
     * @return The updated file
     */
    public File transform(File input);
}