/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.murl

import org.bireme.cl.CheckUrl
import com.mongodb.casbah.Imports._

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

  private def delNotBrokenDocument(doc: MongoDBObject) : Boolean = {
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
  val allDocs = coll.find().toList
  var curDoc = 0
  var totalRemoved = 0

  for (doc <- allDocs) {
    if (delNotBrokenDocument(doc)) totalRemoved += 1
    curDoc += 1
    if (curDoc % 10 == 0) println("+++" + curDoc + " / " + totalRemoved)
    //println(doc)
  }

  println("Total removed docs: " + totalRemoved)

  mongoClient.close()
}
