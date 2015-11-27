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

import akka.actor.{ Actor, ActorRef, ActorSystem, Props, PoisonPill }
import java.io.File
import scala.io._

/**
 *
 * author Heitor Barbieri
 * date 20151103
 */
class ReadUrlActor(file: File,
                   codec: Codec,
                   writeUrl: ActorRef,
                   numberOfCheckers: Int) extends Actor {
  //val lines = Source.fromFile(file)(codec).getLines()
  val reader = new GetFileLines(file, codec.charSet)
  val children = createCheckers()
  var ref : ActorRef = _
  var finished = 0

  def receive: Receive = {
    case Start => {
      ref = sender()
      children.map(child => child ! Start)
    }
    case AskByUrl => getPipedLine() match {
      case Some(line) => sender ! line
      case None => {
        sender ! Finish
        finished += 1
        if (finished == numberOfCheckers) writeUrl ! Finish
      }
    }
    case Finish => {
      //println("ReadUrlActor is finishing")
      reader.close()
      ref ! Finish   // Advise CheckLinksApplication to finish
    }
  }

  def createCheckers() : List[ActorRef] = {
    def create(lst: List[ActorRef],
               idx: Int): List[ActorRef] = {
      if (idx < numberOfCheckers)
        create(lst :+
               context.actorOf(Props(new CheckUrlActor(self, writeUrl)),
               "child" + idx), idx + 1)

      else lst
    }
    create(List.empty[ActorRef], 0)
  }

  def getPipedLine(): Option[String] = {
    try {
      reader.getLine() match {
        case Some(line) =>  if (line.isEmpty() || (line.charAt(0) == '#'))
                                  getPipedLine()
                            else Some(line)
        case None => None
      }
    } catch {
      case ex: Exception => {
        println("[ERROR] bad input line => " + ex.toString())
        //ex.printStackTrace()
        //getPipedLine()
        None
      }
    }
  }

  def killCheckers() = {
  }
}
