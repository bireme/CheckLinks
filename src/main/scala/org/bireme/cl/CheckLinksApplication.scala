/*=========================================================================

    CheckLinks © Pan American Health Organization, 2017.
    See License at: https://github.com/bireme/CheckLinks/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.cl

import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import java.security.cert.X509Certificate
import javax.net.ssl.{HostnameVerifier, HttpsURLConnection, SSLContext, SSLSession, TrustManager, X509TrustManager}

import scala.concurrent._
import scala.concurrent.duration._

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
  def usage(): Unit = {
    Console.err.println("usage CheckLinksApplication <inFile> <outGoodFile> "
      + " <outBrokenFile> [<encoding>] [-wait=<minutes>] [--append]")
    System.exit(1)
  }

  if (args.length < 3) usage()

  // Disable debug information from Apache url check.
  System.setProperty("org.apache.commons.logging.Log",
    "org.apache.commons.logging.impl.SimpleLog")
  System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "error")

  var append = false
  var waitMinutes = 30
  var charset = StandardCharsets.UTF_8
  for (idx <- 3 until args.length) {
    if (args(idx).equals("--append")) append = true
    else if (args(idx).startsWith("-wait=")) waitMinutes =
      Integer.parseInt(args(idx).substring(6))
    else charset = Charset.forName(args(idx))
  }

  CheckLinksMain.checkAll(args(0), args(1), args(2), append, charset, waitMinutes)

  //=====================================================================================
}

object CheckLinksMain {
  def checkAll(inFile: String,
               outGoodFile: String,
               outBadFile: String,
               append: Boolean,
               charset: Charset,
               waitMinutes: Int,
               numberOfCheckers: Int = 100): Unit = {
    // Max number of actors
    val startDate = new Date()
    val broken = s"broken_${startDate.toString}.tmp" // Temporary file of broken links
    val broken2 = s"broken2_${startDate.toString}.tmp" // Temporary file of broken links

    println("\n-----------------------------------------------------------------------")
    println("\nCheck Links - BIREME/PAHO/WHO (c)\n")
    //println("CheckLinksApplication - checking $lines links")
    println("Start checking - " + startDate)

    // First check
    //val lkNum1 = Source.fromFile(args(0)).getLines.size - bug  if contains invalid char
    println("\n+++ Step 1 - Checking all links (parallel mode)")
    check(inFile, outGoodFile, broken, charset, append, numberOfCheckers)
    println("Step 1 - Check finished")

    if (!append) new File(outBadFile).delete()

    // Waiting 4 minutes
    val waitMiliseconds = 4 * 60 * 1000
    println("\nWaiting 4 minutes before step 2")
    Thread.sleep(waitMiliseconds)

    // Second check - only broken links
    //val lkNum2 = Source.fromFile(broken).getLines.size
    println("\n+++ Step 2 - Rechecking only broken links (parallel mode)")
    check(broken, outGoodFile, broken2, charset, append = true, numberOfCheckers, tell = 50)
    println("Step 2 - Check finished")

    // Waiting 'waitMinutes' minutes
    val waitMiliseconds2 = waitMinutes * 60 * 1000
    println("\nWaiting " + waitMinutes + " minutes before step 3")
    Thread.sleep(waitMiliseconds2)

    // Second check - only broken links (one by one)
    //val lkNum2 = Source.fromFile(broken).getLines.size
    println("\n+++ Step 3 - Rechecking remaining broken links (sequential mode)")
    check(broken2, outGoodFile, outBadFile, charset, append = true, numberOfCheckers = 1, tell = 10)
    println("Step 3 - Check finished")

    // Delete temporary files
    new File(broken).delete()
    new File(broken2).delete()

    val endDate = new Date()
    val elapsedTime = endDate.getTime - startDate.getTime
    val teller = new Teller(tell = 1)
    println("\nCheck finished - " + endDate + ". Processing time:"
      + teller.getTimeStr(elapsedTime))
  }

  // Create main actor
  def check(inFile: String,
            outGoodFile: String,
            outBrokenFile: String,
            charset: Charset,
            append: Boolean,
            numberOfCheckers: Int,
            tell: Int = 500) : Unit = {
    disableCertificateValidation()

    val _system = ActorSystem.create("CheckLinksApp",
                                     ConfigFactory.load("application"))

    // Create actor that will write checked urls into output files
    val writeUrl: ActorRef = _system.actorOf(Props(new WriteUrlActor(
                                             new File(outGoodFile),
                                             new File(outBrokenFile),
                                             charset, append, tell)), name = "writeUrl")

    // Create actor that will read urls to be checked from input file
    val readUrl: ActorRef = _system.actorOf(Props(new ReadUrlActor(
                                            new File(inFile),
                                            charset,
                                            writeUrl, numberOfCheckers)), name = "readUrl")

    //implicit val timeout = Timeout(10 seconds)
    implicit val timeout: Timeout = Timeout(Duration(200, HOURS))

    val future: Future[Any] = readUrl ? Start

    //println("antes do Await...")
    //val result = Await.result(future, 10 seconds)
    Await.result(future, Duration(200, HOURS))

    _system.terminate()
  }

  // See: https://nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
  def disableCertificateValidation(): Unit = {

    // Create a trust manager that does not validate certificate chains
    val trustAllCerts = Array[TrustManager] {
      new X509TrustManager() {
        def getAcceptedIssuers: Array[java.security.cert.X509Certificate] = null
        def checkClientTrusted(certs: Array[X509Certificate],
                               authType: String): Unit = {}
        def checkServerTrusted(certs: Array[X509Certificate],
                               authType: String): Unit = {}
      }
    }

    // Install the all-trusting trust manager
    val sc: SSLContext = SSLContext.getInstance("SSL")
    sc.init(null, trustAllCerts, new java.security.SecureRandom())
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)

    // Create all-trusting host name verifier
    val allHostsValid: HostnameVerifier = (hostname: String, session: SSLSession) => true

    // Install the all-trusting host verifier
    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
  }
}
