/*=========================================================================

    Copyright © 2015 BIREME/PAHO/WHO

    This file is part of Check Links.

    Check Links is free software: you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 2.1 of
    the License, or (at your option) any later version.

    Check Links is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Check Links. If not, see
    <http://www.gnu.org/licenses/>.

=========================================================================*/

package org.bireme.murl

import com.mongodb.casbah.Imports._
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
