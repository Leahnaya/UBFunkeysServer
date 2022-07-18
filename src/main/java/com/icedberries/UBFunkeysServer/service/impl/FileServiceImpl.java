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

@Service
public class FileServiceImpl implements FileService {

    private final Path fileStorageLocation;

    @Autowired
    public FileServiceImpl(FileStorageProperty fileStorageProperty) throws IOException {
        this.fileStorageLocation = Paths.get(fileStorageProperty.getProfileDirectory()).toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
    }

    @Override
    public void save(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), fileStorageLocation.resolve(file.getOriginalFilename()));
        } catch(Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Resource resource = new UrlResource(fileStorageLocation.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch(MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
