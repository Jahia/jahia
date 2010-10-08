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
    // this is a bit hardcore, it deletes all content before right now :) For the moment it's just for testing.
    contentHistoryService.deleteHistoryBeforeDate(new Date());
  }
}