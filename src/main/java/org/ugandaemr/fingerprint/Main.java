package org.ugandaemr.fingerprint;

import org.springframework.stereotype.Component;
import org.ugandaemr.fingerprint.digitalpersona.FingerPrintHttpURLConnection;
import org.ugandaemr.fingerprint.digitalpersona.Result;

import java.io.IOException;

/**
 * Created by mets on 4/5/2017.
 */
@Component
public class Main {
   /* public static void main(String[] args) {
        FingerPrintHttpURLConnection client = new FingerPrintHttpURLConnection();
        try {
            Result r = new Result();
            byte[] cpx = "carapai".getBytes();
            r.setFingerprint(cpx);
            Result response = client.postReal(r);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
