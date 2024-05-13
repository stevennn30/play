package com.serafimtech.serafimplay.file.value;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SerafimInfo {
    public static int app_version = 0;
    public static boolean app_update_flag = false;
    public static String app_download_link = "https://play.google.com/store/apps/details?id=com.serafimtech.serafimplay";
    public static int banner_ver = 0;
    public static JSONArray banners;
    public static JSONObject official_website_link = new JSONObject(){
        {
            try {
                put("jp", "http://serafim-tech.com/ja/jp-news.html");
                put("tw", "http://serafim-tech.com/zh-TW/%E6%9C%80%E6%96%B0%E6%B6%88%E6%81%AF.html");
                put("cn", "http://serafim-tech.com/zh-TW/%E6%9C%80%E6%96%B0%E6%B6%88%E6%81%AF.html");
                put("us", "http://serafim-tech.com/news.html");
                put("kr", "http://serafim-tech.com/news.html");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    };
    public static JSONObject shop_website_link = new JSONObject(){
        {
            try {
                put("jp", "https://www.makuake.com/project/serafim-r1/");
                put("tw", "https://www.racing.serafim-tech.com/");
                put("cn", "https://www.racing.serafim-tech.com/");
                put("us", "http://serafim-tech.com/en/");
                put("kr", "https://www.wadiz.kr/web/campaign/detail/46493");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    };
    public static JSONObject share_website_link = new JSONObject(){
        {
            try {
                put("jp", "https://www.makuake.com/project/serafim-r1/");
                put("tw", "https://www.racing.serafim-tech.com/");
                put("cn", "https://www.racing.serafim-tech.com/");
                put("us", "http://serafim-tech.com/en/");
                put("kr", "https://www.wadiz.kr/web/campaign/detail/46493");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    };
    public static String official_website_jp = "http://serafim-tech.com/ja/jp-news.html";
//    public static String official_website_cn = "http://serafim-tech.com/zh-TW/%E6%9C%80%E6%96%B0%E6%B6%88%E6%81%AF.html";
//    public static String official_website_tw = "http://serafim-tech.com/zh-TW/%E6%9C%80%E6%96%B0%E6%B6%88%E6%81%AF.html";
    public static String official_website_us = "http://serafim-tech.com/news.html";
    public static String official_website_kr = "http://serafim-tech.com/news.html";

//    public static String official_website_jp = "http://serafim-tech.com/ja/jp-news.html";
    public static String official_website_cn = "http://www.serafim-tech.com/zh-TW/%E9%83%A8%E8%90%BD%E6%A0%BC/661-s1_unbox.html";
    public static String official_website_tw = "http://www.serafim-tech.com/zh-TW/%E9%83%A8%E8%90%BD%E6%A0%BC/661-s1_unbox.html";
//    public static String official_website_us = "http://serafim-tech.com/news.html";
//    public static String official_website_kr = "http://serafim-tech.com/news.html";

    public static String shop_url_jp = "https://www.makuake.com/project/serafim-r1/";
//    public static String shop_url_cn = "https://www.racing.serafim-tech.com/";
//    public static String shop_url_tw = "https://www.racing.serafim-tech.com/";
    public static String shop_url_us = "http://serafim-tech.com/en/";
    public static String shop_url_kr = "https://www.wadiz.kr/web/campaign/detail/46493";
//    public static String shop_url_jp = "https://www.makuake.com/project/serafim-r1/";
    public static String shop_url_cn = "https://serafim-tech.com/zh/home-tw";
    public static String shop_url_tw = "https://serafim-tech.com/zh/home-tw";
//    public static String shop_url_us = "http://serafim-tech.com/en/";
//    public static String shop_url_kr = "https://www.wadiz.kr/web/campaign/detail/46493";

    public static String share_url_jp = "http://serafim-tech.com/en/";
    public static String share_url_cn = "https://www.makuake.com/project/serafim-r1/";
    public static String share_url_tw = "https://www.racing.serafim-tech.com/";
    public static String share_url_us = "https://www.racing.serafim-tech.com/";
    public static String share_url_kr = "https://www.wadiz.kr/web/campaign/detail/46493";
    public static String update_url_google_play = "https://play.google.com/store/apps/details?id=com.serafimtech.serafimplay";
}
