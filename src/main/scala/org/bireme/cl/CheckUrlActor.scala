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

import akka.actor.Actor
import akka.actor.ActorRef

/**
 *
 * author Heitor Barbieri
 * date 20151103
 */
class CheckUrlActor(reader: ActorRef,
                    writer: ActorRef) extends Actor {
  val sleepTime = 1 * 60 * 1000 // 1 minute
  val maxBadUrls = 200
  var numOfBadUrls = 0

  def receive: Receive = {
    case Start => {
      reader ! AskByUrl
    }
    case url: String => {
      val (isOk, curl) = checkUrl(url)
      writer ! (isOk, curl)
      if (!isOk) numOfBadUrls += 1
      if (numOfBadUrls >= maxBadUrls) {
        println("*")
        Thread.sleep(sleepTime.toLong)
        numOfBadUrls = 0
      }
      reader ! AskByUrl
    }
    case Finish => () // println("CheckUrlActor finishing.")
  }

  def checkUrl(surl: String): (Boolean,String) = {
    parseUrl(surl) match {
      case Some((src, id, url)) => {
        val errCode = CheckUrl.check(url, true)
        val out_url = src + "|" + id + "|" + url + "|" + errCode
        (!CheckUrl.isBroken(errCode), out_url)
      }
      case None => (false, surl + "|invalid string parameter")
    }
  }

  def parseUrl(surl: String): Option[(String,String,String)] = {
    val split = surl.split(" *\\| *")

    if (split.length >= 3) Some((split(0), split(1), split(2)))
    else None
  }
}
