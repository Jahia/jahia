import org.jahia.services.render.Resource
import org.jahia.services.render.RenderService

Resource resource = new Resource(currentNode, "html", "default", currentResource.getContextConfiguration());
print RenderService.getInstance().render(resource, renderContext)