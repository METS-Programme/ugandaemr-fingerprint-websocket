package org.ugandaemr.fingerprint.digitalpersona;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by mets on 4/6/2017.
 */
public class UgandaEMRUtils {
    public static String getAttributeSearch(String searchType, String attributeType, String attributeValue) {
        return String.format("query=patient(%s:{t:\"%s\",v:\"%s\"}){uuid}", searchType, attributeType, attributeValue);
    }

    public static String getFingerprintSearch(String fingerprint) {
        return String.format("query=patient(fingerprint:%s){uuid}", fingerprint);
    }

    public static String getFingerprint(String fingerprint) {
        return String.format("fingerprint=%s", fingerprint);
    }

    public static boolean testInternet(String site) {
        Socket sock = new Socket();
        InetSocketAddress addr = new InetSocketAddress(site, 80);
        try {
            sock.connect(addr, 3000);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                sock.close();
            } catch (IOException e) {
            }
        }
    }
}
