package com.daisy.restaurant.services;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public interface StorageService {

    String store(MultipartFile file, String name);
    Optional<Resource> loadAsResource(String filename);

}
