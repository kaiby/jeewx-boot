package com.jeecg.p3.system.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static JsonFactory jsonFactory = new JsonFactory();
    private static ObjectMapper objectMapper = new ObjectMapper();

    public JsonUtils() {
    }

    public static <T> T fromJson(String jsonAsString, Class<T> pojoClass)
            throws JsonMappingException, JsonParseException, IOException {
        return objectMapper.readValue(jsonAsString, pojoClass);
    }

    public static <T> T fromJson(FileReader fr, Class<T> pojoClass) throws JsonParseException, IOException {
        return objectMapper.readValue(fr, pojoClass);
    }

    public static String toJson(Object pojo) throws JsonMappingException, JsonGenerationException, IOException {
        return toJson(pojo, false);
    }

    public static String toJson(Object pojo, boolean prettyPrint)
            throws JsonMappingException, JsonGenerationException, IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator jg = jsonFactory.createGenerator(sw);
        if (prettyPrint) {
            jg.useDefaultPrettyPrinter();
        }

        objectMapper.writeValue(jg, pojo);
        return sw.toString();
    }

    public static void toJson(Object pojo, FileWriter fw, boolean prettyPrint)
            throws JsonMappingException, JsonGenerationException, IOException {
        JsonGenerator jg = jsonFactory.createGenerator(fw);
        if (prettyPrint) {
            jg.useDefaultPrettyPrinter();
        }

        objectMapper.writeValue(jg, pojo);
    }

    public static Map<String, Object> parseMap(String jsonStr) throws IOException {
        Map map = objectMapper.readValue(jsonStr, Map.class);
        return map;
    }

    public static Map<String, String> parseStringMap(String jsonStr) throws IOException {
        Map map = objectMapper.readValue(jsonStr, Map.class);
        return map;
    }

    public static JsonNode parse(String jsonStr) throws IOException {
        JsonNode node = null;
        node = objectMapper.readTree(jsonStr);
        return node;
    }

    public static <T> List<T> fromJsonToList(String jsonAsString, Class<T> pojoClass)
            throws JsonMappingException, JsonParseException, IOException {
        return JSONArray.parseArray(jsonAsString, pojoClass);
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 去掉value为空的键值对
     *
     * @param jsonStr
     * @return
     */
    public static String trimNullValue(String jsonStr) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);

        // 去掉value为空的键值对
        Iterator<Map.Entry<String, Object>> it = jsonObject.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> temp = it.next();
            if (temp.getValue().toString().length() < 1) {
                it.remove();
            }
        }
        return jsonObject.toJSONString();
    }

    /**
     * 判断json字符串是否是空json
     *
     * @param jsonStr
     * @return
     */
    public static boolean isNullForm(String jsonStr) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        return jsonObject.isEmpty();
    }

    static {
        // objectMapper.configure(Feature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
    }
}
