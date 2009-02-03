/*
WARNING: please optmize all package's import declarations you write here
since the Groovy engine need to check the modification date of these dependencies.
this constraint could be a huge pitfall for global performance.
So avoid all import with * and limit drastically all useless import declarations

In developpement mode, the dependencies are checked anytime, except in the case of frequent call
to the script, the dependecies check are not made. This is controled by the static field enabledPerformanceMode in
the groovyEventListener.(true by default)

In production, the script and dependencies are compiled and checked the first time
and each time the script is modified the script is updated , but all subsequent calls
use the cached version.
*/
import org.jahia.services.containers.ContentContainer
import org.jahia.services.events.GroovyEventListener
import org.apache.log4j.Logger
import org.jahia.data.JahiaData
import org.jahia.registries.ServicesRegistry
import org.jahia.services.mail.MailService
import org.jahia.registries.JahiaContainerDefinitionsRegistry
import org.jahia.data.containers.JahiaContainerList
import org.jahia.data.containers.JahiaContainerDefinition
import org.jahia.data.containers.JahiaContainer
import org.jahia.utils.TextHtml




// register comma separated list of events we want trap
GroovyEventListener.registerEvents("contentObjectCreated");


if(eventName.equals("contentObjectCreated")) {
logger = Logger.getLogger(getClass())


if(jahiaEvent.getObject() instanceof ContentContainer) {
logger.debug("Entering in groovy Eventlistener")
 	//get the pid
	pid=jahiaEvent.getParams().getPageID()
	if(pid==-1) return;

	//construct the jdata
	jData = new JahiaData(jahiaEvent.getParams(),true)

	//get the subscribers containerlist
	subscribers=jData.containers().getAbsoluteContainerList( "subscriber_list", pid)
	if(subscribers!=null && subscribers.size()>0) {
		logger.debug(subscribers.size())
	} else {
		//logger.debug("no subscribers to push")
		//exiting
		return
	}

		//got the mail service
		mailService = ServicesRegistry.getInstance ().getMailService ();
		// get recipients
		sb =new StringBuffer();
		subscriberEnum = subscribers.getContainers();


		container = (ContentContainer) jahiaEvent.getObject()
		mydef=JahiaContainerDefinitionsRegistry.getInstance().getDefinition(container.getDefinitionID(jahiaEvent.getParams().getEntryLoadRequest()))


		defname=mydef.getName()
		logger.debug( "the definition found is "+defname);
		//pid=container.getPageID()
		// please edit this to your servername and port
		url= "http://localhost:8080"+jahiaEvent.getParams().composePageUrl(pid)


		if (defname.equals("blogEntries")) {
				logger.debug("some changes in blogs on page "+pid)
				while (subscriberEnum.hasNext()) {
					subscriber = (JahiaContainer) subscriberEnum.next();
					sub_adress = subscriber.getFieldValue( "subscriber_mail", "" );
					sb.append( TextHtml.html2text(sub_adress)).append(",")
				}
				co1=container.getParent(jahiaEvent.getParams().getEntryLoadRequest())
				co2=co1.getParent(jahiaEvent.getParams().getEntryLoadRequest())
				url+="?entryId="+co2.getID()
				mailService.sendMessage(null, null, sb.toString(), "",
				                               "new post", "smtp.free.fr", "a post is added on this blog \r"+url)
		} else if (defname.equals("comments")){
			while (subscriberEnum.hasNext()) {
				subscriber = (JahiaContainer) subscriberEnum.next();
				sub_adress = subscriber.getFieldValue( "subscriber_mail", "" );
				sb.append( TextHtml.html2text(sub_adress)).append(",")
			}
				logger.debug("some changes in comments")
				co1=container.getParent(jahiaEvent.getParams().getEntryLoadRequest())
				co2=co1.getParent(jahiaEvent.getParams().getEntryLoadRequest())
				logger.debug("parent.parent ctnlist id:"+co2.getID())
				url+="?entryId="+co2.getID()+"#comments"

				mailService.sendMessage(null, null, sb.toString(), "",
				                               "new comment", "smtp.free.fr", "a comment is added on this blog \r"+url)

			}
	}
}