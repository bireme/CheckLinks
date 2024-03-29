/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.cl

import java.text.SimpleDateFormat
import java.util.Date

/**
 * author Heitor Barbieri
 * date 20151111
 */
class Teller(tell: Int) {
  require(tell > 0)

  val second: Int = 1000
  val minute: Int = 60 * second
  val hour: Int = 60 * minute
  val day: Int = 24 * hour

  var cur: Int = 0
  var good: Int = 0
  var broken: Int = 0
  var stepGood: Int = 0
  var stepBroken: Int = 0
  var startDate: Date = new Date()
  var lastDate: Date = startDate
  var minDiff: Long = 0L
  var maxDiff: Long = 0L
  var minFree: Long = Runtime.getRuntime.freeMemory / (1024 * 1024)
  var maxFree: Long = minFree

  def start() : Unit = {
    cur = 0
    good = 0
    broken = 0
    stepGood = 0
    stepBroken = 0
    startDate = new Date()
    lastDate = startDate
    minDiff = 0L
    maxDiff = 0L
    minFree = Runtime.getRuntime.freeMemory() / (1024 * 1024)
    maxFree = minFree
  }

  def add(isGood: Boolean) : Unit = {
    cur = cur + 1
    if (isGood) {
      good = good + 1
      stepGood += 1
    } else {
      broken = broken + 1
      stepBroken += 1
    }
    if (cur % tell == 0) printCurrent()
  }

  def printCurrent() : Unit = {
    val curDate = new Date()
    val fmtCurDate = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(curDate)
    val curTime = curDate.getTime
    val totDiff = curTime - startDate.getTime
    val medDiff = (totDiff.toFloat / cur * tell).toLong
    val curDiff = curTime - lastDate.getTime
    if ((minDiff == 0) || (curDiff < minDiff))  minDiff = curDiff
    if (curDiff > maxDiff) maxDiff = curDiff

    val curStr = getTimeStr(curDiff)
    val minStr = getTimeStr(minDiff)
    val maxStr = getTimeStr(maxDiff)
    val medStr = getTimeStr(medDiff)
    val totStr = getTimeStr(totDiff)
    val free =  Runtime.getRuntime.freeMemory() / (1024 * 1024)

    if (free < minFree) minFree = free
    if (free > maxFree) maxFree = free

    val medFree = (maxFree - minFree) / 2

    if (cur == tell)
      println("---------------------------------------------------------")

    println("[count] " + cur + "\n[date] " + fmtCurDate + "\n[links] good:" +
      good + " broken:" + broken + " stepGood:" + stepGood + " stepBroken:" +
      stepBroken + "\n[time] step:" + curStr + " min:" + minStr + " med:" +
      medStr + " max:" + maxStr + " total:" + totStr + "\n[free mem] cur:" +
      free + "Mb min:" + minFree + "Mb med:" + medFree + "Mb max:" +
      maxFree + "Mb\n" +
      "---------------------------------------------------------")

    stepGood = 0
    stepBroken = 0
    lastDate = curDate
  }

  def getTimeStr(difMili: Long) : String = {
    val days = difMili / day
    val modDays = difMili % day
    val hours = modDays / hour
    val modHours = modDays % hour
    val minutes = modHours / minute
    val modMinutes = modHours % minute
    val seconds = modMinutes / second

    val tstr = "" +
    (if (days > 0) { s"${days}d"} else "") +
    (if (hours > 0) { s"${hours}h"} else "") +
    (if (minutes > 0) { s"${minutes}m"} else "") +
    (if (seconds > 0) { s"${seconds}s"} else "")

    if (tstr.isEmpty) "0s" else tstr
  }
}
