import org.jahia.bin.Jahia
import org.jahia.params.ProcessingContext
import org.jahia.registries.ServicesRegistry
import org.jahia.utils.i18n.JahiaResourceBundle
import org.jahia.services.sites.JahiaSite
import org.jahia.services.sites.JahiaSitesService
import org.jahia.utils.JahiaTools
import org.jahia.version.VersionService

final ServicesRegistry registry = ServicesRegistry.getInstance()
final JahiaSitesService siteService = registry.getJahiaSitesService();
Jahia.getThreadParamBean().setOperationMode(ProcessingContext.EDIT)
Jahia.setMaintenance(false);
sites = siteService.getSites();
sites.each {
    site -> int siteID = ((JahiaSite) site).getHomePageID();
    VersionService.getInstance().setSubStatus(
            JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.patchmanagement.containerlists_17919.sitename",
                    Jahia.getThreadParamBean().getLocale()) + " " + ((JahiaSite) site).getTitle());
    MyStatus.pageCount = 0;
    MyStatus.pageCount++;
    println(((JahiaSite) site).getTitle() + ' Site Homepage = ' + siteID);
    String homepageURL = org.jahia.settings.SettingsBean.getInstance().getLocalAccessUri() + Jahia.getThreadParamBean().composePageUrl(siteID);
    println("Visit Page = " + homepageURL);
    JahiaTools.makeJahiaRequest(new URL(homepageURL + "/op/edit"), Jahia.getThreadParamBean().getUser(), null, null, 5).close();
    pages = registry.getJahiaPageService().getUncheckedPageSubTreeIDs(siteID,true,0);
    MyStatus.total = pages.size()>0?pages.size():1;
    VersionService.getInstance().setPercentCompleted(MyStatus.getPercent());
    pages.each {
        page -> String pageURL = org.jahia.settings.SettingsBean.getInstance().getLocalAccessUri() + Jahia.getThreadParamBean().composePageUrl(page);
        MyStatus.pageCount++;
        println("Visit Page = " + pageURL)
        try {
            JahiaTools.makeJahiaRequest(new URL(URLEncoder.encode(pageURL,"UTF-8")), Jahia.getThreadParamBean().getUser(), null, null, 5).close();
        } catch(IOException e){
            println("Ignoring error : "+e.getMessage()+" the script will continue");
        }
        VersionService.getInstance().setPercentCompleted(MyStatus.getPercent());
    }
}
Jahia.setMaintenance(true);


class MyStatus
{
    public static int total;
    public static int pageCount = 0;
    public static double getPercent() {
        return ((pageCount * 100.0) / total);
    }
}
