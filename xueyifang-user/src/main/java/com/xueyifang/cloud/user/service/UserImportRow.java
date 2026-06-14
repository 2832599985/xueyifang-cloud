package com.xueyifang.cloud.user.service;

record UserImportRow(
        Integer rowNum,
        String studentId,
        String realName,
        String phone,
        String professionalIdText,
        Long professionalId,
        boolean professionalIdInvalid,
        String password,
        String email,
        String dormitory,
        String grade,
        String nickname) {
}
