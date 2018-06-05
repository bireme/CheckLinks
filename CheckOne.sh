#!/bin/bash

JAVA_HOME=/usr/local/java8   # Chamada via ssh de outra maquina
PATH=$JAVA_HOME/bin:$PATH
CHECK_LINKS_HOME=/home/javaapps/sbt-projects/CheckLinks

if [ "$#" -lt 1 ]; then
  echo "usage: <url> [--allContent]"
  exit 1
fi

java -cp $CHECK_LINKS_HOME/CheckLinks.jar org.bireme.cl.CheckUrl $1 $2
