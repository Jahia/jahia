package org.jahia.modules.serversettings.users.management;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author rincevent
 */
public class CsvFile implements Serializable {
    private static final long serialVersionUID = 2592011306396271299L;
    private String csvSeparator;
    private MultipartFile csvFile;

    public String getCsvSeparator() {
        return csvSeparator;
    }

    public void setCsvSeparator(String csvSeparator) {
        this.csvSeparator = csvSeparator;
    }

    public MultipartFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(MultipartFile csvFile) {
        this.csvFile = csvFile;
    }
}
