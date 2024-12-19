import org.jahia.settings.SettingsBean

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

// This script will comment out in all provisioning files the modules that are not compatible with the JDK 11

def nonJDK11CompatibleModules = ["npm-modules-engine", "javascript-modules-engine", "luxe-jahia-demo", "luxe-prepackaged-website"].join("|")
def provisioningDirectory = Paths.get(SettingsBean.getInstance().getJahiaVarDiskPath() + "/patches/provisioning")
def jdk = System.getProperty("java.version");

def processProvisioningFile = { Path file ->
    {
        def lines = file.toFile().readLines()
        def modifiedLines = lines.collect { line ->
            if (line ==~ /.*\/($nonJDK11CompatibleModules)\/.*/) {
                log.info("Commenting out JDK17 module {}", line)
                return "#${line}"
            }
            return line
        }
        file.toFile().write(modifiedLines.join("\n"))
    }
}

// JDK 17 only modules
if (jdk.startsWith("11") && Files.exists(provisioningDirectory)) {
    log.info("JDK11 detected. removing JDK17 only modules from provisioning files")
    log.info("Modules to disable: {}", nonJDK11CompatibleModules)
    // Directory to search files in
    Files.walkFileTree(provisioningDirectory, new SimpleFileVisitor<Path>() {
        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.fileName.toString() ==~ /.*\.yaml/) {
                log.info("Processing provisioning file {}", file)
                processProvisioningFile(file)
            }
            return FileVisitResult.CONTINUE
        }
    })
}
log.info("Done disable JDK17 modules as not compatible with the current JDK {}", jdk);
