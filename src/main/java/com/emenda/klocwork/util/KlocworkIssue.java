package com.emenda.klocwork.util;

public class KlocworkIssue {
    private String id;
    private String code;
    private String message;
    private String file;
    private String line;

    public KlocworkIssue() {}

    public KlocworkIssue(String id, String code, String message, String file, String line) {
        this.id = id;
        this.code = code;
        this.message = message;
        this.file = file;
        this.line = line;
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
}
