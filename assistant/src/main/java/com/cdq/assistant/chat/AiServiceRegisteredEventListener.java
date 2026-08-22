package com.cdq.assistant.chat;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.langchain4j.service.spring.event.AiServiceRegisteredEvent;
import java.util.List;
import java.util.stream.Collectors;
import dev.langchain4j.agent.tool.ToolSpecification;

@Component
class AiServiceRegisteredEventListener implements ApplicationListener<AiServiceRegisteredEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceRegisteredEventListener.class);
    
    @Override
    public void onApplicationEvent(AiServiceRegisteredEvent event) {
        Class<?> aiServiceClass = event.aiServiceClass();
        List<ToolSpecification> toolSpecifications = event.toolSpecifications();

        logger.info("AiService registered: {}, tools: {}", aiServiceClass.getSimpleName(), toolSpecifications.stream().map(ToolSpecification::name).collect(Collectors.joining(", ")));
    }
}