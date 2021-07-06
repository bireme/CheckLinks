/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.cl

import akka.actor.Actor
import akka.actor.ActorRef

/**
 *
 * author Heitor Barbieri
 * date 20151103
 */
class CheckUrlActor(reader: ActorRef,
                    writer: ActorRef) extends Actor {
  val sleepTime: Int = 1 * 60 * 1000 // 1 minute
  val maxBadUrls: Int = 200
  var numOfBadUrls: Int = 0
  val clCore = new CheckLinksCore()

  def receive: Receive = {
    case Start =>
      reader ! AskByUrl
    case url: String =>
      val (isOk, curl) = checkUrl(url)
      writer ! (isOk, curl)
      if (!isOk) numOfBadUrls += 1
      if (numOfBadUrls >= maxBadUrls) {
        println("*")
        Thread.sleep(sleepTime.toLong)
        numOfBadUrls = 0
      }
      reader ! AskByUrl
    case Finish => () // println("CheckUrlActor finishing.")
  }

  def checkUrl(surl: String): (Boolean,String) = {
    parseUrl(surl) match {
      case Some((src, id, url)) =>
//println(s">>>$url")
        val errCode = clCore.checkUrl(url)
        //val errCode = CheckUrl.check(url, true)
        val out_url = src + "|" + id + "|" + url + "|" + errCode
//println(out_url)
        (!CheckUrl.isBroken(errCode), out_url)
      case None => (false, surl + "|invalid string parameter")
    }
  }

  def parseUrl(surl: String): Option[(String,String,String)] = {
    val split = surl.split(" *\\| *")

    if (split.length >= 3) Some((split(0), split(1), split(2)))
    else None
  }
}
