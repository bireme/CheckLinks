/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

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
