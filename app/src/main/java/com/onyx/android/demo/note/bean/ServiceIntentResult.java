package com.onyx.android.demo.note.bean;

public class ServiceIntentResult {
    public int status = ServiceIntentStatus.SUCCESS;
    public int code;

    public ServiceIntentResult() {
    }

    public ServiceIntentResult(int status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return status == ServiceIntentStatus.SUCCESS;
    }

    public boolean isFail() {
        return status == ServiceIntentStatus.FAIL;
    }

    public boolean inProgress() {
        return status == ServiceIntentStatus.PROGRESS;
    }

    public ServiceIntentResult progress() {
        status = ServiceIntentStatus.PROGRESS;
        return this;
    }

    public ServiceIntentResult fail() {
        status = ServiceIntentStatus.FAIL;
        return this;
    }
}
