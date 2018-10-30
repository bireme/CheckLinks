/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.murl

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Files, StandardOpenOption}
import scala.io.Source

/**
 * author: Heitor Barbieri
 * date: 20151028
 */
object MongoUrlApp extends App {
  def usage() : Unit = {
      Console.err.println(
                     "usage: MongoUrlApp <host> <inFile> <outFile> <encoding>")
      System.exit(1)
  }

  if (args.length != 4) usage()

  val host = args(0)    //"localhost"
  val inFile = args(1)  //"teste.txt"
  val outFile = args(2) //"testeout.txt"
  val charset = args(3) //"UTF-8"

  val mongo = new MongoUrl(host, minDiffDays=0)
  val writer =
    Files.newBufferedWriter(new File(outFile).toPath(),
                            Charset.forName(charset), StandardOpenOption.CREATE)
  val src = Source.fromFile(inFile, charset)
  val lines = src.getLines()

  println("Saving urls into Mongodb collection")
  lines.foreach(line => if (mongo.addUrl(line)) writer.append(line + "\n"))

  src.close()
  writer.close()
}
