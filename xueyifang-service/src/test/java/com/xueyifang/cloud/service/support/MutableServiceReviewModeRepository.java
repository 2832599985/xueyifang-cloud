package com.xueyifang.cloud.service.support;

import com.xueyifang.cloud.service.repository.ServiceReviewModeRepository;

public class MutableServiceReviewModeRepository implements ServiceReviewModeRepository {

    private boolean requiresReview;

    public MutableServiceReviewModeRepository(boolean requiresReview) {
        this.requiresReview = requiresReview;
    }

    @Override
    public boolean requiresReview() {
        return requiresReview;
    }

    public void setRequiresReview(boolean requiresReview) {
        this.requiresReview = requiresReview;
    }
}
