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

package org.bireme.murl

import com.mongodb.casbah.Imports._
import java.io.File
import java.util.{Calendar, Date, GregorianCalendar}
import java.nio.charset.Charset
import java.nio.file.{Files, StandardOpenOption}
import scala.io.Source

/**
 * Export to a file all last twiced checked broken urls from MongoDb to a file.
 * author: Heitor Barbieri
 * date: 20160712
 */
object MongoLastBrokenApp extends App {
  def usage() : Unit = {
      Console.err.println(
        "usage: MongoLastBrokenApp <host> <dbname> <outFile> [<encoding>]")
      System.exit(1)
  }

  def getLastUpdated(coll: MongoCollection): Date = {
    val max: Any = coll.distinct("updated").reduce[Any] {
      case (e1,e2) => {
        e1 match {
          case d1:Date => {
            e2 match {
              case d2:Date => if (d1 after d2) d1 else d2
              case _ => throw new ClassCastException
            }
          }
          case _ => throw new ClassCastException
        }
      }
    }
    max.asInstanceOf[Date]
  }

  def getSameDateObjects(coll: MongoCollection,
                         mst: String,
                         date: Date): Iterator[DBObject] = {
    val calendar = new GregorianCalendar()
    calendar.setTime(date)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)

    val date2:Date = calendar.getTime()
    val gte = MongoDBObject("$gte" -> date2)
    val query = MongoDBObject("mst" -> mst,
                              "updated" -> gte)
    coll.find(query)
  }

  if (args.length < 3) usage()

  val host = args(0)    //"localhost"
  val port = 27017
  val mst = args(1)
  val outFile = args(2) //"testeout.txt"
  val charset = if (args.length > 3) args(3) else "UTF-8"
  val writer =
    Files.newBufferedWriter(new File(outFile).toPath(),
                            Charset.forName(charset),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING)
  val mongoClient = MongoClient(host, port)
  val db = mongoClient("SocialCheckLinks")
  val coll = db("BrokenLinks")

  getSameDateObjects(coll, mst, getLastUpdated(coll)).foreach {
    obj => {
      val sid = obj.get("_id").asInstanceOf[String]
      val idx = sid.indexOf("_")
      val id = if (idx == -1) sid else sid.substring(0, idx)
      val line = id + "|" + obj.get("pburl") + "|" +
                 obj.get("msg") + "|" + obj.get("mst")
      writer.write(line)
      writer.newLine()
    }
  }

  writer.close()
}
