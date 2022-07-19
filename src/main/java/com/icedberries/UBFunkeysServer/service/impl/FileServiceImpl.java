package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.property.FileStorageProperty;
import com.icedberries.UBFunkeysServer.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService {

    private final Path fileStorageLocation;

    @Autowired
    public FileServiceImpl(FileStorageProperty fileStorageProperty) throws IOException {
        this.fileStorageLocation = Paths.get(fileStorageProperty.getProfileDirectory()).toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
    }

    @Override
    public void save(MultipartFile file, String subDir) {
        System.out.println("here");
        try {
            Files.createDirectories(fileStorageLocation.resolve(subDir));
            Files.copy(file.getInputStream(), fileStorageLocation.resolve(subDir).resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        } catch(Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
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
                throw new RuntimeException("Could not read the file!");
            }
        } catch(MalformedURLException e) {
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
