package org.bireme.cl

import java.net.{URI, URL, URLDecoder}
import scalaj.http.HttpOptions.HttpOption
import scalaj.http.{Http, HttpOptions}

import java.net.http.{HttpClient, HttpRequest}
import java.net.http.HttpClient.{Redirect, Version}
import java.net.http.HttpResponse.BodyHandlers
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.{HostnameVerifier, HttpsURLConnection, SSLContext, SSLSession, TrustManager, X509TrustManager}
import scala.annotation.tailrec
import scala.collection.concurrent.TrieMap
import scala.util.{Failure, Success, Try}

class CheckLinksCore {
  val CONNECT_TIMEOUT_EXCEPTION: Int = 1003
  val UNKNOWN_HOST_EXCEPTION: Int  = 1008
  val SOCKET_EXCEPTION: Int = 1009
  val SOCKET_TIMEOUT_EXCEPTION: Int = 1010
  val SSL_HANDSHAKE_EXCEPTION: Int = 1013
  val REDIRECTED_TOO_MANY_TIMES_EXCEPTION: Int = 1017
  val UNEXPECTED_END_OF_FILE_EXCEPTION: Int = 1018
  val CONNECTION_RESET_EXCEPTION: Int = 1019
  val SSL_EXCEPTION: Int = 1020
  val UNSUPPORTED_ENCODING_EXCEPTION = 1021
  val SSL_PROTOCOL_EXCEPTION = 1022
  val ILLEGAL_ARGUMENT_EXCEPTION = 1023
  val DNS_EXCEPTION = 1024
  val UNKNOWN: Int = 1100

  val brokenPaths: TrieMap[String, Int] = new TrieMap[String, Int] // Concurrent map

  val timeout: Int = 4 * 60 * 1000

  /*
  // https://stackoverflow.com/questions/52988677/allow-insecure-https-connection-for-java-jdk-11-httpclient
  val trustAllCerts: Array[TrustManager] = Array[TrustManager] {
    new X509TrustManager() {
      def getAcceptedIssuers(): Array[java.security.cert.X509Certificate] = null
      def checkClientTrusted(certs: Array[java.security.cert.X509Certificate], authType: String): Unit = {}
      def checkServerTrusted(certs: Array[java.security.cert.X509Certificate], authType: String): Unit = {}
    }
  }

  val sslContext: SSLContext = SSLContext.getInstance("TLS")
  sslContext.init(null, trustAllCerts, new SecureRandom()) */

  //disableSslVerification()

  val client: HttpClient = HttpClient.newBuilder()
    .version(Version.HTTP_1_1)
    .followRedirects(Redirect.ALWAYS)
    .connectTimeout(Duration.ofSeconds(timeout))
    //.sslContext(sslContext)
    //.proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
    //.authenticator(Authenticator.getDefault())
    .build()

  def checkUrl(surl: String): Int = {
    Try (new URL(surl)) orElse Try (new URL(urlEncode(surl))) match {
      case Success(url) =>
        url.getProtocol match {
          case "http" | "https" => httpCheck(url)
          case "ftp" => ftpCheck(url)
          case _ => 501
        }
      case Failure(_) => 500
    }
  }

  def ftpCheck(url: URL): Int = {
    Try (url.openStream().close()) match {
      case Success(_) => 200
      case Failure(_) => 500
    }
  }

  def httpCheck(url: URL): Int = {
    val port: Int = url.getPort
    val sport = if (port > 0) {
      if (port == 80) ":80" else s":$port"
    } else ""
    val prefix: String = s"${url.getProtocol}://${url.getHost}$sport"
    val parts: Seq[String] = Seq(prefix) ++ (url.getPath match {
      case "" | "/" => Array[String]()
      case path =>
        if (path(0) == '/') path.tail.split("/")
        else path.split("/")
    })

    foundInBrokenList(parts) match {
      case Some(err) => /*println(s"""pulei=${parts.mkString("/")}""");*/ err
      case None =>
        val err: Int = check(url.toString)
        if (!isOk(err))
          putInBrokenList(parts)
        err
    }
  }

  @tailrec
  private def foundInBrokenList(parts: Seq[String]): Option[Int] = {
    if (parts.isEmpty) None
    else {
      val path = parts.mkString("/")
      brokenPaths.get(path) match {
        case Some(err) =>
          //println(s"foundInBrokenList - path=$path err=$err")
          Some(err)
        case None => foundInBrokenList(parts.dropRight(1))
      }
    }
  }

  @tailrec
  private def putInBrokenList(parts: Seq[String],
                              prefix: String = ""): Unit = {
    if (parts.nonEmpty) {
      val surl: String = if (prefix.isEmpty) parts.head else s"$prefix/${parts.head}"
      val err: Int = check(surl)
      if (isOk(err)) {
        //println(s"putInBrokenList - resto=${parts.tail} prefixo=$surl")
        putInBrokenList(parts.tail, surl)
      }
      else {
        //println(s"Colocando no brokenPaths - surl=$surl err=$err")
        brokenPaths.putIfAbsent(surl, err)
      }
    }
  }

  private def check0(url: String): Int = {
    val timeout: Int = 4 * 60 * 1000

    val options: Seq[HttpOption] = Seq(
      HttpOptions.connTimeout(timeout),
      HttpOptions.readTimeout(timeout),
      HttpOptions.followRedirects(true),
      HttpOptions.allowUnsafeSSL)

    Try(Http(url).options(options).asString.code) match {
      case Success(value) => value
      case Failure(exception) => getErrorCode(exception)
    }
  }

