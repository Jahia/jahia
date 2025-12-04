import org.jahia.settings.JahiaPropertiesUtils

private updateJahiaProperties(){
    JahiaPropertiesUtils.addEntry("introspectionCheckEnabled", "false",
            "Enable GraphQL feature to limit introspection queries only to authorized users (graphql-dxm-provider >= 3.5.0)",
            null,
            "Unable to set introspectionCheckEnabled property to false. This will default to false unless overridden.");
}

updateJahiaProperties();
