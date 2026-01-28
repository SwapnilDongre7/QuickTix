package com.quicktix.catalogue.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.quicktix.catalogue.service.ImageUploadService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ImageUploadServiceImpl implements ImageUploadService {

	@Autowired
	private final Cloudinary cloudinary;

    public ImageUploadServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    
    @Override
    public String uploadImage(MultipartFile file) {

    	try {
    	    Map uploadResult = cloudinary.uploader().upload(
    	        file.getBytes(),
    	        ObjectUtils.emptyMap()
    	    );
    	    
    	    String url = uploadResult.get("secure_url").toString();
            System.out.println("CLOUDINARY URL = " + url);

            return url;
//    	    return uploadResult.get("secure_url").toString();
    	} catch (Exception e) {
    		e.printStackTrace();
    	    throw new RuntimeException("Image upload failed");
    	}

    }

}
