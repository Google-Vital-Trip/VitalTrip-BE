package com.vitaltrip.firstaid.constant;

import com.vitaltrip.location.dto.EmergencyContactDto;

import java.util.HashMap;
import java.util.Map;

public class EmergencyContacts {

    private static final Map<String, EmergencyContactDto> CONTACTS = new HashMap<>();

    static {
        // 아시아
        CONTACTS.put("KR", new EmergencyContactDto("119", "112", "119", "112"));
        CONTACTS.put("JP", new EmergencyContactDto("119", "110", "119", "110"));
        CONTACTS.put("CN", new EmergencyContactDto("119", "110", "120", "120"));
        CONTACTS.put("TW", new EmergencyContactDto("119", "110", "119", "119"));
        CONTACTS.put("HK", new EmergencyContactDto("999", "999", "999", "999"));
        CONTACTS.put("MO", new EmergencyContactDto("28572222", "999", "999", "999"));
        CONTACTS.put("MN", new EmergencyContactDto("101", "102", "103", "103"));
        CONTACTS.put("TH", new EmergencyContactDto("199", "191", "1669", "191"));
        CONTACTS.put("VN", new EmergencyContactDto("114", "113", "115", "113"));
        CONTACTS.put("SG", new EmergencyContactDto("995", "999", "995", "999"));
        CONTACTS.put("MY", new EmergencyContactDto("994", "999", "999", "999"));
        CONTACTS.put("ID", new EmergencyContactDto("113", "110", "118", "112"));
        CONTACTS.put("PH", new EmergencyContactDto("911", "911", "911", "911"));
        CONTACTS.put("MM", new EmergencyContactDto("191", "199", "192", "192"));
        CONTACTS.put("KH", new EmergencyContactDto("118", "117", "119", "117"));
        CONTACTS.put("LA", new EmergencyContactDto("190", "191", "195", "191"));
        CONTACTS.put("BN", new EmergencyContactDto("995", "993", "991", "991"));
        CONTACTS.put("TL", new EmergencyContactDto("115", "112", "110", "112"));
        CONTACTS.put("IN", new EmergencyContactDto("101", "100", "108", "112"));
        CONTACTS.put("PK", new EmergencyContactDto("16", "15", "115", "115"));
        CONTACTS.put("BD", new EmergencyContactDto("199", "999", "199", "999"));
        CONTACTS.put("LK", new EmergencyContactDto("110", "119", "110", "119"));
        CONTACTS.put("NP", new EmergencyContactDto("101", "100", "102", "100"));
        CONTACTS.put("AF", new EmergencyContactDto("119", "119", "119", "119"));
        CONTACTS.put("IR", new EmergencyContactDto("125", "110", "115", "115"));
        CONTACTS.put("IQ", new EmergencyContactDto("115", "104", "122", "115"));
        CONTACTS.put("SA", new EmergencyContactDto("911", "911", "911", "911"));
        CONTACTS.put("AE", new EmergencyContactDto("997", "999", "998", "999"));
        CONTACTS.put("QA", new EmergencyContactDto("999", "999", "999", "999"));
        CONTACTS.put("KW", new EmergencyContactDto("112", "112", "112", "112"));
        CONTACTS.put("BH", new EmergencyContactDto("999", "999", "999", "999"));
        CONTACTS.put("OM", new EmergencyContactDto("9999", "9999", "9999", "9999"));
        CONTACTS.put("YE", new EmergencyContactDto("191", "194", "191", "191"));
        CONTACTS.put("JO", new EmergencyContactDto("911", "911", "911", "911"));
        CONTACTS.put("LB", new EmergencyContactDto("175", "112", "140", "112"));
        CONTACTS.put("SY", new EmergencyContactDto("113", "112", "110", "112"));
        CONTACTS.put("IL", new EmergencyContactDto("102", "100", "101", "100"));
        CONTACTS.put("TR", new EmergencyContactDto("110", "155", "112", "112"));
        CONTACTS.put("KZ", new EmergencyContactDto("101", "102", "103", "112"));
        CONTACTS.put("UZ", new EmergencyContactDto("101", "102", "103", "112"));

        // 유럽
        CONTACTS.put("GB", new EmergencyContactDto("999", "999", "999", "999"));
        CONTACTS.put("DE", new EmergencyContactDto("112", "110", "112", "112"));
        CONTACTS.put("FR", new EmergencyContactDto("18", "17", "15", "112"));
        CONTACTS.put("IT", new EmergencyContactDto("115", "113", "118", "112"));
        CONTACTS.put("ES", new EmergencyContactDto("080", "091", "061", "112"));
        CONTACTS.put("PT", new EmergencyContactDto("117", "112", "112", "112"));
        CONTACTS.put("NL", new EmergencyContactDto("112", "112", "112", "112"));
        CONTACTS.put("BE", new EmergencyContactDto("100", "101", "100", "112"));
        CONTACTS.put("CH", new EmergencyContactDto("118", "117", "144", "112"));
        CONTACTS.put("AT", new EmergencyContactDto("122", "133", "144", "112"));
        CONTACTS.put("SE", new EmergencyContactDto("112", "112", "112", "112"));
        CONTACTS.put("NO", new EmergencyContactDto("110", "112", "113", "112"));
        CONTACTS.put("DK", new EmergencyContactDto("112", "112", "112", "112"));
        CONTACTS.put("FI", new EmergencyContactDto("112", "112", "112", "112"));
        CONTACTS.put("PL", new EmergencyContactDto("998", "997", "999", "112"));
        CONTACTS.put("CZ", new EmergencyContactDto("150", "158", "155", "112"));
        CONTACTS.put("SK", new EmergencyContactDto("150", "158", "155", "112"));
        CONTACTS.put("HU", new EmergencyContactDto("105", "107", "104", "112"));
        CONTACTS.put("RO", new EmergencyContactDto("112", "112", "112", "112"));
        CONTACTS.put("BG", new EmergencyContactDto("160", "166", "150", "112"));
        CONTACTS.put("HR", new EmergencyContactDto("193", "192", "194", "112"));
        CONTACTS.put("RS", new EmergencyContactDto("193", "192", "194", "112"));
        CONTACTS.put("GR", new EmergencyContactDto("199", "100", "166", "112"));
        CONTACTS.put("UA", new EmergencyContactDto("101", "102", "103", "112"));
        CONTACTS.put("RU", new EmergencyContactDto("101", "102", "103", "112"));

        // 아메리카
        CONTACTS.put("US", new EmergencyContactDto("911", "911", "911", "911"));
        CONTACTS.put("CA", new EmergencyContactDto("911", "911", "911", "911"));
        CONTACTS.put("MX", new EmergencyContactDto("911", "911", "911", "911"));
        CONTACTS.put("BR", new EmergencyContactDto("193", "190", "192", "190"));
        CONTACTS.put("AR", new EmergencyContactDto("100", "101", "107", "911"));
        CONTACTS.put("CL", new EmergencyContactDto("132", "133", "131", "131"));
        CONTACTS.put("CO", new EmergencyContactDto("119", "112", "125", "112"));
        CONTACTS.put("PE", new EmergencyContactDto("116", "105", "117", "105"));
        CONTACTS.put("VE", new EmergencyContactDto("171", "171", "171", "171"));

        // 오세아니아
        CONTACTS.put("AU", new EmergencyContactDto("000", "000", "000", "000"));
        CONTACTS.put("NZ", new EmergencyContactDto("111", "111", "111", "111"));

        // 아프리카
        CONTACTS.put("ZA", new EmergencyContactDto("10111", "10111", "10177", "10111"));
        CONTACTS.put("NG", new EmergencyContactDto("199", "199", "199", "199"));
        CONTACTS.put("EG", new EmergencyContactDto("180", "122", "123", "123"));
        CONTACTS.put("KE", new EmergencyContactDto("999", "999", "999", "999"));
        CONTACTS.put("GH", new EmergencyContactDto("192", "191", "193", "191"));

        // 기본값
        CONTACTS.put("DEFAULT", new EmergencyContactDto("112", "112", "112", "112"));
    }

    public static EmergencyContactDto get(String countryCode) {
        return CONTACTS.getOrDefault(countryCode, CONTACTS.get("DEFAULT"));
    }

    private EmergencyContacts() {}
}
