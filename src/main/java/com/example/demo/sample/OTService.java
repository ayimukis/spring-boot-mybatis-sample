package com.example.demo.sample;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Please explain the class!!
 *
 * @fileName      : OTService
 * @author        : hyuk
 * @since         : 24. 9. 5.
 */
@Service
public class OTService {
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserDao userDao;

	private final static Logger log = LoggerFactory.getLogger(OTService.class);

	public void setAllw(Map<String, Object> param) throws ParseException {
		List<Map<String, Object>> list = userDao.getOtList(param);
		setAllw(list);
	}

	public void setAllw(List<Map<String, Object>> list) throws ParseException {
		Map<String, Object> param = new HashMap<>();
		Map<String, Object> otSetting = userDao.CWG0006_OT_DATA(param);
		// 계획시작종료 그대로 이정하는 직군들
		List<String> strList = new ArrayList<>(Arrays.asList("10", "11", "12", "13", "14", "15", "8", "16"));
		// 위 직군 중 보안을 제외
		List<String> strList2 = new ArrayList<>(Arrays.asList("10", "11", "12", "13", "14", "15"));

		for (Map<String, Object> data : list) {

			String crtrDtStr = String.valueOf(data.get("crtrYmd"));
			if (crtrDtStr != null && !crtrDtStr.isEmpty()
			) {
				int crtrDt = Integer.parseInt(crtrDtStr);
				if (crtrDt < 20240812 && !strList.contains(String.valueOf(data.get("commGroupSeq")))) {
					continue;
				}
			}

			if ("003".equals(data.get("cmpnDiv"))) {
				continue;
			}

			String come = String.valueOf(data.get("comeTm"));
			String leave = String.valueOf(data.get("leaveTm"));


			if (come != null && !come.isEmpty() && !"null".equals(come)) {
				SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMddHHmm");
				Date date = srcFormat.parse(come);
				SimpleDateFormat destFormat = new SimpleDateFormat("yyyyMMdd");
				String formattedDate = destFormat.format(date);
				if (!formattedDate.equals(String.valueOf(data.get("crtrYmd")))) {
					String comeTm = Integer.parseInt(come.substring(come.length()-4, come.length())) + 2400 + "";
					data.put("comeStTm", comeTm);
				} else {
					data.put("comeStTm", come.substring(come.length()-4, come.length()));
				}
			}

			if (leave != null && !leave.isEmpty() && !"null".equals(leave)) {
				SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMddHHmm");
				Date date = srcFormat.parse(leave);
				SimpleDateFormat destFormat = new SimpleDateFormat("yyyyMMdd");
				String formattedDate = destFormat.format(date);
				if (!formattedDate.equals(String.valueOf(data.get("crtrYmd")))) {
					String leaveTm = Integer.parseInt(leave.substring(leave.length()-4, leave.length())) + 2400 + "";
					data.put("leaveStTm", leaveTm);
				} else {
					data.put("leaveStTm", leave.substring(leave.length()-4, leave.length()));
				}

			}

			int comeTm = Integer.parseInt(String.valueOf(data.get("comeStTm")));
			int leaveTm = Integer.parseInt(String.valueOf(data.get("leaveStTm")));
			// 선택적근로 야간시간 계산 데이터는 신청 시작종료 시간 없으므로 계획 시작종료 시간 세팅
			int planStartTm = "".equals(data.get("planStartTm")) ? Integer.parseInt(String.valueOf(data.get("bgngTm"))) : Integer.parseInt(String.valueOf(data.get("planStartTm")));
			int planEndTm = "".equals(data.get("planEndTm")) ? Integer.parseInt(String.valueOf(data.get("endTm"))) :  Integer.parseInt(String.valueOf(data.get("planEndTm")));
			int startTm = "".equals(data.get("bgngTm")) ? planStartTm : Integer.parseInt(String.valueOf(data.get("bgngTm")));
			int endTm = "".equals(data.get("endTm")) ? planEndTm : Integer.parseInt(String.valueOf(data.get("endTm")));
			int nghtwrkStartTime = String.valueOf(otSetting.get("nghtwrkStartTime")) != null ? Integer.parseInt(String.valueOf(otSetting.get("nghtwrkStartTime"))) : 0;
			int nghtwrkEndTime = String.valueOf(otSetting.get("nghtwrkEndTime")) != null ? Integer.parseInt(String.valueOf(otSetting.get("nghtwrkEndTime"))) : 0;

			if (!String.valueOf(data.get("bgngYmd")).equals(String.valueOf(data.get("crtrYmd")))) {
				startTm = startTm + 2400;
			}

			if (!String.valueOf(data.get("endYmd")).equals(String.valueOf(data.get("crtrYmd"))) && endTm < 2400) {
				endTm = endTm + 2400;
			}

			if (!String.valueOf(data.get("planStartDt")).equals(String.valueOf(data.get("crtrYmd")))) {
				planStartTm = planStartTm + 2400;
			}

			if (!String.valueOf(data.get("planEndDt")).equals(String.valueOf(data.get("crtrYmd"))) && planEndTm < 2400) {
				planEndTm = planEndTm + 2400;
			}

			String start = "";
			String end = "";

			if (comeTm > startTm) {
				start = String.format("%04d", comeTm);
			} else {
				start = String.format("%04d", startTm);
			}

			if (leaveTm < endTm) {
				end = String.format("%04d", leaveTm);
			} else {
				end = String.format("%04d", endTm);
			}

			List<String> breakTimeArr = new ArrayList<>();
			String dataString = JsonUtil.convertToJson(data);
			log.info("data : " + dataString);

			if ("003".equals(data.get("aplyDiv"))) {
				int st = Integer.parseInt(String.valueOf(data.get("bgngTm")));
				int ed = Integer.parseInt(String.valueOf(data.get("endTm")));
				if (!String.valueOf(data.get("bgngYmd")).equals(String.valueOf(data.get("crtrYmd")))) {
					st = st + 2400;
				}
				if (!String.valueOf(data.get("endYmd")).equals(String.valueOf(data.get("crtrYmd")))) {
					ed = ed + 2400;
				}
				List<Map<String, Object>> rcsList = getBreakTimes(String.format("%04d", st), String.format("%04d", ed));
				for (Map<String, Object> rcs : rcsList) {
					if (rcs.get("startTm") != null && rcs.get("endTm") != null) {
						breakTimeArr.add(String.valueOf(rcs.get("startTm")));
						breakTimeArr.add(String.valueOf(rcs.get("endTm")));
					}
				}
			} else {
				List<Map<String, Object>> rcsList = data.get("rcsJson") == null ? new ArrayList<>() : JsonUtil.convertToList(String.valueOf(data.get("rcsJson")));
				for (Map<String, Object> rcs : rcsList) {
					if (rcs.get("RCSBGN_TM") != null && rcs.get("RCSEND_TM") != null) {
						if (rcs.get("RCSBGN_DT") != null && rcs.get("RCSEND_DT") != null) {
							String rcsStartTm = String.valueOf(rcs.get("RCSBGN_TM"));
							String rcsEndTm = String.valueOf(rcs.get("RCSEND_TM"));
							if (Integer.parseInt(String.valueOf(data.get("crtrYmd"))) < Integer.parseInt(String.valueOf(rcs.get("RCSBGN_DT")))) {
								rcsStartTm = Integer.parseInt(String.valueOf(rcs.get("RCSBGN_TM"))) + 2400 + "";
							} else {
								rcsStartTm = String.valueOf(rcs.get("RCSBGN_TM"));
							}
							if (Integer.parseInt(String.valueOf(data.get("crtrYmd"))) < Integer.parseInt(String.valueOf(rcs.get("RCSEND_DT")))) {
								rcsEndTm = Integer.parseInt(String.valueOf(rcs.get("RCSEND_TM"))) + 2400 + "";
							} else {
								rcsEndTm = String.valueOf(rcs.get("RCSEND_TM"));
							}

							breakTimeArr.add(rcsStartTm);
							breakTimeArr.add(rcsEndTm);
						} else {
							breakTimeArr.add(String.valueOf(rcs.get("RCSBGN_TM")));
							breakTimeArr.add(String.valueOf(rcs.get("RCSEND_TM")));
						}
					}
				}
			}

			if ("7".equals(data.get("dayOfWeek")) && "OT".equals(data.get("type")) && breakTimeArr.size() == 0) {
				breakTimeArr = new ArrayList<>();
				int st = Integer.parseInt(String.valueOf(data.get("bgngTm")));
				int ed = Integer.parseInt(String.valueOf(data.get("endTm")));
				if (!String.valueOf(data.get("bgngYmd")).equals(String.valueOf(data.get("crtrYmd")))) {
					st = st + 2400;
				}
				if (!String.valueOf(data.get("endYmd")).equals(String.valueOf(data.get("crtrYmd")))) {
					ed = ed + 2400;
				}
				List<Map<String, Object>> rcsList = getBreakTimes(String.format("%04d", st), String.format("%04d", ed));
				for (Map<String, Object> rcs : rcsList) {
					if (rcs.get("startTm") != null && rcs.get("endTm") != null) {
						breakTimeArr.add(String.valueOf(rcs.get("startTm")));
						breakTimeArr.add(String.valueOf(rcs.get("endTm")));
					}
				}
			}

			if (Integer.parseInt(String.valueOf(data.get("workMin"))) == 0) {
				breakTimeArr = new ArrayList<>();
				int st = Integer.parseInt(start);
				int ed = Integer.parseInt(end);
				if (!String.valueOf(data.get("bgngYmd")).equals(String.valueOf(data.get("crtrYmd")))) {
					st = st + 2400;
				}
				if (!String.valueOf(data.get("endYmd")).equals(String.valueOf(data.get("crtrYmd")))) {
					ed = ed + 2400;
				}
				List<Map<String, Object>> rcsList = getBreakTimes(String.format("%04d", st), String.format("%04d", ed));
				for (Map<String, Object> rcs : rcsList) {
					if (rcs.get("startTm") != null && rcs.get("endTm") != null) {
						breakTimeArr.add(String.valueOf(rcs.get("startTm")));
						breakTimeArr.add(String.valueOf(rcs.get("endTm")));
					}
				}
			}

			int allwMin = getTimediffMinute(start, end, breakTimeArr, false);
			int nghtMin = 0;
			int ovtmMin = 0;

			// if ("FOT".equals(String.valueOf(data.get("type")))) {

			int amStartTm = 0;
			int amEndTm = 0;
			int pmStartTm = nghtwrkStartTime;
			int pmEndTm = nghtwrkEndTime;
			int amMin = 0;
			int pmMin = 0;
			String amStart = "";
			String amEnd = "";
			String pmStart = "";
			String pmEnd = "";

			if (nghtwrkStartTime > 2400) {
				amStartTm = nghtwrkStartTime - 2400;
			} else {
				amStartTm = 0;
			}

			if (nghtwrkEndTime > 2400) {
				amEndTm = nghtwrkEndTime - 2400;
			} else {
				amEndTm = 0;
			}

			log.info("start : " + start);
			log.info("end : " + end);
			log.info("comeTm : " + comeTm);
			log.info("endTm : " + endTm);
			log.info("amStartTm : " + amStartTm);
			log.info("amEndTm : " + amEndTm);
			log.info("pmStartTm : " + pmStartTm);
			log.info("pmEndTm : " + pmEndTm);
			log.info("breakTimeArr : " + breakTimeArr);
			log.info("planStartTm : " + planStartTm);
			log.info("planEndTm : " + planEndTm);
			log.info("nghtwrkStartTime : " + nghtwrkStartTime);
			log.info("nghtwrkEndTime : " + nghtwrkEndTime);

			amStart = String.format("%04d", Math.max(Math.max(comeTm, amStartTm), planStartTm >= 2400 ? planStartTm - 2400 : planStartTm));
			amEnd = String.format("%04d", Math.min(Math.min(endTm, amEndTm), planEndTm >= 2400 ? planEndTm - 2400 : planEndTm));
			log.info("amStart : " + amStart);
			log.info("amEnd : " + amEnd);
			amMin = getTimediffMinute(amStart, amEnd, breakTimeArr, false);

			pmStart = String.format("%04d", Math.max(Math.max(comeTm, pmStartTm), planStartTm));
			pmEnd = String.format("%04d", Math.min(Math.min(endTm, pmEndTm), planEndTm));
			log.info("pmStart : " + pmStart);
			log.info("pmEnd : " + pmEnd);
			pmMin = getTimediffMinute(pmStart, pmEnd, breakTimeArr, false);

			// if (comeTm < amEndTm && leaveTm > amStartTm) {
			//     start = String.format("%04d", Math.max(comeTm, amStartTm), planStartTm);
			//     end = String.format("%04d", Math.min(Math.min(leaveTm, amEndTm), planEndTm));
			//     amMin = getTimediffMinute(start, end, breakTimeArr, false);
			// }
			//
			// if (pmStartTm != 0 && pmEndTm != 0) {
			//     if (comeTm < pmEndTm && leaveTm > pmStartTm) {
			//         if (comeTm < pmStartTm) {
			//             start = String.format("%04d", pmStartTm);
			//         }
			//         if (leaveTm > nghtwrkEndTime) {
			//             end = String.format("%04d", Math.min(endTm, pmEndTm));
			//         }
			//         pmMin = getTimediffMinute(start, end, breakTimeArr, false);
			//     }
			// }

			if ("OT".equals(String.valueOf(data.get("type")))) {
				amStart = String.format("%04d", Math.max(Integer.parseInt(start), amStartTm));
				amEnd = String.format("%04d", Math.min(Integer.parseInt(end), amEndTm));
				log.info("amStart : " + amStart);
				log.info("amEnd : " + amEnd);
				amMin = getTimediffMinute(amStart, amEnd, breakTimeArr, false);

				pmStart = String.format("%04d", Math.max(Integer.parseInt(start), pmStartTm));
				pmEnd = String.format("%04d", Math.min(Integer.parseInt(end), pmEndTm));
				log.info("pmStart : " + pmStart);
				log.info("pmEnd : " + pmEnd);
				pmMin = getTimediffMinute(pmStart, pmEnd, breakTimeArr, false);
			}

			nghtMin = amMin + pmMin;
			// } else {
			//     if ("001".equals(data.get("aplyDiv"))) {
			//         nghtwrkStartTime = 0;
			//         nghtwrkEndTime = nghtwrkEndTime - 2400;
			//         if (comeTm < nghtwrkEndTime && leaveTm > nghtwrkStartTime) {
			//             start = String.format("%04d", Math.max(Math.max(comeTm, nghtwrkStartTime), startTm));
			//             end = String.format("%04d", Math.min(Math.min(leaveTm, nghtwrkEndTime), endTm));
			//             nghtMin = getTimediffMinute(start, end, breakTimeArr, false);
			//         }
			//
			//     } else {
			//         if (nghtwrkStartTime != 0 && nghtwrkEndTime != 0) {
			//             if (comeTm < nghtwrkEndTime && leaveTm > nghtwrkStartTime) {
			//                 if (comeTm < nghtwrkStartTime) {
			//                     start = String.format("%04d", nghtwrkStartTime);
			//                 }
			//                 if (leaveTm > nghtwrkEndTime) {
			//                     end = String.format("%04d", Math.min(endTm, nghtwrkEndTime));
			//                 }
			//                 nghtMin = getTimediffMinute(start, end, breakTimeArr, false);
			//             }
			//         }
			//     }
			//
			// }

			// 휴일근무
			if ("003".equals(data.get("aplyDiv")) || "004".equals(data.get("aplyDiv"))) {
				if (allwMin > Integer.parseInt(String.valueOf(data.get("whrsum")))) {
					ovtmMin = allwMin - Integer.parseInt(String.valueOf(data.get("whrsum")));
					data.put("allwHldyOvtmwrkMin", ovtmMin);
				}
				// if ("Y".equals(data.get("hdayYn")) && !("1".equals(data.get("dayOfWeek"))) || "7".equals(data.get("dayOfWeek"))) {
				//     data.put("allwDayhldyWrkMin", allwMin);
				//
				// } else {
				data.put("allwHldyWrkMin", allwMin);
				// }
				if (!"".equals(data.get("aplyAltDyoffYmd"))) {
					// 미화 대체휴무 보상인 일자에 소정근로 시간 초과하여 근무한 시간에 대하여 평일연장 비율로 인정시간 계산해줘야함
					data.remove("allwHldyWrkMin");
					data.put("allwOvtmwrkMin", allwMin);
				}
			} else {
				data.put("allwOvtmwrkMin", allwMin);
			}
			data.put("allwNghtMin", nghtMin);
			data.put("loginEmpSeq", "SYSTEM");

			if ("FOT".equals(String.valueOf(data.get("type"))) && "8".equals(String.valueOf(data.get("commGroupSeq"))) && String.valueOf(data.get("holiYn")).contains("Y")) {
				String[] splitValues = String.valueOf(data.get("holiYn")).split("_");
				if (splitValues[0].equals("S")) {
					// 시작일과 종료일이 같을 경우
					int holiMin = 0;
					int overMin = 0;
					start = String.format("%04d", Math.max(comeTm, planStartTm));
					end = String.format("%04d", Math.min(endTm, planEndTm));
					holiMin = getTimediffMinute(start, end, breakTimeArr, false);
					if (holiMin > Integer.parseInt(String.valueOf(data.get("whrsum")))) {
						overMin = holiMin - Integer.parseInt(String.valueOf(data.get("whrsum")));
						holiMin = Integer.parseInt(String.valueOf(data.get("whrsum")));
						data.put("allwHldyOvtmwrkMin", overMin);
					}
					data.put("allwHldyWrkMin", holiMin);
				} else {
					// 시작일과 종료일이 다를 경우
					String holiStr = splitValues[1];
					char[] holiArr = holiStr.toCharArray();
					int sHoliMin = 0;
					int eHoliMin = 0;

					if (holiArr[0] == 'Y') {
						start = String.format("%04d", Math.max(comeTm, planStartTm));
						end = String.format("%04d", Math.min(endTm, 2400));
						int holiMin = getTimediffMinute(start, end, breakTimeArr, false);
						if (holiMin > Integer.parseInt(String.valueOf(data.get("whrsum")))) {
							holiMin = Integer.parseInt(String.valueOf(data.get("whrsum")));
						}
						sHoliMin = holiMin;
					}

					if (holiArr[1] == 'Y') {
						start = String.format("%04d", 2400);
						end = String.format("%04d", Math.min(endTm, planEndTm));
						int holiMin = getTimediffMinute(start, end, breakTimeArr, false);
						if (holiMin > Integer.parseInt(String.valueOf(data.get("whrsum")))) {
							holiMin = Integer.parseInt(String.valueOf(data.get("whrsum")));
						}
						eHoliMin = holiMin;
					}
					data.put("allwHldyWrkMin", sHoliMin + eHoliMin);

				}
			}

			if ("FOT".equals(String.valueOf(data.get("type"))) && strList2.contains(String.valueOf(data.get("commGroupSeq"))) && String.valueOf(data.get("holiYn")).contains("Y")) {
				String[] splitValues = String.valueOf(data.get("holiYn")).split("_");
				if (splitValues[0].equals("S")) {
					// 시작일과 종료일이 같을 경우
					int holiMin = 0;
					start = String.format("%04d", Math.max(comeTm, planStartTm));
					end = String.format("%04d", Math.min(endTm, planEndTm));
					holiMin = getTimediffMinute(start, end, breakTimeArr, false);
					if (holiMin > Integer.parseInt(String.valueOf(data.get("whrsum")))) {
						holiMin = Integer.parseInt(String.valueOf(data.get("whrsum")));
					}
					data.put("allwHldyWrkMin", holiMin);
				} else {
					// 시작일과 종료일이 다를 경우
					String holiStr = splitValues[1];
					char[] holiArr = holiStr.toCharArray();
					int sHoliMin = 0;
					int eHoliMin = 0;

					if (holiArr[0] == 'Y') {
						start = String.format("%04d", Math.max(comeTm, planStartTm));
						end = String.format("%04d", Math.min(endTm, 2400));
						int holiMin = getTimediffMinute(start, end, breakTimeArr, false);
						if (holiMin > Integer.parseInt(String.valueOf(data.get("whrsum")))) {
							holiMin = Integer.parseInt(String.valueOf(data.get("whrsum")));
						}
						sHoliMin = holiMin;
					}

					if (holiArr[1] == 'Y') {
						start = String.format("%04d", 2400);
						end = String.format("%04d", Math.min(endTm, planEndTm));
						int holiMin = getTimediffMinute(start, end, breakTimeArr, false);
						if (holiMin > Integer.parseInt(String.valueOf(data.get("whrsum")))) {
							holiMin = Integer.parseInt(String.valueOf(data.get("whrsum")));
						}
						eHoliMin = holiMin;
					}
					data.put("allwHldyWrkMin", sHoliMin + eHoliMin);

				}
			}

			// 선택적 근로 야간시간 제외하고 키 삭제
			if ("".equals(data.get("bgngTm"))) {
				data.remove("allwOvtmwrkMin");
				data.remove("allwHldyWrkMin");
				data.remove("allwHldyOvtmwrkMin");
			}

			String resData = JsonUtil.convertToJson(data);
			log.info("resData : " + resData);

			if ("OT".equals(String.valueOf(data.get("type")))) {
				userDao.updateAllw(data);
			} else {
				userDao.updateAllwWorkPlan(data);
			}

			// 보상휴가 발생 로직
			if("002".equals(data.get("cmpnDiv"))) {
				double rate = Double.parseDouble(String.valueOf(otSetting.get("hldyallwTotrt"))) / 100;
				double ovtmRate = Double.parseDouble(String.valueOf(otSetting.get("hldyovrtallwTotrt"))) / 100;
				double nghtRate = Double.parseDouble(String.valueOf(otSetting.get("nghtallwTotrt"))) / 100;

				double hldyAllw = Math.floor(allwMin * rate);
				double hldyOvrtAllw = Math.floor(ovtmMin * ovtmRate);
				double nghtAllw = Math.floor(nghtMin * nghtRate);

				System.out.println(hldyAllw + " " + hldyOvrtAllw + " " + nghtAllw);
				Map<String, Object> cmpnMst = userDao.getCmpnMst(data);

				double cmpnMin = hldyAllw + hldyOvrtAllw + nghtAllw;

				// CMPN_OCRN_SEQ, CMPN_MIN_SEQ, EMP_SEQ, OCRN_DIV, OVTMWRK_APLY_DTL_SEQ, OVTMWRK_CFMTN_SEQ,
				//     OCRN_CMPN_MIN, OCRN_CMPN_YMD, USE_CMPN_MIN, DEL_YN, RGTR_SEQ, REG_YMD, MDF_SEQ, MDF_YMD

				Map<String, Object> cmpnMap = new HashMap<>();
				cmpnMap.put("empSeq", data.get("empSeq"));
				cmpnMap.put("loginEmpSeq", "SYSTEM");
				cmpnMap.put("ocrnDiv", "1");
				cmpnMap.put("ovtmwrkAplyDtlSeq", data.get("ovtmwrkAplyDtlSeq"));
				cmpnMap.put("ocrnCmpnYmd", data.get("crtrYmd"));

				if (cmpnMst != null) {
					double ocrnCmpnMin = Double.parseDouble(String.valueOf(cmpnMst.get("ocrnCmpnMin")));
					cmpnMap.put("ocrnCmpnMin", cmpnMin + ocrnCmpnMin);
					cmpnMap.put("cmpnMinSeq", cmpnMst.get("cmpnMinSeq"));
					userDao.updateCmpnMst(cmpnMap);
					cmpnMap.put("ocrnCmpnMin", cmpnMin);
					userDao.insertCmpnOcrn(cmpnMap);

				} else {
					cmpnMap.put("ocrnCmpnMin", cmpnMin);
					userDao.insertCmpnMst(cmpnMap);
					userDao.insertCmpnOcrn(cmpnMap);

				}
			}
		}
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

	public List<Map<String, Object>> getBreakTimes(String start, String end) {
		// Convert time to minutes
		int startMinutes = Integer.parseInt(start.substring(0, 2)) * 60 + Integer.parseInt(start.substring(2));
		int endMinutes = Integer.parseInt(end.substring(0, 2)) * 60 + Integer.parseInt(end.substring(2));

		List<Map<String, Object>> breakTimes = new ArrayList<>();
		int breakStart = startMinutes + 240; // First break time is 4 hours after start time

		while (breakStart < endMinutes) {
			int breakEnd = breakStart + 30; // Break for 30 minutes

			// Convert to time format
			String breakStartHour = String.format("%02d", breakStart / 60);
			String breakStartMinute = String.format("%02d", breakStart % 60);
			String breakEndHour = String.format("%02d", breakEnd / 60);
			String breakEndMinute = String.format("%02d", breakEnd % 60);

			Map<String, Object> breakTime = new HashMap<>();
			breakTime.put("startTm", breakStartHour + breakStartMinute);
			breakTime.put("endTm", breakEndHour + breakEndMinute);

			breakTimes.add(breakTime);

			breakStart = breakEnd + 240; // Next break time is 4 hours after this break time ends
		}

		return breakTimes;
	}

	public void setOtAllwCncl(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> list = userDao.getOtList(param);
		setAllwCncl(list);
	}

	public void setAllwCncl(List<Map<String, Object>> list) throws Exception {
		for (Map<String, Object> data : list) {

			String crtrDtStr = String.valueOf(data.get("crtrYmd"));
			if (crtrDtStr != null && !crtrDtStr.isEmpty()) {
				int crtrDt = Integer.parseInt(crtrDtStr);
				if (crtrDt < 20240812) {
					continue;
				}
			}

			Map<String, Object> cmpnMst = userDao.getCmpnMst(data);
			Map<String, Object> cmpnDtl = userDao.getCmpnDtl(data);
			if (cmpnDtl != null) {
				int useCmpnMin = 0;
				if (cmpnDtl.get("useCmpnMin") != null) {
					useCmpnMin = Integer.parseInt(String.valueOf(cmpnDtl.get("useCmpnMin")));
				}
				if (useCmpnMin > 0) {
					log.info(String.valueOf(data));
				}
				double mstMin = Double.parseDouble(String.valueOf(cmpnMst.get("ocrnCmpnMin")));
				double dtlMin = Double.parseDouble(String.valueOf(cmpnDtl.get("ocrnCmpnMin")));
				userDao.delCmpnDtl(data);
				if (mstMin == dtlMin) {
					userDao.delCmpnMst(data);
				} else {
					cmpnMst.put("ocrnCmpnMin", mstMin - dtlMin);
					userDao.updateCmpnMst(cmpnMst);
				}
			}
			if ("OT".equals(String.valueOf(data.get("type")))) {
				userDao.allwCncl(data);

			} else {
				userDao.allwCnclWorkPlan(data);
			}
		}
	}

	public void checkNight2100() throws ParseException {
		List<Map<String, Object>> list = userDao.getNight2100List();
		userDao.delAtt21();
		int cnt = 0;
		for (Map<String, Object> data : list) {
			log.info("data : " + data);

			String come = String.valueOf(data.get("comeTm"));
			String leave = String.valueOf(data.get("leaveTm"));
			String comeDt = "";
			String leaveDt = "";


			if (come != null && !come.isEmpty()) {
				SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMddHHmm");
				Date date = srcFormat.parse(come);
				SimpleDateFormat destFormat = new SimpleDateFormat("yyyyMMdd");
				String formattedDate = destFormat.format(date);
				comeDt = formattedDate;
				if (!formattedDate.equals(String.valueOf(data.get("date")))) {
					String comeTm = Integer.parseInt(come.substring(come.length()-4, come.length())) + 2400 + "";
					data.put("comeStTm", comeTm);
				} else {
					data.put("comeStTm", come.substring(come.length()-4, come.length()));
				}
			}

			if (leave != null && !leave.isEmpty()) {
				SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMddHHmm");
				Date date = srcFormat.parse(leave);
				SimpleDateFormat destFormat = new SimpleDateFormat("yyyyMMdd");
				String formattedDate = destFormat.format(date);
				leaveDt = formattedDate;
				if (!formattedDate.equals(String.valueOf(data.get("date")))) {
					String leaveTm = Integer.parseInt(leave.substring(leave.length()-4, leave.length())) + 2400 + "";
					data.put("leaveStTm", leaveTm);
				} else {
					data.put("leaveStTm", leave.substring(leave.length()-4, leave.length()));
				}

			}

			int comeTm = Integer.parseInt(String.valueOf(data.get("comeStTm")));
			int leaveTm = Integer.parseInt(String.valueOf(data.get("leaveStTm")));
			// int startTm = Integer.parseInt(String.valueOf(data.get("planStartTm")));
			// int endTm = Integer.parseInt(String.valueOf(data.get("planEndTm")));
			//
			// if (!String.valueOf(data.get("planStartDt")).equals(String.valueOf(data.get("date")))) {
			// 	startTm = startTm + 2400;
			// }
			//
			// if (!String.valueOf(data.get("planEndDt")).equals(String.valueOf(data.get("date"))) && endTm < 2400) {
			// 	endTm = endTm + 2400;
			// }

			String start = "";
			String end = "";

			// if (comeTm > startTm) {
				start = String.format("%04d", comeTm);
			// } else {
			// 	start = String.format("%04d", startTm);
			// }

			// if (leaveTm < endTm) {
				end = String.format("%04d", leaveTm);
			// } else {
			// 	end = String.format("%04d", endTm);
			// }
			List<String> breakTimeArr = new ArrayList<>();
			List<Map<String, Object>> rcsList = data.get("rcsJson") == null ? new ArrayList<>() : JsonUtil.convertToList(String.valueOf(data.get("rcsJson")));
			for (Map<String, Object> rcs : rcsList) {
				if (rcs.get("RCSBGN_TM") != null && rcs.get("RCSEND_TM") != null) {
					if (rcs.get("RCSBGN_DT") != null && rcs.get("RCSEND_DT") != null) {
						String rcsStartTm = String.valueOf(rcs.get("RCSBGN_TM"));
						String rcsEndTm = String.valueOf(rcs.get("RCSEND_TM"));
						if (Integer.parseInt(String.valueOf(data.get("date"))) < Integer.parseInt(String.valueOf(rcs.get("RCSBGN_DT")))) {
							rcsStartTm = Integer.parseInt(String.valueOf(rcs.get("RCSBGN_TM"))) + 2400 + "";
						} else {
							rcsStartTm = String.valueOf(rcs.get("RCSBGN_TM"));
						}
						if (Integer.parseInt(String.valueOf(data.get("date"))) < Integer.parseInt(String.valueOf(rcs.get("RCSEND_DT")))) {
							rcsEndTm = Integer.parseInt(String.valueOf(rcs.get("RCSEND_TM"))) + 2400 + "";
						} else {
							rcsEndTm = String.valueOf(rcs.get("RCSEND_TM"));
						}

						breakTimeArr.add(rcsStartTm);
						breakTimeArr.add(rcsEndTm);
					} else {
						breakTimeArr.add(String.valueOf(rcs.get("RCSBGN_TM")));
						breakTimeArr.add(String.valueOf(rcs.get("RCSEND_TM")));
					}
				}
			}

			if (rcsList.size() == 0) {
				List<Map<String, Object>> brkList = getBreakTimes(start, end);
				for (Map<String, Object> rcs : brkList) {
					if (rcs.get("startTm") != null && rcs.get("endTm") != null) {
						breakTimeArr.add(String.valueOf(rcs.get("startTm")));
						breakTimeArr.add(String.valueOf(rcs.get("endTm")));
					}
				}
			}

			int realWorkMin = getTimediffMinute(start, end, new ArrayList<>(), false);
			int workMin = getTimediffMinute(start, end, breakTimeArr, false);
			data.put("rcsList", JsonUtil.convertToJson(breakTimeArr));
			data.put("workMin", workMin);
			data.put("realWorkMin", realWorkMin);

			if (workMin > 480) {
				cnt++;
				log.info("사원 : " + String.valueOf(data.get("empName")));
				log.info("날짜 : " + String.valueOf(data.get("date")));
				log.info("startDt : " + comeDt);
				log.info("start : " + start);
				log.info("endDt : " + leaveDt);
				log.info("end : " + end);
				log.info("breakTimeArr : " + breakTimeArr);
				log.info("휴게시간 제외 근무시간 : " + workMin);
				log.info("실근무시간 : " + realWorkMin);
				log.info("cnt : " + cnt);
				userDao.insertAtt21(data);
			} else if (workMin > 360 && "2025".equals(String.valueOf(data.get("deptSeq")))) {
				cnt++;
				log.info("사원 : " + String.valueOf(data.get("empName")));
				log.info("날짜 : " + String.valueOf(data.get("date")));
				log.info("start : " + start);
				log.info("end : " + end);
				log.info("breakTimeArr : " + breakTimeArr);
				log.info("휴게시간 제외 근무시간 : " + workMin);
				log.info("실근무시간 : " + realWorkMin);
				log.info("cnt : " + cnt);
				userDao.insertAtt21(data);
			}



		}
	}
}
