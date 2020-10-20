import org.jahia.settings.JahiaPropertiesUtils

JahiaPropertiesUtils.removeEntry([
        new JahiaPropertiesUtils.RemoveOperation("jahiaToolManagerUsername", JahiaPropertiesUtils.RemoveOperation.Type.EXACT_BLOCK,
                "######################################################################",
                "### Jahia Tool Manager ###############################################",
                "######################################################################",
                "# Specifies the Tool user name. You can access tools by default",
                "# at localhost:8080/modules/tools/jcrConsole.jsp"),
        new JahiaPropertiesUtils.RemoveOperation("jahiaToolManagerUsername",
                JahiaPropertiesUtils.RemoveOperation.Type.REGEXP_LINE, ".*jahiaToolManagerUsername.*"),
        new JahiaPropertiesUtils.RemoveOperation("jahiaToolManagerPassword", JahiaPropertiesUtils.RemoveOperation.Type.EXACT_BLOCK,
                "# Specifies the Tool password. You can access tools by default",
                "# at localhost:8080/modules/tools/jcrConsole.jsp."),
        new JahiaPropertiesUtils.RemoveOperation("jahiaToolManagerPassword",
                JahiaPropertiesUtils.RemoveOperation.Type.REGEXP_LINE, ".*jahiaToolManagerPassword.*")
] as JahiaPropertiesUtils.RemoveOperation[]);
