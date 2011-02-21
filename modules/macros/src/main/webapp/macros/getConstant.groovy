
/**
 * Created by IntelliJ IDEA.
 * User: dorth
 * Date: 18/02/11
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
if(param1 != null){
   print renderContext.getSite().getNode("constants").getNode(param1).getPropertyAsString("constantValue");
}