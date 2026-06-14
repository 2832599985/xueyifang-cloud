package com.xueyifang.cloud.system.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.system.dto.PageResponse;
import com.xueyifang.cloud.system.dto.ProfessionalRequest;
import com.xueyifang.cloud.system.dto.ProfessionalResponse;
import com.xueyifang.cloud.system.dto.SysConfigRegisterStatusResponse;
import com.xueyifang.cloud.system.dto.SysConfigResponse;
import com.xueyifang.cloud.system.dto.SysConfigUpdateRequest;
import com.xueyifang.cloud.system.dto.TradeLocationRequest;
import com.xueyifang.cloud.system.dto.TradeLocationResponse;
import com.xueyifang.cloud.system.support.InMemorySystemDictionaryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemDictionaryServiceTest {

    private final InMemorySystemDictionaryRepository repository = new InMemorySystemDictionaryRepository();

    private final SystemDictionaryService service = new SystemDictionaryService(repository);

    private Long professionalId;

    private Long disabledLocationId;

    private Long registerConfigId;

    @BeforeEach
    void setUp() {
        repository.clear();
        professionalId = repository.putProfessional("Computer Science", "CS", false);
        repository.putProfessional("Deleted", "hidden", true);
        repository.putTradeLocation("Library", "front desk", "1 Main", 1, false);
        disabledLocationId = repository.putTradeLocation("Old Hall", "closed", "2 Main", 0, false);
        registerConfigId = repository.putSysConfig("REGISTER_ENABLED", "1", "register switch", 1);
        repository.putSysConfig("REVIEW_MODE", "1", "review mode", 1);
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void listsPublicDictionariesWithoutLogin() {
        List<ProfessionalResponse> professionals = service.listProfessionals();
        List<TradeLocationResponse> locations = service.listTradeLocations(true);

        assertThat(professionals).extracting(ProfessionalResponse::professionalName)
                .containsExactly("Computer Science");
        assertThat(locations).extracting(TradeLocationResponse::locationName)
                .containsExactly("Library");
    }

    @Test
    void returnsRegisterStatusFromEnabledConfig() {
        SysConfigRegisterStatusResponse response = service.getRegisterStatus();

        assertThat(response.registerEnabled()).isTrue();
    }

    @Test
    void letsAdminCreateAndPageProfessionals() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));

        Long id = service.createProfessional(new ProfessionalRequest(null, "Software Engineering", "SE"));
        PageResponse<ProfessionalResponse> page = service.pageProfessionals("Software", 1, 10);

        assertThat(id).isPositive();
        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records().getFirst().professionalName()).isEqualTo("Software Engineering");
    }

    @Test
    void rejectsAdminWritesForNormalUsers() {
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));

        assertThatThrownBy(() -> service.createProfessional(
                new ProfessionalRequest(null, "Software Engineering", "SE")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode()));
    }

    @Test
    void rejectsDuplicateProfessionalNames() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));

        assertThatThrownBy(() -> service.createProfessional(
                new ProfessionalRequest(null, "Computer Science", "duplicate")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    @Test
    void updatesTradeLocationAndSupportsAdminFiltering() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));

        service.updateTradeLocation(new TradeLocationRequest(disabledLocationId, "Old Hall", "open", "2 Main", 1));
        PageResponse<TradeLocationResponse> page = service.pageTradeLocations(1, "Old", 1, 10, true);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records().getFirst().locationDescription()).isEqualTo("open");
    }

    @Test
    void updatesSysConfigAndReadsKeyValues() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));

        service.updateSysConfig(new SysConfigUpdateRequest(registerConfigId, "0", "closed", 1));
        SysConfigRegisterStatusResponse registerStatus = service.getRegisterStatus();
        Map<String, String> values = service.getEnabledConfigValues(List.of("REGISTER_ENABLED", "REVIEW_MODE"));
        SysConfigResponse detail = service.getSysConfig(registerConfigId);

        assertThat(registerStatus.registerEnabled()).isFalse();
        assertThat(values).containsEntry("REGISTER_ENABLED", "0");
        assertThat(detail.description()).isEqualTo("closed");
    }

    @Test
    void batchDeletesTradeLocations() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));

        int deleted = service.deleteTradeLocations(List.of(disabledLocationId));

        assertThat(deleted).isEqualTo(1);
        assertThat(service.listTradeLocations(false)).extracting(TradeLocationResponse::id)
                .doesNotContain(disabledLocationId);
    }

    @Test
    void getsProfessionalById() {
        ProfessionalResponse response = service.getProfessional(professionalId);

        assertThat(response.professionalName()).isEqualTo("Computer Science");
    }
}
