import org.jahia.services.SpringContextSingleton;
import org.jahia.services.history.ContentHistoryService;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Oct 8, 2010
 * Time: 10:01:53 AM
 * To change this template use File | Settings | File Templates.
 */

if (SpringContextSingleton.getInstance() != null) {
  ContentHistoryService contentHistoryService = (ContentHistoryService) SpringContextSingleton.getInstance().getBean("ContentHistoryService");
  if (contentHistoryService != null) {
    // This removes all history older than the day before.
    Calendar calendar = calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -1);
    contentHistoryService.deleteHistoryBeforeDate(calendar.getTime());
  }
}