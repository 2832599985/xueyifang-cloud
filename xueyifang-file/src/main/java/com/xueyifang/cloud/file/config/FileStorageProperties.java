package com.xueyifang.cloud.file.config;

import com.xueyifang.cloud.file.service.FileUploadBizType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "xueyifang.file.storage")
public class FileStorageProperties {

    private String type = "local";

    private Local local = new Local();

    private Oss oss = new Oss();

    private List<String> allowedTypes = new ArrayList<>(List.of("jpg", "jpeg", "png", "gif", "webp"));

    private Map<String, DataSize> maxSize = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public Oss getOss() {
        return oss;
    }

    public void setOss(Oss oss) {
        this.oss = oss;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(List<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public Map<String, DataSize> getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Map<String, DataSize> maxSize) {
        this.maxSize = maxSize;
    }

    public long maxSizeFor(FileUploadBizType bizType) {
        DataSize configuredSize = maxSize.get(bizType.value());
        return configuredSize == null ? bizType.defaultMaxSize() : configuredSize.toBytes();
    }

    public Path localUploadPath() {
        Path configuredPath = Path.of(local.getUploadPath());
        Path absolutePath = configuredPath.isAbsolute()
                ? configuredPath
                : Path.of(System.getProperty("user.dir")).resolve(configuredPath);
        return absolutePath.toAbsolutePath().normalize();
    }

    public static class Local {

        private String uploadPath = "uploads";

        private String urlPrefix = "/api/file/view";

        private boolean enableDatePath = true;

        public String getUploadPath() {
            return uploadPath;
        }

        public void setUploadPath(String uploadPath) {
            this.uploadPath = uploadPath;
        }

        public String getUrlPrefix() {
            return urlPrefix;
        }

        public void setUrlPrefix(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }

        public boolean isEnableDatePath() {
            return enableDatePath;
        }

        public void setEnableDatePath(boolean enableDatePath) {
            this.enableDatePath = enableDatePath;
        }
    }

    public static class Oss {

        private String endpoint;

        private String accessKeyId;

        private String accessKeySecret;

        private String bucketName;

        private String urlPrefix;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getUrlPrefix() {
            return urlPrefix;
        }

        public void setUrlPrefix(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }
    }
}
