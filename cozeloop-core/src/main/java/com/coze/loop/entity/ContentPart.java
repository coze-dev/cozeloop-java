package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Content part in a message.
 */
public class ContentPart {
    @JsonProperty("type")
    private ContentType type;
    
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("image_url")
    private String imageUrl;
    
    @JsonProperty("base64_data")
    private String base64Data;
    
    public ContentPart() {
    }
    
    public ContentPart(ContentType type) {
        this.type = type;
    }
    
    // Getters and Setters
    public ContentType getType() {
        return type;
    }
    
    public void setType(ContentType type) {
        this.type = type;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getBase64Data() {
        return base64Data;
    }
    
    public void setBase64Data(String base64Data) {
        this.base64Data = base64Data;
    }
    
    /**
     * Deep copy this content part.
     *
     * @return a new ContentPart instance with copied values
     */
    public ContentPart deepCopy() {
        ContentPart copy = new ContentPart();
        copy.type = this.type;
        copy.text = this.text;
        copy.imageUrl = this.imageUrl;
        copy.base64Data = this.base64Data;
        return copy;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ContentPart contentPart = new ContentPart();
        
        public Builder type(ContentType type) {
            contentPart.type = type;
            return this;
        }
        
        public Builder text(String text) {
            contentPart.text = text;
            return this;
        }
        
        public Builder imageUrl(String imageUrl) {
            contentPart.imageUrl = imageUrl;
            return this;
        }
        
        public Builder base64Data(String base64Data) {
            contentPart.base64Data = base64Data;
            return this;
        }
        
        public ContentPart build() {
            return contentPart;
        }
    }
}

