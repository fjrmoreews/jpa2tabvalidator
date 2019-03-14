#!/bin/bash

JAVA=$(which java) 
 
OPT=" -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=commons-logging.properties "
 
LIB=$(echo ./lib/*.jar | tr ' ' ':')

CPL=bin:$LIB


ENTRYPOINT=tabvalidator.ValidateEntityFromFile

#PARAM=" -n example/tab/lipmuscl.xlsx -t oo -g example/jpa_generated_test2.jar"

#PARAM=" -n example/tab/baseanalyseDateIssue.xlsx -t oo -g example/generated_model.jar"

PARAM=" -n example/tab/classa.xlsx -t oo -g example/model.gen.test1.jar"

echo $JAVA $OPT -classpath $CPL $ENTRYPOINT $PARAM
$JAVA $OPT -classpath $CPL $ENTRYPOINT $PARAM


if [ $? -eq 0 ]
then
  echo "Successfully model validation"
  exit 0
else
  echo "error model validation" >&2
  exit 1
fi

