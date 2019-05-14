#!/bin/bash

JAVA=$(which java) 
 
OPT=" -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=commons-logging.properties "
 
LIB=$(echo lib/*.jar | tr ' ' ':')
CPL=bin:$LIB
ENTRYPOINT=tabtemplatecreator.CreateTemplateFromJPA

PARAM=" -o example/template -t oo -g example/jpa_generated_test1.jar"

echo $JAVA $OPT -classpath $CPL $ENTRYPOINT $PARAM
$JAVA $OPT -classpath $CPL $ENTRYPOINT $PARAM