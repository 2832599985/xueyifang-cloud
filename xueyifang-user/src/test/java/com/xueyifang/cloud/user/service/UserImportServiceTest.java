package com.xueyifang.cloud.user.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.user.dto.UserImportResultResponse;
import com.xueyifang.cloud.user.dto.UserImportTemplateResponse;
import com.xueyifang.cloud.user.support.InMemoryUserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserImportServiceTest {

    private final InMemoryUserAccountRepository repository = new InMemoryUserAccountRepository();

    private final UserImportService service = new UserImportService(repository, new BCryptPasswordEncoder());

    @BeforeEach
    void setUp() {
        repository.putProfessionalId(1L);
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void importsCsvUsersAndReportsSkippedAndFailedRows() {
        String csv = """
                studentId,realName,phone,professionalId,password,email,dormitory,grade,nickname
                学号(必填),姓名(必填),手机号(必填),专业ID(必填),密码(必填),邮箱(可选),寝室号(可选),年级(可选),昵称(可选)
                20240001,张三,13800138001,1,Test1234,zhangsan@example.com,宿舍1-101,大一,
                20240001,张三,13800138001,1,Test1234,zhangsan@example.com,宿舍1-101,大一,
                20240002,李四,10000000000,1,Test1234,lisi@example.com,宿舍2-202,大二,小李
                """;

        UserImportResultResponse response = service.importUsers(new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)));

        assertThat(response.getTotalCount()).isEqualTo(3);
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getSkippedCount()).isEqualTo(1);
        assertThat(response.getFailedCount()).isEqualTo(1);
        assertThat(response.getFailedRows().getFirst().studentId()).isEqualTo("20240002");
        assertThat(repository.findExistingStudentIds()).contains("20240001");
        assertThat(repository.findById(1L).orElseThrow().password()).startsWith("$2a$");
    }

    @Test
    void createsCsvAndExcelTemplates() {
        UserImportTemplateResponse csv = service.createTemplate("csv");
        UserImportTemplateResponse xlsx = service.createTemplate("xlsx");

        assertThat(csv.filename()).isEqualTo("user_import_template.csv");
        assertThat(new String(csv.content(), StandardCharsets.UTF_8)).contains("studentId");
        assertThat(xlsx.filename()).isEqualTo("user_import_template.xlsx");
        assertThat(xlsx.content()).isNotEmpty();
    }

    @Test
    void rejectsImportByNormalUser() {
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));

        assertThatThrownBy(() -> service.importUsers(new MockMultipartFile(
                "file", "users.csv", "text/csv", "studentId\n".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode()));
    }
}
