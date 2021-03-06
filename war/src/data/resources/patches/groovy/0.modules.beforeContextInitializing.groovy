import org.apache.commons.io.FileUtils
import org.jahia.bin.Jahia
import org.jahia.settings.SettingsBean
import org.jahia.tools.patches.Patcher

setResult("keep")

if (Patcher.getInstance().getJahiaPreviousVersion() != null && !Patcher.getInstance().getJahiaPreviousVersion().equals(Jahia.JAHIA_VERSION)) {
    def file = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/bundles-deployed")
    log.info("*** Migration detected, cleaning all previous bundles and instructing for modules reinstallation ***")
    FileUtils.deleteQuietly(file)
    FileUtils.touch(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "[persisted-bundles].dorestore"))
    FileUtils.touch(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "[generated-resources].dodelete"))
    FileUtils.touch(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "[persisted-configurations].dorestore"))
}