package com.icedberries.UBFunkeysServer.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    void save(MultipartFile file, String subDir);

    Resource load(String filename);
}
