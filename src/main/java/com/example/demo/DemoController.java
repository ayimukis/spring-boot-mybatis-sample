package com.example.demo;

import com.example.demo.sample.OTService;
import com.example.demo.sample.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DemoController {
    @Autowired
    private UserService userService;
    @Autowired
    private OTService otService;
    public DemoController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/usersDao")
    public List<Map<String, Object>> getUsersDao() {
        return userService.getUsersWithDao();
    }

    @GetMapping("/checkRegData")
    public void checkRegData() {
        userService.checkRegData();
    }

    @GetMapping("/checkWorkData")
    public void checkWorkData() {
        userService.checkWorkData();
    }

    @GetMapping("/HRD0001_INFO")
    public void HRD0001_INFO(@RequestBody Map<String, Object> param) {
        userService.HRD0001_INFO(param);
    }

    @GetMapping("/setAllw")
    public void setAllw(@RequestBody Map<String, Object> param) throws ParseException {
        otService.setAllw(param);
    }

    @GetMapping("/setOtAllwCncl")
    public void setOtAllwCncl(@RequestBody Map<String, Object> param) throws Exception {
        otService.setOtAllwCncl(param);
    }

    @GetMapping("/checkAnnvData")
    public void checkAnnvData() {
        userService.checkAnnvData();
    }

    @GetMapping("/checkNight2100")
    public void checkNight2100() throws ParseException {
        otService.checkNight2100();
    }
}
