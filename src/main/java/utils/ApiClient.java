package utils;

import constants.ConfigOld;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.io.File;

import static io.restassured.RestAssured.*;

public class ApiClient {

    @Step("Upload PDF to Parsing Engine")
    public static String uploadReport(String pdfPath) {


        Response response =
                given()
                        .baseUri(ConfigOld.getBaseUrl())
                        .header("X-API-Key",
                                ConfigOld.getApiKey())
                        .contentType("multipart/form-data")
                        .multiPart(
                                "file",
                                new File(pdfPath),
                                "application/pdf"
                        )
                        .when()
                        .post(
                                ConfigOld.getUploadUrl(
                                        ConfigOld.getTenantId(),
                                        ConfigOld.getProjectId()
                                )
                        );

        response.then().statusCode(200);

        String reportId =
                response.jsonPath().getString("report_id");

        String jobId =
                response.jsonPath().getString("job_id");

        String status =
                response.jsonPath().getString("status");

        System.out.println("\n===== UPLOAD RESPONSE =====");
        System.out.println(
                "JobId     : "+jobId
        );
        System.out.println(
                "ReportId  : "+reportId
        );
        System.out.println(
                "Status    : "+status
        );
        System.out.println(
                "Message   : Upload successful"
        );

//        String reportId=
//                response.jsonPath()
//                        .getString("report_id");

        return reportId;
    }





    @Step("Poll parser until status completed")
    public static Response pollFinalReport(
                String reportId){

            Response response=null;

            for(int i=1;i<=30;i++){

                response=
                        given()
                                .baseUri(ConfigOld.getBaseUrl())
                                .header(
                                        "x-api-key",
                                        ConfigOld.getApiKey()
                                )
                                .when()
                                .get(
                                        ConfigOld.getFinalUrl(
                                                ConfigOld.getTenantId(),
                                                reportId
                                        )
                                );
//                System.out.println(
//                        "Polling Attempt : "+i
//                );


                // optional full response debug
//                System.out.println(
//                        response.asPrettyString()
//                );
                if(response.statusCode()==200){

                    String status=
                            response.jsonPath()
                                    .getString("report.status");

                    System.out.println(
                            "Attempt "+i+
                                    " status="+status
                    );

                    if("completed"
                            .equalsIgnoreCase(status)){
                        return response;
                    }
                }

                try{
                    Thread.sleep(10000);
                }catch(Exception e){}
            }

//            throw new RuntimeException(
//                    "Timeout"
//            );
            System.out.println(
                    "API timeout. Trying Mongo fallback..."
            );

            return null;
        }

    }
