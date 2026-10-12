package com.example.shop.services.impl;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUploaderServiceTest {

    private static final String PUBLIC_URL = "http://localhost/storage";
    private static final String BUCKET = "shop-files";

    @Mock
    private MinioClient minioClient;

    private ImageUploaderService imageUploaderService;

    @BeforeEach
    void setUp() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        imageUploaderService = new ImageUploaderService(minioClient, BUCKET, PUBLIC_URL, 40_000_000L, 1920, 1920);
    }

    @Nested
    @DisplayName("Удаление изображения")
    class DeleteImage {

        @Test
        @DisplayName("Файл по ссылке с путём в публичном адресе хранилища удаляется из бакета по имени объекта")
        void publicUrlWithPath() throws Exception {
            imageUploaderService.deleteImage(PUBLIC_URL + "/" + BUCKET + "/client_123.jpg");

            ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
            verify(minioClient).removeObject(captor.capture());
            assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
            assertThat(captor.getValue().object()).isEqualTo("client_123.jpg");
        }

        @Test
        @DisplayName("Ссылка не на публичный адрес хранилища отклоняется без обращения к бакету")
        void foreignUrl() throws Exception {
            assertThatThrownBy(() -> imageUploaderService.deleteImage("http://cdn.example.com/" + BUCKET + "/client_123.jpg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Incorrect format of image URL");
            verify(minioClient, never()).removeObject(any());
        }
    }
}
