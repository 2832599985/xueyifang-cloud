package com.xueyifang.cloud.user.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.user.dto.UserImportResultResponse;
import com.xueyifang.cloud.user.dto.UserImportTemplateResponse;
import com.xueyifang.cloud.user.repository.UserAccountRepository;
import com.xueyifang.cloud.user.repository.UserImportCreateCommand;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserImportService {

    private static final int ADMIN_ROLE = 2;

    private static final int HAS_PUBLISH_PERMISSION = 1;

    private static final int PERMISSION_APPROVED = 1;

    private static final int ACTIVE_STATUS = 1;

    private static final int NORMAL_ACCOUNT_STATUS = 1;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,16}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

    private static final Set<String> VALID_GRADES = Set.of("大一", "大二", "大三", "大四");

    private static final List<String> TEMPLATE_COLUMNS = List.of(
            "studentId", "realName", "phone", "professionalId", "password",
            "email", "dormitory", "grade", "nickname");

    private static final Map<String, String> COLUMN_MAPPING = createColumnMapping();

    private final UserAccountRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserImportService(UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserImportResultResponse importUsers(MultipartFile file) {
        requireAdmin();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "file must not be empty");
        }

        String filename = normalizeOptional(file.getOriginalFilename());
        if (filename == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "filename is invalid");
        }

        List<UserImportRow> rows = parseRows(file, suffix(filename));
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "file has no import rows");
        }

        Set<String> existingStudentIds = userRepository.findExistingStudentIds().stream()
                .map(this::identityKey)
                .collect(Collectors.toSet());
        Set<String> existingUsernames = userRepository.findExistingUsernames().stream()
                .map(this::identityKey)
                .collect(Collectors.toSet());
        Set<Long> validProfessionalIds = userRepository.findActiveProfessionalIds();

        UserImportResultResponse result = new UserImportResultResponse();
        for (UserImportRow row : rows) {
            if (isTemplateDescriptionRow(row)) {
                continue;
            }

            String validationError = validate(row, validProfessionalIds);
            if (validationError != null) {
                result.addFailed(row.rowNum(), row.studentId(), validationError);
                continue;
            }

            String studentIdKey = identityKey(row.studentId());
            if (existingStudentIds.contains(studentIdKey) || existingUsernames.contains(studentIdKey)) {
                result.addSkipped();
                continue;
            }

            try {
                userRepository.createImportedUser(toCreateCommand(row));
                existingStudentIds.add(studentIdKey);
                existingUsernames.add(studentIdKey);
                result.addSuccess();
            } catch (DuplicateKeyException exception) {
                existingStudentIds.add(studentIdKey);
                existingUsernames.add(studentIdKey);
                result.addSkipped();
            }
        }

        return result;
    }

    public UserImportTemplateResponse createTemplate(String format) {
        requireAdmin();
        if ("csv".equalsIgnoreCase(format)) {
            byte[] content = """
                    studentId,realName,phone,professionalId,password,email,dormitory,grade,nickname
                    20240001,张三,13800138001,1,Test1234,zhangsan@example.com,宿舍1-101,大一,
                    20240002,李四,13800138002,2,Test1234,lisi@example.com,宿舍2-202,大二,小李
                    """.getBytes(StandardCharsets.UTF_8);
            return new UserImportTemplateResponse(content, "user_import_template.csv", "text/csv; charset=UTF-8");
        }

        return new UserImportTemplateResponse(
                createExcelTemplate(),
                "user_import_template.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    private List<UserImportRow> parseRows(MultipartFile file, String suffix) {
        try {
            if ("csv".equals(suffix)) {
                return parseCsv(file);
            }
            if ("xlsx".equals(suffix) || "xls".equals(suffix)) {
                return parseExcel(file);
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "file parse failed: " + exception.getMessage());
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "only .xlsx, .xls and .csv files are supported");
    }

    private List<UserImportRow> parseCsv(MultipartFile file) throws IOException {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            Map<String, String> headerToField = mapHeaderNames(parser.getHeaderMap().keySet());
            validateRequiredColumns(headerToField);
            return parser.stream()
                    .map(record -> parseCsvRecord(record, headerToField))
                    .filter(row -> !isBlankRow(row))
                    .toList();
        }
    }

    private UserImportRow parseCsvRecord(CSVRecord record, Map<String, String> headerToField) {
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, String> entry : headerToField.entrySet()) {
            values.put(entry.getValue(), record.get(entry.getKey()));
        }
        return toRow(Math.toIntExact(record.getRecordNumber() + 1), values);
    }

    private List<UserImportRow> parseExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel sheet is empty");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel header is empty");
            }

            DataFormatter formatter = new DataFormatter();
            Map<Integer, String> columnToField = mapExcelColumns(headerRow, formatter);
            validateRequiredColumnsByField(columnToField.values());

            return java.util.stream.IntStream.rangeClosed(1, sheet.getLastRowNum())
                    .mapToObj(index -> parseExcelRow(sheet.getRow(index), index + 1, columnToField, formatter))
                    .filter(row -> row != null && !isBlankRow(row))
                    .toList();
        }
    }

    private UserImportRow parseExcelRow(Row row, int rowNum, Map<Integer, String> columnToField,
                                        DataFormatter formatter) {
        if (row == null) {
            return null;
        }
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<Integer, String> entry : columnToField.entrySet()) {
            values.put(entry.getValue(), formatter.formatCellValue(row.getCell(entry.getKey())).trim());
        }
        return toRow(rowNum, values);
    }

    private UserImportRow toRow(int rowNum, Map<String, String> values) {
        String professionalIdText = normalizeOptional(values.get("professionalId"));
        Long professionalId = null;
        boolean professionalIdInvalid = false;
        if (professionalIdText != null) {
            try {
                professionalId = Long.parseLong(professionalIdText.replaceAll("\\.0$", ""));
            } catch (NumberFormatException exception) {
                professionalIdInvalid = true;
            }
        }

        return new UserImportRow(
                rowNum,
                normalizeOptional(values.get("studentId")),
                normalizeOptional(values.get("realName")),
                normalizeOptional(values.get("phone")),
                professionalIdText,
                professionalId,
                professionalIdInvalid,
                normalizeOptional(values.get("password")),
                normalizeOptional(values.get("email")),
                normalizeOptional(values.get("dormitory")),
                normalizeOptional(values.get("grade")),
                normalizeOptional(values.get("nickname")));
    }

    private String validate(UserImportRow row, Set<Long> validProfessionalIds) {
        if (row.studentId() == null) {
            return "studentId must not be blank";
        }
        if (row.studentId().length() < 6 || row.studentId().length() > 20) {
            return "studentId length must be between 6 and 20";
        }
        if (row.realName() == null) {
            return "realName must not be blank";
        }
        if (row.realName().length() < 2 || row.realName().length() > 20) {
            return "realName length must be between 2 and 20";
        }
        if (row.phone() == null || !PHONE_PATTERN.matcher(row.phone()).matches()) {
            return "phone format is invalid";
        }
        if (row.professionalIdText() == null) {
            return "professionalId must not be blank";
        }
        if (row.professionalIdInvalid()) {
            return "professionalId must be numeric";
        }
        if (!validProfessionalIds.contains(row.professionalId())) {
            return "professionalId does not exist: " + row.professionalId();
        }
        if (row.password() == null || !PASSWORD_PATTERN.matcher(row.password()).matches()) {
            return "password must be 8-16 chars and contain letters and numbers";
        }
        if (row.email() != null && !EMAIL_PATTERN.matcher(row.email()).matches()) {
            return "email format is invalid";
        }
        if (row.dormitory() != null && row.dormitory().length() > 100) {
            return "dormitory length must be <= 100";
        }
        if (row.grade() != null && !VALID_GRADES.contains(row.grade())) {
            return "grade must be one of 大一, 大二, 大三, 大四";
        }
        if (row.nickname() != null && row.nickname().length() > 64) {
            return "nickname length must be <= 64";
        }
        return null;
    }

    private UserImportCreateCommand toCreateCommand(UserImportRow row) {
        String nickname = row.nickname() != null ? row.nickname() : row.realName();
        return new UserImportCreateCommand(
                row.studentId(),
                passwordEncoder.encode(row.password()),
                row.studentId(),
                row.realName(),
                nickname,
                row.phone(),
                row.email(),
                row.dormitory(),
                row.grade(),
                row.professionalId(),
                UserRole.STUDENT.databaseValue(),
                HAS_PUBLISH_PERMISSION,
                PERMISSION_APPROVED,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ACTIVE_STATUS,
                NORMAL_ACCOUNT_STATUS);
    }

    private byte[] createExcelTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("users");
            writeRow(sheet.createRow(0), TEMPLATE_COLUMNS);
            writeRow(sheet.createRow(1), List.of(
                    "学号(必填)", "姓名(必填)", "手机号(必填)", "专业ID(必填)", "密码(必填)",
                    "邮箱(可选)", "寝室号(可选)", "年级(可选)", "昵称(可选)"));
            writeRow(sheet.createRow(2), List.of(
                    "20240001", "张三", "13800138001", "1", "Test1234",
                    "zhangsan@example.com", "宿舍1-101", "大一", ""));
            writeRow(sheet.createRow(3), List.of(
                    "20240002", "李四", "13800138002", "2", "Test1234",
                    "lisi@example.com", "宿舍2-202", "大二", "小李"));
            for (int i = 0; i < TEMPLATE_COLUMNS.size(); i++) {
                sheet.setColumnWidth(i, 18 * 256);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "template create failed");
        }
    }

    private void writeRow(Row row, List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            row.createCell(i).setCellValue(values.get(i));
        }
    }

    private Map<String, String> mapHeaderNames(Set<String> headers) {
        Map<String, String> headerToField = new LinkedHashMap<>();
        for (String header : headers) {
            String field = COLUMN_MAPPING.get(normalizeHeader(header));
            if (field != null) {
                headerToField.put(header, field);
            }
        }
        return headerToField;
    }

    private Map<Integer, String> mapExcelColumns(Row headerRow, DataFormatter formatter) {
        Map<Integer, String> columnToField = new LinkedHashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String header = formatter.formatCellValue(headerRow.getCell(i));
            String field = COLUMN_MAPPING.get(normalizeHeader(header));
            if (field != null) {
                columnToField.put(i, field);
            }
        }
        return columnToField;
    }

    private void validateRequiredColumns(Map<String, String> headerToField) {
        validateRequiredColumnsByField(headerToField.values());
    }

    private void validateRequiredColumnsByField(Iterable<String> fields) {
        java.util.Set<String> presentFields = new java.util.HashSet<>();
        fields.forEach(presentFields::add);
        for (String field : List.of("studentId", "realName", "phone", "professionalId", "password")) {
            if (!presentFields.contains(field)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "missing required columns: studentId, realName, phone, professionalId, password");
            }
        }
    }

    private boolean isBlankRow(UserImportRow row) {
        return row.studentId() == null
                && row.realName() == null
                && row.phone() == null
                && row.professionalIdText() == null
                && row.password() == null
                && row.email() == null
                && row.dormitory() == null
                && row.grade() == null
                && row.nickname() == null;
    }

    private boolean isTemplateDescriptionRow(UserImportRow row) {
        return row.studentId() != null && row.studentId().contains("必填");
    }

    private String suffix(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String identityKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeHeader(String value) {
        return value == null ? "" : value.trim();
    }

    private LoginUserContext requireAdmin() {
        LoginUserContext user = UserContextHolder.get()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
        if (!Integer.valueOf(ADMIN_ROLE).equals(user.role())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "admin role required");
        }
        return user;
    }

    private static Map<String, String> createColumnMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("studentId", "studentId");
        mapping.put("学号", "studentId");
        mapping.put("realName", "realName");
        mapping.put("姓名", "realName");
        mapping.put("真实姓名", "realName");
        mapping.put("phone", "phone");
        mapping.put("手机号", "phone");
        mapping.put("电话", "phone");
        mapping.put("professionalId", "professionalId");
        mapping.put("专业ID", "professionalId");
        mapping.put("password", "password");
        mapping.put("密码", "password");
        mapping.put("email", "email");
        mapping.put("邮箱", "email");
        mapping.put("dormitory", "dormitory");
        mapping.put("寝室", "dormitory");
        mapping.put("寝室号", "dormitory");
        mapping.put("grade", "grade");
        mapping.put("年级", "grade");
        mapping.put("nickname", "nickname");
        mapping.put("昵称", "nickname");
        return Map.copyOf(mapping);
    }
}
