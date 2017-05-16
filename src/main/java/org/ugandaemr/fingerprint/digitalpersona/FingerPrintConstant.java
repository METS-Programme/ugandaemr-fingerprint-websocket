package org.ugandaemr.fingerprint.digitalpersona;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by lubwamasamuel on 23/02/2017.
 */
public class FingerPrintConstant {
    public static String FINGER_PRINT_ATTRIBUTE_NAME = "FingerPrint";
    public static String FINGER_PRINT_ATTRIBUTE_DESCRIPTION = "This attribute is the type for person attributed which stores the ugandaemrfingerprint template";
    public static String FINGER_PRINT_ATTRIBUTE_UUID = "a41339f9-5014-45f4-91d6-bab84c6c62f1";


    public static final String CONNECTION_SERVER_IP_GLOBALPROPERTY = "ugandaemrsync.serverIP";

    public static final String CONNECTION_PROTOCOL = "http://";

    public static final int CONNECTION_SUCCESS = 200;


    public static final String NORMAL_CONTENT_TYPE = "application/x-wwww-form-urlencoded";

    public static final String JSON_CONTENT_TYPE = "application/json";

    public static final String SEARCH_URL = "fingerprint";

    public static final String FACILITY_NAME = "facility.name";
    public static final String FACILITY_ID = "facility.id";
    public static final String PATIENT_ID = "patient.id";
    public static final String PATIENT_SUMMARY = "patient.summary";

    /*public static final String SEARCH_PARAMS_ATTRIBUTE = "query=patient(attribute:{t:\"a41339f9-5014-45f4-91d6-bab84c6c62f2\",v:\"105708474\"}){uuid}";
    public static final String SEARCH_PARAMS_IDENTIFIER = "query=patient(identifier:{t:\"a41339f9-5014-45f4-91d6-bab84c6c62f2\",v:\"105708474\"}){uuid}";
    public static final String SEARCH_PARAMS_FINGERPRINT = "query=patient(attribute:{t:\"a41339f9-5014-45f4-91d6-bab84c6c62f2\",v:\"105708474\"}){uuid}";*/
    public static final String PATIENT_ONLINE_ID = "identifiers";
    public static final String PATIENT_NOT_FOUND = "Patient Not Found";
    public static final String CONNECTION_TEST_IP = "google.com/";


}
