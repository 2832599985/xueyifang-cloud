package com.xueyifang.cloud.user.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.user.dto.UserImportResultResponse;
import com.xueyifang.cloud.user.dto.UserImportTemplateResponse;
import com.xueyifang.cloud.user.service.UserImportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/user-import")
public class AdminUserImportController {

    private final UserImportService userImportService;

    public AdminUserImportController(UserImportService userImportService) {
        this.userImportService = userImportService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<UserImportResultResponse> importUsers(@RequestParam("file") MultipartFile file) {
        return ResultUtils.success(userImportService.importUsers(file));
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate(
            @RequestParam(value = "format", defaultValue = "xlsx") String format) {
        UserImportTemplateResponse template = userImportService.createTemplate(format);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(template.contentType()));
        headers.setContentLength(template.content().length);
        headers.setContentDisposition(ContentDisposition.attachment().filename(template.filename()).build());
        return ResponseEntity.ok().headers(headers).body(template.content());
    }
}
