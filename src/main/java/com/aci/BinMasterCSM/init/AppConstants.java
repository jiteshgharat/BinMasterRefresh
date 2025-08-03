package com.aci.BinMasterCSM.init;

import java.text.Normalizer;

public final class AppConstants {
    public static final String DESede = "DESede";
    public static final String SEP = "/";
    public static final String CBC = "CBC";
    public static final String ECB = "ECB";
    public static final String PKCS5Padding = "PKCS5Padding";
    public static final String SINGLEPROCESSING = "SingleProcessing";
    public static final String INPUT_VLDTN_1 = "\n";
    public static final String INPUT_VLDTN_2 = "\r";
    public static final String INPUT_VLDTN_3 = "..\\";
    public static final String INPUT_VLDTN_4 = "../";
    public static final String INPUT_VLDTN_5 = "";
    public static final String PBKDF2WITHHMACSHA512 = "PBKDF2WithHmacSHA512";
    public static final String AES = "AES";
    public static final String COLON = ":";
    public static final String PKCS5PADDING = "PKCS5Padding";
    public static String APP_PATH;

    static {
        try {
            APP_PATH = Normalizer.normalize(System.getProperty("user.dir"),
                    Normalizer.Form.NFC).replace(AppConstants.INPUT_VLDTN_1,
                    AppConstants.INPUT_VLDTN_5).replace(AppConstants.INPUT_VLDTN_2,
                    AppConstants.INPUT_VLDTN_5).replace(AppConstants.INPUT_VLDTN_3,
                    AppConstants.INPUT_VLDTN_5).replace(AppConstants.INPUT_VLDTN_4,
                    AppConstants.INPUT_VLDTN_5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String CONFIG_PATH = String.valueOf(APP_PATH) + "\\Configuration\\";
    public static final String CONFIG_PROPERTIES_FILE = "config.properties";
}