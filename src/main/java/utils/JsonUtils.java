package utils;

import io.restassured.response.Response;
import org.bson.Document;

import java.util.List;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
public class JsonUtils {


//    public static String getString(Response response, String path) {
//        return response.jsonPath().getString(path);
//    }
//    public static List<Map<String, Object>> getLabResults(Response response) {
//        return response.jsonPath().getList("report.parsed_data.lab_results");
//    }
    // =============================
    // PHOTO VALIDATION
    // =============================
    public static void validatePhoto(Document parsedData, List<String> warnings) {

        Document photo = (Document) parsedData.get("photo_comparison");

        if (photo != null) {

            Double similarity = photo.getDouble("similarity");

            if (similarity != null) {

                if (similarity < 0.5) {
                    System.out.println("⚠️ Low face similarity: " + similarity);
                    warnings.add("Low similarity: " + similarity);
                } else {
                    System.out.println("Face similarity OK ✅: " + similarity);
                }

            } else {
                System.out.println("⚠️ Similarity missing");
                warnings.add("Similarity missing");
            }

        } else {
            System.out.println("⚠️ Photo comparison missing");
            warnings.add("Photo comparison missing");
        }
    }

    // =============================
    // FRAUD VALIDATION
    // =============================
    public static void validateFraud(Document doc, List<String> warnings) {

        Document duplicate = (Document) doc.get("duplicate_check");

        if (duplicate != null && !duplicate.isEmpty()) {

            Document ecg = (Document) duplicate.get("ecg");

            if (ecg != null) {

                Boolean performed = ecg.getBoolean("performed");

                if (Boolean.TRUE.equals(performed)) {

                    Integer matches = ecg.getInteger("matches_found");

                    if (matches != null && matches > 0) {

                        System.out.println("⚠️ ECG Duplicate detected: " + matches);
                        warnings.add("ECG duplicate: " + matches);

                    } else {
                        System.out.println("No ECG duplicate ✅");
                    }

                } else {
                    System.out.println("⚠️ ECG duplicate not performed");
                    warnings.add("ECG duplicate not performed");
                }
            }

        } else {
            System.out.println("No duplicate data available ✅");
        }
    }

    // =============================
    // HDL VALIDATION
    // =============================
    public static void validateHDL(Document test, List<String> warnings) {

        String testName = test.getString("test_name");

        if (testName != null && testName.toLowerCase().contains("hdl")) {

            String valueStr = test.getString("result");

            if (valueStr != null && !valueStr.isEmpty()) {

                double value = Double.parseDouble(valueStr.replaceAll("[^0-9.]", ""));

                if (value < 40) {
                    System.out.println("⚠️ HDL Low: " + value);
                    warnings.add("HDL Low: " + value);
                } else if (value <= 60) {
                    System.out.println("HDL Normal ✅: " + value);
                } else {
                    System.out.println("HDL Good ✅: " + value);
                }

            } else {
                System.out.println("⚠️ HDL value missing");
                warnings.add("HDL value missing");
            }
        }
    }

    // =============================
    // BP VALIDATION
    // =============================
    public static void validateBP(Document test, List<String> warnings) {

        String testName = test.getString("test_name");

        if (testName != null &&
                (testName.toLowerCase().contains("bp") ||
                        testName.toLowerCase().contains("blood pressure"))) {

            String result = test.getString("result");

            if (result != null && result.contains("/")) {

                String[] parts = result.split("/");

                int sys = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                int dia = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));

                if (sys < 90 || sys > 140 || dia < 60 || dia > 90) {
                    System.out.println("⚠️ Abnormal BP");
                    warnings.add("Abnormal BP: " + sys + "/" + dia);
                }
            }
        }
    }

    // =============================
    // GENERIC ABNORMAL
    // =============================
    public static void checkAbnormal(Document test, List<String> warnings) {

        String status = test.getString("status");

        if (status != null &&
                (status.equalsIgnoreCase("Low") || status.equalsIgnoreCase("High"))) {

            warnings.add("Abnormal: " + test.getString("test_name"));
        }
    }

    // =============================
    // ECG VALIDATION
    // =============================
    public static void validateECG(String examName, List<Document> tests, List<String> warnings) {

        if (examName != null && examName.toLowerCase().contains("ecg")) {

            boolean found = false;

            for (Document t : tests) {

                String name = t.getString("test_name");
                String result = t.getString("result");

                if (name != null && name.toLowerCase().contains("overall")) {

                    found = true;

                    if (result != null && result.toLowerCase().contains("normal")) {
                        System.out.println("ECG Normal ✅");
                    } else {
                        System.out.println("⚠️ ECG abnormal");
                        warnings.add("ECG abnormal");
                    }
                }
            }

            if (!found) {
                warnings.add("ECG overall missing");
            }
        }
    }
}