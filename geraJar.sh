JAVA_HOME=/usr/local/java8   # Chamada via ssh de outra maquina
PATH=$JAVA_HOME/bin:$PATH

sbt clean assembly

cp target/scala-2.12/CheckLinks-assembly-1.0.jar ./CheckLinks.jar
