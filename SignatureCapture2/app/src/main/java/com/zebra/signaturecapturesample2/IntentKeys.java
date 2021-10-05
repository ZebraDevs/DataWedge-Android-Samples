package com.zebra.signaturecapturesample2;

public class IntentKeys {

    public static final String PROFILE_NAME = "SignatureCaptureProfile";

    public static final String DATAWEDGE_PACKAGE = "com.symbol.datawedge";
    public static final String DATAWEDGE_API_ACTION = "com.symbol.datawedge.api.ACTION";
    public static final String DATAWEDGE_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";

    public static final String 	EXTRA_GET_PROFILES_LIST  = "com.symbol.datawedge.api.GET_PROFILES_LIST";
    public static final String 	EXTRA_DELETE_PROFILE  = "com.symbol.datawedge.api.DELETE_PROFILE";
    public static final String 	EXTRA_RESULT_GET_PROFILES_LIST  = "com.symbol.datawedge.api.RESULT_GET_PROFILES_LIST";

    public static final String RESULT_ACTION = "com.symbol.datawedge.api.RESULT_ACTION";
    public static final String NOTIFICATION_ACTION = "com.symbol.datawedge.api.NOTIFICATION_ACTION";
    public static final String INTENT_OUTPUT_ACTION = "com.symbol.genericdata.INTENT_OUTPUT";
    public static final String INTENT_CATEGORY = "android.intent.category.DEFAULT";

    public static final Integer INTENT_DELIVERY = 2; // Set intent delivery mechanism, Use "0" for Start Activity, "1" for Start Service, "2" for Broadcast, "3" for start foreground service
    public static final String COMMAND_IDENTIFIER_CREATE_PROFILE = "CREATE_PROFILE";
    public static final String COMMAND_IDENTIFIER_EXTRA = "COMMAND_IDENTIFIER";

    public static final String INTENT_RESULT_CODE_FAILURE = "FAILURE";
    public static final String EXTRA_SOFT_SCAN_TRIGGER = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";

    public static final String DECODED_MODE = "com.symbol.datawedge.decoded_mode";
    public static final String LABEL_TYPE_TAG = "com.symbol.datawedge.label_type";
    public static final String IMAGE_URI = "com.symbol.datawedge.image_data";

    public static final String IMAGE_SIGNATURE_TYPE = "signature_type";
    public static final String IMAGE_DATA = "image_data";
    public static final String IMAGE_SIZE = "image_size";
    public static final String IMAGE_FORMAT = "image_format";
    public static final String IMAGE_NEXT_URI = "next_data_uri";
    public static final String IMAGE_FULL_DATA_SIZE = "full_data_size";
    public static final String IMAGE_BUFFER = "data_buffer_size";

}
