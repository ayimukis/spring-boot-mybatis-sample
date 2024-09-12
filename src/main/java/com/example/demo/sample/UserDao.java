package com.example.demo.sample;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDao {
    private SqlSession sqlSession;
    public UserDao(SqlSession sqlSession){
        this.sqlSession = sqlSession;
    }

    public List<Map<String, Object>> getUsers() {
        return sqlSession.selectList("com.example.demo.sample.UserMapper.selectUsers");
    }

    public List<Map<String, Object>> getRegList() {
        return sqlSession.selectList("com.example.demo.sample.UserMapper.getRegList");
    }

    public List<Map<String, Object>> getWorkList() {
        return sqlSession.selectList("com.example.demo.sample.UserMapper.getWorkList");
    }

    public Map<String, Object> WORK_DATE_INFO(Map<String, Object> param) {
        return sqlSession.selectOne("com.example.demo.sample.UserMapper.WORK_DATE_INFO", param);
    }

    public Map<String, Object> HRD0001_OT_DATA(Map<String, Object> param) {
        return sqlSession.selectOne("com.example.demo.sample.UserMapper.HRD0001_OT_DATA", param);
    }

    public List<Map<String, Object>> getHoliDayList(Map<String, Object> data) {
        return sqlSession.selectList("com.example.demo.sample.UserMapper.getHoliDayList", data);
    }

    public List<Map<String, Object>> getWpOtList(Map<String, Object> param) {
        return sqlSession.selectList("com.example.demo.sample.UserMapper.getWpOtList", param);
    }

    public List<Map<String, Object>> getWorkPlanList(Map<String, Object> data) {
        return sqlSession.selectList("com.example.demo.sample.UserMapper.getWorkPlanList", data);
    }

    public Map<String, Object> CWG0006_OT_DATA(Map<String, Object> param) {
        return sqlSession.selectOne("com.example.demo.sample.UserMapper.CWG0006_OT_DATA", param);
    }

    public void updateAllw(Map<String, Object> param) {
        sqlSession.update("com.example.demo.sample.UserMapper.updateAllw", param);
    }

    public void updateAllwWorkPlan(Map<String, Object> param) {
        sqlSession.update("com.example.demo.sample.UserMapper.updateAllwWorkPlan", param);
    }

    public Map<String, Object> getCmpnMst(Map<String, Object> param) {
        return sqlSession.selectOne("com.example.demo.sample.UserMapper.getCmpnMst", param);
    }

    public void updateCmpnMst(Map<String, Object> param) {
        sqlSession.update("com.example.demo.sample.UserMapper.updateCmpnMst", param);
    }

    public void insertCmpnOcrn(Map<String, Object> param) {
        sqlSession.insert("com.example.demo.sample.UserMapper.insertCmpnOcrn", param);
    }

    public void insertCmpnMst(Map<String, Object> param) {
        sqlSession.insert("com.example.demo.sample.UserMapper.insertCmpnMst", param);
    }

    public List<Map<String, Object>> getOtList(Map<String, Object> param) {
        return sqlSession.selectList("com.example.demo.sample.UserMapper.getOtList", param);
    }

    public Map<String, Object> getCmpnDtl(Map<String, Object> param) {
        return sqlSession.selectOne("com.example.demo.sample.UserMapper.getCmpnDtl", param);
    }

    public void delCmpnDtl(Map<String, Object> param) {
        sqlSession.update("com.example.demo.sample.UserMapper.delCmpnDtl", param);
    }

    public void delCmpnMst(Map<String, Object> param) {
        sqlSession.update("com.example.demo.sample.UserMapper.delCmpnMst", param);
    }

    public void allwCncl(Map<String, Object> param) {
        sqlSession.update("com.example.demo.sample.UserMapper.allwCncl", param);
    }

    public void allwCnclWorkPlan(Map<String, Object> param) {
        sqlSession.update("com.example.demo.sample.UserMapper.allwCnclWorkPlan", param);
    }

    public List<Map<String, Object>> getAnnvList() {
        return sqlSession.selectList("com.example.demo.sample.UserMapper.getAnnvList");
    }

    public List<Map<String, Object>> getNight2100List() {
        return sqlSession.selectList("com.example.demo.sample.UserMapper.getNight2100List");
    }

    public void delAtt21() {
        sqlSession.delete("com.example.demo.sample.UserMapper.delAtt21");
    }

    public void insertAtt21(Map<String, Object> param) {
        sqlSession.insert("com.example.demo.sample.UserMapper.insertAtt21", param);
    }
}
