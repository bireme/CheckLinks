/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.cl

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Files, StandardOpenOption}
import scala.io.Source

/**
 * author: Heitor Barbieri
 * date: 20151103
 */
object Tools {
  def mergeFile(infile: String,
                inoutfile: String,
                encoding: String): Unit = {
    val src = Source.fromFile(infile, encoding)
    val lines = src.getLines()
    val writer = Files.newBufferedWriter(new File(inoutfile).toPath,
                                         Charset.forName(encoding),
                                         StandardOpenOption.APPEND)
    for (line <- lines) {
      writer.append(line)
      writer.newLine()
    }
    src.close()
    writer.close()
  }
}
