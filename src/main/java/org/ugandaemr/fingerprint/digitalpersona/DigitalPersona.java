package org.ugandaemr.fingerprint.digitalpersona;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.DPFPCapturePriority;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPDataListener;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.readers.DPFPReaderDescription;
import com.digitalpersona.onetouch.readers.DPFPReadersCollection;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class DigitalPersona {

    private final SimpMessagingTemplate websocket;

    @Autowired
    public DigitalPersona(SimpMessagingTemplate websocket) {
        this.websocket = websocket;
    }

    public Connection cn() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://192.168.8.109:3306/openmrs", "openmrs", "openmrs");
        } catch (Exception e) {
            System.out.println(e);
        }
        return conn;
    }

    public void insert(String patient, int finger, byte[] fingerprint) {
        PreparedStatement st;
        try {
            st = cn().prepareStatement("INSERT INTO fingerprint(patient,finger,fingerprint) VALUES(?, ?, ?)");
            st.setString(1, patient);
            st.setInt(2, finger);
            st.setBytes(3, fingerprint);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createTable() {
        Statement st;
        String statement = "CREATE TABLE IF NOT EXISTS fingerprint (\n" +
                "  fingerprint_id INT(32) AUTO_INCREMENT,\n" +
                "  patient   CHAR(38),\n" +
                "  finger         INT(1),\n" +
                "  fingerprint    TEXT,\n" +
                "  PRIMARY KEY (fingerprint_id))";

        try {
            st = cn().createStatement();
            st.execute(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ListMultimap<String, byte[]> get() {
        ResultSet rs;
        PreparedStatement st;
        ListMultimap<String, byte[]> multimap = ArrayListMultimap.create();

        try {
            st = cn().prepareStatement("SELECT * FROM fingerprint");
            rs = st.executeQuery();
            while (rs.next()) {
                String fingerprint = rs.getString("fingerprint");
                byte[] b = Base64.decodeBase64(fingerprint);
                multimap.put(rs.getString("patient"), b);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return multimap;
    }

    public static void listReaders() {
        DPFPReadersCollection readers = DPFPGlobal.getReadersFactory().getReaders();
        if (readers == null || readers.size() == 0) {
            System.out.printf("There are no readers available.\n");
            return;
        }
        System.out.printf("Available readers:\n");
        for (DPFPReaderDescription readerDescription : readers)
            System.out.println(readerDescription.getSerialNumber());
    }

    public static final EnumMap<DPFPFingerIndex, String> fingerNames;

    static {
        fingerNames = new EnumMap<DPFPFingerIndex, String>(DPFPFingerIndex.class);
        fingerNames.put(DPFPFingerIndex.LEFT_PINKY, "left pinky");
        fingerNames.put(DPFPFingerIndex.LEFT_RING, "left ring");
        fingerNames.put(DPFPFingerIndex.LEFT_MIDDLE, "left middle");
        fingerNames.put(DPFPFingerIndex.LEFT_INDEX, "left index");
        fingerNames.put(DPFPFingerIndex.LEFT_THUMB, "left thumb");
        fingerNames.put(DPFPFingerIndex.RIGHT_PINKY, "right pinky");
        fingerNames.put(DPFPFingerIndex.RIGHT_RING, "right ring");
        fingerNames.put(DPFPFingerIndex.RIGHT_MIDDLE, "right middle");
        fingerNames.put(DPFPFingerIndex.RIGHT_INDEX, "right index");
        fingerNames.put(DPFPFingerIndex.RIGHT_THUMB, "right thumb");
    }

    public DPFPTemplate getTemplate(String activeReader, int nFinger) {

        websocket.convertAndSend("/topic/showResult", new Result("Performing fingerprint enrollment...\n"));

        DPFPTemplate template = null;

        try {
            DPFPFingerIndex finger = DPFPFingerIndex.values()[nFinger];
            DPFPFeatureExtraction featureExtractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
            DPFPEnrollment enrollment = DPFPGlobal.getEnrollmentFactory().createEnrollment();

            while (enrollment.getFeaturesNeeded() > 0) {
                DPFPSample sample = getSample(activeReader,
                        String.format("Scan your %s finger (%d remaining)\n", fingerName(finger), enrollment.getFeaturesNeeded()));
                if (sample == null)
                    continue;
                DPFPFeatureSet featureSet;
                try {
                    featureSet = featureExtractor.createFeatureSet(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
                } catch (DPFPImageQualityException e) {
                    websocket.convertAndSend("/topic/showResult", new Result(String.format("Bad image quality: %s. Try again. \n", e.getCaptureFeedback().toString())));
                    continue;
                }
                enrollment.addFeatures(featureSet);
            }
            template = enrollment.getTemplate();
            websocket.convertAndSend("/topic/showResult", new Result(String.format("The %s was enrolled.\n", fingerprintName(finger))));

        } catch (DPFPImageQualityException e) {
            websocket.convertAndSend("/topic/showResult", new Result("Failed to enroll the finger.\n"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return template;
    }

    public Result verify(String activeReader, ListMultimap<String, DPFPTemplate> templates) {
        Result result = new Result();
        try {
            DPFPSample sample = getSample(activeReader, "Scan your finger\n");
            if (sample == null)
                throw new Exception();

            DPFPFeatureExtraction featureExtractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
            DPFPFeatureSet featureSet = featureExtractor.createFeatureSet(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

            DPFPVerification matcher = DPFPGlobal.getVerificationFactory().createVerification();
            matcher.setFARRequested(DPFPVerification.MEDIUM_SECURITY_FAR);

            for (DPFPFingerIndex finger : DPFPFingerIndex.values()) {
                for (Map.Entry entry : templates.entries()) {
                    String key = String.valueOf(entry.getKey());
                    DPFPTemplate template = (DPFPTemplate) entry.getValue();
                    if (template != null) {
                        DPFPVerificationResult verificationResult = matcher.verify(featureSet, template);
                        if (verificationResult.isVerified()) {
                            // System.out.printf("Matching finger: %s, FAR achieved: %g.\n", fingerName(finger), (double) result.getFalseAcceptRate() / DPFPVerification.PROBABILITY_ONE);
                            result.setPatient(key);
                            result.setType("local");
                            return result;
                        }
                    }
                }
            }

            if (UgandaEMRUtils.testInternet("google.com")) {
                websocket.convertAndSend("/topic/showResult", new Result("No facility matching patient searching online"));
                FingerPrintHttpURLConnection fingerPrintHttpURLConnection = new FingerPrintHttpURLConnection();

                Result resultToServer = new Result();
                resultToServer.setFingerprint(sample.serialize());

                Result serverResult = fingerPrintHttpURLConnection.postReal(resultToServer);

                if (serverResult.getPatient() != null) {
                    websocket.convertAndSend("/topic/showResult", new Result("Patient found online"));
                    serverResult.setType("online");
                    return serverResult;
                } else {
                    websocket.convertAndSend("/topic/showResult", new Result("No patient found online"));
                }
            } else {
                websocket.convertAndSend("/topic/showResult", new Result("No internet connectivity, so aborting online search"));
            }

        } catch (Exception e) {
            System.out.printf("Failed to perform verification.");
            return null;
        }
        return result;
    }

    public DPFPSample getSample(String activeReader, String prompt) throws InterruptedException {
        final LinkedBlockingQueue<DPFPSample> samples = new LinkedBlockingQueue<DPFPSample>();

        DPFPCapture capture = DPFPGlobal.getCaptureFactory().createCapture();
        capture.setReaderSerialNumber(activeReader);
        capture.setPriority(DPFPCapturePriority.CAPTURE_PRIORITY_LOW);
        capture.addDataListener(new DPFPDataListener() {
            public void dataAcquired(DPFPDataEvent e) {
                if (e != null && e.getSample() != null) {
                    try {
                        DPFPSample fromDevice = e.getSample();
                        websocket.convertAndSend("/topic/showResult", new Result(encodeToString(fromDevice, "png"), "image"));
                        samples.put(e.getSample());
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        capture.addReaderStatusListener(new DPFPReaderStatusAdapter() {
            int lastStatus = DPFPReaderStatusEvent.READER_CONNECTED;

            public void readerConnected(DPFPReaderStatusEvent e) {
                if (lastStatus != e.getReaderStatus()) {
                    websocket.convertAndSend("/topic/showResult", new Result("Reader is connected"));
                }
                lastStatus = e.getReaderStatus();
            }

            public void readerDisconnected(DPFPReaderStatusEvent e) {
                if (lastStatus != e.getReaderStatus()) {
                    websocket.convertAndSend("/topic/showResult", new Result("Reader is disconnected"));
                }
                lastStatus = e.getReaderStatus();
            }

        });
        try {
            capture.startCapture();
            websocket.convertAndSend("/topic/showResult", new Result(prompt));
            DPFPSample template = samples.take();
            return template;
        } catch (RuntimeException e) {
            System.out.printf("Failed to start capture. Check that reader is not used by another application.\n");
            throw e;
        } finally {
            capture.stopCapture();
        }
    }

    public String fingerName(DPFPFingerIndex finger) {
        return fingerNames.get(finger);
    }

    public String fingerprintName(DPFPFingerIndex finger) {
        return fingerNames.get(finger) + " fingerprint";
    }

    public String encodeToString(DPFPSample sample, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedImage image = (BufferedImage) DPFPGlobal.getSampleConversionFactory().createImage(sample);
        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }

    public byte[] encodeToByte(DPFPSample sample, String type) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedImage image = (BufferedImage) DPFPGlobal.getSampleConversionFactory().createImage(sample);
        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();
            return imageBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
