package com.daisy.restaurant.services.impl;

import com.daisy.restaurant.exceptions.StorageException;
import com.daisy.restaurant.services.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
@Slf4j
public class FileSystemStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(FileSystemStorageService.class);
    @Value("${app.storage.location:uploads}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(storageLocation);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage location", e);
        }
    }


    /* Return the url to the file */
    @Override
    public String store(MultipartFile file, String filename) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Can not save an empty file");
            }

            String filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String finalFileName = filename + "." + filenameExtension;

            Path destinationFile = rootLocation
                    .resolve(Paths.get(finalFileName))
                    .normalize()
                    .toAbsolutePath();

            if (!destinationFile.getParent().equals(rootLocation)) {
                throw new StorageException("Can not save file outside the specified directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return finalFileName;

        } catch (IOException e) {
            throw new StorageException("Fail to store file", e);
        }
    }

    @Override
    public Optional<Resource> loadAsResource(String filename) {

        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return Optional.of(resource);
            } else {
                return Optional.empty();
            }
        } catch (MalformedURLException e) {
            log.warn("Could not read file: %s".formatted(filename), e);
            return Optional.empty();
        }
    }
}
