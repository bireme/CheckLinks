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

import java.io._
import java.nio.charset.Charset
import java.nio.file.{Files, StandardOpenOption}
import scala.collection.mutable._
import scala.io._

/**
 * author: Heitor Barbieri
 * date: 20151103
 */
object Tools {
  def mergeFile(infile: String,
                inoutfile: String,
                charset: Charset): Unit = {
    val lines = Source.fromFile(new File(infile))(charset).getLines()
    val writer = Files.newBufferedWriter(new File(inoutfile).toPath(), charset,
                                         StandardOpenOption.APPEND)
    for (line <- lines) {
      writer.append(line)
      writer.newLine()
    }
    writer.close()
  }

  def stats(infile: String): Unit = {
    val map = HashMap.empty[String, Int]
    val regExp = " *\\| *".r
    val lines = Source.fromFile(new File(infile)).getLines()

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

    for (x <- map) {
      println(x._1 + ": " + x._2)
    }
  }
}
