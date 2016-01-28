import org.jahia.services.SpringContextSingleton;
import org.jahia.services.history.ContentHistoryService;

/**
 * Script to purge the content history that is older than a specific date.
 * User: loom
 * Date: Oct 8, 2010
 * Time: 10:01:53 AM
 */

if (SpringContextSingleton.getInstance() != null) {
  ContentHistoryService contentHistoryService = (ContentHistoryService) SpringContextSingleton.getInstance().getBean("ContentHistoryService");
  if (contentHistoryService != null) {
    Calendar calendar = Calendar.getInstance();
    // This removes all history older than a year
    calendar.add(Calendar.YEAR, -1);
    //calendar.add(Calendar.HOUR_OF_DAY, -1);
    contentHistoryService.deleteHistoryBeforeDate(calendar.getTime());
  }
}