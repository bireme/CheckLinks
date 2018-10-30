/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.murl

import com.mongodb.casbah.Imports._
import java.util.{Calendar,Date}

/**
 * author: Heitor Barbieri
 * date: 20151026
 */
class MongoUrl(host: String,
               port: Int = 27017,
               minDiffDays: Int = 7,
               maxDiffDays: Int = 60) {

  val mongoClient = MongoClient(host, port)
  val db = mongoClient("SocialCheckLinks")
  val coll = db("HistoryBrokenLinks")
  val today = new Date()
  val calendar = Calendar.getInstance()
  val min = {
    calendar.setTime(today)
    calendar.add(Calendar.DAY_OF_YEAR, -minDiffDays)
    calendar.getTime()
  }
  val max = {
    calendar.setTime(today)
    calendar.add(Calendar.DAY_OF_YEAR, -maxDiffDays)
    calendar.getTime()
  }

  def addUrl(line: String): Boolean = {
    val split = line.split(" *\\| *", 4)

    if (split.length == 4) {
      val _id = MongoDBObject("db" -> split(0),
                              "id" -> split(1),
                              "url" -> split(2))
      val elem = MongoDBObject("date" -> today,
                               "errCode" -> split(3).toInt)

      coll.findOne(MongoDBObject("_id" -> _id)) match {
        case None => {
          val doc = MongoDBObject("_id" -> _id, "broken" -> MongoDBList(elem))
          coll.insert(doc)
          false
        }
        case Some(doc: BasicDBObject) => {
          val broken =
                  doc.getAsOrElse[MongoDBList]("broken", MongoDBList()) += elem
          coll.update(MongoDBObject("_id" -> _id),
                                             MongoDBObject("broken" -> broken))
          twiceChecked(broken)
        }
        case other => {
          println("ERROR: found unexpected case class " + other.getClass)
          false
        }
      }
    } else {
      println("ERROR: invalid line format - " + line)
      false
    }
  }

  def twiceChecked(broken: MongoDBList): Boolean = {
    if (broken.size < 2) false
    else {
      broken.last match {
        case doc: BasicDBObject => {
          doc.getOrElse("date", today) match {
            /*case lastDate: Date => (lastDate.after(max)) &&
                                   (lastDate.before(min))*/
            case lastDate: Date => (lastDate.after(max)) &&
                                                (lastDate.compareTo(min) <= 0)

          }
        }
      }
    }
  }
}
