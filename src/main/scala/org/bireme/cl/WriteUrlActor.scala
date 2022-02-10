/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

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
                    append: Boolean = false,
                    tell: Int) extends Actor {
  val option: StandardOpenOption = if (append) StandardOpenOption.APPEND
               else StandardOpenOption.TRUNCATE_EXISTING
  val goodWriter: BufferedWriter = Files.newBufferedWriter(goodUrlFile.toPath, charset,
                    StandardOpenOption.CREATE,StandardOpenOption.WRITE, option)
  val brokenWriter: BufferedWriter = Files.newBufferedWriter(brokenUrlFile.toPath, charset,
                    StandardOpenOption.CREATE,StandardOpenOption.WRITE, option)
  val teller: Teller = new Teller(tell)

  teller.start()

  def receive: Receive = {
    case (isGood:Boolean,url:String) =>
      if (isGood) {
        goodWriter.write(url)
        goodWriter.newLine()
      } else {
        brokenWriter.write(url)
        brokenWriter.newLine()
      }
      teller.add(isGood)
    case Finish =>
      goodWriter.close()
      brokenWriter.close()
      context.actorSelection("../readUrl") ! Finish
      //println("WriteUrlActor is finishing")
      // finish itself
  }
}
