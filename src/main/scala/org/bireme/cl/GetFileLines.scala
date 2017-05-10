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
import java.nio._
import java.nio.charset.Charset

/**
 *
 * author Heitor Barbieri
 * date 20151103
 */
class GetFileLines(file: File,
                   charset: Charset) {
  val bis = new BufferedInputStream(new FileInputStream(file))
  val buffer = ByteBuffer.allocate(1024)

  def getLine(): Option[String] = {
    def getLine0(): Option[String] = {
      bis.read() match {
        case i if (i == -1) => { // end of file
          if (buffer.position() == 0) None
          else {
            buffer.flip()
            Some(charset.decode(buffer).toString())
          }
        }
        case i if ((i == 10) || (i == 13)) => { // end of line or carriage return
          if (buffer.position() == 0) getLine0()
          else {
            buffer.flip()
            Some(charset.decode(buffer).toString())
          }
        }
        case i if (i > Character.MAX_VALUE) => { // invalid char
          buffer.put('?'.toByte)
          getLine0()
        }
        case i => { // typical char
          buffer.put(i.toByte)
          getLine0()
        }
      }
    }

    buffer.clear()
    getLine0()
  }

  def close(): Unit = bis.close()
}
