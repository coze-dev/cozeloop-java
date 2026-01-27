package com.coze.loop.prompt;

import com.coze.loop.entity.VariableDef;
import com.coze.loop.entity.VariableType;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.exception.PromptException;

import java.util.List;
import java.util.Map;

/**
 * Validator for prompt variables.
 */
public class VariableValidator {
    
    /**
     * Validate variables against their definitions.
     *
     * @param variables the variables to validate
     * @param variableDefs the variable definitions
     * @throws PromptException if validation fails
     */
    public void validate(Map<String, Object> variables, List<VariableDef> variableDefs) {
        if (variableDefs == null || variableDefs.isEmpty()) {
            return;
        }
        
        for (VariableDef def : variableDefs) {
            String key = def.getKey();
            Object value = variables.get(key);
            
            // Check if required variable is present
            if (value == null) {
                // For now, we don't enforce required variables
                // You can add a "required" field to VariableDef if needed
                continue;
            }
            
            // Validate type
            validateType(key, value, def.getType());
        }
    }
    
    /**
     * Validate variable type.
     */
    private void validateType(String key, Object value, VariableType expectedType) {
        if (expectedType == null) {
            return;
        }
        
        boolean valid = false;
        
        switch (expectedType) {
            case STRING:
                valid = value instanceof String;
                break;
            case BOOLEAN:
                valid = value instanceof Boolean;
                break;
            case INTEGER:
                valid = value instanceof Integer || value instanceof Long;
                break;
            case FLOAT:
                valid = value instanceof Float || value instanceof Double;
                break;
            case OBJECT:
                valid = value instanceof Map;
                break;
            case ARRAY_STRING:
            case ARRAY_BOOLEAN:
            case ARRAY_INTEGER:
            case ARRAY_FLOAT:
            case ARRAY_OBJECT:
                valid = value instanceof List;
                break;
            case PLACEHOLDER:
            case MULTI_PART:
                // These types don't need strict validation
                valid = true;
                break;
        }
        
        if (!valid) {
            throw new PromptException(ErrorCode.INVALID_PARAM,
                String.format("Variable '%s' has invalid type. Expected: %s, Got: %s",
                    key, expectedType.getValue(), value.getClass().getSimpleName()));
        }
    }
}

