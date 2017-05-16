package org.ugandaemr.fingerprint.controller;

import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.ugandaemr.fingerprint.digitalpersona.DigitalPersona;
import org.ugandaemr.fingerprint.digitalpersona.Fingerprint;
import org.ugandaemr.fingerprint.digitalpersona.Result;

import java.util.Map;


@Controller
public class WebSocketController {
    @Autowired
    private DigitalPersona digitalPersona;

    @MessageMapping("/add")
    @SendTo("/topic/showResult")
    public Result addFingerprint(Fingerprint input) throws Exception {
        DPFPTemplate temp = digitalPersona.getTemplate(null, input.getFinger());
        byte[] b = temp.serialize();
        // digitalPersona.createTable();
        //digitalPersona.insert(input.getPatient(), input.getFinger(), b);
       /* StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.newStringUtf8(Base64.encodeBase64(b)));*/
        // Fingerprint fingerprint = new Fingerprint();
        // fingerprint.setFinger(input.getFinger());
        //fingerprint.setFingerprint(sb.toString());
        //fingerprint.setFingerprintBytes(b);
        //return new Fingerprint();

        return new Result(Base64.encodeBase64String(b), "template", input.getPatient(), input.getFinger());
    }

    @MessageMapping("/fingerprint")
    @SendTo("/topic/showResult")
    public Result fingerprint() throws Exception {
        DPFPSample sample = digitalPersona.getSample(null, "Please swipe your finger");
        return new Result(Base64.encodeBase64String(sample.serialize()), "sample");
    }

    @MessageMapping("/search")
    @SendTo("/topic/showResult")
    public Result search() throws Exception {
        //digitalPersona.createTable();
        ListMultimap<String, byte[]> others = digitalPersona.get();
        ListMultimap<String, DPFPTemplate> templates = ArrayListMultimap.create();
        for (Map.Entry entry : others.entries()) {
            String key = String.valueOf(entry.getKey());
            byte[] val = (byte[]) entry.getValue();
            DPFPTemplate temp2 = DPFPGlobal.getTemplateFactory().createTemplate();
            temp2.deserialize(val);
            templates.put(key, temp2);
        }

        return digitalPersona.verify(null, templates);

    }

    @RequestMapping("/start")
    public String start() {
        return "start";
    }
}
