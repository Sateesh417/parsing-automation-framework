package tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ParsingRegressionTest {
       // extends ParsingTest {


    @DataProvider(name="pdfFiles")
    public Object[][] pdfFiles(){

        return new Object[][]{

                {"DharmendraBhambu_organized.pdf"},
                {"ManishBishla_organized.pdf"}

        };
    }
    ParsingTest parser =
            new ParsingTest();

    @Test(dataProvider="pdfFiles")
    @Description(
            "Run parsing regression for multiple PDFs"
    )
    public void regressionRun(
            String pdfFile){

        System.out.println(
                "\nRunning PDF : "+
                        pdfFile
        );

        Allure.label(
                "document",
                pdfFile
        );

        Allure.addAttachment(
                "Current PDF",
                pdfFile
        );
        //testUploadAPI(pdfFile);
        parser.testUploadAPI(
                pdfFile
        );

    }

}