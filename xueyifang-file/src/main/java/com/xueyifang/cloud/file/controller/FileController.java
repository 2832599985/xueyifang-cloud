package com.xueyifang.cloud.file.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.file.dto.FileView;
import com.xueyifang.cloud.file.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile file,
                                           @RequestParam("biz") String biz) {
        return new BaseResponse<>(ErrorCode.SUCCESS, fileService.uploadFile(file, biz, currentUserId()));
    }

    @PostMapping("/upload/batch")
    public BaseResponse<List<String>> uploadFiles(@RequestPart("files") List<MultipartFile> files,
                                                  @RequestParam("biz") String biz) {
        return ResultUtils.success(fileService.uploadFiles(files, biz, currentUserId()));
    }

    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteFile(@RequestParam("url") String url) {
        return ResultUtils.success(fileService.deleteFile(url, currentUserId()));
    }

    @GetMapping("/view/**")
    public ResponseEntity<Resource> viewFile(HttpServletRequest request) {
        FileView fileView = fileService.viewFile(extractRelativePath(request));
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(fileView.filename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(fileView.contentType())
                .contentLength(fileView.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(fileView.resource());
    }

    private Long currentUserId() {
        return UserContextHolder.get()
                .map(LoginUserContext::userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "请先登录"));
    }

    private String extractRelativePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String apiMarker = "/api/file/view/";
        int apiIndex = requestUri.indexOf(apiMarker);
        if (apiIndex >= 0) {
            return requestUri.substring(apiIndex + apiMarker.length());
        }

        String marker = "/file/view/";
        int index = requestUri.indexOf(marker);
        if (index < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件路径不能为空");
        }
        return requestUri.substring(index + marker.length());
    }
}
