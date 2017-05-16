package org.ugandaemr.fingerprint.digitalpersona;

/**
 * Created by lubwamasamuel on 11/10/16.
 */


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FingerPrintHttpURLConnection {

    public FingerPrintHttpURLConnection() {
    }

    private final String USER_AGENT = "Mozilla/5.0";

    public StringBuffer getResponseString(BufferedReader bufferedReader) throws IOException {
        String inputLine;

        StringBuffer response = new StringBuffer();

        while ((inputLine = bufferedReader.readLine()) != null) {
            response.append(inputLine);
        }
        return response;
    }

    public Result postReal(Result r) throws IOException {
        String url = "http://ugandaemr.mets.or.ug/fingerprint";

        HttpClient client = HttpClientBuilder.create().build();
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(r);
        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);

        HttpPost postMethod = new HttpPost(url);
        postMethod.setEntity(requestEntity);

        HttpResponse response = client.execute(postMethod);

        if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            String message = IOUtils.toString(br);
            Result returnResult = mapper.readValue(message, Result.class);
            return returnResult;
        } else {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
        }
    }
}
