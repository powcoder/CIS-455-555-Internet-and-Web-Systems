https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
https://powcoder.com
代写代考加微信 powcoder
Assignment Project Exam Help
Add WeChat powcoder
package edu.upenn.cis.cis455.crawler.utils;

import spark.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
    /**
     * generate html content
     */
    public static String htmlpage(String content){
        String html = "<html><head></head><body>";
        html += content;
        html += "</body></html>";
        return html;
    }

    /**
     * parse post form data
     */
    public static Map<String, String> parseFormData(String body){
        Map<String, String> hashmap = new HashMap<>();
        if(!isEmpty(body)){
            String[] arr = body.split("&");
            for(int i = 0; i < arr.length; i++){
                String[] kvs = arr[i].split("=");
                if(kvs.length == 2){
                    hashmap.put(kvs[0], kvs[1]);
                }
            }
        }
        return hashmap;
    }

    /**
     * check session timeout
     */
    public static boolean isSessionValid(Session s){
        if(s == null){
            return false;
        }
        if(System.currentTimeMillis() - s.creationTime() < 30000){
            return true;
        } else {
            s.invalidate();
            return false;
        }
    }

    /**
     * is String empty
     */
    public static boolean isEmpty(String s){
        return s == null || s.length() <= 0;
    }

    /**
     * byte to hex string
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * Sha256 hash
     */
    public static String sha256(String str){
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes());
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * md5 hash
     */
    public static String md5(String source){
        String des = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] result = md.digest(source.getBytes());
            StringBuilder buf = new StringBuilder();
            for (int i=0;i<result.length;i++) {
                byte b = result[i];
                buf.append(String.format("%02X", b));
            }
            des = buf.toString().toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("md5 failure");
        }
        return des;
    }

    /**
     * http get requets, return content
     */
    public static String httpGet(String getUrl) throws IOException {
        URL getURL = new URL(getUrl);
        HttpURLConnection connection = (HttpURLConnection) getURL.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent", "cis455crawler");
        connection.connect();

        InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
        StringBuilder sbStr = new StringBuilder();
        int ch;
        while ((ch = inputStream.read()) != -1) {
            sbStr.append((char)ch);
        }
        connection.disconnect();
        Integer status = connection.getResponseCode();
        String body = new String(sbStr.toString().getBytes(), "utf-8");

        return body;
    }

    /**
     * http head request, return header
     */
    public static Map<String, List<String>> httpHead(String getUrl) throws IOException {
        URL getURL = new URL(getUrl);
        HttpURLConnection connection = (HttpURLConnection) getURL.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("HEAD");
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent", "cis455crawler");
        connection.connect();
        connection.disconnect();
        return connection.getHeaderFields();
    }

    /**
     * parse robot.txt
     */
    public static RobotsTxtInfo parseRobot(String content){
        RobotsTxtInfo robotsTxtInfo = new RobotsTxtInfo();
        if(isEmpty(content)){
            return robotsTxtInfo;
        }
        String[] lines = content.split("\r\n");
        String currentUserAgent = "*";
        for(String line : lines){
            if(line.toLowerCase().startsWith("user-agent:")){
                currentUserAgent = line.substring(11).trim();
                robotsTxtInfo.addUserAgent(currentUserAgent);
            } else if(line.toLowerCase().startsWith("disallow:")){
                robotsTxtInfo.addDisallowedLink(currentUserAgent, line.substring(9).trim());
            } else if(line.toLowerCase().startsWith("allow:")){
                robotsTxtInfo.addAllowedLink(currentUserAgent, line.substring(6).trim());
            } else if(line.toLowerCase().startsWith("crawl-delay:")){
                Integer delay = 0;
                try{
                    delay = Integer.parseInt(line.substring(12).trim());
                } catch (Exception e){
                    delay = 0;
                }
                robotsTxtInfo.addCrawlDelay(currentUserAgent, delay);
            }
        }

        return robotsTxtInfo;
    }

    public static String formatDate(Long time){
        Date dt = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return formatter.format(dt).trim();
    }
}
