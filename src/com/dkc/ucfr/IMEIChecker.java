package com.dkc.ucfr;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IMEIChecker{
	private final String baseURL = "http://213.156.91.27/index.aspx";
	public String CheckIMEI(String imei_field){
		ParseResults result=null;
		try{
			//getting page content
			InputStream instream=getUrl(baseURL);			
			String resultStr= convertStreamToString(instream);
			if(instream!=null){
				instream.close();
			}
			//retrieving some special fields from page
			result=parseString(resultStr);
	        if(result!=null){	        
		       List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>(6);         
		       pairs.add(new BasicNameValuePair(
		    			"__EVENTARGUMENT", result.eventArgument));
		       pairs.add(new BasicNameValuePair(
		   			"__EVENTTARGET", result.eventTarget));
		       pairs.add(new BasicNameValuePair(
		   			"__EVENTVALIDATION", result.eventValidation));
		       pairs.add(new BasicNameValuePair(
		   			"__VIEWSTATE", result.viewState));
		       pairs.add(new BasicNameValuePair(
		      			"btCheck", "Перевірити"));
		       pairs.add(new BasicNameValuePair(
		      			"tbIMEI", imei_field));
		        
		       //posting request to server
		       instream=postUrl(baseURL,pairs);
		       resultStr= convertStreamToString(instream);
		       if(instream!=null){
					instream.close();
		       }
		       result=parseString(resultStr);
	        }
		}
		catch(Exception ex){
			result=null;
		}
		if(result!=null){
			return result.lAnswerValue;
		}
		return "";
	}
		
	
	 private ParseResults parseString(String result) {
		/* receives response string(page source) and parsing it.
		* looking for specified fields.
		*/
		 ParseResults res=new ParseResults();
		 String expr ="<form\\s?name=\"Form1\"[^>]*>([\\w|\\t|\\r\\|\\W]*?)</form>";
		 String exprVS="<\\s*input[^>]*name=\"__VIEWSTATE\"[^>]*value=\"([\\S^>^\"]*)\"[^>]*/>";
		 String exprARG="<\\s*input[^>]*name=\"__EVENTARGUMENT\"[^>]*value=\"([\\S^>^\"]*)\"[^>]*/>";
		 String exprVAL="<\\s*input[^>]*name=\"__EVENTVALIDATION\"[^>]*value=\"([\\S^>^\"]*)\"[^>]*/>";
		 String exprTAR="<\\s*input[^>]*name=\"__EVENTTARGET\"[^>]*value=\"([\\S^>^\"]*)\"[^>]*/>";
		 String exprRES="<\\s*span[^>]*id=\"lAnswerValue\"[^>]*>(.*?)<\\s*/\\s*span>";
		 try{
		 	Pattern patt = Pattern.compile(expr, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
			Matcher m = patt.matcher(result);
			String form="";
			while (m.find()) {
			  form = m.group(1);	
			}
			if(form.length()>0){					
				Pattern pattVS = Pattern.compile(exprVS, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				Matcher mVS = pattVS.matcher(result);
				while (mVS.find()) {
					res.viewState = mVS.group(1);
				}
				
				Pattern pattARG = Pattern.compile(exprARG, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				Matcher mARG = pattARG.matcher(result);
				while (mARG.find()) {
					res.eventArgument = mARG.group(1);
				}
				Pattern pattVAL = Pattern.compile(exprVAL, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				Matcher mVAL = pattVAL.matcher(result);
				while (mVAL.find()) {
					res.eventValidation = mVAL.group(1);
				}
				Pattern pattTAR = Pattern.compile(exprTAR, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				Matcher mTAR = pattTAR.matcher(result);
				while (mTAR.find()) {
					res.eventTarget = mTAR.group(1);
				}
				Pattern pattRES = Pattern.compile(exprRES, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				Matcher mRES = pattRES.matcher(result);
				while (mRES.find()) {
					res.lAnswerValue = mRES.group(1);
				}
			}				
		 }catch (Exception e) {
			 e.printStackTrace();
			 res=null;
		 }
		 return res;
	}
	 
	 private InputStream getUrl(String uri) {
			HttpURLConnection con = null;
			URL url;
			InputStream is=null;
			try {
				url = new URL(uri);
				con = (HttpURLConnection) url.openConnection();
				con.setReadTimeout(10000 /* milliseconds */);
				con.setConnectTimeout(15000 /* milliseconds */);
				con.setRequestMethod("GET");
				con.setDoInput(true);
				// Start the query
				con.connect();
				is = con.getInputStream();
			}catch (IOException e) {
                //handle the exception !
				e.printStackTrace();
				is=null;
			}	
			return is;
	 
		}
	private InputStream postUrl(String uri,List<BasicNameValuePair> nameValuePairs) {
        InputStream myInputStream =null;
		try {
            HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(uri);
			//say that this me google chrome, just for fun
			httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.4 (KHTML, like Gecko) Chrome/5.0.375.70 Safari/533.4");
			httpPost.setHeader("Accept-Charset", "utf-8");
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httpPost);
			myInputStream = response.getEntity().getContent();			
		} catch (Exception e) {
            //handle the exception !
			Log.e("m",e.getMessage(),e);
		}
        return myInputStream;
}
	
	private static String convertStreamToString(InputStream is) {
	        /*
	         * To convert the InputStream to String we use the BufferedReader.readLine()
	         * method. We iterate until the BufferedReader return null which means
	         * there's no more data to read. Each line will appended to a StringBuilder
	         * and returned as String.
	         */
		 
		BufferedReader rd = new BufferedReader(new InputStreamReader(is),4096);
		String line;
		StringBuilder sb =  new StringBuilder();
        try {
        	 while ((line = rd.readLine()) != null) {
 		 		sb.append(line);
 		 } 		 
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            	rd.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String contentOfMyInputStream = sb.toString();
        return contentOfMyInputStream;
    }  
}
