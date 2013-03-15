import org.jahia.services.scheduler.SchedulerService

SchedulerService.getScheduler().unscheduleJob("DataStoreGarbageCollectorJobTrigger", "DEFAULT");
