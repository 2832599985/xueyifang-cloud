package com.xueyifang.cloud.user.dto;

import java.util.ArrayList;
import java.util.List;

public class UserImportResultResponse {

    private int totalCount;

    private int successCount;

    private int skippedCount;

    private int failedCount;

    private final List<FailedRow> failedRows = new ArrayList<>();

    public int getTotalCount() {
        return totalCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public List<FailedRow> getFailedRows() {
        return failedRows;
    }

    public void addSuccess() {
        successCount++;
        totalCount++;
    }

    public void addSkipped() {
        skippedCount++;
        totalCount++;
    }

    public void addFailed(Integer rowNum, String studentId, String reason) {
        failedCount++;
        totalCount++;
        failedRows.add(new FailedRow(rowNum, studentId, reason));
    }

    public record FailedRow(
            Integer rowNum,
            String studentId,
            String reason) {
    }
}
