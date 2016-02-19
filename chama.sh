sbt "run-main org.bireme.cl.CheckLinksApplication urls.txt urls-out-good.txt urls-out-broken.txt ISO-8859-1"

#nohup scala -classpath /home/heitor/sbt-projects/CheckLinks/target/scala-2.11/CheckLinks-assembly-1.0.jar org.bireme.cl.Stats broken.tmp ISO-8859-1 &
