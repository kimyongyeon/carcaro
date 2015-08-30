package carcaro.util;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Util {

    public static final String DF = "yyyy-MM-dd HH:mm:ss";
    public static final String DF2 = "HH:mm";
    public static final String DF3 = "MM-dd HH:mm";
    public static final String DF4 = "yyyy-MM-dd";
    
	static Logger logger = Logger.getRootLogger();

    /**
     * 입력 받은 시간까지 남은 시간을 문자열로 반환 
     * @param gap TimeMillis
     * @return
     */
    public static String getRemainTime(long gap) {
        
        gap /= 1000;

        int hh = (int) (gap / 3600);
        int mm = (int) (gap / 60) % 60;
        int ss = (int) gap % 60;

        return String.format("%d시간 %d분 %d초", hh, mm, ss);
    }
    
    public static String getCurrDate(){
    	
    	return new SimpleDateFormat(DF4).format(new Date());
    }

    /**
     * 연,월,일을 제외한 시간을 24시 형식으로 반환
     * @param time
     * @return
     */
    public static String getTime(String time) {

        final SimpleDateFormat df1 = new SimpleDateFormat(DF);
        final SimpleDateFormat df2 = new SimpleDateFormat(DF2);
        
        try {
            return df2.format(df1.parse(time));
        } catch (ParseException e) {
            logger.error("Util.getTime() " + e.getMessage());
        }
        return time;
    }
    
    public static String getSimeDate(String time) {

        final SimpleDateFormat df1 = new SimpleDateFormat(DF);
        final SimpleDateFormat df2 = new SimpleDateFormat(DF3);
        
        try {
            return df2.format(df1.parse(time));
        } catch (ParseException e) {
        	logger.error( "Util.getTime() " + e.getMessage());
        }
        return time;
    }
    
    /**
     * min, max 사이의 값을 랜덤값을 돌려준다.
     * 
     * @param min
     * @param max
     * @return
     */
    public static int randInt(int min, int max) 
    { 
        return min + (int)(Math.random() * ((max - min) + 1));
    } 
    
    
    public static double distance(double P1_latitude, double P1_longitude, double P2_latitude, double P2_longitude) {
        if ((P1_latitude == P2_latitude) && (P1_longitude == P2_longitude)) {
            return 0;
        }
        double e10 = P1_latitude * Math.PI / 180;
        double e11 = P1_longitude * Math.PI / 180;
        double e12 = P2_latitude * Math.PI / 180;
        double e13 = P2_longitude * Math.PI / 180;
        /* 타원체 GRS80 */
        double c16 = 6356752.314140910;
        double c15 = 6378137.000000000;
        double c17 = 0.0033528107;
        double f15 = c17 + c17 * c17;
        double f16 = f15 / 2;
        double f17 = c17 * c17 / 2;
        double f18 = c17 * c17 / 8;
        double f19 = c17 * c17 / 16;
        double c18 = e13 - e11;
        double c20 = (1 - c17) * Math.tan(e10);
        double c21 = Math.atan(c20);
        double c22 = Math.sin(c21);
        double c23 = Math.cos(c21);
        double c24 = (1 - c17) * Math.tan(e12);
        double c25 = Math.atan(c24);
        double c26 = Math.sin(c25);
        double c27 = Math.cos(c25);
        double c29 = c18;
        double c31 = (c27 * Math.sin(c29) * c27 * Math.sin(c29)) + (c23 * c26 - c22 * c27 * Math.cos(c29))
                * (c23 * c26 - c22 * c27 * Math.cos(c29));
        double c33 = (c22 * c26) + (c23 * c27 * Math.cos(c29));
        double c35 = Math.sqrt(c31) / c33;
        double c36 = Math.atan(c35);
        double c38 = 0;
        if (c31 == 0) {
            c38 = 0;
        } else {
            c38 = c23 * c27 * Math.sin(c29) / Math.sqrt(c31);
        }
        double c40 = 0;
        if ((Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38))) == 0) {
            c40 = 0;
        } else {
            c40 = c33 - 2 * c22 * c26 / (Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38)));
        }
        double c41 = Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38)) * (c15 * c15 - c16 * c16) / (c16 * c16);
        double c43 = 1 + c41 / 16384 * (4096 + c41 * (-768 + c41 * (320 - 175 * c41)));
        double c45 = c41 / 1024 * (256 + c41 * (-128 + c41 * (74 - 47 * c41)));
        double c47 = c45 * Math.sqrt(c31)
                * (c40 + c45 / 4 * (c33 * (-1 + 2 * c40 * c40) - c45 / 6 * c40 * (-3 + 4 * c31) * (-3 + 4 * c40 * c40)));
        double c50 = c17 / 16 * Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38))
                * (4 + c17 * (4 - 3 * Math.cos(Math.asin(c38)) * Math.cos(Math.asin(c38))));
        double c52 = c18 + (1 - c50) * c17 * c38
                * (Math.acos(c33) + c50 * Math.sin(Math.acos(c33)) * (c40 + c50 * c33 * (-1 + 2 * c40 * c40)));
        double c54 = c16 * c43 * (Math.atan(c35) - c47);
        // return distance in meter
        return Math.abs(c54);
    }

    // 방위각 구하는 부분
    public static short bearingP1toP2(double P1_latitude, double P1_longitude, double P2_latitude, double P2_longitude) {
        // 현재 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에
        // 라디안 각도로 변환한다.
        double Cur_Lat_radian = P1_latitude * (3.141592 / 180);
        double Cur_Lon_radian = P1_longitude * (3.141592 / 180);
        // 목표 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에
        // 라디안 각도로 변환한다.
        double Dest_Lat_radian = P2_latitude * (3.141592 / 180);
        double Dest_Lon_radian = P2_longitude * (3.141592 / 180);
        // radian distance
        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_Lat_radian) * Math.sin(Dest_Lat_radian) + Math.cos(Cur_Lat_radian)
                * Math.cos(Dest_Lat_radian) * Math.cos(Cur_Lon_radian - Dest_Lon_radian));
        // 목적지 이동 방향을 구한다.(현재 좌표에서 다음 좌표로 이동하기 위해서는
        // 방향을 설정해야 한다. 라디안값이다.
        double radian_bearing = Math.acos((Math.sin(Dest_Lat_radian) - Math.sin(Cur_Lat_radian) * Math.cos(radian_distance))
                / (Math.cos(Cur_Lat_radian) * Math.sin(radian_distance)));
        // acos의 인수로 주어지는 x는 360분법의 각도가 아닌 radian(호도)값이다.
        double true_bearing = 0;
        if (Math.sin(Dest_Lon_radian - Cur_Lon_radian) < 0) {
            true_bearing = radian_bearing * (180 / 3.141592);
            true_bearing = 360 - true_bearing;
        } else {
            true_bearing = radian_bearing * (180 / 3.141592);
        }
        return (short) true_bearing;
    }

    public static String NVL(String str){
    		return str == null ? "" : str;
    }
    
    
    
    
    /**
     * ResultSet -> JSON
     * @param rs
     * @return
     */
    public JSONArray convertResultSetToJSON(java.sql.ResultSet rs){

    	JSONArray json = new JSONArray();

    	try {
    		java.sql.ResultSetMetaData rsmd = rs.getMetaData();
    		while(rs.next()){
    			int numColumns = rsmd.getColumnCount();
    			JSONObject obj = new JSONObject();

    			for (int i=1; i<numColumns+1; i++) {

    				String column_name = rsmd.getColumnName(i);

    				switch (rsmd.getColumnType(i)){
    				case java.sql.Types.ARRAY:
    					obj.put(column_name, rs.getArray(i));
    					break;
    				case java.sql.Types.BIGINT:{
    					obj.put(column_name, rs.getInt(i));
    					break;
    				}
    				case java.sql.Types.BOOLEAN:{
    					obj.put(column_name, rs.getBoolean(i));
    					break;
    				}
    				case java.sql.Types.BLOB:{
    					obj.put(column_name, rs.getBlob(i));
    					break;
    				}
    				case java.sql.Types.DOUBLE:{
    					obj.put(column_name, rs.getDouble(i)); 
    					break;
    				}
    				case java.sql.Types.FLOAT:{
    					obj.put(column_name, rs.getFloat(i));
    					break;
    				}
    				case java.sql.Types.INTEGER:{
    					obj.put(column_name, rs.getInt(i));
    					break;
    				}
    				case java.sql.Types.NVARCHAR:{
    					obj.put(column_name, rs.getNString(i));
    					break;
    				}
    				case java.sql.Types.VARCHAR:{
    					obj.put(column_name, rs.getString(i));
    					break;
    				}
    				case java.sql.Types.TINYINT:{
    					obj.put(column_name, rs.getInt(i));
    					break;
    				}
    				case java.sql.Types.SMALLINT:{
    					obj.put(column_name, rs.getInt(i));
    					break;
    				}
    				case java.sql.Types.DATE:{
    					obj.put(column_name, rs.getDate(i));
    					break;
    				}
    				case java.sql.Types.TIMESTAMP:{
    					obj.put(column_name, rs.getTimestamp(i));   
    					break;
    				}
    				default :{
    					obj.put(column_name, rs.getObject(i));
    					break;
    				} 
    				}//end foreach
    				json.put(obj);
    			}
    		}//end while
    	} catch (SQLException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (JSONException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	return json;
    }
    
    
    
}
