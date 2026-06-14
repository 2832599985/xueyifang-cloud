package com.xueyifang.cloud.file.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.file.config.FileStorageProperties;
import com.xueyifang.cloud.file.dto.FileView;
import com.xueyifang.cloud.file.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final FileStorageProperties properties;

    private final FileStorageService storageService;

    public FileService(FileStorageProperties properties, FileStorageService storageService) {
        this.properties = properties;
        this.storageService = storageService;
    }

    public String uploadFile(MultipartFile file, String biz, Long userId) {
        FileUploadBizType bizType = requireBizType(biz);
        requireUserId(userId);
        validateFile(file, bizType);
        return storageService.store(file, bizType, userId).accessUrl();
    }

    public List<String> uploadFiles(List<MultipartFile> files, String biz, Long userId) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件列表不能为空");
        }
        return files.stream()
                .map(file -> uploadFile(file, biz, userId))
                .toList();
    }

    public boolean deleteFile(String fileUrl, Long userId) {
        requireUserId(userId);
        return storageService.delete(fileUrl);
    }

    public FileView viewFile(String relativePath) {
        return storageService.loadLocalFile(relativePath);
    }

    private FileUploadBizType requireBizType(String biz) {
        return FileUploadBizType.from(biz)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的业务类型"));
    }

    private void requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN, "请先登录");
        }
    }

    private void validateFile(MultipartFile file, FileUploadBizType bizType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        long maxSize = properties.maxSizeFor(bizType);
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "文件大小不能超过 " + formatMaxSize(maxSize));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || extension.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型不能为空");
        }

        Set<String> allowedTypes = normalizedAllowedTypes();
        if (!allowedTypes.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "不支持的文件类型，仅支持: " + String.join(", ", allowedTypes));
        }
    }

    private Set<String> normalizedAllowedTypes() {
        List<String> allowedTypes = properties.getAllowedTypes();
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            allowedTypes = List.of("jpg", "jpeg", "png", "gif", "webp");
        }
        return allowedTypes.stream()
                .filter(type -> type != null && !type.isBlank())
                .map(type -> type.replace(".", "").toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private String formatMaxSize(long bytes) {
        if (bytes % (1024 * 1024) == 0) {
            return (bytes / (1024 * 1024)) + "MB";
        }
        if (bytes % 1024 == 0) {
            return (bytes / 1024) + "KB";
        }
        return bytes + "B";
    }
}
