package com.example.demo.sample;

import static org.springframework.jdbc.support.JdbcUtils.*;

import org.apache.commons.collections.map.ListOrderedMap;

/**
 * 모든 key 는 camelCase로 변환돼 저장되는 Map.
 *
 * EgovMap 은 put할때만 camelCase 변환되므로 Map#get 은 반드시 camelCase 변환된 key문자열을 넘겨야 한다.
 * voMap 은 SNAKE_CASE 로 조회해도 값을 얻을 수 있다.
 *  ex)  voMap.get("empSeq").equals(voMap.get("EMP_SEQ")) => true
 *
 * TODO
 *
 * INFO　퍼포먼스 이슈는 일단 보류.
 *
 * @see egovframework.rte.psl.dataaccess.util.EgovMap
 *
 */
public class VoMap<K, V> extends ListOrderedMap {

//	public static void main(String[] args) {
//      final voMap voMap = new voMap();
//      voMap.put("EMP_SEQ", "hello");
//      voMap.get("empSeq");
//	}

    public VoMap() {
        super.put("errMsg", "");
        super.put("isOk", "Y");
    }

	private static final long serialVersionUID = 47L;

	/**
     * 문자열에 _ 가 있으면 camel case 변환 수행.
     * 전부 대문자일 경우도 camel case 변환 수행.
     * camel_case -> camelCase
     * CAMEL_CASE -> camelCase
     * <p>
     * camel -> 안 함
     * camelCase -> 안 함
     */
    public static Object convertKey(Object key) {
        if (key == null) return key;
        String s = key.toString();
        boolean containsLowerCase = false;
        // _ 가 하나라도 있으면 camelCase처리
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '_') {
                return convertUnderscoreNameToPropertyName(s);
            }
            if ('a' <= c && c <= 'z') {
                containsLowerCase = true;
            }
        }
        // 소문자가 하나도 없었다면 camelCase처리
        if (!containsLowerCase) {
            return convertUnderscoreNameToPropertyName(s);
        }
        return key;
    }

/*
    // 2020-03-03 성능 이슈로 put만 camelCase로 변환.


    @Override
    public Object get(Object key) {
        final Object newKey = convertKey(key);
        return super.get(newKey);
    }

    @Override
    public Object nextKey(Object key) {
        return super.nextKey(convertKey(key));
    }

    @Override
    public Object previousKey(Object key) {
        return super.previousKey(convertKey(key));
    }
    */


    @Override
    public Object remove(Object key) {
        return super.remove(convertKey(key));
    }

    @Override
    public Object put(Object key, Object value) {
        final Object newKey = convertKey(key);
        return super.put(newKey, value);
    }

    @Override
    public Object put(int index, Object key, Object value) {
        return super.put(index, convertKey(key), value);
    }

    public void putErrObj(Exception e) {
        this.put("isOk", "N");
        this.put("errMsg", e.getMessage());
    }

}
