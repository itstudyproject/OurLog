package com.example.ourLog.dto;

public class ChatMessageDTO {
  private String sender;
  private String receiver; // ← 이 필드가 있어야 함
  private String content;
  private String timestamp;

  // 기본 생성자
  public ChatMessageDTO() {}

  // 생성자, Getter, Setter
  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getReceiver() {
    return receiver;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }
}
