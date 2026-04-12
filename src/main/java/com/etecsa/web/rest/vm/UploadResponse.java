package com.etecsa.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response object for file upload.
 */
public class UploadResponse {

    private String url;
    private String error;

    public UploadResponse() {}

    public UploadResponse(String url, String error) {
        this.url = url;
        this.error = error;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("error")
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null && !error.isEmpty();
    }

    public boolean isSuccess() {
        return url != null && !url.isEmpty();
    }
}
