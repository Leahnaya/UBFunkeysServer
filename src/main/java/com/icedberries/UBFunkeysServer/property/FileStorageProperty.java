package com.icedberries.UBFunkeysServer.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file")
public class FileStorageProperty {

    private String profileDirectory;

    public String getProfileDirectory() {
        return profileDirectory;
    }

    public void setProfileDirectory(String profileDirectory) {
        this.profileDirectory = profileDirectory;
    }
}
