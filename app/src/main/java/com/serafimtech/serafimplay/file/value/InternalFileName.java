package com.serafimtech.serafimplay.file.value;

import static com.serafimtech.serafimplay.file.DataReadAndWrite.ReadFile;

public class InternalFileName {
    //預設座標方案儲存目錄
    public static final String DEFAULT_INFO = "DefaultInfo";
    //預設座標方案支援清單儲存資料夾
    public static final String SUPPORTED_GAME_LIST = "SupportedGameList";
    //手機遊戲清單儲存目錄
    public static final String DEVICE_INFO = "DeviceInfo";
    //手機遊戲加入清單儲存資料夾
    public static final String ADDED_GAME_LIST = "AddedGameList";
    //

    public static final String JSON = "JSON";

    public static final String CUSTOM_INFO_BIND_GAME = "CustomInfoBindGame";
    public static final String BIND_GAME = "BindGame";
    public static final String CUSTOM_INFO = "CustomInfo";
    public static final String CUSTOM_MACRO_INFO = "CustomMacroInfo";

    public static final String DATA_SYNC = "DATA_SYNC";
    public static final String USER_INFO = "USER_INFO";
    public static final String LOGIN_TYPE = "login_type";
    public static final String USER_AVATAR = "user_avatar";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_NAME = "user_name";
    public static final String USER_ID = "user_id";

    public static final String SERAFIM_BANNER = "SERAFIM_BANNER";
    public static final String SERAFIM_IMAGES = "SERAFIM_IMAGES";
    public static final String SERAFIM_IMAGE = "serafim_image";
    public static final String BANNER_VER = "banner_ver";
    public static final String FW_INFO = "FWInfo";
    public static final String FW_UPDATE_INFO = "fwUpdateInfo";
    public static final String BLE_DEVICE_INFO = "BLE_DEVICE_INFO";
    public static final String S_SERIES_ADDRESS = "s_series_device_address_record";
    public static final String R_SERIES_ADDRESS = "r_series_device_address_record";
    public static final String APP_INFO = "AppInfo";
    public static final String APP_MODE = "AppMode";
    public static final String FTP_Default_PATH = "FTPDefaultPath";

    public static final String FileDir = "/data/user/0/com.serafimtech.serafimplay/app_AppInfo";

    public static final String s_update_version = "s_update_version";
    public static final String s_gamefile = "s_gamefile";
    public static final String s_Manual = "s_Manual";
    public static final String banner_file = "banner";

    public static final String s1_Manual_file = "s1.pdf";
    public static final String s2_Manual_file = "s2.pdf";


    public static final String r_DEFAULT_INFO = "r_DEFAULT_INFO";
    public static final String r_update_version = "r_update_version";
    public static final String r_gamefile = "r_gamefile";
    public static final String r_Manual = "r_Manual";

    public static final String r1_Manual_file = "r1.pdf";

    public static final String FTP_USER_STICK_DATA_PATH = "User/SerafimPlay/Stick/"+ReadFile(USER_INFO,USER_ID).replace("\n","").replace(" ","")+"/";
    public static final String FTP_TEST_PATH = "test/";
    public static final String FTP_STICK_SHARE_PATH = "User/SerafimPlay/Stick/Share/";
    public static final String FTP_TEST_SHARE_PATH = "test/Share/";
    public static final String FTP_ACTION = "FTP_ACTION";
}
