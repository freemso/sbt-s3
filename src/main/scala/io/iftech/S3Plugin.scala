package io.iftech

import com.amazonaws.auth.{AWSStaticCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.event.{ProgressEvent, ProgressEventType, SyncProgressListener}
import com.amazonaws.services.s3.model.{GeneratePresignedUrlRequest, GetObjectRequest, PutObjectRequest}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.{AmazonWebServiceRequest, ClientConfiguration, HttpMethod, Protocol}
import sbt.Keys.{mappings, streams}
import sbt.{AutoPlugin, Def, SettingKey, TaskKey, URL}

import java.io.File
import java.util.Date

/**
 * S3Plugin is a simple sbt plugin that can manipulate objects on Amazon S3.
 *
 * == Example ==
 * Here is a complete example:
 *
 *  - project/plugin.sbt:
 * {{{addSbtPlugin("io.iftech" % "sbt-s3" % "0.0.1-SNAPSHOT")}}}
 *
 *  - build.sbt:
 * {{{
 *
 * enablePlugins(S3Plugin)
 *
 * mappings in s3Upload := Seq((new java.io.File("a"),"zipa.txt"),(new java.io.File("b"),"pongo/zipb.jar"))
 *
 * s3Bucket in s3Upload := "s3sbt-test"
 *
 * s3Region in s3Upload := "cn-northwest-1"
 * }}}
 *
 * Just create two sample files called "a" and "b" in the same directory that contains build.sbt,
 * then try:
 * {{{$ sbt s3Upload}}}
 *
 * You can also see progress while uploading:
 * {{{
 * $ sbt
 * > set s3Progress in s3Upload := true
 * > s3Upload
 * [==================================================]   100%   zipa.txt
 * [=====================================>            ]    74%   zipb.jar
 * }}}
 *
 * Please select the nested `S3` object link, below, for additional information on the available tasks.
 */
object S3Plugin extends AutoPlugin {

  object autoImport extends S3Keys {
    private[S3Plugin] val dummy = SettingKey[Unit]("dummy-internal", "Dummy setting")
  }

  import autoImport._

  private def makeProxyableClientConfiguration(): ClientConfiguration = {
    def doWith(prop: String)(f: String => Unit): Unit = {
      sys.props.get(prop).foreach(f)
    }

    val config = new ClientConfiguration().withProtocol(Protocol.HTTPS)
    doWith("http.proxyHost")(config.setProxyHost)
    doWith("http.proxyPort")(port => config.setProxyPort(port.toInt))
    doWith("http.proxyUser")(config.setProxyUsername)
    doWith("http.proxyPassword")(config.setProxyPassword)
    config
  }

  private def getClient(bucket: String, region: String): AmazonS3 = {
    val credentials = {
      val provider = new DefaultAWSCredentialsProviderChain
      try {
        provider.getCredentials
      } catch {
        case _: com.amazonaws.AmazonClientException =>
          sys.error("Could not find S3 credentials for the host: " + bucket + ", and no IAM credentials available")
      }
    }
    AmazonS3ClientBuilder
      .standard()
      .withClientConfiguration(makeProxyableClientConfiguration())
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(System.getenv("AWS_ENDPOINT"), region))
      .withPathStyleAccessEnabled(false)
      .build()
  }

  private def s3InitTask[Item, Extra, Return](thisTask: TaskKey[Seq[Return]], itemsKey: TaskKey[Seq[Item]],
                                              extra: SettingKey[Extra], // may be unused (a dummy value)
                                              op: (AmazonS3, Bucket, Item, Extra, Boolean) => Return,
                                              msg: (Bucket, Item) => String, lastMsg: (Bucket, Seq[Item]) => String) = Def.task {
    val items = (itemsKey in thisTask).value
    val bucket = (s3Bucket in thisTask).value
    val region = (s3Region in thisTask).value
    val ext = (extra in thisTask).value
    val progress = (s3Progress in thisTask).value
    val log = streams.value.log

    val client = getClient(bucket, region)
    log.debug(client.getRegionName)
    val ret = items map { item =>
      log.debug(msg(bucket, item))
      op(client, bucket, item, ext, progress)
    }

    log.info(lastMsg(bucket, items))
    ret
  }

  private def progressBar(percent: Int): String = {
    val b = "=================================================="
    val s = "                                                 "
    val p = percent / 2
    val z: StringBuilder = new StringBuilder(80)
    z.append("\r[")
    z.append(b.substring(0, p))
    if (p < 50) {
      z.append(">")
      z.append(s.substring(p))
    }
    z.append("]   ")
    if (p < 5) z.append(" ")
    if (p < 50) z.append(" ")
    z.append(percent)
    z.append("%   ")
    z.mkString
  }

  private def addProgressListener(request: AmazonWebServiceRequest, fileSize: Long, key: String): Unit = {
    request.setGeneralProgressListener(new SyncProgressListener {
      var uploadedBytes = 0L
      val fileName: Bucket = {
        val area = 30
        val n = new File(key).getName
        val l = n.length()
        if (l > area - 3)
          "..." + n.substring(l - area + 3)
        else
          n
      }

      override def progressChanged(progressEvent: ProgressEvent): Unit = {
        if (progressEvent.getEventType == ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT ||
          progressEvent.getEventType == ProgressEventType.RESPONSE_BYTE_TRANSFER_EVENT) {
          uploadedBytes = uploadedBytes + progressEvent.getBytesTransferred
        }
        print(progressBar(if (fileSize > 0) ((uploadedBytes * 100) / fileSize).toInt else 100))
        print(fileName)
        if (progressEvent.getEventType == ProgressEventType.TRANSFER_COMPLETED_EVENT)
          println()
      }
    })
  }

  def prettyLastMsg(verb: String, objects: Seq[String], preposition: String, bucket: String): String =
    if (objects.length == 1) s"$verb '${objects.head}' $preposition the S3 bucket '$bucket'."
    else s"$verb ${objects.length} objects $preposition the S3 bucket '$bucket'."

  /*
   * Include the line {{{s3Settings}}} in your build.sbt file, in order to import the tasks defined by this S3 plugin.
   */
  private val s3Settings = Seq(

    s3Upload := s3InitTask[(File, String), MetadataMap, String](s3Upload, mappings, s3Metadata,
      { case (client, bucket, (file, key), metadata, progress) =>
        val request = new PutObjectRequest(bucket, key, file)
        if (progress) addProgressListener(request, file.length(), key)
        client.putObject(metadata.get(key).map(request.withMetadata).getOrElse(request))
        key
      },
      { case (bucket, (file, key)) => "Uploading " + file.getAbsolutePath + " as " + key + " into " + bucket },
      { (bucket, mapps) => prettyLastMsg("Uploaded", mapps.map(_._2), "to", bucket) }
    ).value,

    s3Download := s3InitTask[(File, String), Unit, File](s3Download, mappings, dummy,
      { case (client, bucket, (file, key), _, progress) =>
        val request = new GetObjectRequest(bucket, key)
        val objectMetadata = client.getObjectMetadata(bucket, key)
        if (progress) addProgressListener(request, objectMetadata.getContentLength, key)
        client.getObject(request, file)
        file
      },
      { case (bucket, (file, key)) => "Downloading " + file.getAbsolutePath + " as " + key + " from " + bucket },
      { (bucket, mapps) => prettyLastMsg("Downloaded", mapps.map(_._2), "from", bucket) }
    ).value,

    s3Delete := s3InitTask[String, Unit, String](s3Delete, s3Keys, dummy,
      { (client, bucket, key, _, _) => client.deleteObject(bucket, key); key },
      { (bucket, key) => "Deleting " + key + " from " + bucket },
      { (bucket, keys1) => prettyLastMsg("Deleted", keys1, "from", bucket) }
    ).value,

    s3GenerateLinks := s3InitTask[String, Date, URL](s3GenerateLinks, s3Keys, s3ExpirationDate,
      { (client, bucket, key, date, _) =>
        val request = new GeneratePresignedUrlRequest(bucket, key)
        request.setMethod(HttpMethod.GET)
        request.setExpiration(date)
        val url = client.generatePresignedUrl(request)
        println(s"$key link: $url")
        url
      },
      { (bucket, key) => s"Creating link for $key in $bucket" },
      { (bucket, keys1) => prettyLastMsg("Generated link", keys1, "from", bucket) }
    ).value,

    s3Bucket := "",
    s3Region := "",
    s3Keys := Seq(),
    s3Metadata := Map(),
    mappings in s3Download := Seq(),
    mappings in s3Upload := Seq(),
    s3Progress := false,
    s3ExpirationDate := new java.util.Date(),
    dummy := ()
  )

  override def projectSettings: Seq[Def.Setting[_]] = super.projectSettings ++ s3Settings
}
