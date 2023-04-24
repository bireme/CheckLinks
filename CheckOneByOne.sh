#!/bin/bash

JAVA_HOME=/usr/local/java811   # Chamada via ssh de outra maquina
PATH=$JAVA_HOME/bin:$PATH
HOME=/home/javaapps/sbt-projects/CheckLinks
NOW=$(date +"%Y%m%d-%T")

cd $HOME

/usr/loval/sbt/bin/sbt "run-main org.bireme.murl.MongoCheck mongodb.bireme.br" &> $HOME/logs/$NOW.log

cd -

