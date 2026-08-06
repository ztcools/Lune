package com.lune.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;
    private String content;
    private String toolCallId;
    private String toolName;
    private Map<String, Object> toolResult;
    private LocalDateTime timestamp;
}
