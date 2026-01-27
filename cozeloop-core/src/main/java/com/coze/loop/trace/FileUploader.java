package com.coze.loop.trace;

import com.coze.loop.entity.UploadFile;
import com.coze.loop.http.HttpClient;
import com.coze.loop.internal.IdGenerator;
import com.coze.loop.internal.JsonUtils;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.data.SpanData;
import okhttp3.MultipartBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uploader for multimodal files extracted from span data.
 */
public class FileUploader {
    private static final Logger logger = LoggerFactory.getLogger(FileUploader.class);
    private static final Pattern BASE64_PATTERN = Pattern.compile(
        "data:image/([a-z]+);base64,([A-Za-z0-9+/=]+)", Pattern.CASE_INSENSITIVE);
    
    private final HttpClient httpClient;
    private final String uploadEndpoint;
    private final String workspaceId;
    
    public FileUploader(HttpClient httpClient, String uploadEndpoint, String workspaceId) {
        this.httpClient = httpClient;
        this.uploadEndpoint = uploadEndpoint;
        this.workspaceId = workspaceId;
    }
    
    /**
     * Extract multimodal files from span data.
     *
     * @param spanData the span data
     * @return list of upload files
     */
    public List<UploadFile> extractFiles(SpanData spanData) {
        List<UploadFile> files = new ArrayList<>();
        
        // Check input for base64 images
        String input = spanData.getAttributes().get(AttributeKey.stringKey("cozeloop.input"));
        if (input != null) {
            extractBase64Files(input, "input", files);
        }
        
        // Check output for base64 images
        String output = spanData.getAttributes().get(AttributeKey.stringKey("cozeloop.output"));
        if (output != null) {
            extractBase64Files(output, "output", files);
        }
        
        return files;
    }
    
    /**
     * Extract base64 encoded files from content.
     */
    private void extractBase64Files(String content, String tagKey, List<UploadFile> files) {
        Matcher matcher = BASE64_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String fileType = matcher.group(1);
            String base64Data = matcher.group(2);
            
            UploadFile file = UploadFile.builder()
                .tosKey(generateTosKey())
                .data(base64Data)
                .uploadType("base64")
                .tagKey(tagKey)
                .fileType(fileType)
                .name("image." + fileType)
                .spaceId(workspaceId)
                .build();
            
            files.add(file);
        }
    }
    
    /**
     * Upload files to the server.
     *
     * @param files list of files to upload
     * @return object storage key
     */
    public String uploadFiles(List<UploadFile> files) {
        if (files.isEmpty()) {
            return null;
        }
        
        try {
            // Build multipart form data
            MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
            
            // Add files as JSON
            String filesJson = JsonUtils.toJson(files);
            builder.addFormDataPart("files", filesJson);
            
            MultipartBody formData = builder.build();
            
            // Upload files
            String response = httpClient.postMultipart(uploadEndpoint, formData);
            
            // Parse response to get object storage key
            // Assuming response contains {"object_storage": "key"}
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> responseMap = JsonUtils.fromJson(
                response, java.util.Map.class);
            
            return (String) responseMap.get("object_storage");
        } catch (Exception e) {
            logger.error("Failed to upload files", e);
            return null;
        }
    }
    
    /**
     * Generate a unique TOS (Object Storage) key.
     */
    private String generateTosKey() {
        return "cozeloop/" + workspaceId + "/" + IdGenerator.generateUuid();
    }
}

