/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

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
