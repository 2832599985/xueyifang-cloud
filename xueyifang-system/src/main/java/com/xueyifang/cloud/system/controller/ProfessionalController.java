package com.xueyifang.cloud.system.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/professional")
public class ProfessionalController {

    private final SystemDictionaryService systemDictionaryService;

    public ProfessionalController(SystemDictionaryService systemDictionaryService) {
        this.systemDictionaryService = systemDictionaryService;
    }

    @GetMapping("/list")
    public BaseResponse<List<ProfessionalResponse>> listProfessionals() {
        return ResultUtils.success(systemDictionaryService.listProfessionals());
    }

    @GetMapping("/{id}")
    public BaseResponse<ProfessionalResponse> getProfessional(@PathVariable Long id) {
        return ResultUtils.success(systemDictionaryService.getProfessional(id));
    }

    @PostMapping("/add")
    public BaseResponse<Void> addProfessional(@RequestBody ProfessionalRequest request) {
        systemDictionaryService.createProfessional(request);
        return ResultUtils.success();
    }

    @PutMapping("/update")
    public BaseResponse<Void> updateProfessional(@RequestBody ProfessionalRequest request) {
        systemDictionaryService.updateProfessional(request);
        return ResultUtils.success();
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteProfessional(@PathVariable Long id) {
        systemDictionaryService.deleteProfessional(id);
        return ResultUtils.success();
    }
}
