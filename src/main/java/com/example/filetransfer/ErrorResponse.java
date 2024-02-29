package com.example.filetransfer;

public class ErrorResponse {
    private String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    // Getter and setter
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
