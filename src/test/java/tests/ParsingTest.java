package tests;

import base.BaseTest;
import constants.ConfigOld;
import io.qameta.allure.*;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.Test;

import utils.ApiClient;
import utils.MongoUtils;
import utils.JsonUtilsV2;

import java.util.*;


@Epic("Parsing Automation")
@Feature("Document Parsing Validation")
public class ParsingTest extends BaseTest {


    // =============================
    // Smoke Test Entry Point
    // =============================
    @Test(
            retryAnalyzer=
                    utils.RetryAnalyzer.class
    )
    @Description(
            "Validate end-to-end document parsing flow"
    )
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Sateesh")
    public void smokeTest(){

        testUploadAPI(
                "DharmendraBhambu_organized.pdf"
        );
        Assert.fail("Force failure");

    }


    // =============================
    // Reusable Core Method
    // =============================

    public void testUploadAPI(String pdfFile){

        List<String> warnings =
                new ArrayList<>();


        // =============================
        // 1 Upload
        // =============================
        String pdfPath =
                ConfigOld.getPdfPath(
                        pdfFile
                );

        System.out.println(
                "\nRunning PDF : "
                        + pdfFile
        );


        String reportId =
                ApiClient.uploadReport(
                        pdfPath
                );


        Assert.assertNotNull(
                reportId,
                "report_id missing ❌"
        );



        // =============================
        // 2 Poll Final Report
        // =============================
        Response finalResponse =
                ApiClient.pollFinalReport(
                        reportId
                );


        Map<String,Object> parsedData;
        Map<String,Object> report;



        // =============================
        // API OR Mongo Fallback
        // =============================
        if(finalResponse!=null){

            System.out.println(
                    "✔ Data from Final API"
            );

            Allure.addAttachment(
                    "Final API Response",
                    finalResponse.asPrettyString()
            );

            parsedData=
                    finalResponse
                            .jsonPath()
                            .getMap(
                                    "report.parsed_data"
                            );


            report=
                    finalResponse
                            .jsonPath()
                            .getMap(
                                    "report"
                            );
        }

        else{

            System.out.println(
                    "⚠ API unavailable, using Mongo fallback"
            );


            org.bson.Document job=
                    MongoUtils
                            .getJobByReportId(
                                    reportId
                            );


            Assert.assertNotNull(
                    job,
                    "Mongo fallback failed ❌"
            );


            parsedData=
                    (Map<String,Object>)
                            job.get(
                                    "parsed_data"
                            );


            report=
                    (Map<String,Object>)
                            job;


            System.out.println(
                    "✔ Data from Mongo Fallback"
            );

        }



        // =============================
        // 3 Summary Checks
        // =============================
        if(parsedData==null || report==null){

            System.out.println(
                    "⚠ Parsed data missing"
            );

            return;
        }


        System.out.println(
                "\n===== FINAL RESPONSE SUMMARY ====="
        );

        System.out.println(
                "Confidence Score : "
                        + report.get(
                        "confidence_score"
                )
        );

        System.out.println(
                "Parsing Time : "
                        + report.get(
                        "parsing_time_seconds"
                )
        );

        System.out.println(
                "Pages : "
                        + report.get(
                        "total_pages"
                )
        );


        Integer score=
                (Integer)report.get(
                        "confidence_score"
                );

        Assert.assertTrue(
                score>=80,
                "Low confidence parsing ❌"
        );



        // =============================
        // 4 Face + Fraud
        // =============================
        JsonUtilsV2.validatePhoto(
                parsedData,
                warnings
        );

        JsonUtilsV2.validateFraud(
                report,
                warnings
        );



        // =============================
        // 5 Lab Results
        // =============================
        List<Map<String,Object>> labResults;

        if(finalResponse!=null){

            labResults=
                    finalResponse
                            .jsonPath()
                            .getList(
                                    "report.parsed_data.lab_results"
                            );
        }

        else{

            labResults=
                    (List<Map<String,Object>>)
                            parsedData.get(
                                    "lab_results"
                            );
        }



        if(labResults==null){

            System.out.println(
                    "⚠ Lab results missing"
            );

            return;
        }



        for(Map<String,Object> exam
                : labResults){

            String examName=
                    String.valueOf(
                            exam.get(
                                    "examination_name"
                            )
                    );


            System.out.println(
                    "\n=== "
                            +examName+
                            " ==="
            );


            List<Map<String,Object>> tests=
                    (List<Map<String,Object>>)
                            exam.get("tests");


            if(tests==null){
                continue;
            }



            JsonUtilsV2.validateECG(
                    examName,
                    tests,
                    warnings
            );



            for(Map<String,Object> test
                    : tests){

                String testName=
                        String.valueOf(
                                test.get(
                                        "test_name"
                                )
                        );


                String result=
                        String.valueOf(
                                test.get(
                                        "result"
                                )
                        );


                System.out.println(
                        "Test : "
                                +testName+
                                " | Value : "
                                +result
                );


                JsonUtilsV2.validateHDL(
                        test,
                        warnings
                );


                JsonUtilsV2.validateBP(
                        test,
                        warnings
                );


                JsonUtilsV2.checkAbnormal(
                        test,
                        warnings
                );
            }
        }



        // =============================
        // Questionnaire Presence
        // =============================
        List<Map<String,Object>> questions;

        if(finalResponse!=null){

            questions=
                    finalResponse
                            .jsonPath()
                            .getList(
                                    "report.parsed_data.medical_history_questions"
                            );
        }

        else{

            questions=
                    (List<Map<String,Object>>)
                            parsedData.get(
                                    "medical_history_questions"
                            );
        }


        if(questions!=null){

            System.out.println(
                    "\nQuestionnaire extracted : "
                            +questions.size()
                            +" questions"
            );
        }



        // =============================
        // Warning Summary
        // =============================
        if(!warnings.isEmpty()){

            System.out.println(
                    "\n⚠ WARNINGS:"
            );


            for(String warning
                    : warnings){

                System.out.println(
                        warning
                );
            }

        }

        else{

            System.out.println(
                    "\n✅ All validations passed"
            );
        }
        if(!warnings.isEmpty()){

            Allure.addAttachment(
                    "Abnormal Findings",
                    String.join(
                            "\n",
                            warnings
                    )
            );

        }
//==============================
        //Summary
// =============================

        System.out.println(
                "\n===== EXECUTION SUMMARY ====="
        );

        System.out.println(
                "PDF : " + pdfFile
        );

        System.out.println(
                "Confidence : " +
                        report.get("confidence_score")
        );
        System.out.println(
                "Pages : "+
                        report.get("total_pages")
        );
        System.out.println(
                "Warnings Count : " +
                        warnings.size()
        );
        String status =
                warnings.isEmpty()
                        ? "PASSED"
                        : "PASSED WITH WARNINGS";
        System.out.println(
                "Status : " +
                        status
        );

        Allure.addAttachment(
                "Execution Summary",
                "PDF: "+pdfFile+
                        "\nWarnings:"+warnings.size()
        );

    }

}