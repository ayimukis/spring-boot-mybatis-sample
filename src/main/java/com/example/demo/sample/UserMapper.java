package com.example.demo.sample;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UserMapper {
    List<Map<String, Object>> selectUsers();
    List<Map<String, Object>> getRegList();
    List<Map<String, Object>> getWorkList();
    Map<String, Object> WORK_DATE_INFO(Map<String, Object> param);
    Map<String, Object> HRD0001_OT_DATA(Map<String, Object> param);
    List<Map<String, Object>> getHoliDayList(Map<String, Object> data);
    List<Map<String, Object>> getWpOtList(Map<String, Object> param);
    List<Map<String, Object>> getWorkPlanList(Map<String, Object> data);
    List<Map<String, Object>> getOtList(Map<String, Object> data);
    List<Map<String, Object>> getAnnvList(Map<String, Object> data);
    List<Map<String, Object>> getNight2100List(Map<String, Object> data);
    Map<String, Object> CWG0006_OT_DATA(Map<String, Object> data);
    void updateAllw(Map<String, Object> param);
    void updateAllwWorkPlan(Map<String, Object> param);
    void updateCmpnMst(Map<String, Object> param);
    void insertCmpnOcrn(Map<String, Object> param);
    void insertCmpnMst(Map<String, Object> param);
    void delCmpnDtl(Map<String, Object> param);
    void delCmpnMst(Map<String, Object> param);
    void allwCncl(Map<String, Object> param);
    void allwCnclWorkPlan(Map<String, Object> param);
    void insertAtt21(Map<String, Object> param);
    void delAtt21();
    Map<String, Object> getCmpnMst(Map<String, Object> data);
    Map<String, Object> getCmpnDtl(Map<String, Object> data);

}
