#!/bin/bash

JAVA=$(which java) 
 
OPT=" -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=commons-logging.properties "
 
LIB=$(echo lib/*.jar | tr ' ' ':')
CPL=bin:$LIB


ENTRYPOINT=tabvalidator.ValidateEntityFromFile

#PARAM=" -n example/tab/lipmuscl.xlsx -t oo -g example/jpa_generated_test2.jar"


PARAM=" -n example/tab/baseanalyseDateIssue.xlsx -t oo -g example/generated_model.jar"

echo $JAVA $OPT -classpath $CPL $ENTRYPOINT $PARAM
$JAVA $OPT -classpath $CPL $ENTRYPOINT $PARAM

