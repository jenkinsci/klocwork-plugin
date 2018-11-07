package com.emenda.klocwork.definitions;

public class KlocworkIssue {
    private String id;
    private String code;
    private String message;
    private String file;
    private String line;
    private String severity;
    private String status;

    public KlocworkIssue() {}

    public KlocworkIssue(String id, String code, String message, String file, String line, String severity, String status) {
        this.id = id;
        this.code = code;
        this.message = message;
        this.file = file;
        this.line = line;
        this.severity = severity;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getFile() {
        return file;
    }

    public String getLine() {
        return line;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
