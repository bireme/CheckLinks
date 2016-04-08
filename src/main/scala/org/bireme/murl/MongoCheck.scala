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

import org.bireme.cl.CheckUrl
import com.mongodb.casbah.Imports._
import java.io.File
import scala.io._

/**
 * Removes from SocialCheckLinks database / CheckUrl collection all
 * documents whose urls are not broken.
 * author: Heitor Barbieri
 * date: 20160405
 */
object MongoCheck extends App {
  private def usage() : Unit = {
      Console.err.println("usage: MongoUrlApp <host> [-port=<port>]")
      System.exit(1)
  }

  private def isBrokenDocument(doc: MongoDBObject) : Boolean = {
    doc.get("burl") match {
      case Some(url: String) => CheckUrl.isBroken(CheckUrl.check(url, false))
      case _ => true
    }
  }

  private def delNotBrokenDocument(col: MongoCollection,
                                   doc: MongoDBObject) : Boolean = {
    if (isBrokenDocument(doc)) false
    else {
      coll.remove(doc)
      //println(doc)
      true
    }
  }

  if (args.length < 1) usage()

  val host = args(0)    //"localhost"
  val port = if (args.length == 1) 27017
             else Integer.parseInt(args(1).substring(6))
  val mongoClient = MongoClient(host, port)
  val db = mongoClient("SocialCheckLinks")
  val coll = db("BrokenLinks")

  println("urls=" + coll.count())
  val allDocs = coll.find()
  var curDoc = 0
  var totalRemoved = 0

  for (doc <- allDocs) {
    if (delNotBrokenDocument(coll, doc)) totalRemoved += 1
    curDoc += 1
    if (curDoc % 10 == 0) println("+++" + curDoc + " / " + totalRemoved)
    //println(doc)
  }

  println("Total removed docs: " + totalRemoved)

  mongoClient.close()
}
