# Parsing Automation Framework

Java + RestAssured + MongoDB framework for
document parsing validation.

## Features
- API validations
- Mongo source validations
- Data-driven regression
- PDF vs JSON comparison
- Allure reporting

## Run Smoke
mvn test -DsuiteXmlFile=testng/smoke.xml

## Run Regression
mvn test -DsuiteXmlFile=testng/regression.xml