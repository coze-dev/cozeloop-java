package com.coze.loop.entity;

/**
 * File entity for uploading to CozeLoop platform (multimodal content).
 */
public class UploadFile {
    private String tosKey;
    private String data;
    private String uploadType;
    private String tagKey;
    private String name;
    private String fileType;
    private String spaceId;
    
    public UploadFile() {
    }
    
    // Getters and Setters
    public String getTosKey() {
        return tosKey;
    }
    
    public void setTosKey(String tosKey) {
        this.tosKey = tosKey;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getUploadType() {
        return uploadType;
    }
    
    public void setUploadType(String uploadType) {
        this.uploadType = uploadType;
    }
    
    public String getTagKey() {
        return tagKey;
    }
    
    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getSpaceId() {
        return spaceId;
    }
    
    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final UploadFile file = new UploadFile();
        
        public Builder tosKey(String tosKey) {
            file.tosKey = tosKey;
            return this;
        }
        
        public Builder data(String data) {
            file.data = data;
            return this;
        }
        
        public Builder uploadType(String uploadType) {
            file.uploadType = uploadType;
            return this;
        }
        
        public Builder tagKey(String tagKey) {
            file.tagKey = tagKey;
            return this;
        }
        
        public Builder name(String name) {
            file.name = name;
            return this;
        }
        
        public Builder fileType(String fileType) {
            file.fileType = fileType;
            return this;
        }
        
        public Builder spaceId(String spaceId) {
            file.spaceId = spaceId;
            return this;
        }
        
        public UploadFile build() {
            return file;
        }
    }
}

