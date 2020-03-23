package org.bitbucket.mlopatkin.android.logviewer.search;

import org.checkerframework.checker.nullness.qual.Nullable;

public class RequestCompilationException extends Exception {
    private String request;

    public RequestCompilationException(String message, String request) {
        this(message, request, null);
    }

    public RequestCompilationException(String message, String request, @Nullable Throwable cause) {
        super(message, cause);
        this.request = request;
    }

    public final String getRequestValue() {
        return request;
    }

    public final void setRequestValue(String request) {
        this.request = request;
    }
}
