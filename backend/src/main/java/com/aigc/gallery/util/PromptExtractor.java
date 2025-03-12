package com.aigc.gallery.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PromptExtractor {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 提取提示词
     * @param metadata 元数据文本
     * @return [正面提示词, 负面提示词]
     */
    public String[] extractPrompts(String metadata) {
        String[] result = new String[]{"", ""};
        
        if (metadata == null || metadata.trim().isEmpty()) {
            return result;
        }
        
        try {
            String trimmedMetadata = metadata.trim();
            
            // 处理各种特殊格式
            String jsonData = preprocessMetadata(trimmedMetadata);
            
            // 尝试不同的提取方法
            try {
                // 首先尝试解析为 JSON
                if (jsonData != null) {
                    // 检查是否为 workflow 格式
                    if (jsonData.contains("\"nodes\":")) {
                        return extractFromComfyWorkflow(jsonData);
                    } else if (jsonData.contains("\"class_type\":")) {
                        // 处理 prompt 格式
                        return extractFromComfyPrompt(jsonData);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to extract from JSON: {}", e.getMessage());
                // 继续尝试其他方法
            }
            
            // 最后尝试纯文本提取
            try {
                String[] plainTextResult = extractFromPlainText(metadata);
                if (!plainTextResult[0].isEmpty() || !plainTextResult[1].isEmpty()) {
                    return plainTextResult;
                }
            } catch (Exception e) {
                log.warn("Failed to extract from plain text: {}", e.getMessage());
            }
            
            // 如果上述方法都失败，尝试从原始字符串中搜索关键模式
            return fallbackExtraction(metadata);
            
        } catch (Exception e) {
            log.error("Error extracting prompts: {}", e.getMessage());
            log.debug("Original metadata: {}", metadata);
            return result;
        }
    }
    
    /**
     * 预处理元数据，提取可能的 JSON 数据
     */
    private String preprocessMetadata(String metadata) {
        try {
            // 处理 [prompt: {...}] 或 [workflow: {...}] 格式
            if (metadata.startsWith("[") && metadata.contains(":")) {
                // 提取 JSON 部分
                int start = metadata.indexOf('{');
                int end = metadata.lastIndexOf('}');
                if (start >= 0 && end > start) {
                    return metadata.substring(start, end + 1);
                }
                else {
                    if (metadata.contains("Description:")){
                        return metadata.split("Description:")[1].replace("]", "");
                    }
                }
            }
            
            // 处理 raw json 格式
            if (metadata.startsWith("{") && metadata.endsWith("}")) {
                return metadata;
            }
            
            // 尝试在字符串中查找可能的 JSON 内容
            int start = metadata.indexOf('{');
            int end = metadata.lastIndexOf('}');
            if (start >= 0 && end > start && (end - start) > 50) { // 至少要有足够长度才可能是 JSON
                return metadata.substring(start, end + 1);
            }
            
            return null;
        } catch (Exception e) {
            log.warn("Error preprocessing metadata: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 当所有方法都失败时的回退提取
     */
    private String[] fallbackExtraction(String metadata) {
        try {
            String positivePrompt = "";
            String negativePrompt = "";
            
            // 查找常见的负面提示词标记
            String[] negativeMarkers = {"Negative prompt:", "negative prompt:", "Negative Prompt:", "负面提示词:", "反向提示词:"};
            
            for (String marker : negativeMarkers) {
                if (metadata.contains(marker)) {
                    String[] parts = metadata.split(marker, 2);
                    positivePrompt = parts[0].trim();
                    if (parts.length > 1) {
                        negativePrompt = parts[1].trim();
                    }
                    return new String[]{positivePrompt, negativePrompt};
                }
            }
            
            // 如果找不到明确的标记，尝试找含有常见负面关键词的部分
            String[] commonNegativeTerms = {"lowres", "bad anatomy", "worst quality", "low quality"};
            for (String term : commonNegativeTerms) {
                if (metadata.contains(term)) {
                    // 找到包含此术语的句子或段落
                    int index = metadata.indexOf(term);
                    // 向前找到句子开始
                    int start = metadata.lastIndexOf(".", index);
                    if (start == -1) start = 0; else start += 1;
                    
                    // 假设这个句子是负面提示词
                    negativePrompt = metadata.substring(start).trim();
                    positivePrompt = metadata.substring(0, start).trim();
                    return new String[]{positivePrompt, negativePrompt};
                }
            }
            
            // 如果还是无法确定，就返回整个文本作为正面提示词
            return new String[]{metadata.trim(), ""};
        } catch (Exception e) {
            log.warn("Error in fallback extraction: {}", e.getMessage());
            return new String[]{"", ""};
        }
    }
    
    /**
     * 从 ComfyUI workflow JSON 中提取提示词
     */
    private String[] extractFromComfyWorkflow(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode nodes = root.path("nodes");
            
            List<String> positivePrompts = new ArrayList<>();
            List<String> negativePrompts = new ArrayList<>();
            
            // 遍历所有节点
            if (nodes.isArray()) {
                // 第一遍遍历，建立节点ID到节点的映射
                Map<String, JsonNode> nodeMap = new HashMap<>();
                for (JsonNode node : nodes) {
                    String nodeId = node.path("id").asText("");
                    if (!nodeId.isEmpty()) {
                        nodeMap.put(nodeId, node);
                    }
                }
                
                // 第二遍遍历，找到所有 KSampler 节点
                for (JsonNode node : nodes) {
                    String nodeType = node.path("type").asText("");
                    if ("KSampler".equals(nodeType)) {
                        // 获取 positive 和 negative conditioning
                        JsonNode inputs = node.path("inputs");
                        String positiveNodeId = getNodeIdFromInput(inputs.path("positive"));
                        String negativeNodeId = getNodeIdFromInput(inputs.path("negative"));
                        
                        // 处理 positive conditioning
                        if (positiveNodeId != null) {
                            String prompt = extractPromptFromConditioningChain(positiveNodeId, nodeMap);
                            if (prompt != null && !prompt.isEmpty()) {
                                positivePrompts.add(prompt);
                            }
                        }
                        
                        // 处理 negative conditioning
                        if (negativeNodeId != null) {
                            String prompt = extractPromptFromConditioningChain(negativeNodeId, nodeMap);
                            if (prompt != null && !prompt.isEmpty()) {
                                negativePrompts.add(prompt);
                            }
                        }
                    }
                }
            }
            
            return new String[]{
                String.join(", ", positivePrompts),
                String.join(", ", negativePrompts)
            };
            
        } catch (Exception e) {
            log.error("Error parsing ComfyUI workflow: " + e.getMessage(), e);
            return new String[]{"", ""};
        }
    }
    
    /**
     * 从输入连接中获取节点ID
     */
    private String getNodeIdFromInput(JsonNode input) {
        if (input.isArray() && input.size() > 0) {
            return input.get(0).asText();
        }
        return null;
    }
    
    /**
     * 从 conditioning 链中提取提示词
     */
    private String extractPromptFromConditioningChain(String nodeId, Map<String, JsonNode> nodeMap) {
        JsonNode node = nodeMap.get(nodeId);
        if (node == null) return null;
        
        String nodeType = node.path("type").asText("");
        
        // 如果是 CLIPTextEncode 节点，直接获取提示词
        if ("CLIPTextEncode".equals(nodeType)) {
            return node.path("widgets_values").get(0).asText("");
        }
        
        // 如果是其他类型的节点，尝试追踪其输入
        JsonNode inputs = node.path("inputs");
        for (JsonNode input : inputs) {
            String inputNodeId = getNodeIdFromInput(input);
            if (inputNodeId != null) {
                String prompt = extractPromptFromConditioningChain(inputNodeId, nodeMap);
                if (prompt != null && !prompt.isEmpty()) {
                    return prompt;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 从纯文本中提取提示词
     */
    private String[] extractFromPlainText(String text) {
        String[] parts = text.split("Negative prompt:", 2);
        
        if (parts.length > 1) {
            return new String[]{
                parts[0].trim(),
                parts[1].trim()
            };
        } else {
            return new String[]{text.trim(), ""};
        }
    }
    
    /**
     * 从 ComfyUI prompt JSON 中提取提示词
     */
    private String[] extractFromComfyPrompt(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String positivePrompt = "";
            String negativePrompt = "";
            
            // 遍历所有节点
            for (JsonNode node : root) {
                String classType = node.path("class_type").asText("");
                if ("CLIPTextEncode".equals(classType)) {
                    JsonNode inputs = node.path("inputs");
                    String text = inputs.path("text").asText("");
                    
                    // 检查是否为负面提示词
                    JsonNode clipInput = inputs.path("clip");
                    if (clipInput.isArray() && clipInput.size() > 0) {
                        // 由于这种格式中无法直接判断正负面提示词
                        // 我们假设第一个遇到的是正面提示词，第二个是负面提示词
                        if (positivePrompt.isEmpty()) {
                            positivePrompt = text;
                        } else {
                            negativePrompt = text;
                        }
                    }
                }
            }
            
            return new String[]{positivePrompt, negativePrompt};
            
        } catch (Exception e) {
            log.error("Error parsing ComfyUI prompt: " + e.getMessage(), e);
            return new String[]{"", ""};
        }
    }
} 