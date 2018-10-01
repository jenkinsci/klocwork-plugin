package com.emenda.klocwork.util;

public class KlocworkIssue {
    public String id;
    public String code;

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

    public String message;
    public String file;
    public String line;

    public KlocworkIssue(String id, String code, String message, String file, String line) {
        this.id = id;
        this.code = code;
        this.message = message;
        this.file = file;
        this.line = line;
    }
}
