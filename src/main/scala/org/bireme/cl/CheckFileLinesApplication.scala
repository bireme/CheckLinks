/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.cl

import java.io.File
import java.nio.charset.{ Charset,StandardCharsets }

/**
 * author Heitor Barbieri
 * date 20151103
 */
object CheckFileLinesApplication extends App {
  def usage() : Unit = {
    Console.err.println("usage CheckFileLinesApplication <inFile> [<encoding>]")
    System.exit(1)
  }

  def showLines(gfl: GetFileLines): Unit = {
    gfl.getLine.foreach { line =>
      println(line)
      showLines(gfl)
    }
  }

  if (args.length < 1) usage()

  val charset = if (args.length > 1) Charset.forName(args(1))
                else StandardCharsets.UTF_8

  val gfl = new GetFileLines(new File(args(0)), charset)

  showLines(gfl)
}
