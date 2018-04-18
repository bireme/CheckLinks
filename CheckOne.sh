#!/bin/bash

#CHECK_LINKS_HOME=/home/javaapps/sbt-projects/CheckLinks
CHECK_LINKS_HOME=/home/heitor/sbt-projects/CheckLinks

if [ "$#" -lt 1 ]; then
  echo "usage: <url> [--allContent]"
  exit 1
fi

java -cp $CHECK_LINKS_HOME/CheckLinks.jar org.bireme.cl.CheckUrl $1 $2
