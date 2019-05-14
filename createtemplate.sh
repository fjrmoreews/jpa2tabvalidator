#!/bin/bash

JAVA=$(which java) 
 
OPT=" -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=commons-logging.properties "
 
LIB=$(echo lib/*.jar | tr ' ' ':')
CPL=bin:$LIB
ENTRYPOINT=tabtemplatecreator.CreateTemplateFromJPA

#PARAM=" -o example/template -t oo -g example/CreateTemplate_generator.jar"

#PARAM=" -o example/template -t oo -g example/model.gen.test3.jar"

#PARAM=" -o example/template_inra_dev_test -t oo -g example/model.gen.test4.jar"
PARAM=" -o example/template_pegase_v1 -t oo -g example/model.gen.pegase_v1.jar"

echo $JAVA $OPT -classpath $CPL $ENTRYPOINT $PARAM
$JAVA $OPT -classpath $CPL $ENTRYPOINT $PARAM
