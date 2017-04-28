INTRODUCTION
============

This project aims to help Arnaldo project reviewers to easily test all the application submitted by the students.

INSTALLATION AND BUILDING
=========================

You can fetch the project by executing

    git clone https://github.com/STB1019/tools
	cd arnaldo-root
	mvn install

USAGE INFORMATION
=================

Here's an example on how you can use this project

	cd arnaldo-root/automatic-tester/target
	#show the help of the program
	java -jar automatic-tester-X.Y-SNAPSHOT-jar-with-dependencies.jar --help
	#use the test specified by "--test-case" to test the jar "--jar" containing one class implementing "--interface"
	java -jar automatic-tester-X.Y-SNAPSHOT-jar-with-dependencies.jar --interface it.unibs.ieeesb.arnaldotester.Greeting --test-case it.unibs.ieeesb.arnaldotester.tests.TestGreeting --jar common-interface-impl-example-0.1-SNAPSHOT.jar
	#the output contains a JSON
	#(Use -l ERROR to mute the logging)


OUTPUT JSON DESCRIPTION
=======================

the json contains useful information about how the test went.