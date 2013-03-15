import org.jahia.registries.ServicesRegistry

ServicesRegistry.getInstance().getSchedulerService().getScheduler().unscheduleJob("DataStoreGarbageCollectorJobTrigger", "DEFAULT"); 
