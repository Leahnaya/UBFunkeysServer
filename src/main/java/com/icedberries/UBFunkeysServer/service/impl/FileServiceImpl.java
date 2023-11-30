package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.property.FileStorageProperty;
import com.icedberries.UBFunkeysServer.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String LOG_BASE = "[Galaxy]";

    private final Path fileStorageLocation;

    @Autowired
    public FileServiceImpl(FileStorageProperty fileStorageProperty) throws IOException {
        this.fileStorageLocation = Paths.get(fileStorageProperty.getProfileDirectory()).toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
    }

    @Override
    public void saveGameMakerImage(byte[] imageData, String subDirAndFileName) {
        try {
            String path = subDirAndFileName.substring(0, subDirAndFileName.lastIndexOf("/"));
            Path folderPath = Paths.get(fileStorageLocation.toString(), path);
            Files.createDirectories(folderPath);

            InputStream imageContentStream = new ByteArrayInputStream(imageData);
            Path pathToFile = Paths.get(fileStorageLocation.toString(), subDirAndFileName);
            Files.copy(imageContentStream, pathToFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("{}[PUT] Image saved successfully", LOG_BASE);
        } catch(Exception e) {
            throw new RuntimeException("Could not store the profile file. Error: " + e.getMessage());
        }
    }

    @Override
    public void saveProfileFile(MultipartFile file, String subDir) {
        try {
            Files.createDirectories(fileStorageLocation.resolve(subDir));
            Files.copy(file.getInputStream(), fileStorageLocation.resolve(subDir).resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        } catch(Exception e) {
            log.error("{} Failed to save profile part", LOG_BASE, e);
            throw new RuntimeException("Could not store the profile file. Error: " + e.getMessage());
        }
    }

    @Override
    public Resource load(String path) {
        try {
            Path file = fileStorageLocation.resolve(path);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch(MalformedURLException e) {
            log.error("{} Failed to load resource at: {}", LOG_BASE, path, e);
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public Boolean fileExists(String path) {
        try {
            Path file = fileStorageLocation.resolve(path);
            Resource resource = new UrlResource(file.toUri());

            return resource.exists() || resource.isReadable();
        } catch(MalformedURLException e) {
            return false;
        }
    }
}
