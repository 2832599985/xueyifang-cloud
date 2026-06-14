package com.xueyifang.cloud.file.storage;

import com.xueyifang.cloud.file.dto.FileView;
import com.xueyifang.cloud.file.dto.StoredFile;
import com.xueyifang.cloud.file.service.FileUploadBizType;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile store(MultipartFile file, FileUploadBizType bizType, Long userId);

    boolean delete(String fileUrl);

    FileView loadLocalFile(String relativePath);
}
