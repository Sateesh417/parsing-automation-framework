package base;

import constants.ConfigOld;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

public class BaseTest {

    @BeforeClass
    public void setup(){

        RestAssured.baseURI=
                ConfigOld.getBaseUrl();

        System.out.println(
                "Running ENV: "+
                        ConfigOld.ENV
        );

        System.out.println(
                "Base URL: "+
                        RestAssured.baseURI
        );
    }
}