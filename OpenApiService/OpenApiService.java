

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OpenApiService {
	
	private final static Log log = LogFactory.getLog(OpenApiService.class);
	
	private final static String PARAM_ENCODING = "UTF-8";
	
	public static String call(String apiUrl, String secret, Map<String, String> parameters) throws Exception {
		String postStr = joinItems(parameters) + "&signature=" + sign(parameters, secret);
		byte[] postData = postStr.getBytes(PARAM_ENCODING);
		
		OutputStream output = null;
		BufferedReader reader = null;
		try {
			URL postUrl = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();
	        
			conn.setRequestMethod("POST");
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
	        conn.setRequestProperty("User-Agent", "Mozilla/4.0");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
	        
	        output = conn.getOutputStream();
	        output.write(postData);
	        output.flush();
	        output.close();
	        output = null;
	        
	        int rc = conn.getResponseCode();
	        if (rc == 200) {
	            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	            
	            StringBuffer rsp = new StringBuffer();
	            String line;
	            while ((line = reader.readLine()) != null) {
	                rsp.append(line);
	            }
	            
	            return rsp.toString();
	        } else {
	        	log.error("failed to call open api: \nParameters: " + 
            			parameters + "\nResponse Code: " + rc);
	        	
	        	throw new RuntimeException("" + rc);
	        }
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					log.error("failed to close output stream to open api", e);
				}
			}
			
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error("failed to close input stream from open api", e);
				}
			}
		}
	}
	
	public static void download(String apiUrl, String secret, Map<String, String> parameters) throws Exception {
		String postStr = joinItems(parameters) + "&signature=" + sign(parameters, secret);
		byte[] postData = postStr.getBytes(PARAM_ENCODING);
		
		OutputStream output = null;
		InputStream input = null;
		try {
			URL postUrl = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();
	        
			conn.setRequestMethod("POST");
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setConnectTimeout(600000);
			conn.setReadTimeout(600000);
	        conn.setRequestProperty("User-Agent", "Mozilla/4.0");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
	        
	        output = conn.getOutputStream();
	        output.write(postData);
	        output.flush();
	        output.close();
	        output = null;
	        
	        int rc = conn.getResponseCode();
	        if (rc == 200) {
	        	input = conn.getInputStream();
	        	
	        	OutputStream fos = new FileOutputStream("D:\\test.zip");
	        	try {
	        		IOUtils.copy(input, fos);
	        	} finally {
	        		fos.close();
	        	}
	        } else {
	        	log.error("failed to call open api: \nParameters: " + 
            			parameters + "\nResponse Code: " + rc);
	        	
	        	throw new RuntimeException("" + rc);
	        }
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					log.error("failed to close output stream to open api", e);
				}
			}
			
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.error("failed to close input stream from open api", e);
				}
			}
		}
	}
	
	private static String joinItems(Map<String, String> itemMap) {
		if (itemMap == null) {
    		return "";
    	}
		
    	StringBuffer sb = new StringBuffer();
    	for (Entry<String, String> e : itemMap.entrySet()) {
            String key = (String) e.getKey();
        	String value = (String) e.getValue();
        	
        	if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
        		if (sb.length() > 0) {
        			sb.append('&');
        		}
        		
        		sb.append(key);
        		sb.append('=');
				sb.append(escape(value));
        	}
        }
        
        return sb.toString();
    }
	
	private static String sign(Map<String, String> itemMap, String securityCode) {
		Map sortedMap = new TreeMap(itemMap);
		StringBuffer sb = new StringBuffer();
		for (Entry e : (Set<Entry>) sortedMap.entrySet()) {
			sb.append(e.getValue());
		}
		
		return MD5Digester.digest(sb + "@" + securityCode);
	}
	
	private static String escape(String str) {
    	if (StringUtils.isNotBlank(str)) {
	    	try {
				return URLEncoder.encode(str, PARAM_ENCODING);
	    	} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}
    	}
    	
    	return "";
    }
	
	public static void main(String[] args) throws Exception {
		String key = "TESTKEY";
		String secret = "TESTSECRET";
		
		// Flush CDN dir 
		if (false) {
			String apiUrl = "http://www.sunsky-api.com/openapi/cdn!purgeDir.do";
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("key", key);
			parameters.put("email", "admin@mysite.com");
			parameters.put("url", "http://img.mysite.com/");
	
			String result = OpenApiService.call(apiUrl, secret, parameters);
			
			System.out.println("rsp" + result);
			JSONObject jsonResult = JSONObject.fromObject(result);
			if (! "success".equals(jsonResult.getString("result"))) {
				throw new Exception("Error: " + result);
			}
		}
		
		// Flush CDN files 
		if (false) {
			String apiUrl = "http://www.sunsky-api.com/openapi/cdn!purgeFiles.do";
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("key", key);
			parameters.put("urls", "http://img.mysite.com/images/a.jpg");
			parameters.put("urls", "http://img.mysite.com/images/b.jpg");
	
			String result = OpenApiService.call(apiUrl, secret, parameters);
			
			System.out.println("rsp" + result);
			JSONObject jsonResult = JSONObject.fromObject(result);
			if (! "success".equals(jsonResult.getString("result"))) {
				throw new Exception("Error: " + result);
			}
		}

		// Fetch the categories ever changed since gmtModifiedStart
		if (false) {
			String apiUrl = "http://www.sunsky-api.com/openapi/category!getChildren.do";
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("key", key);
			parameters.put("gmtModifiedStart", "10/31/2013");
	
			String result = OpenApiService.call(apiUrl, secret, parameters);
			
			JSONObject jsonResult = JSONObject.fromObject(result);
			if (! "success".equals(jsonResult.getString("result"))) {
				throw new Exception("Error: " + result);
			}
			
			JSONArray jsonData = jsonResult.getJSONArray("data");
			for (int i = 0, n = jsonData.size(); i < n; i++) {
				JSONObject category = jsonData.getJSONObject(i);
				System.out.println(category);
			}
		}
		
		// Fetch the products ever changed since gmtModifiedStart
		if (false) {
			String apiUrl = "http://www.sunsky-api.com/openapi/product!search.do";
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("key", key);
			parameters.put("gmtModifiedStart", "10/31/2012");
			
			String result = OpenApiService.call(apiUrl, secret, parameters);
			
			JSONObject jsonResult = JSONObject.fromObject(result);
			if (! "success".equals(jsonResult.getString("result"))) {
				throw new Exception("Error: " + result);
			}
			
			JSONObject jsonListModel = jsonResult.getJSONObject("data");
			int pageCount = jsonListModel.getInt("pageCount");
			System.out.println("Page Count: " + pageCount);
			
			for (int i = 0; i < pageCount; i++) {
				parameters.put("page", Integer.toString(i + 1));
				result = OpenApiService.call(apiUrl, secret, parameters);
	  			
				jsonResult = JSONObject.fromObject(result);
				if (! "success".equals(jsonResult.getString("result"))) {
					throw new Exception("Error: " + result);
				}
				
				jsonListModel = jsonResult.getJSONObject("data");
				JSONArray list = jsonListModel.getJSONArray("result");
				for (int j = 0, n = list.size(); j < n; j++) {
					JSONObject product = list.getJSONObject(j);
					System.out.println(product);
					
					// update the product's fields(including categoryId) in DB
				}
			}
		}
		
		// Fetch the details for the product
		if (false) {
			String apiUrl = "http://www.sunsky-api.com/openapi/product!detail.do";
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("key", key);
			parameters.put("itemNo", "S-MPH-6016B");
			System.out.println(OpenApiService.call(apiUrl, secret, parameters));
		}
		
		// Download the images
		if (false) {
			String apiUrl = "http://www.sunsky-api.com/openapi/product!getImages.do";
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("key", key);
			parameters.put("itemNo", "S-MPH-6016B");
			parameters.put("size", "50");
			parameters.put("watermark", "mysite.com");
			OpenApiService.download(apiUrl, secret, parameters);
		}
		
		// Fetch the countries and states
		if (false) {
			String apiUrl = "http://www.sunsky-api.com/openapi/order!getCountries.do";
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("key", key);
			System.out.println(OpenApiService.call(apiUrl, secret, parameters));
		}
		
		// Check the balance
		if (false) {
			String apiUrl = "http://www.sunsky-api.com/openapi/order!getBalance.do";
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("key", key);
			System.out.println(OpenApiService.call(apiUrl, secret, parameters));
			
			apiUrl = "http://www.sunsky-api.com/openapi/order!getBillList.do";
			parameters = new HashMap<String, String>();
			parameters.put("key", key);
			parameters.put("gmtCreatedStart", "1/31/2011");
			
			String result = OpenApiService.call(apiUrl, secret, parameters);
			
			JSONObject jsonResult = JSONObject.fromObject(result);
			if (! "success".equals(jsonResult.getString("result"))) {
				throw new Exception("Error: " + result);
			}
			
			JSONObject jsonListModel = jsonResult.getJSONObject("data");
			int pageCount = jsonListModel.getInt("pageCount");
			System.out.println("Page Count: " + pageCount);
			
			for (int i = 0; i < pageCount; i++) {
				parameters.put("page", Integer.toString(i + 1));
				result = OpenApiService.call(apiUrl, secret, parameters);
	  			
				jsonResult = JSONObject.fromObject(result);
				if (! "success".equals(jsonResult.getString("result"))) {
					throw new Exception("Error: " + result);
				}
				
				jsonListModel = jsonResult.getJSONObject("data");
				JSONArray list = jsonListModel.getJSONArray("result");
				for (int j = 0, n = list.size(); j < n; j++) {
					JSONObject tx = list.getJSONObject(j);
					System.out.println(tx);
				}
			}
		}
		
		// Calculate the prices and freights for the items
		if (false) {
			String apiUrl = "http://www.sunsky-api.com/openapi/order!getPricesAndFreights.do";
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("key", key);
			parameters.put("countryId", "41");
			parameters.put("items.1.itemNo", "S-IP4G-0363");
			parameters.put("items.1.qty", "20");
			parameters.put("items.2.itemNo", "S-MAC-0230");
			parameters.put("items.2.qty", "5");
			String result = OpenApiService.call(apiUrl, secret, parameters);
			
			JSONObject jsonResult = JSONObject.fromObject(result);
			if (! "success".equals(jsonResult.getString("result"))) {
				throw new Exception("Error: " + result);
			}
			
			JSONObject jsonPricesAndFreights = jsonResult.getJSONObject("data");
			JSONArray freightList = jsonPricesAndFreights.getJSONArray("freightList");
			if (freightList.size() == 0) {
				System.out.println("no shipping way to the country");
				return;
			}
			
			JSONObject way = freightList.getJSONObject(0);
			
			// Create an order
			if (false) {
				apiUrl = "http://www.sunsky-api.com/openapi/order!createOrder.do";
				parameters = new HashMap<String, String>();
				parameters.put("key", key);
				parameters.put("deliveryAddress.countryId", "41");
				parameters.put("deliveryAddress.state", "NY");
				parameters.put("deliveryAddress.city", "New York");
				parameters.put("deliveryAddress.address", "New York");
				parameters.put("deliveryAddress.postcode", "100098");
				parameters.put("deliveryAddress.receiver", "Test");
				parameters.put("deliveryAddress.telephone", "123456");
				parameters.put("deliveryAddress.shippingWayId", way.getString("id"));
				parameters.put("siteNumber", "MyNumber002");
				parameters.put("items.1.itemNo", "S-IP4G-0363");
				parameters.put("items.1.qty", "20");
				parameters.put("items.2.itemNo", "S-MAC-0230");
				parameters.put("items.2.qty", "5");
				System.out.println(OpenApiService.call(apiUrl, secret, parameters));
			}
			
			String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
			
			apiUrl = "http://www.sunsky-api.com/openapi/order!getOrderList.do";
			parameters = new HashMap<String, String>();
			parameters.put("key", key);
			parameters.put("gmtCreatedStart", date);
			parameters.put("gmtCreatedEnd", date);
			result = OpenApiService.call(apiUrl, secret, parameters);
			
			jsonResult = JSONObject.fromObject(result);
			if (! "success".equals(jsonResult.getString("result"))) {
				throw new Exception("Error: " + result);
			}
			
			JSONObject jsonListModel = jsonResult.getJSONObject("data");
			int pageCount = jsonListModel.getInt("pageCount");
			System.out.println("Page Count: " + pageCount);
			
			for (int i = 0; i < pageCount; i++) {
				parameters.put("page", Integer.toString(i + 1));
				result = OpenApiService.call(apiUrl, secret, parameters);
	  			
				jsonResult = JSONObject.fromObject(result);
				if (! "success".equals(jsonResult.getString("result"))) {
					throw new Exception("Error: " + result);
				}
				
				jsonListModel = jsonResult.getJSONObject("data");
				JSONArray list = jsonListModel.getJSONArray("result");
				for (int j = 0, n = list.size(); j < n; j++) {
					JSONObject order = list.getJSONObject(j);
					
					// Fetch the details for the order
					String apiUrl2 = "http://www.sunsky-api.com/openapi/order!getOrderList.do";
					Map<String, String> parameters2 = new HashMap<String, String>();
					parameters2.put("key", key);
					parameters2.put("number", order.getString("number"));
					System.out.println(OpenApiService.call(apiUrl2, secret, parameters2));
				}
			}
		}
		
		// Fetch the hot items
		if (false) {
			String apiUrl = "http://www.sunsky-api.com/openapi/stats!getHotItems.do";
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("key", key);
			parameters.put("countryId", "41");
			
			String result = OpenApiService.call(apiUrl, secret, parameters);
			
			JSONObject jsonResult = JSONObject.fromObject(result);
			if (! "success".equals(jsonResult.getString("result"))) {
				throw new Exception("Error: " + result);
			}
			
			System.out.println(jsonResult.getJSONArray("data"));
		}
	}
    
}
