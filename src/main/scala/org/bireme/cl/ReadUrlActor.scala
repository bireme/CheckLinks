/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.cl

import akka.actor.{ Actor, ActorRef, Props }
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
      reader.getLine().flatMap { line =>
        if (line.isEmpty() || (line.charAt(0) == '#')) getPipedLine()
        else Some(line)
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
