[consequence][]Remove URL mappings for node {node} and language {language}=seoService.removeMappings({node}, {language}, drools);
[consequence][]Add URL mapping {url} for node {node} and language {language}=seoService.addMapping({node}, {language}, {url}, true, drools);
[consequence][]Check URL mapping uniqueness for {url}=seoService.checkVanityUrl({url}, drools);