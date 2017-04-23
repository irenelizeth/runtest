# Description
Runs individual Java tests from list of tests created with a minimization test suite strategy

# compile
javac -cp /usr/share/java/junit4.jar MinTestSuite.java 

# execute
java -cp ./:lib/*:path_to_junit4_library MinTestSuite path_subject_application_jar path_to_test_cases_txt_file

## Example execute:
java -cp ./:/lib/*:/usr/share/java/junit4.jar MinTestSuite ../res/barbecue-1.0.0_variation.jar ../res/testcases/barbecue.testcases.min.txt
