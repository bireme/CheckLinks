#!/bin/bash

JAVA_HOME=/usr/local/java11   # Chamada via ssh de outra maquina
PATH=$JAVA_HOME/bin:$PATH
CHECK_LINKS_HOME=/home/javaapps/sbt-projects/CheckLinks

if [ "$#" -lt 3 ]; then
  echo "usage: <inFile> <outGoodFile> <outBrokenFile> [<encoding>] [-wait=<minutes>] [--append]"
  exit 1
fi

if [ -f $2 ]; then
  rm $2
fi
if [ -f $3 ]; then
  rm $3
fi

lines=`wc -l $1 | cut -d ' ' -f 1`

echo
echo "-----------------------------------------------------------------------"
echo "CheckLinksApplication - checking $lines links"
#sbt "run-main org.bireme.cl.CheckLinksApplication $1 $2 $3 $5"
java -cp $CHECK_LINKS_HOME/target/scala-2.12/CheckLinks-assembly-1.0.jar org.bireme.cl.CheckLinksApplication $1 $2 $3 $4 $5 $6

