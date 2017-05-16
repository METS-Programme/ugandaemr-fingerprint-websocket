package org.ugandaemr.fingerprint.digitalpersona;

/**
 * Created by mets on 3/23/2017.
 */
public class Fingerprint {
    private String patient;
    private Integer finger;

    public Fingerprint() {
    }

    public Fingerprint(String patient, Integer finger) {
        this.patient = patient;
        this.finger = finger;
    }

    public Fingerprint(String patient) {
        this.patient = patient;
    }


    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public Integer getFinger() {
        return finger;
    }

    public void setFinger(Integer finger) {
        this.finger = finger;
    }
}
