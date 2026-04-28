package utils;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtilsV2 {

    // =============================
    // FACE VALIDATION
    // =============================
    public static void validatePhoto(Map<String, Object> parsedData, List<String> warnings) {

        if (parsedData == null) {
            System.out.println("⚠️ Parsed data missing");
            return;
        }

        Map<String, Object> photo =
                (Map<String, Object>) parsedData.get("photo_comparison");

        if (photo == null) {
            System.out.println("⚠️ Face comparison not processed");
            return;
        }

        Object simObj = photo.get("similarity");

        if (simObj == null) {
            System.out.println("⚠️ Similarity missing");
            warnings.add("Similarity missing");
            return;
        }

        double similarity = Double.parseDouble(simObj.toString());

        System.out.println("Face similarity: " + similarity);

        if (similarity < 0.5) {
            warnings.add("Low face similarity: " + similarity);
        }
    }


    public static void validateFraud(
            Map<String, Object> report,
            List<String> warnings) {

        if (report == null) {
            System.out.println("⚠ Report missing");
            return;
        }

        Map<String,Object> duplicate =
                (Map<String,Object>)
                        report.get("duplicate_check");


        // =========================
        // Case 1 Empty Object
        // =========================
        if(duplicate==null || duplicate.isEmpty()){

            System.out.println(
                    "ℹ Duplicate check not processed"
            );

            return;
        }


        // =========================
        // ECG Check
        // =========================
        validateDuplicateBlock(
                duplicate,
                "ecg"
        );


        // =========================
        // TMT Check
        // =========================
        validateDuplicateBlock(
                duplicate,
                "tmt"
        );

    }



    private static void validateDuplicateBlock(
            Map<String,Object> duplicate,
            String blockName){

        Map<String,Object> block =
                (Map<String,Object>)
                        duplicate.get(blockName);


        if(block==null){
            return;
        }


        Boolean performed =
                (Boolean) block.get("performed");


        // =========================
        // performed=true
        // =========================
        if(Boolean.TRUE.equals(performed)){

            Object matches =
                    block.get("matches_found");

            System.out.println(
                    "✔ "+
                            blockName.toUpperCase()+
                            " duplicate check performed"
            );

            System.out.println(
                    "Matches found: "+
                            matches
            );
        }


        // =========================
        // performed=false
        // =========================
        else{

            System.out.println(
                    "✔ "+
                            blockName.toUpperCase()+
                            " duplicate check performed"
            );

            System.out.println(
                    "No duplicates found"
            );
        }

    }
    // =============================
    // ECG
    // =============================
    public static void validateECG(String examName,
                                   List<Map<String, Object>> tests,
                                   List<String> warnings) {

        if (examName == null || !examName.toLowerCase().contains("ecg")) return;

        System.out.println("ECG Exam Detected");

        boolean abnormal = false;

        for (Map<String, Object> test : tests) {

            String result = String.valueOf(test.get("result"));

            if (result != null && result.toLowerCase().contains("abnormal")) {
                abnormal = true;
                warnings.add("ECG abnormal");
            }
        }

        if (!abnormal) {
            System.out.println("ECG Normal ✅");
        }
    }

    // =============================
    // HDL
    // =============================
    public static void validateHDL(
            Map<String,Object> test,
            List<String> warnings){

        String testName=
                String.valueOf(
                        test.get("test_name")
                );


        if(!testName.equalsIgnoreCase(
                "HDL Cholesterol"
        )) {
            return;
        }


        double hdl=
                Double.parseDouble(
                        String.valueOf(
                                test.get("result")
                        )
                );


        if(hdl<40){
            warnings.add(
                    "HDL Low: "+hdl
            );
        }
        else{
            System.out.println(
                    "HDL Normal ✅: "+hdl
            );
        }

    }

    // =============================
    // BP
    // =============================
    public static void validateBP(Map<String, Object> test, List<String> warnings) {

        String name = String.valueOf(test.get("test_name"));

        if (name.toLowerCase().contains("blood pressure") || name.toLowerCase().contains("bp")) {

            String result = String.valueOf(test.get("result"));

            if (result.contains("/")) {
                System.out.println("BP Reading: " + result);
            }
        }
    }

    // =============================
    // ABNORMAL
    // =============================
    public static void checkAbnormal(Map<String, Object> test, List<String> warnings) {

        String status = String.valueOf(test.get("status"));

        if ("High".equalsIgnoreCase(status) || "Low".equalsIgnoreCase(status)) {

            String name = String.valueOf(test.get("test_name"));
            warnings.add("Abnormal: " + name);
        }
    }


    public static Map<String,String>
    extractLabResults(Response response){

        Map<String,String> actual=
                new HashMap<>();


        List<Map<String,Object>> labs=
                response.jsonPath()
                        .getList(
                                "report.parsed_data.lab_results"
                        );


        if(labs==null){
            return actual;
        }


        for(Map<String,Object> exam: labs){

            List<Map<String,Object>> tests=
                    (List<Map<String,Object>>)
                            exam.get("tests");

            if(tests==null) continue;


            for(Map<String,Object> test: tests){

                String name=
                        String.valueOf(
                                test.get("test_name")
                        );

                String result=
                        String.valueOf(
                                test.get("result")
                        );

                actual.put(
                        name,
                        result
                );
            }
        }

        return actual;
    }
}