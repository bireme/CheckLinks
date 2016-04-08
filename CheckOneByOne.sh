#!/bin/bash

HOME=/home/javaapps/sbt-projects/CheckLinks
SBT_DIR=/home/users/heitor.barbieri/bin
NOW=$(date +"%Y%m%d-%T")

cd $HOME

$SBT_DIR/sbt "run-main org.bireme.murl.MongoCheck mongodb.bireme.br" &> $HOME/logs/$NOW.log

cd -

