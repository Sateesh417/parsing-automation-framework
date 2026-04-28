![CI](https://github.com/Sateesh417/parsing-automation-framework/actions/workflows/main.yml/badge.svg)

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
