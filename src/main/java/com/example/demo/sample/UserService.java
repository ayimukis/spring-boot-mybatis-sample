package com.example.demo.sample;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {
    private UserMapper userMapper;
    private UserDao userDao;

    public UserService(UserMapper userMapper, UserDao userDao){
        this.userMapper = userMapper;
        this.userDao = userDao;
    }

    public List<Map<String, Object>> getUsers() {
        return userMapper.selectUsers();
    }

    public List<Map<String, Object>> getUsersWithDao() {
        return userDao.getUsers();
    }

    public void checkRegData() {
        List<Map<String, Object>> list = userDao.getRegList();
        HashSet<String> regSet = new HashSet<>();
        List<Map<String, Object>> checkList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();

        for (Map<String, Object> data : list) {
            List<String> breakTimeArr = new ArrayList<>();
            List<Map<String, Object>> rcsList = data.get("rcsJson") == null ? new ArrayList<>() : JsonUtil.convertToList(String.valueOf(data.get("rcsJson")));

            String crtrDt = String.valueOf(data.get("crtrDt"));
            String startDt = String.valueOf(data.get("gowrkDt"));
            String endDt = String.valueOf(data.get("lvwrkDt"));
            String gowrkTm = String.valueOf(data.get("gowrkTm"));
            String lvwrkTm = String.valueOf(data.get("lvwrkTm"));
            String startTm = gowrkTm;
            String endTm = lvwrkTm;
            for (Map<String, Object> rcs : rcsList) {
                if (rcs.get("startTm") != null && rcs.get("endTm") != null) {
                    String rcsStTm = String.valueOf(rcs.get("startTm"));
                    String rcsEdTm = String.valueOf(rcs.get("endTm"));
                    if (!crtrDt.equals(String.valueOf(rcs.get("startDt")))) {
                        rcsStTm = Integer.parseInt(String.valueOf(rcs.get("startTm"))) + 2400 + "";
                    }
                    if (!crtrDt.equals(String.valueOf(rcs.get("endDt")))) {
                        rcsEdTm = Integer.parseInt(String.valueOf(rcs.get("endTm"))) + 2400 + "";
                    }
                    breakTimeArr.add(rcsStTm);
                    breakTimeArr.add(rcsEdTm);
                }
            }

            if (!crtrDt.equals(startDt)) {
                startTm = Integer.parseInt(gowrkTm) + 2400 + "";
            }
            if (!crtrDt.equals(endDt)) {
                endTm = Integer.parseInt(lvwrkTm) + 2400 + "";
            }

            int workMin = getTimediffMinute(startTm, endTm, breakTimeArr, false);

            if (Integer.parseInt(String.valueOf(data.get("workMin"))) != workMin) {
                System.out.println("WORK : " + String.valueOf(data.get("workGrupRegSeq")) + " / "  + String.valueOf(data.get("workGrupRegDtlSeq")) + " / " +String.valueOf(data.get("crtrDt")) + " / " + String.valueOf(data.get("empNm")) + " / " + String.valueOf(data.get("workMin")) + " != " + workMin);
                regSet.add(String.valueOf(data.get("workGrupRegSeq")));
                checkList.add(data);
            }

            if (data.get("fotGowrkTm") != null && !"".equals(String.valueOf(data.get("fotGowrkTm")))) {
                String otStartDt = String.valueOf(data.get("fotGowrkDt"));
                String otEndDt = String.valueOf(data.get("fotLvwrkDt"));
                String otGowrkTm = String.valueOf(data.get("fotGowrkTm"));
                String otLvwrkTm = String.valueOf(data.get("fotLvwrkTm"));
                String otStartTm = otGowrkTm;
                String otEndTm = otLvwrkTm;

                if (!crtrDt.equals(otStartDt)) {
                    otStartTm = Integer.parseInt(otGowrkTm) + 2400 + "";
                }
                if (!crtrDt.equals(otEndDt)) {
                    otEndTm = Integer.parseInt(otLvwrkTm) + 2400 + "";
                }

                int otMin = getTimediffMinute(otStartTm, otEndTm, breakTimeArr, false);
                int beforeOtMin = Integer.parseInt(String.valueOf(data.get("otMin"))) + Integer.parseInt(String.valueOf(data.get("otHldyMin")));

                if (beforeOtMin != otMin) {
                    System.out.println("OT : " + String.valueOf(data.get("workGrupRegSeq")) + " / "  + String.valueOf(data.get("workGrupRegDtlSeq")) + " / " +String.valueOf(data.get("crtrDt")) + " / " + String.valueOf(data.get("empNm")) + " / " + beforeOtMin + " != " + otMin);
                }
            }


        }
        System.out.println("regSet : " + regSet);
        System.out.println("checkList : " + checkList);

        for (String regSeq : regSet) {
            List<Map<String, Object>> checkList2 = new ArrayList<>();
            for (Map<String, Object> data : checkList) {
                if (regSeq.equals(String.valueOf(data.get("workGrupRegSeq")))) {
                    checkList2.add(data);
                }
            }
            resultMap.put(regSeq, checkList2);
        }

        System.out.println(resultMap);

    }

    public static List<String> timeArr(String startTime, String endTime) {
        int startHour = Integer.parseInt(startTime.substring(0, 2));
        int startMinute = Integer.parseInt(startTime.substring(2, 4));
        int endHour = Integer.parseInt(endTime.substring(0, 2));
        int endMinute = Integer.parseInt(endTime.substring(2, 4));

        List<String> result = new ArrayList<>();

        for (int hour = startHour; hour <= endHour; hour++) {
            for (int minute = 0; minute < 60; minute++) {
                if (hour == startHour && minute <= startMinute) continue;
                if (hour == endHour && minute > endMinute) break;
                result.add(String.format("%02d", hour) + String.format("%02d", minute));
            }
        }

        return result;
    }

    public int getTimediffMinute(String startTime, String endTime, List<String> breakTimeArr, boolean colonYn) {
        int startHour = Integer.parseInt(startTime.substring(0, 2));
        int startMinute = colonYn ? Integer.parseInt(startTime.substring(3, 5)) : Integer.parseInt(startTime.substring(2, 4));
        int endHour = Integer.parseInt(endTime.substring(0, 2));
        int endMinute = colonYn ? Integer.parseInt(endTime.substring(3, 5)) : Integer.parseInt(endTime.substring(2, 4));

        List<String> totalArr = timeArr(startTime, endTime);
        List<String> breakTotalArr = new ArrayList<>();

        for (int i = 0; i < breakTimeArr.size(); i += 2) {
            String breakStartTime = breakTimeArr.get(i);
            String breakEndTime = breakTimeArr.get(i + 1);

            int breakStartHour = Integer.parseInt(breakStartTime.substring(0, 2));
            int breakStartMinute = colonYn ? Integer.parseInt(breakStartTime.substring(3, 5)) : Integer.parseInt(breakStartTime.substring(2, 4));
            int breakEndHour = Integer.parseInt(breakEndTime.substring(0, 2));
            int breakEndMinute = colonYn ? Integer.parseInt(breakEndTime.substring(3, 5)) : Integer.parseInt(breakEndTime.substring(2, 4));

            List<String> tempBreakArr = timeArr(breakStartTime, breakEndTime);
            breakTotalArr.addAll(tempBreakArr);
        }

        totalArr.removeAll(breakTotalArr);

        return totalArr.size();
    }

    public void checkWorkData() {
        List<Map<String, Object>> list = userDao.getWorkList();
        HashSet<String> regSet = new HashSet<>();
        List<Map<String, Object>> checkList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();

        for (Map<String, Object> data : list) {
            List<String> breakTimeArr = new ArrayList<>();
            List<Map<String, Object>> rcsList = data.get("rcsJson") == null ? new ArrayList<>() : JsonUtil.convertToList(String.valueOf(data.get("rcsJson")));

            String crtrDt = String.valueOf(data.get("crtrDt"));
            String startDt = String.valueOf(data.get("gowrkDt"));
            String endDt = String.valueOf(data.get("lvwrkDt"));
            String gowrkTm = String.valueOf(data.get("gowrkTm"));
            String lvwrkTm = String.valueOf(data.get("lvwrkTm"));
            String startTm = gowrkTm;
            String endTm = lvwrkTm;
            for (Map<String, Object> rcs : rcsList) {
                if (rcs.get("startTm") != null && rcs.get("endTm") != null) {
                    String rcsStTm = String.valueOf(rcs.get("startTm"));
                    String rcsEdTm = String.valueOf(rcs.get("endTm"));
                    if (!crtrDt.equals(String.valueOf(rcs.get("startDt")))) {
                        rcsStTm = Integer.parseInt(String.valueOf(rcs.get("startTm"))) + 2400 + "";
                    }
                    if (!crtrDt.equals(String.valueOf(rcs.get("endDt")))) {
                        rcsEdTm = Integer.parseInt(String.valueOf(rcs.get("endTm"))) + 2400 + "";
                    }
                    breakTimeArr.add(rcsStTm);
                    breakTimeArr.add(rcsEdTm);
                }
            }

            if (!crtrDt.equals(startDt)) {
                startTm = Integer.parseInt(gowrkTm) + 2400 + "";
            }
            if (!crtrDt.equals(endDt)) {
                endTm = Integer.parseInt(lvwrkTm) + 2400 + "";
            }

            int workMin = getTimediffMinute(startTm, endTm, breakTimeArr, false);

            if (Integer.parseInt(String.valueOf(data.get("workMin"))) != workMin) {
                System.out.println("WORK : " + String.valueOf(data.get("workPlanSeq")) + " / " +String.valueOf(data.get("crtrDt")) + " / " + String.valueOf(data.get("empNm")) + " / " + String.valueOf(data.get("workMin")) + " != " + workMin);
                regSet.add(String.valueOf(data.get("workPlanSeq")));
                checkList.add(data);
            }

            String start = "";
            String end = "";
            int planStartTm = Integer.parseInt(startTm);
            int planEndTm = Integer.parseInt(endTm);

            int amStartTm = 0;
            int amEndTm = 0;
            int pmStartTm = 2200;
            int pmEndTm = 600;
            int amMin = 0;
            int pmMin = 0;

            if (2200 > 2400) {
                amStartTm = 600 - 2400;
            } else {
                amStartTm = 0;
            }

            if (600 > 2400) {
                amEndTm = 2200 - 2400;
            } else {
                amEndTm = 0;
            }

            start = String.format("%04d", Math.max(amStartTm, planStartTm >= 2400 ? planStartTm - 2400 : planStartTm));
            end = String.format("%04d", Math.min(amEndTm, planEndTm >= 2400 ? planEndTm - 2400 : planEndTm));
            amMin = getTimediffMinute(start, end, breakTimeArr, false);

            start = String.format("%04d", Math.max(pmStartTm, planStartTm));
            end = String.format("%04d", Math.min(pmEndTm, planEndTm));
            pmMin = getTimediffMinute(start, end, breakTimeArr, false);

            int nghtMin = amMin + pmMin;

            // if (Integer.parseInt(String.valueOf(data.get("allwOtNghtMin"))) != nghtMin) {
            //     System.out.println("NIGHT : " + String.valueOf(data.get("workPlanSeq")) + " / " +String.valueOf(data.get("crtrDt")) + " / " + String.valueOf(data.get("empNm")) + " / " + String.valueOf(data.get("workMin")) + " != " + workMin);
            //     regSet.add(String.valueOf(data.get("workPlanSeq")));
            //     checkList.add(data);
            // }

            if (data.get("fotGowrkTm") != null && !"".equals(String.valueOf(data.get("fotGowrkTm")))) {
                String otStartDt = String.valueOf(data.get("fotGowrkDt"));
                String otEndDt = String.valueOf(data.get("fotLvwrkDt"));
                String otGowrkTm = String.valueOf(data.get("fotGowrkTm"));
                String otLvwrkTm = String.valueOf(data.get("fotLvwrkTm"));
                String otStartTm = otGowrkTm;
                String otEndTm = otLvwrkTm;

                if (!crtrDt.equals(otStartDt)) {
                    otStartTm = Integer.parseInt(otGowrkTm) + 2400 + "";
                }
                if (!crtrDt.equals(otEndDt)) {
                    otEndTm = Integer.parseInt(otLvwrkTm) + 2400 + "";
                }

                String otStr = data.get("otMin") == null ? "0" : String.valueOf(data.get("otMin"));
                String otHldyStr = data.get("otHldyMin") == null ? "0" : String.valueOf(data.get("otHldyMin"));

                int otMin = getTimediffMinute(otStartTm, otEndTm, breakTimeArr, false);
                int beforeOtMin = Integer.parseInt(otStr) + Integer.parseInt(otHldyStr);

                if (beforeOtMin != otMin) {
                    System.out.println("OT : " + String.valueOf(data.get("workPlanSeq")) + " / " +String.valueOf(data.get("crtrDt")) + " / " + String.valueOf(data.get("empNm")) + " / " + beforeOtMin + " != " + otMin);
                }
            }


        }
        System.out.println("regSet : " + regSet);
        System.out.println("checkList : " + checkList);

        // for (String regSeq : regSet) {
        //     List<Map<String, Object>> checkList2 = new ArrayList<>();
        //     for (Map<String, Object> data : checkList) {
        //         if (regSeq.equals(String.valueOf(data.get("workPlanSeq")))) {
        //             checkList2.add(data);
        //         }
        //     }
        //     resultMap.put(regSeq, checkList2);
        // }
        //
        // System.out.println(resultMap);
    }

    public void HRD0001_INFO(Map<String, Object> param) {

        param.put("crtrYmd", param.get("date"));
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("flxb", false);
        param.put("empSeq", param.get("searchEmpSeq"));
        Map<String, Object> dateInfo = userDao.WORK_DATE_INFO(param);
        param.remove("empSeq");
        Map<String, Object> otSetting = userDao.HRD0001_OT_DATA(param);
        int wklyPsbltyMin = Integer.parseInt(String.valueOf(otSetting.get("wklyPsbltyMin"))) * 60;
        List<Map<String, Object>> holiList = new ArrayList<>();
        int workLimitMin = 52 * 60;

        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> workList = new ArrayList<>();

        if (dateInfo.get("flxbwkAplySeq") != null && "002".equals(dateInfo.get("typeMst"))) {
            wklyPsbltyMin = Integer.parseInt(String.valueOf(otSetting.get("wklyPsbltyMin"))) * 60 * 2;
            holiList = getHoliDayList("flxb", (String) param.get("searchEmpSeq"), (String) param.get("crtrYmd"), String.valueOf(dateInfo.get("flxbwkAplySeq")));
            workLimitMin = 104 * 60;
            resultData.put("flxb", true);
            param.replace("type", "flxb");
            param.put("flxbwkAplySeq", dateInfo.get("flxbwkAplySeq"));
            list = getWpOtList(param);
            workList = getWorkList("flxb", (String) param.get("searchEmpSeq"), "", String.valueOf(dateInfo.get("flxbwkAplySeq")));
        } else {
            holiList = getHoliDayList("week", (String) param.get("searchEmpSeq"), (String) param.get("crtrYmd"), "");
            list = getWpOtList(param);
            workList = getWorkList("week", (String) param.get("searchEmpSeq"), (String) param.get("crtrYmd"), "");
        }

        List<Map<String, Object>> finalHoliList = holiList;

        int workSum = workList.stream()
            .map(x -> {
                int workMin = 0;
                if (x.get("workMin") == null || "".equals(x.get("workMin")) || !"".equals(x.get("otAltDyoffYmd"))) {
                    // 평일시간외근무 인정 시간
                    workMin = 0;
                } else {
                    // 평일시간외근무 신청 시간
                    workMin = Integer.parseInt(String.valueOf(x.get("workMin")));
                }
                return workMin;
            }).reduce(0, Integer::sum);
        int minSum = list.stream()
            .map(x -> {
                List<Map<String, Object>> holi = finalHoliList.stream()
                    .filter(h -> h.get("date").equals(x.get("crtrYmd")))
                    .collect(Collectors.toList());
                int min = 0;
                int hMin = 0;
                if ("Y".equals(x.get("allwCalYn"))) {
                    // 평일시간외근무 인정 시간
                    min = Integer.parseInt(String.valueOf(x.get("allwOvtmwrkMin")));
                    hMin = Integer.parseInt(String.valueOf(x.get("allwHldyWrkMin")));
                } else {
                    // 평일시간외근무 신청 시간
                    min = Integer.parseInt(String.valueOf(x.get("aplyOvtmwrkMin")));
                    hMin = Integer.parseInt(String.valueOf(x.get("aplyHldyWrkMin")));
                }
                if (holi.size() > 0) {
                    // 평일 공휴이거나 대체휴무일 경우
                    min = 0;
                    hMin = 0;
                }
                return min + hMin;
            }).reduce(0, Integer::sum);
        int allMinSum = list.stream()
            .map(x -> {
                int min = 0;
                int hMin = 0;
                if (x.get("allwOvtmwrkMin") == null) {
                    min = 0;
                } else {
                    min = Integer.parseInt(String.valueOf(x.get("allwOvtmwrkMin")));
                }
                if (x.get("allwHldyWrkMin") == null) {
                    hMin = 0;
                } else {
                    hMin = Integer.parseInt(String.valueOf(x.get("allwHldyWrkMin")));
                }
                return min + hMin;
            }).reduce(0, Integer::sum);
        int weekAplySumMin = list.stream()
            .filter(x -> "N".equals(x.get("allwCalYn")))
            .map(x -> {
                int min = 0;
                int hMin = 0;
                if (x.get("aplyOvtmwrkMin") == null) {
                    min = 0;
                } else {
                    min = Integer.parseInt(String.valueOf(x.get("aplyOvtmwrkMin")));
                }
                if (x.get("aplyHldyWrkMin") == null) {
                    hMin = 0;
                } else {
                    hMin = Integer.parseInt(String.valueOf(x.get("aplyHldyWrkMin")));
                }
                return min + hMin;
            }).reduce(0, Integer::sum);

        param.replace("type", "month");
        param.replace("date", param.get("month") + "01");
        List<Map<String, Object>> monthlist = getWpOtList(param);
        int monthPayAplySumMin = monthlist.stream()
            .filter(x -> ("001".equals(x.get("aprvSts")) || "002".equals(x.get("aprvSts"))) && "001".equals(x.get("cmpnDiv")))
            .map(x -> {
                int min = 0;
                if (x.get("aplyWrkMin") == null) {
                    min = 0;
                } else {
                    min = Integer.parseInt(String.valueOf(x.get("aplyWrkMin")));
                }
                return min;
            }).reduce(0, Integer::sum);
        int monthHdayAplySumMin = monthlist.stream()
            .filter(x -> ("001".equals(x.get("aprvSts")) || "002".equals(x.get("aprvSts"))) && "002".equals(x.get("cmpnDiv")))
            .map(x -> {
                int min = 0;
                if (x.get("aplyWrkMin") == null) {
                    min = 0;
                } else {
                    min = Integer.parseInt(String.valueOf(x.get("aplyWrkMin")));
                }
                return min;
            }).reduce(0, Integer::sum);
        int monthPayAllwSumMin = monthlist.stream()
            .filter(x -> "002".equals(x.get("aprvSts")) && "001".equals(x.get("cmpnDiv")))
            .map(x -> {
                int min = 0;
                int hMin = 0;
                if (x.get("allwOvtmwrkMin") == null) {
                    min = 0;
                } else {
                    min = Integer.parseInt(String.valueOf(x.get("allwOvtmwrkMin")));
                }
                if (x.get("allwHldyWrkMin") == null) {
                    hMin = 0;
                } else {
                    hMin = Integer.parseInt(String.valueOf(x.get("allwHldyWrkMin")));
                }
                return min + hMin;
            }).reduce(0, Integer::sum);
        int monthHdayAllwSumMin = monthlist.stream()
            .filter(x -> "002".equals(x.get("aprvSts")) && "002".equals(x.get("cmpnDiv")))
            .map(x -> {
                int min = 0;
                int hMin = 0;
                if (x.get("allwOvtmwrkMin") == null) {
                    min = 0;
                } else {
                    min = Integer.parseInt(String.valueOf(x.get("allwOvtmwrkMin")));
                }
                if (x.get("allwHldyWrkMin") == null) {
                    hMin = 0;
                } else {
                    hMin = Integer.parseInt(String.valueOf(x.get("allwHldyWrkMin")));
                }
                return min + hMin;
            }).reduce(0, Integer::sum);

        int holiMin = holiList.stream()
            .map(x -> {
                return Integer.parseInt(String.valueOf(x.get("whrsum")));
            }).reduce(0, Integer::sum);

        int workHoliMin = holiList.stream()
            .map(x -> {
                int min = 0;
                int hMin = 0;
                min = Math.max(Integer.parseInt(String.valueOf(x.get("aplyOvtmwrkMin"))), Integer.parseInt(String.valueOf(x.get("allwOvtmwrkMin"))));
                hMin = Math.max(Integer.parseInt(String.valueOf(x.get("aplyHldyWrkMin"))), Integer.parseInt(String.valueOf(x.get("allwHldyWrkMin"))));
                return min + hMin;
            }).reduce(0, Integer::sum);

        resultData.put("weekPsbMin", Math.min(((wklyPsbltyMin - minSum)) , (workLimitMin - (workSum + minSum))));
        // resultData.put("weekPsbMin", Math.min(((wklyPsbltyMin + holiMin) - (workHoliMin + minSum)) , ((workLimitMin + holiMin) - (workSum + minSum + workHoliMin))));
        // resultData.put("weekPsbMin", Math.min((wklyPsbltyMin - minSum) , (workLimitMin - (workSum + minSum))));
        resultData.put("weekAllMin", allMinSum);
        resultData.put("weekAplySumMin", weekAplySumMin);
        resultData.put("monthPayAplySumMin", monthPayAplySumMin);
        resultData.put("monthHdayAplySumMin", monthHdayAplySumMin);
        resultData.put("monthPayAllwSumMin", monthPayAllwSumMin);
        resultData.put("monthHdayAllwSumMin", monthHdayAllwSumMin);
        resultData.put("holiMin", holiMin);
        resultData.put("workHoliMin", workHoliMin);
        resultData.put("weekHoliList", holiList);
    }

    public List<Map<String, Object>> getHoliDayList(String type, String empSeq, String date, String flxbwkAplySeq) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("empSeq", empSeq);
        data.put("date", date);
        data.put("flxbwkAplySeq", flxbwkAplySeq);
        return userDao.getHoliDayList(data);
    }

    public List<Map<String, Object>> getWpOtList(Map<String, Object> param) {
        List<Map<String, Object>> list = userDao.getWpOtList(param);
        return list;
    }

    public List<Map<String, Object>> getWorkList(String type, String empSeq, String date, String flxbwkAplySeq) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("empSeq", empSeq);
        data.put("date", date);
        data.put("flxbwkAplySeq", flxbwkAplySeq);
        return userDao.getWorkPlanList(data);
    }

    public void checkAnnvData() {
        List<Map<String, Object>> list = userDao.getAnnvList();
        for (Map<String, Object> data : list) {
            List<String> breakTimeArr = new ArrayList<>();
            List<Map<String, Object>> rcsList = data.get("rcsJson") == null ? new ArrayList<>() :
                JsonUtil.convertToList(String.valueOf(data.get("rcsJson")));
            for (Map<String, Object> rcs : rcsList) {
                if (rcs.get("RCSBGN_TM") != null && rcs.get("RCSEND_TM") != null) {
                    String rcsStTm = String.valueOf(rcs.get("RCSBGN_TM"));
                    String rcsEdTm = String.valueOf(rcs.get("RCSEND_TM"));
                    breakTimeArr.add(rcsStTm);
                    breakTimeArr.add(rcsEdTm);
                }
            }
            int useMin = Integer.parseInt(String.valueOf(data.get("useMin")));
            int calcMin = getTimediffMinute(String.valueOf(data.get("annvStartTm")), String.valueOf(data.get("annvEndTm")), breakTimeArr, false);

            if (useMin != calcMin) {
                // System.out.println("APLY_DTL_SEQ : " + String.valueOf(data.get("aplyDtlSeq")));
                // System.out.println("ANNV : " + String.valueOf(data.get("aplyDtlSeq")) + " / " + String.valueOf(data.get("empSeq")) + " / " + String.valueOf(data.get("empName")) + " / " + useMin + " != " + calcMin);
                System.out.println(String.valueOf(data.get("aplyDtlSeq")) + "         / " + String.valueOf(data.get("rcsJson")));
            }


        }
    }

}
