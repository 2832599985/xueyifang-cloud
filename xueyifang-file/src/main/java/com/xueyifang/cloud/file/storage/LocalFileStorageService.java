package com.xueyifang.cloud.file.storage;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.file.config.FileStorageProperties;
import com.xueyifang.cloud.file.dto.FileView;
import com.xueyifang.cloud.file.dto.StoredFile;
import com.xueyifang.cloud.file.service.FileUploadBizType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "xueyifang.file.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final FileStorageProperties properties;

    public LocalFileStorageService(FileStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public StoredFile store(MultipartFile file, FileUploadBizType bizType, Long userId) {
        String filename = buildStoredFilename(file.getOriginalFilename());
        String relativePath = buildRelativePath(bizType, userId, filename);
        Path basePath = properties.localUploadPath();
        Path targetPath = safeTargetPath(basePath, relativePath);

        try {
            Files.createDirectories(targetPath.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredFile(relativePath, buildAccessUrl(relativePath));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        }
    }

    @Override
    public boolean delete(String fileUrl) {
        String relativePath = extractRelativePath(fileUrl);
        Path basePath = properties.localUploadPath();
        Path targetPath = safeTargetPath(basePath, relativePath);

        try {
            return Files.deleteIfExists(targetPath);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件删除失败");
        }
    }

    @Override
    public FileView loadLocalFile(String relativePath) {
        String normalizedRelativePath = normalizeRelativePath(relativePath);
        Path basePath = properties.localUploadPath();
        Path targetPath = safeTargetPath(basePath, normalizedRelativePath);
        if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        Resource resource = new FileSystemResource(targetPath);
        try {
            return new FileView(
                    resource,
                    targetPath.getFileName().toString(),
                    mediaTypeFor(targetPath.getFileName().toString()),
                    Files.size(targetPath));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "读取文件失败");
        }
    }

    private String buildStoredFilename(String originalFilename) {
        String filename = StringUtils.getFilename(originalFilename);
        if (filename == null || filename.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        String sanitized = filename.replaceAll("[\\p{Cntrl}\\\\/:*?\"<>|]+", "_");
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "-" + sanitized;
    }

    private String buildRelativePath(FileUploadBizType bizType, Long userId, String filename) {
        StringBuilder path = new StringBuilder()
                .append(bizType.value())
                .append("/")
                .append(userId)
                .append("/");
        if (properties.getLocal().isEnableDatePath()) {
            path.append(LocalDate.now().format(YEAR_MONTH_FORMATTER)).append("/");
        }
        return path.append(filename).toString();
    }

    private String buildAccessUrl(String relativePath) {
        String prefix = normalizePrefix(properties.getLocal().getUrlPrefix());
        return prefix + "/" + relativePath;
    }

    private Path safeTargetPath(Path basePath, String relativePath) {
        Path targetPath = basePath.resolve(normalizeRelativePath(relativePath)).normalize();
        if (!targetPath.startsWith(basePath)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无权访问该文件");
        }
        return targetPath;
    }

    private String extractRelativePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件URL不能为空");
        }

        String path = fileUrl.trim();
        try {
            URI uri = URI.create(path);
            if (uri.getPath() != null && !uri.getPath().isBlank()) {
                path = uri.getPath();
            }
        } catch (IllegalArgumentException ignored) {
            path = UriUtils.decode(path, StandardCharsets.UTF_8);
        }

        path = UriUtils.decode(path, StandardCharsets.UTF_8);
        path = stripKnownPrefix(path, normalizePrefix(properties.getLocal().getUrlPrefix()));
        path = stripKnownPrefix(path, "/api/file/view");
        path = stripKnownPrefix(path, "/file/view");
        return normalizeRelativePath(path);
    }

    private String normalizeRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件路径不能为空");
        }

        String normalized = relativePath.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件路径不能为空");
        }
        return normalized;
    }

    private String stripKnownPrefix(String path, String prefix) {
        String normalizedPrefix = normalizePrefix(prefix);
        if (path.equals(normalizedPrefix)) {
            return "";
        }
        if (path.startsWith(normalizedPrefix + "/")) {
            return path.substring(normalizedPrefix.length() + 1);
        }
        return path;
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/file/view";
        }
        String normalized = prefix.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private MediaType mediaTypeFor(String filename) {
        String extension = StringUtils.getFilenameExtension(filename);
        if (extension == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        return switch (extension.toLowerCase(Locale.ROOT)) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            case "svg" -> MediaType.parseMediaType("image/svg+xml");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
