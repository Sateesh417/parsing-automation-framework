package utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer
        implements IRetryAnalyzer {

    private int count=0;
    private static final int maxTry=2;

    @Override
    public boolean retry(ITestResult result){

        Throwable error = result.getThrowable();

        if(error != null){

            String message = error.getMessage();

            if(message != null && (
                    message.contains("404") ||
                            message.contains("403") ||
                            message.contains("Timeout")
            )){
                if(count < maxTry){
                    count++;
                    System.out.println(
                            "Retrying due to API issue: attempt " + count
                    );
                    return true;
                }
            }
        }

        return false;
    }

}