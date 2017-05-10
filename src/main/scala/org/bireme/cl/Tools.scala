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
    val writer = Files.newBufferedWriter(new File(inoutfile).toPath(),
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
