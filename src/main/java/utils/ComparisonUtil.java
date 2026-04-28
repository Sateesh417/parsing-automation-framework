package utils;

import java.util.Map;

public class ComparisonUtil {

    public static void compareWithPdfText(
            Map<String,String> actual,
            String pdfText){

        int pass=0;
        int fail=0;


        System.out.println(
                "\n===== PDF vs JSON VALIDATION ====="
        );


        for(Map.Entry<String,String> entry
                : actual.entrySet()){

            String testName=
                    entry.getKey();

            String value=
                    entry.getValue();


            if(value==null){
                continue;
            }


            String combined=
                    (testName+" "+value)
                            .toLowerCase();

            String pdf=
                    pdfText.toLowerCase();


            if(pdf.contains(
                    value.toLowerCase())){

                System.out.println(
                        "✔ Match : "
                                +testName+
                                " = "+
                                value
                );

                pass++;
            }
            else{

                System.out.println(
                        "❌ Missing : "
                                +testName+
                                " = "+
                                value
                );

                fail++;
            }

        }


        System.out.println(
                "\nPass : "+pass
        );

        System.out.println(
                "Fail : "+fail
        );

    }

}