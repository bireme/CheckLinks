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

import com.typesafe.config.ConfigFactory

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
//import akka.util.duration._
//import akka.pattern.pipe

import scala.concurrent._
import scala.concurrent.duration._
import scala.io.Source

import java.io.File
import java.nio.charset.{ Charset,StandardCharsets }
import java.util.Date

case object Start
case object Finish
case object AskByUrl

/**
 *
 * author Heitor Barbieri
 * date 20151103
 */
object CheckLinksApplication extends App {
  def usage() : Unit = {
    Console.err.println("usage CheckLinksApplication <inFile> <outGoodFile> "
          + " <outBrokenFile> [<encoding>] [-wait=<minutes>] [--append]")
    System.exit(1)
  }

  // Create main actor
  def check(inFile: String,
            outGoodFile: String,
            outBrokenFile: String,
            charset: Charset,
            append: Boolean) : Unit = {
    val _system = ActorSystem.create("CheckLinksApp",
                                     ConfigFactory.load("application"))

    // Create actor that will write checked urls into output files
    val writeUrl = _system.actorOf(Props(new WriteUrlActor(
                              new File(outGoodFile),
                              new File(outBrokenFile),
                              charset, append)), name = "writeUrl")

    // Create actor that will read urls to be checked from input file
    val readUrl = _system.actorOf(Props(new ReadUrlActor(
                              new File(inFile),
                              charset,
                              writeUrl, numOfCheckers)), name = "readUrl")

    //implicit val timeout = Timeout(10 seconds)
    implicit val timeout = Timeout(Duration(50, HOURS))

    val future = readUrl ? Start

    //println("antes do Await...")
    //val result = Await.result(future, 10 seconds)
    val result = Await.result(future, Duration(50, HOURS))

    _system.terminate
  }

  //============================================================================
  if (args.length < 3) usage()

  // Disable debug information from Apache url check.
  System.setProperty("org.apache.commons.logging.Log",
                     "org.apache.commons.logging.impl.SimpleLog")
  System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "error")

  var append = false
  var waitMiliS = 0
  var charset = StandardCharsets.UTF_8
  for (idx <- 3 until args.length) {
      if (args(idx).equals("--append")) append = true
      else if (args(idx).startsWith("-wait=")) waitMiliS =
                                        Integer.parseInt(args(idx).substring(6))
      else charset = Charset.forName(args(idx))
  }

  val numOfCheckers = 25    // Max number of actors
  val broken = "broken.tmp" // Temporary file of broken links
  val startDate = new Date()
  println("Starting check - " + startDate)

  // First check
  //val lkNum1 = Source.fromFile(args(0)).getLines.size - bug  if contains invalid char
  println("\nStep 1 - Checking links")
  check(args(0), args(1), broken, charset, append)
  println("Step 1 - Check finished")

  if (!append) new File(args(2)).delete()

  // Waiting miliseconds
  val totWait = waitMiliS * 60 * 1000
  println("\nWaiting " + totWait + " miliseconds before step 2")
  Thread.sleep(totWait)

  // Second check - only broken links
  //val lkNum2 = Source.fromFile(broken).getLines.size
  println("\nStep 2 - Rechecking broken links")
  check(broken, args(1), args(2), charset, true)
  println("Step 2 - Check finished")

  // Delete temporary files
  new File(broken).delete()

  val endDate = new Date()
  val elapsedTime = endDate.getTime - startDate.getTime
  val teller = new Teller()
  println("\nCheck finished - " + endDate + ". Processing time:"
                                         + teller.getTimeStr(elapsedTime))
}
