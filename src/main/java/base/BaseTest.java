package base;

import constants.Config;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

public class BaseTest {

    @BeforeClass
    public void setup(){

        RestAssured.baseURI=
                Config.getBaseUrl();

        System.out.println(
                "Running ENV: "+
                        Config.ENV
        );

        System.out.println(
                "Base URL: "+
                        RestAssured.baseURI
        );
    }
}