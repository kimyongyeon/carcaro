package carcaro.test;

import java.sql.SQLException;

public class DriverSample {

	
	private void test_DriverSample() throws SQLException {

//		final int MAX = 5000;
//
//		String[] fName = { "김", "이", "박", "최", "정", "윤", "길", "서", "하", "허",
//				"한", "홍", "어", "우", "전", "진", "오", "문", "강", "고", "권", "남",
//				"채", "맹", "송", "성" };
//		String[] lName = { "아", "석", "은", "솔", "효", "진", "수", "영", "은", "지",
//				"민", "봉", "재", "희" };
//		String[] num = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
//		String[] eng = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
//				"l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w",
//				"x", "y", "z" };
//
//		String[] pwQuest = { "hint_01", "hint_02", "hint_03", "hint_04",
//				"hint_05", "hint_06", "hint_07" };
//
//		String[] car = { "1년 미만", "1년", "2년", "3년", "4년" };
//
//		String[] comp = { "", "1577대리", "오빠달려", "바로대리", "이수근대리", "김대리",
//				"8282대리", "대리대리", "대신대리", "든든대리", "징징대리", "효진대리", "귀찮은대리" };
//
//		String[] tit = { "", "하이", "반가워요", "열심히 하겠습니다.", "운전잘해요", "빨리 일시켜 주세요",
//				"친절하게 모시겠습니다.", "친근한대리", "스타대리입니다.", "근면성실" };
//
//		String driverId, passwd, pwQuestion, pwAnswer, residentNo1, residentNo2, phone, stockNo, career, company, title, name;
//		int e1 = 0, e2 = 0, en = 0, q = 0, e = 0;
//		int rn1 = 0, rn2 = 0, rn3 = 0, rn4 = 0;
//		int f = 0, l1 = 0, l2 = 0;
//
//		for (int i = 0; i < MAX; i++) {
//			// make driverId, passwd, stockNo
//			if (en == 9) {
//				e2++;
//				en = 0;
//			}
//			if (e2 == 25) {
//				e1++;
//				e2 = 0;
//			}
//			driverId = "test" + eng[e1] + eng[e2] + num[en];
//			passwd = "psw" + eng[e1] + eng[e2] + num[en];
//			stockNo = "driver" + eng[e1] + eng[e2] + num[en++];
//
//			// make pwQuestion, pwAnswer
//			pwQuestion = pwQuest[i % 7];
//			pwAnswer = eng[i % 26];
//
//			// make residentNo1, residentNo2, phone
//			if (rn4 == 9) {
//				rn3++;
//				rn4 = 0;
//			}
//			if (rn3 == 9) {
//				rn2++;
//				rn3 = 0;
//			}
//			if (rn2 == 9) {
//				rn1++;
//				rn2 = 0;
//			}
//			residentNo1 = num[rn1] + num[rn2] + num[rn3] + num[rn4] + "00";
//			residentNo2 = num[rn1] + num[rn2] + num[rn3] + num[rn4] + "000";
//			phone = "010" + num[rn1] + num[rn2] + num[rn3] + num[rn4]
//					+ num[rn1] + num[rn2] + num[rn3] + num[rn4++];
//			career = car[i % 5];
//			company = comp[i % 13];
//			title = tit[i % 10];
//
//			// make name
//			if (l2 == 14) {
//				l1++;
//				l2 = 0;
//			}
//			if (l1 == 14) {
//				f++;
//				l1 = 0;
//			}
//			name = fName[f] + lName[l1] + lName[l2++];
//
//			RegistDriver d = new RegistDriver();
//			d.setDriverId(driverId);
//			d.setPasswd(passwd);
//			d.setPwQuestion(pwQuestion);
//			d.setPwAnswer(pwAnswer);
//			d.setName(name);
//			d.setResidentNo1(residentNo1);
//			d.setResidentNo2(residentNo2);
//			d.setPhone(phone);
//			d.setStockNo(stockNo);
//			d.setCareer(career);
//			d.setCompany(company);
//			d.setIntroduce(title);
//
//			System.out.println("" + driverId + "," + passwd + "," + pwQuestion + ","
//					+ pwAnswer + "," + name + "," + residentNo1 + ","
//					+ residentNo2 + "," + phone + "," + stockNo + "," + career
//					+ "," + company + "," + title);
//
//			// RegistDriverDAO.registDriver(d);
		}

	}

