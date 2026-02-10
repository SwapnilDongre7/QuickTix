package com.quicktix.catalogue.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
	String uploadImage(MultipartFile file);
}
