package org.ugandaemr.fingerprint.digitalpersona;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Result implements Serializable {

    private String result;
    private String type;
    private String patient;
    private Integer finger;
    private byte[] fingerprint;
    Map patientSummary;

    public Result() {
    }

    public Result(String result) {
        this.result = result;
        this.type = "string";
    }

    public Result(String result, String type) {
        this.result = result;
        this.type = type;
    }

    public Result(String result, String type, String patient, Integer finger) {
        this.result = result;
        this.type = type;
        this.patient = patient;
        this.finger = finger;
    }

    public String getResult() {
        return result;
    }

    public String getType() {
        return type;
    }

    public String getPatient() {
        return patient;
    }

    public Integer getFinger() {
        return finger;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public void setFinger(Integer finger) {
        this.finger = finger;
    }

    public byte[] getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(byte[] fingerprint) {
        this.fingerprint = fingerprint;
    }

    public Map<String, String> getPatientSummary() {
        return patientSummary;
    }

    public void setPatientSummary(Map<String, String> patientSummary) {
        this.patientSummary = patientSummary;
    }

    public void addPatientAttribute(String key, String value) {
        this.getPatientSummary().put(key, value);
    }
}
