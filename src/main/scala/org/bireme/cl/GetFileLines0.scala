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

import java.io._
import java.nio.charset.Charset

/**
 *
 * author Heitor Barbieri
 * date 20151103
 */
class GetFileLines0(file: File,
                    charset: Charset) {
  val bis = new BufferedInputStream(new FileInputStream(file))
  var nextLine = getLine()

  def hasNext() : Boolean = {
    nextLine match {
      case Some(_) => true
      case None => false
    }
  }

  def next() : Option[String] = {
    if (hasNext) {
      val next = nextLine
      nextLine = getLine()
      next
    } else None
  }

  def getLine(): Option[String] = {
    def getLine0(builder: StringBuilder): Option[String] = {
      bis.read() match {
        case i if (i == -1) => {
          bis.close();
          if (builder.length == 0) None else Some(builder.toString())
        }  // end of file
        case i if (i == 10) => Some(builder.toString())  // end of line
        case i if (i > Character.MAX_VALUE) => getLine0(builder.append('?'))
        case i => getLine0(builder.append(i.toChar))             // typical char
      }
    }
    getLine0(new StringBuilder())
  }
}
