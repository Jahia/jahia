import org.apache.commons.io.filefilter.SuffixFileFilter
import org.jahia.settings.SettingsBean

import java.nio.file.Files
import java.nio.file.Paths


// BACKLOG-10224
File karafEtc = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf/etc")
File modules = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "modules")

modules
        .listFiles((FilenameFilter) new SuffixFileFilter(".cfg"))
        .each(
                { file ->
                    def target = Paths.get(karafEtc.getPath(), file.getName())
                    if (!Files.exists(target)) {
                        log.info("Moving file " + file + " to " + karafEtc.toPath())
                        Files.move(file.toPath(), target)
                    } else {
                        log.warn("File " + file + " already exists at " + karafEtc.toPath() + ", please check etc and modules folder for duplicates")
                    }
                }
        )

