package com.example.datn;

public class Conversation {

    private String date;
    private String reply_path_present;
    private String body;
    private String type;
    private String thread_id;
    private String locked;
    private String date_send;
    private String read;
    private String address;
    private String service_center;
    private String error_code;
    private String _id;
    private String status;

    public Conversation(String date, String reply_path_present, String body, String type,
                        String thread_id, String locked, String date_send, String read,
                        String address, String service_center, String error_code,
                        String _id, String status) {
        this.date = date;
        this.reply_path_present = reply_path_present;
        this.body = body;
        this.type = type;
        this.thread_id = thread_id;
        this.locked = locked;
        this.date_send = date_send;
        this.read = read;
        this.address = address;
        this.service_center = service_center;
        this.error_code = error_code;
        this._id = _id;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public String getReply_path_present() {
        return reply_path_present;
    }

    public String getBody() {
        return body;
    }

    public String getType() {
        return type;
    }

    public String getThread_id() {
        return thread_id;
    }

    public String getLocked() {
        return locked;
    }

    public String getDate_send() {
        return date_send;
    }

    public String getRead() {
        return read;
    }

    public String getAddress() {
        return address;
    }

    public String getService_center() {
        return service_center;
    }

    public String getError_code() {
        return error_code;
    }

    public String get_id() {
        return _id;
    }

    public String getStatus() {
        return status;
    }
}
