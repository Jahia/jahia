package org.jahia.test;

import junit.framework.TestCase;
import org.jahia.bin.Jahia;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 12, 2009
 * Time: 4:49:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestHelper {


    public static JahiaSite createSite(String name) throws Exception {

        ProcessingContext ctx = Jahia.getThreadParamBean();
        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey("testSite");

        if (site !=null) {
            service.removeSite(site);
        }

        site = service.addSite(admin, name, "localhost", name, name, null,
                ctx.getLocale(), "Jahia TCK templates (Jahia Test Compatibility Kit)", "noImport", null, null, false, false, ctx);
        ctx.setSite(site);

        return site;
    }

    public static void deleteSite(String name) throws Exception {
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        service.removeSite(service.getSiteByKey(name));
    }


}
