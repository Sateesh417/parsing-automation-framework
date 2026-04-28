package tests;

import base.BaseTest;
import constants.Config;
import io.restassured.response.Response;
import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.JsonUtils;
import utils.MongoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class ParsingDB extends BaseTest {

    String tenantId = Config.getTenantId();
    String projectId = Config.getProjectId();

    @Test
    public void testFullFlow() throws Exception {

        File file = new File(System.getProperty("user.dir") +
                "/src/test/resources/files/DharmendraBhambu_organized.pdf");

        Assert.assertTrue(file.exists(), "File not found ❌");

        // =============================
        // 1. UPLOAD API
        // =============================
        Response uploadResponse = given()
                .header("X-API-Key", Config.getApiKey())
                .contentType("multipart/form-data")
                .queryParam("webhook_url", Config.WEBHOOK_URL)
                .multiPart("file", file, "application/pdf")
                .when()
                .post(Config.getApiPrefix() + "/tenants/" + tenantId + "/projects/" + projectId + "/reports");

        uploadResponse.then().statusCode(200);

        System.out.println("===== UPLOAD RESPONSE =====");
        System.out.println(uploadResponse.asPrettyString());

        String reportId = uploadResponse.jsonPath().getString("report_id");

        // =============================
        // 2. MONGO POLLING
        // =============================
        Document doc = null;

        for (int i = 0; i < 30; i++) {

            doc = MongoUtils.getJobByReportId(reportId);

            if (doc != null) {

                String status = doc.getString("status");
                System.out.println("Mongo Status: " + status);

                if ("completed".equalsIgnoreCase(status)
                        && doc.get("parsed_data") != null) {

                    System.out.println("✅ Parsing completed");
                    break;
                }

                if ("failed".equalsIgnoreCase(status)) {
                    throw new RuntimeException("Parsing failed ❌");
                }
            }

            Thread.sleep(10000);
        }

        Assert.assertNotNull(doc, "Mongo doc missing ❌");

        System.out.println("===== FULL MONGO JSON =====");
        System.out.println(doc.toJson());

        Document parsedData = doc.get("parsed_data", Document.class);
        Assert.assertNotNull(parsedData, "parsed_data missing ❌");

        System.out.println("===== PARSED DATA =====");
        System.out.println(parsedData.toJson());

        List<String> warnings = new ArrayList<>();

        // =============================
        // 3. BASIC VALIDATION
        // =============================
        String patientName = parsedData.getString("patient_name");
        Assert.assertNotNull(patientName);

        // =============================
        // 4. FACE VALIDATION
        // =============================
        String faceStatus = doc.getString("face_comparison_status");

        System.out.println("Face Status: " + faceStatus);

        if ("SUCCESS".equalsIgnoreCase(faceStatus)) {
            JsonUtils.validatePhoto(parsedData, warnings);
        } else {
            System.out.println("⚠️ Face comparison not processed");
        }

        // =============================
        // 5. FRAUD / DUPLICATE
        // =============================
        JsonUtils.validateFraud(doc, warnings);

        // =============================
        // 6. LAB RESULTS
        // =============================
        List<Document> labResults = parsedData.getList("lab_results", Document.class);

        if (labResults != null) {

            for (Document exam : labResults) {

                String examName = exam.getString("examination_name");
                System.out.println("=== " + examName + " ===");

                List<Document> tests = exam.getList("tests", Document.class);

                if (tests == null) continue;

                JsonUtils.validateECG(examName, tests, warnings);

                for (Document test : tests) {

                    String testName = test.getString("test_name");
                    String result = test.getString("result");

                    System.out.println("Test: " + testName + " | Value: " + result);

                    JsonUtils.validateHDL(test, warnings);
                    JsonUtils.validateBP(test, warnings);
                    JsonUtils.checkAbnormal(test, warnings);
                }
            }
        }

        // =============================
        // 7. WEBHOOK CHECK
        // =============================
        Document webhook = doc.get("webhook_meta", Document.class);

        boolean delivered = webhook != null && webhook.getBoolean("delivered", false);

        System.out.println("Webhook Delivered: " + delivered);

        // =============================
        // 8. FINAL API
        // =============================
        if (delivered) {

            Response finalResponse = null;

            for (int i = 0; i < 10; i++) {

                finalResponse = given()
                        .header("X-API-Key", Config.getApiKey())
                        .when()
                        .get(Config.getApiPrefix() + "/tenants/" + tenantId + "/reports/" + reportId);

                if (finalResponse.getStatusCode() == 200) break;

                Thread.sleep(3000);
            }

            Assert.assertEquals(finalResponse.getStatusCode(), 200);

            System.out.println("===== FINAL API RESPONSE =====");
            System.out.println(finalResponse.asPrettyString());

            finalResponse.then()
                    .body(matchesJsonSchemaInClasspath("schema/report-schema.json"));

        } else {
            warnings.add("Webhook not delivered");
        }

        // =============================
        // 9. WARNINGS
        // =============================
        if (!warnings.isEmpty()) {
            System.out.println("⚠️ WARNINGS: " + warnings);
        }
    }
}