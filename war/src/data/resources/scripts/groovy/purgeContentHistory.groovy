import org.jahia.services.SpringContextSingleton
import org.jahia.services.history.ContentHistoryService
import org.jahia.settings.SettingsBean

/**
 * Script to purge the content history that is older than a specific date.
 * User: loom
 * Date: Oct 8, 2010
 * Time: 10:01:53 AM
 */

if (SpringContextSingleton.getInstance() != null) {
  ContentHistoryService contentHistoryService = (ContentHistoryService) SpringContextSingleton.getInstance().getBean("ContentHistoryService");
  int retentionInMonths = Math.max(SettingsBean.getInstance().getContentHistoryRetentionInMonths(), 1); // the minimum is 1 month
  if (contentHistoryService != null) {
    Calendar calendar = Calendar.getInstance();
    // This removes all history older than the retention period configured
    calendar.add(Calendar.MONTH, -retentionInMonths)
    //calendar.add(Calendar.HOUR_OF_DAY, -1);
    contentHistoryService.deleteHistoryBeforeDate(calendar.getTime());
  }
}
