package utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfExtractorUtil {

    public static String extractText(
            String pdfPath)
            throws Exception {

        PDDocument doc =
                PDDocument.load(
                        new File(pdfPath)
                );

        PDFTextStripper stripper =
                new PDFTextStripper();

        stripper.setStartPage(1);

        stripper.setEndPage(
                doc.getNumberOfPages()
        );

        System.out.println(
                "Pages = " +
                        doc.getNumberOfPages()
        );

        String text =
                stripper.getText(doc);

        doc.close();

        return text;
    }

    public static Map<String,String> extractLabData(String text) {

        Map<String,String> labData = new HashMap<>();

        // Sample regex patterns
        extractValue(text,"Hemoglobin",labData);
        extractValue(text,"HDL",labData);
        extractValue(text,"BP",labData);

        return labData;
    }


    private static void extractValue(
            String text,
            String testName,
            Map<String,String> labData) {

        Pattern pattern = Pattern.compile(
                testName+"\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?(/\\d+)?)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        if(matcher.find()){
            labData.put(testName, matcher.group(1));
        }
    }
}