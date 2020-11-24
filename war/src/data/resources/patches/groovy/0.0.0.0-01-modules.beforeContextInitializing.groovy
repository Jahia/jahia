import org.apache.commons.io.FileUtils
import org.jahia.settings.SettingsBean

def file = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/bundles-deployed")
if (file.exists()) {
    FileUtils.deleteQuietly(file)
    FileUtils.touch(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "[persisted-bundles].dorestore"))
    FileUtils.touch(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "[generated-resources].dodelete"))
}
