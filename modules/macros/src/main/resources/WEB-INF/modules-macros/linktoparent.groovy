import org.jahia.services.content.JCRContentUtils
/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 12/10/11
 * Time: 5:04 PM
 * To change this template use File | Settings | File Templates.
 */
print JCRContentUtils.getParentOfType(currentNode,"jnt:page").getAbsoluteUrl(renderContext.getRequest());