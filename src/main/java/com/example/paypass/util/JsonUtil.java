package com.example.paypass.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;


public class JsonUtil {
    private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";

    public static ObjectMapper getMapperWithDateFormat() {
        return new ObjectMapper().setDateFormat(new SimpleDateFormat(dateFormat));
    }

    public static String serializeToJsonString(Object object) throws JsonProcessingException {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .writeValueAsString(object);
    }
}
