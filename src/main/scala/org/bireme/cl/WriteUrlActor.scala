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

import java.io._
import java.nio.charset.Charset
import java.nio.file.{Files,StandardOpenOption}

/**
 * author Heitor Barbieri
 * date 20151103
 */

class WriteUrlActor(goodUrlFile: File,
                    brokenUrlFile: File,
                    charset: Charset,
                    append: Boolean = false) extends Actor {
  val option = if (append) StandardOpenOption.APPEND
               else StandardOpenOption.TRUNCATE_EXISTING
  val goodWriter = Files.newBufferedWriter(goodUrlFile.toPath(), charset,
                    StandardOpenOption.CREATE,StandardOpenOption.WRITE, option)
  val brokenWriter = Files.newBufferedWriter(brokenUrlFile.toPath(), charset,
                    StandardOpenOption.CREATE,StandardOpenOption.WRITE, option)
  val teller = new Teller()

  teller.start()

  def receive: Receive = {
    case (isGood:Boolean,url:String) => {
      if (isGood) {
        goodWriter.write(url)
        goodWriter.newLine()
      } else {
        brokenWriter.write(url)
        brokenWriter.newLine()
      }
      teller.add(isGood)
    }
    case Finish => {
      goodWriter.close()
      brokenWriter.close()
      context.actorSelection("../readUrl") ! Finish
      //println("WriteUrlActor is finishing")
      // finish itself
    }
  }
}
