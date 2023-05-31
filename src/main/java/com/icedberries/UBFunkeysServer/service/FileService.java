package com.icedberries.UBFunkeysServer.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    void saveProfileFile(MultipartFile file, String subDir);

    void saveGameMakerImage(byte[] imageData, String subDirAndFileName);

    Resource load(String path);

    Boolean fileExists(String path);
}