  private def check(url: String): Int = {
    Try {
      val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .setHeader("User-Agent", "Wget/1.16.1 (linux-gnu)")
        .setHeader("Accept", "text/*;*/*")
        .setHeader("Accept-Encoding", "*")
        .method("HEAD", HttpRequest.BodyPublishers.noBody())
        //.setHeader("Connection", "Keep-Alive")
        //.setHeader("Connection", "close")
        .build()

      val response = client.send(request, BodyHandlers.ofString())
      response.statusCode()
    } match {
      case Success(value) => value
      case Failure(exception) => getErrorCode(exception)
    }
  }

  private def getErrorCode(exception: Throwable): Int = {
    exception match {
      case _: javax.net.ssl.SSLHandshakeException => SSL_HANDSHAKE_EXCEPTION
      case _: java.net.UnknownHostException => UNKNOWN_HOST_EXCEPTION
      case _: java.net.ProtocolException => REDIRECTED_TOO_MANY_TIMES_EXCEPTION
      case ex: java.net.ConnectException =>
        if (Option(ex.getMessage).getOrElse("") contains "timed out") CONNECT_TIMEOUT_EXCEPTION
        else UNKNOWN
      case ex: java.net.SocketException =>
        if (Option(ex.getMessage).getOrElse("") contains "end of file") UNEXPECTED_END_OF_FILE_EXCEPTION
        else if (Option(ex.getMessage).getOrElse("") contains "Connection reset") CONNECTION_RESET_EXCEPTION
        else SOCKET_EXCEPTION
      case _: java.net.SocketTimeoutException => SOCKET_TIMEOUT_EXCEPTION
      case _: javax.net.ssl.SSLException => SSL_EXCEPTION
      case _: java.io.UnsupportedEncodingException => UNSUPPORTED_ENCODING_EXCEPTION
      case _: javax.net.ssl.SSLProtocolException => SSL_PROTOCOL_EXCEPTION
      case _: java.lang.IllegalArgumentException => ILLEGAL_ARGUMENT_EXCEPTION
      case ex: java.io.IOException =>
        if (Option(ex.getMessage).getOrElse("") contains "DNS") DNS_EXCEPTION
        else SOCKET_EXCEPTION
      case _ =>
        println(s"check ERROR: ${exception.toString}")
        UNKNOWN
    }
  }

  private def isOk(errCode: Int): Boolean = ((errCode >= 200) && (errCode < 400)) || (errCode == 401) || (errCode == 403)

  /**
    * Encoding a url. See https://stackoverflow.com/questions/724043/http-url-address-encoding-in-java
    * @param surl input url string to encode
    * @return the encoded url
    */
  private def urlEncode(surl: String): String = {
    val url: URL = new URL(URLDecoder.decode(surl, "utf-8"))     // To avoid double encoding
    val uri: URI = new URI(url.getProtocol, url.getUserInfo, url.getHost, url.getPort, url.getPath, url.getQuery, url.getRef)
    //url.getPort, URLEncoder.encode(url.getPath, "utf-8"), url.getQuery, url.getRef)
    uri.toURL.toString  // Do not treat # in the URLpath
  }

  //https://stackoverflow.com/questions/19540289/how-to-fix-the-java-security-cert-certificateexception-no-subject-alternative
  private def disableSslVerification(): Unit = {
    Try {

      /*
       // https://stackoverflow.com/questions/52988677/allow-insecure-https-connection-for-java-jdk-11-httpclient
       val trustAllCerts: Array[TrustManager] = Array[TrustManager] {
         new X509TrustManager() {
           def getAcceptedIssuers(): Array[java.security.cert.X509Certificate] = null
           def checkClientTrusted(certs: Array[java.security.cert.X509Certificate], authType: String): Unit = {}
           def checkServerTrusted(certs: Array[java.security.cert.X509Certificate], authType: String): Unit = {}
         }
       }

       val sslContext: SSLContext = SSLContext.getInstance("TLS")
       sslContext.init(null, trustAllCerts, new SecureRandom()) */

      // Create a trust manager that does not validate certificate chains
      val trustAllCerts = Array[TrustManager] {
        new X509TrustManager() {
          def getAcceptedIssuers: Array[java.security.cert.X509Certificate] = null
          def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
          def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
        }
      }

      // Install the all-trusting trust manager
      val sc: SSLContext = SSLContext.getInstance("SSL")
      sc.init(null, trustAllCerts, new java.security.SecureRandom())
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory)

      // Create all-trusting host name verifier
      val allHostsValid: HostnameVerifier = new HostnameVerifier() {
        def verify(hostname: String, session: SSLSession): Boolean = true
      }

      // Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
    } match {
      case Success(_) => ()
      case Failure(exception) => exception.printStackTrace()
    }
  }
}
object CheckLinksCore extends App {
  private def usage(): Unit = {
    System.err.println("usage: CheckLinksCore <url>")
    System.exit(-1)
  }

  if (args.length != 1) usage()

  val core: CheckLinksCore = new CheckLinksCore()
  val retCode: Int = core.checkUrl(args(0))

  println(s"url:${args(0)} retCode=$retCode")
}

