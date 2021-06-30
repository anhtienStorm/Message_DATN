package com.example.datn;

public class Message {
    private String _id;
    private String thread_id;
    private String date;
    private String date_sent;
    private String read;
    private String status;
    private String type;
    private String reply_path_present;
    private String subject;
    private String body;
    private String service_center;
    private String locked;
    private String error_code;
    private String seen;

    public Message(String _id, String thread_id, String date, String date_sent, String read,
                   String status, String type, String reply_path_present, String subject,
                   String body, String service_center, String locked, String error_code,
                   String seen) {
        this._id = _id;
        this.thread_id = thread_id;
        this.date = date;
        this.date_sent = date_sent;
        this.read = read;
        this.status = status;
        this.type = type;
        this.reply_path_present = reply_path_present;
        this.subject = subject;
        this.body = body;
        this.service_center = service_center;
        this.locked = locked;
        this.error_code = error_code;
        this.seen = seen;
    }

    public String get_id() {
        return _id;
    }

    public String getThread_id() {
        return thread_id;
    }

    public String getDate() {
        return date;
    }

    public String getDate_sent() {
        return date_sent;
    }

    public String getRead() {
        return read;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getReply_path_present() {
        return reply_path_present;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getService_center() {
        return service_center;
    }

    public String getLocked() {
        return locked;
    }

    public String getError_code() {
        return error_code;
    }

    public String getSeen() {
        return seen;
    }
}
