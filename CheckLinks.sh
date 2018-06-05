#!/bin/bash

JAVA_HOME=/usr/local/java8   # Chamada via ssh de outra maquina
PATH=$JAVA_HOME/bin:$PATH
CHECK_LINKS_HOME=/home/javaapps/sbt-projects/CheckLinks

if [ "$#" -lt 4 ]; then
  echo "usage: <inFile> <outGoodFile> <outBrokenFile> <outMongoBrokenFile> [<encoding>]"
  exit 1
fi

if [ -f $2 ]; then
  rm $2
fi
if [ -f $3 ]; then
  rm $3
fi
if [ -f $4 ]; then
  rm $4
fi

lines=`wc -l $1 | cut -d ' ' -f 1`

echo
echo "-----------------------------------------------------------------------"
echo "CheckLinksApplication - checking $lines links"
#sbt "run-main org.bireme.cl.CheckLinksApplication $1 $2 $3 $5"
java -cp $CHECK_LINKS_HOME/CheckLinks.jar org.bireme.cl.CheckLinksApplication $1 $2 $3 $5

echo
echo "Checking if broken urls were broken some time ago ..."
#sbt "run-main org.bireme.murl.MongoUrlApp mongodb.bireme.br $3 $4 $5"
java -cp $CHECK_LINKS_HOME/CheckLinks.jar org.bireme.murl.MongoUrlApp mongodb.bireme.br $3 $4 $5
