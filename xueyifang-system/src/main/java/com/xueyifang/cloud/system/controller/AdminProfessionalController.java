package com.xueyifang.cloud.system.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.system.dto.PageResponse;
import com.xueyifang.cloud.system.dto.ProfessionalRequest;
import com.xueyifang.cloud.system.dto.ProfessionalResponse;
import com.xueyifang.cloud.system.service.SystemDictionaryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/professional")
public class AdminProfessionalController {

    private final SystemDictionaryService systemDictionaryService;

    public AdminProfessionalController(SystemDictionaryService systemDictionaryService) {
        this.systemDictionaryService = systemDictionaryService;
    }

    @GetMapping("/list")
    public BaseResponse<PageResponse<ProfessionalResponse>> listProfessionals(
            @RequestParam(value = "nameLike", required = false) String nameLike,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResultUtils.success(systemDictionaryService.pageProfessionals(
                nameLike,
                firstNonNull(pageNum, current),
                pageSize));
    }

    @PostMapping("/add")
    public BaseResponse<Boolean> addProfessional(@RequestBody ProfessionalRequest request) {
        systemDictionaryService.createProfessional(request);
        return ResultUtils.success(true);
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> updateProfessional(@RequestBody ProfessionalRequest request) {
        systemDictionaryService.updateProfessional(request);
        return ResultUtils.success(true);
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteProfessional(@PathVariable Long id) {
        systemDictionaryService.deleteProfessional(id);
        return ResultUtils.success(true);
    }

    private Integer firstNonNull(Integer primary, Integer fallback) {
        return primary != null ? primary : fallback;
    }
}
