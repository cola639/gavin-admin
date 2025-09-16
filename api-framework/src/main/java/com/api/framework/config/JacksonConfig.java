package com.api.framework.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            // 默认 Date 类型的序列化/反序列化
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
            builder.timeZone(TimeZone.getTimeZone("GMT+8"));

            JavaTimeModule module = new JavaTimeModule();

            // LocalDateTime 反序列化器：支持多种格式
            module.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                private final List<DateTimeFormatter> formatters = Arrays.asList(DateTimeFormatter.ISO_OFFSET_DATE_TIME,        // 2025-09-06T15:25:49.000+08:00
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), // 2025-09-06 15:25:49
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")      // 2025-09-06
                );

                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String value = p.getText().trim();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            // ISO_OFFSET_DATE_TIME 需要特殊处理时区
                            if (formatter == DateTimeFormatter.ISO_OFFSET_DATE_TIME) {
                                return OffsetDateTime.parse(value, formatter).toLocalDateTime();
                            }
                            return LocalDateTime.parse(value, formatter);
                        } catch (Exception ignored) {
                        }
                    }
                    throw new IOException("Unparseable LocalDateTime: " + value);
                }
            });

            // LocalDate 反序列化器
            module.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
                private final List<DateTimeFormatter> formatters = Arrays.asList(DateTimeFormatter.ISO_DATE,                        // 2025-09-06
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")          // 2025-09-06
                );

                @Override
                public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String value = p.getText().trim();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            return LocalDate.parse(value, formatter);
                        } catch (Exception ignored) {
                        }
                    }
                    throw new IOException("Unparseable LocalDate: " + value);
                }
            });

            // LocalDateTime 序列化器（统一输出 yyyy-MM-dd HH:mm:ss）
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            builder.modules(module);
        };
    }
}
