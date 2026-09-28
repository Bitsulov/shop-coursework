package com.example.shop.services.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.UUID;

@Slf4j
@Service
public class ImageUploaderService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String publicUrl;
    private final long maxPixels;
    private final int maxWidth;
    private final int maxHeight;

    public ImageUploaderService(
            MinioClient minioClient,
            @Value("${app.s3.bucket}") String bucketName,
            @Value("${app.s3.public-url}") String publicUrl,
            @Value("${app.image.max-pixels}") long maxPixels,
            @Value("${app.image.max-width}") int maxWidth,
            @Value("${app.image.max-height}") int maxHeight
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.publicUrl = publicUrl;
        this.maxPixels = maxPixels;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;

        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created object storage bucket: {}", bucketName);
            }

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(buildReadOnlyPolicy(bucketName))
                            .build());
        } catch (Exception e) {
            throw new IllegalStateException("Error connecting to object storage", e);
        }
    }

    public String uploadImage(MultipartFile file) {
        BufferedImage originalImage = readImage(file);

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .size(Math.min(originalImage.getWidth(), maxWidth),
                            Math.min(originalImage.getHeight(), maxHeight))
                    .outputFormat("jpg")
                    .outputQuality(0.7)
                    .toOutputStream(os);
            byte[] imageData = os.toByteArray();

            String objectName = "client_" + UUID.randomUUID() + ".jpg";
            log.debug("Prepared image for upload, objectName={}, size={} bytes", objectName, imageData.length);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(imageData), (long) imageData.length, -1L)
                            .contentType("image/jpeg")
                            .build());

            String url = publicUrl + "/" + bucketName + "/" + objectName;
            log.info("Uploaded image to storage, url={}", url);
            return url;
        } catch (Exception e) {
            log.error("Image upload failed, bucket={}", bucketName, e);
            throw new IllegalStateException("Image upload failed", e);
        }
    }

    public void deleteImage(String imageUrl) {
        String objectName = extractObjectNameFromUrl(imageUrl);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            log.info("Removed image from storage, url={}", imageUrl);
        } catch (Exception e) {
            log.error("Image deletion failed, url={}", imageUrl, e);
            throw new IllegalStateException("Image deletion failed", e);
        }
    }

    private BufferedImage readImage(MultipartFile file) {
        byte[] fileData;
        try {
            fileData = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid image file");
        }

        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(fileData))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Invalid image file");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(stream);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > maxPixels) {
                    log.warn("Image rejected: {} pixels is over the limit", pixels);
                    throw new IllegalArgumentException("Image is too large");
                }
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid image file");
        }
    }

    private String extractObjectNameFromUrl(String imageUrl) {
        String path = URI.create(imageUrl).getPath();
        String prefix = "/" + bucketName + "/";
        if (path == null || !path.startsWith(prefix)) {
            throw new IllegalArgumentException("Incorrect format of image URL");
        }
        return path.substring(prefix.length());
    }

    private String buildReadOnlyPolicy(String bucket) {
        return """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": {"AWS": ["*"]},
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """.formatted(bucket);
    }
}