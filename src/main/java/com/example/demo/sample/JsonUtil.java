package com.example.demo.sample;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonUtil {

    /* JSON 문자열을 List 객체로 변환 */
    public static List<Map<String, Object>> convertToList(String data) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        List<Map<String, Object>> list = gson.fromJson(data, listType);
        return list;
    }

    /* JSON 문자열을 List 객체로 변환 */
    public static List<String> convertToStringList(String data) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> list = gson.fromJson(data, listType);
        return list;
    }

    /* JSON 문자열을 Map 객체로 변환 */
    public static Map<String, Object> convertToMap(String data) {
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map = gson.fromJson(data, mapType);
        return map;
    }
    
    /* Map 객체를 JSON 문자열로 변환 */
    public static String convertToJson (Map<String, Object> map) {
        return new Gson().toJson(map);
    }
    
    /* List 객체를 JSON 문자열로 변환 */
    public static String convertToJson (List<?> list) {
        return new Gson().toJson(list);
    }

    public static List<Integer> convertToInt(String data) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Integer>>() {}.getType();
        List<Integer> list = gson.fromJson(data, listType);
        return list;
    }
    
}
