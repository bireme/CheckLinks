/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.cl

import java.io._

/**
 *
 * author Heitor Barbieri
 * date 20151103
 */
class GetFileLines0(file: File) {
  val bis: BufferedInputStream = new BufferedInputStream(new FileInputStream(file))
  var nextLine: Option[String] = getLine

  def hasNext: Boolean = {
    nextLine match {
      case Some(_) => true
      case None => false
    }
  }

  def next() : Option[String] = {
    if (hasNext) {
      val next = nextLine
      nextLine = getLine
      next
    } else None
  }

  def getLine: Option[String] = {
    def getLine0(builder: StringBuilder): Option[String] = {
      bis.read() match {
        case i if i == -1 =>
          bis.close()
          if (builder.isEmpty) None else Some(builder.toString())  // end of file
        case i if i == 10 => Some(builder.toString())  // end of line
        case i if i > Character.MAX_VALUE => getLine0(builder.append('?'))
        case i => getLine0(builder.append(i.toChar))             // typical char
      }
    }
    getLine0(new StringBuilder())
  }
}
