package io.iftech

import com.amazonaws.services.s3.model.ObjectMetadata
import sbt._

trait S3Keys {
  type MetadataMap = Map[String, ObjectMetadata]
  type Bucket = String

  /**
   * The task "s3-upload" uploads a set of files to a specificed S3 bucket.
   * Depends on:
   *  - ''mappings in S3.upload'': the list of local files and S3 keys (pathnames), for example:
   * `Seq((File("f1.txt"),"aaa/bbb/file1.txt"), ...)`
   *  - ''S3.bucket in S3.upload'': the bucket name.
   *  - ''S3.region in S3.upload'': the region name.
   *
   * If you set logLevel to "Level.Debug", the list of files will be printed while uploading.
   *
   * Returns: the sequence of uploaded keys (pathnames).
   */
  val s3Upload = TaskKey[Seq[String]]("s3-upload", "Uploads files to an S3 bucket.")
  val ossUpload = TaskKey[Seq[String]]("oss-upload", "Uploads files to an OSS bucket.")

  /**
   * The task "s3-download" downloads a set of files from a specificed S3 bucket.
   * Depends on:
   *  - ''mappings in S3.download'': the list of local files and S3 keys (pathnames), for example:
   * `Seq((File("f1.txt"),"aaa/bbb/file1.txt"), ...)`
   *  - ''S3.bucket in S3.download'': the bucket name.
   *  - ''S3.region in S3.download'': the region name.
   *
   * If you set logLevel to "Level.Debug", the list of files will be printed while downloading.
   *
   * Returns: the sequence of downloaded files.
   */
  val s3Download = TaskKey[Seq[File]]("s3-download", "Downloads files from an S3 bucket.")

  /**
   * The task "s3-delete" deletes a set of files from a specificed S3 bucket.
   * Depends on:
   *  - ''S3.keys in S3.delete'': the list of S3 keys (pathnames), for example:
   *    `Seq("aaa/bbb/file1.txt", ...)`
   *  - ''S3.bucket in S3.delete'': the bucket name.
   *  - ''S3.region in S3.delete'': the region name.
   *
   * If you set logLevel to "Level.Debug", the list of keys will be printed while the S3 objects are being deleted.
   *
   * Returns: the sequence of deleted keys (pathnames).
   */
  val s3Delete = TaskKey[Seq[String]]("s3-delete", "Delete files from an S3 bucket.")

  /**
   * The task "s3-generate-links" creates a link for set of files in a S3 bucket.
   * Depends on:
   *  - ''keys in S3.generateLinks'': the list of remote files, for example:
   * `Seq("aaa/bbb/file1.txt", ...)`
   *  - ''expirationDate in S3.generateLinks'': the expiration date at which point the
   * pre-signed URL will no longer be accepted by Amazon S3. It must be specified.
   *  - ''S3.bucket in S3.generateLinks'': the bucket name.
   *  - ''S3.region in S3.generateLinks'': the region name.
   *
   * If you set logLevel to "Level.Debug", the list of files will be printed while uploading.
   *
   * Returns: the sequence of generated URLs.
   */
  val s3GenerateLinks = TaskKey[Seq[URL]]("s3-generate-links", "Creates links to a set of files in an S3 bucket.")

  /**
   * A string representing the S3 bucket name.
   */
  val s3Bucket = SettingKey[String]("s3-bucket", "Bucket used by the S3 operation.")

  /**
   * A string representing the S3 region name.
   */
  val s3Region = SettingKey[String]("s3-region", "Region used by the S3 operation.")

  /**
   * A list of S3 keys (pathnames) representing objects in a bucket on which a certain operation should be performed.
   */
  val s3Keys = TaskKey[Seq[String]]("s3-keys", "List of S3 keys (pathnames) on which to perform a certain operation.")

  /**
   * If you set "progress" to true, a progress indicator will be displayed while the individual files are uploaded or downloaded.
   * Only recommended for interactive use or testing; the default value is false.
   */
  val s3Progress = SettingKey[Boolean]("s3-progress", "Set to true to get a progress indicator during S3 uploads/downloads (default false).")

  val s3Metadata = SettingKey[MetadataMap]("s3-metadata", "Mapping from S3 keys (pathnames) to the corresponding metadata")

  val s3ExpirationDate = SettingKey[java.util.Date]("s3-expiration-date", "Expiration date for the generated link")
}

object S3Keys extends S3Keys
