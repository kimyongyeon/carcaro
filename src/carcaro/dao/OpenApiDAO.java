package carcaro.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class OpenApiDAO {
	/**
	 * 위도,경도 주소변환
	 * @param lat
	 * @param lng
	 * @return
	 * @throws IOException
	 */
	public static String geoConvert(String lat, String lng) throws IOException {
		// 1. 질의 URL
		URL url;
		URLConnection conn;
		InputStream is;
		InputStreamReader isr;
		BufferedReader br;
		String buf = null;
		String re ="";

		String openAPI;
		//http://maps.googleapis.com/maps/api/geocode/json?address=37.4817688,127.0576308&sensor=false
		// 개포동 1동 구하기. 샘플
		//lat = "37.4817688";
		//lng = "127.0576308";
		openAPI = "http://maps.googleapis.com/maps/api/geocode/json?address="
				+ lat + "," + lng + "&sensor=false";
		System.out.println("openAPI==="+openAPI);
		try {
			url = new URL(openAPI);
			conn = url.openConnection();
			is = conn.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			while (true) {
				buf = br.readLine();
				if (buf == null)
					break;
				re = re + buf;
				System.out.println(buf);
			}
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		
		return re;
	}
}
