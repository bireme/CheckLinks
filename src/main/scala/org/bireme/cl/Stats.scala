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

import scala.collection.immutable.{HashMap, TreeMap}
import scala.io.Source

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
            encoding: String): Unit = {
    val regExp = " *\\| *".r
    val src = Source.fromFile(infile, encoding)
    val lines = src.getLines()

    val hmap = lines.foldLeft[Map[String,Int]](HashMap()) {
      case (map, line) => {
        val split = regExp.split(line)
        if (split.length >= 4) {
          val key = split(3)
          val value = map.getOrElse(key, 0)
          map + ((key, value + 1))
        } else map
      }
    }
    src.close()

    val tmap = hmap.foldLeft[Map[Int,String]](TreeMap()(Ordering[Int]
                                                                    .reverse)) {
      case (map,(k,v)) => map + ((v, k))
    }
    tmap.foreach { case (k,v) => println("[" + v + "] => " + k)}
  }

  if (args.length < 1) usage()
  val encoding = if (args.length == 1) "UTF-8" else args(1)

  stats(args(0), encoding)
}
