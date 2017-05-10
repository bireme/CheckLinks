/*=========================================================================

    Copyright © 2016 BIREME/PAHO/WHO

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

package org.bireme.cl

import java.net.URL
import java.nio.file.{Files,Paths}
import java.nio.charset.Charset

import scala.collection.immutable.TreeMap
import scala.io.Source

/**
 * author: Heitor Barbieri
 * date: 20161220
 */
object CheckOnlyHosts extends App {
  def usage() : Unit = {
    Console.err.println("usage: checkOnlyHosts <inFile> <inEncoding> " +
                        "<outFile> [<outEncoding>]")
    System.exit(1)
  }

  if (args.length < 3) usage()
  val outEncoding = if (args.length > 3) args(3) else "utf-8"

  checkOnlyHosts(args(0), args(1), args(2), outEncoding)

  def checkOnlyHosts(inFile: String,
                     inEncoding: String,
                     outFile: String,
                     outEncoding: String): Unit = {
    val writer = Files.newBufferedWriter(Paths.get(outFile),
                                         Charset.forName(outEncoding))
    getHosts(inFile, inEncoding).foreach {
      case (url,qtt) => if (checkHost(url)) writer.write(url + "|" + qtt + "\n")
    }
    writer.close()
  }

  private def getHosts(infile: String,
                       encoding: String): Map[String,Int] = {
    val regExp = " *\\| *".r
    val src = Source.fromFile(infile, encoding)
    val lines = src.getLines()

    val hmap = lines.foldLeft[Map[String,Int]](TreeMap()) {
      case (map, line) => {
        val split = regExp.split(line)
        if (split.length >= 3) {
          try {
            val url = new URL(split(2))
            val port = url.getPort()
            val key = url.getProtocol() + "://" + url.getHost() +
                    (if ((port == 80) || (port == -1)) "" else ":" + port)
            val value = map.getOrElse(key, 0)
            map + ((key, value + 1))
          } catch {
            case _: Throwable =>
              println("ignoring url:" + split(2))
              map
          }
        } else {
          println("ignoring url:" + line)
          map
        }
      }
    }
    src.close()
    hmap
  }

  private def checkHost(url: String): Boolean = {
    println("+++ " + url)
    CheckUrl.isBroken(CheckUrl.check(url, true))
  }
}
