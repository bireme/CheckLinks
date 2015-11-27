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

package org.bireme.cl

import java.io.File
import java.nio.charset.{Charset,StandardCharsets}
import java.nio.file.{Files, StandardOpenOption}
import java.util.TreeMap
import scala.collection.JavaConversions._
import scala.collection.mutable._
import scala.io._



/**
 * author: Heitor Barbieri
 * date: 20151109
 */
object Stats extends App {
  def usage() : Unit = {
    Console.err.println("usage Stats <inFile> [<encoding>]")
    System.exit(1)
  }

  def stats(infile: String,
            charset: Charset): Unit = {
    val map = HashMap.empty[String, Int]
    val regExp = " *\\| *".r
    val lines = Source.fromFile(new File(infile))(charset).getLines()

    for (line <- lines) {
      val split = regExp.split(line)
      if (split.length >= 4) {
        val key = split(3)
        map.get(key) match {
          case Some(value) => map.put(key, value+1)
          case None => map.put(key, 1)
        }
      }
    }
    val tmap = new TreeMap[Int,String]()
    for (x <- map) {
      tmap.put(x._2,x._1)
    }
    for (y <- tmap.descendingMap()) {
      println("[" + y._2 + "] => " + y._1)
    }
  }

  if (args.length < 1) usage()
  val charset = if (args.length == 1) StandardCharsets.UTF_8
                else  Charset.forName(args(1))
  stats(args(0), charset)
}
