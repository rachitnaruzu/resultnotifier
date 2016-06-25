package com.resultnotifier.main;

import java.util.HashMap;
import java.util.Map;

public class CommonUtility {

    public static int TotalNotificationCount = 0;


    public final static String DOMAIN = "<DOMAIN>";
    public final static String SECRET_KEY = "<SECRET_KEY>"; //must be same as that in backend
    public final static String APP_NAME = "Result Notifier"; 
    public final static String GCM_SENDER_ID = "<GCM_SENDER_KEY>";

    public static Map<String, Integer> dataTypeMap = new HashMap<>();

}
