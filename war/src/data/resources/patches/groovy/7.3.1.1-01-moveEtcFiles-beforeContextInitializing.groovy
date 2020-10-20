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
                    log.info("Moving file " + file + " to " + karafEtc.toPath())
                    Files.move(file.toPath(), Paths.get(karafEtc.getPath(), file.getName()))
                }
        )

