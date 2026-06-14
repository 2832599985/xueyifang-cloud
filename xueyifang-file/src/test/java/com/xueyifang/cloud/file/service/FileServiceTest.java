package com.xueyifang.cloud.file.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.file.config.FileStorageProperties;
import com.xueyifang.cloud.file.dto.FileView;
import com.xueyifang.cloud.file.storage.LocalFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileServiceTest {

    @TempDir
    private Path uploadDir;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.getLocal().setUploadPath(uploadDir.toString());
        properties.getLocal().setUrlPrefix("/api/file/view");
        properties.getLocal().setEnableDatePath(false);
        fileService = new FileService(properties, new LocalFileStorageService(properties));
    }

    @Test
    void uploadsAndDeletesLocalFile() throws Exception {
        MockMultipartFile file = image("avatar.png", "hello");

        String url = fileService.uploadFile(file, "user_avatar", 10L);
        String relativePath = url.substring("/api/file/view/".length());

        assertThat(url).startsWith("/api/file/view/user_avatar/10/");
        assertThat(Files.readString(uploadDir.resolve(relativePath), StandardCharsets.UTF_8))
                .isEqualTo("hello");

        FileView view = fileService.viewFile(relativePath);
        assertThat(view.filename()).endsWith("-avatar.png");
        assertThat(view.contentLength()).isEqualTo(5);

        assertThat(fileService.deleteFile(url, 10L)).isTrue();
        assertThat(Files.exists(uploadDir.resolve(relativePath))).isFalse();
    }

    @Test
    void uploadsBatchFiles() {
        List<String> urls = fileService.uploadFiles(
                List.of(image("first.jpg", "1"), image("second.webp", "2")),
                "service_image",
                20L);

        assertThat(urls).hasSize(2);
        assertThat(urls).allMatch(url -> url.startsWith("/api/file/view/service_image/20/"));
    }

    @Test
    void rejectsUnsupportedBusinessType() {
        assertThatThrownBy(() -> fileService.uploadFile(image("avatar.png", "hello"), "unknown", 10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    @Test
    void rejectsUnsupportedFileType() {
        assertThatThrownBy(() -> fileService.uploadFile(
                new MockMultipartFile("file", "avatar.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
                "user_avatar",
                10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    @Test
    void rejectsOversizedAvatar() {
        byte[] bytes = new byte[(1024 * 1024) + 1];

        assertThatThrownBy(() -> fileService.uploadFile(
                new MockMultipartFile("file", "avatar.png", "image/png", bytes),
                "user_avatar",
                10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    @Test
    void rejectsPathTraversalOnView() {
        assertThatThrownBy(() -> fileService.viewFile("../secret.png"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.FORBIDDEN_ERROR.getCode()));
    }

    @Test
    void requiresLoginForWrites() {
        assertThatThrownBy(() -> fileService.uploadFile(image("avatar.png", "hello"), "user_avatar", null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_LOGIN.getCode()));
    }

    private MockMultipartFile image(String filename, String content) {
        return new MockMultipartFile(
                "file",
                filename,
                "image/png",
                content.getBytes(StandardCharsets.UTF_8));
    }
}
