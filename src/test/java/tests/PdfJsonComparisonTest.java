package tests;

import base.BaseTest;
import constants.ConfigOld;

import io.restassured.response.Response;

import org.testng.annotations.Test;

import utils.ApiClient;
import utils.ComparisonUtil;
import utils.JsonUtilsV2;
import utils.PdfExtractorUtil;

import java.util.Map;

public class PdfJsonComparisonTest extends BaseTest {


    @Test
    public void comparePdfVsJson() throws Exception {

        String pdfPath =
                ConfigOld.getPdfPath(
                        "DharmendraBhambu_organized.pdf"
                );


        // Upload
        String reportId =
                ApiClient.uploadReport(
                        pdfPath
                );


        // Poll final parsed response
        Response response =
                ApiClient.pollFinalReport(
                        reportId
                );


        // Extract text from PDF
        String pdfText =
                PdfExtractorUtil.extractText(
                        pdfPath
                );


        System.out.println(
                "===== PDF EXTRACTED TEXT ====="
        );
        System.out.println(pdfText);


//        // Expected values from PDF
//        Map<String,String> expected =
//                PdfExtractorUtil.extractLabData(
//                        pdfText
//                );


        // Actual parser output
        Map<String,String> actual=
                JsonUtilsV2.extractLabResults(
                        response
                );

        ComparisonUtil.compareWithPdfText(
                actual,
                pdfText
        );

    }

}