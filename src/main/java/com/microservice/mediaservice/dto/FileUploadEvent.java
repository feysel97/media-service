package com.microservice.mediaservice.dto;

public record FileUploadEvent(String username, String filename, String contentType, long size) {}