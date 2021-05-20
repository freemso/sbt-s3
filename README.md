# sbt-s3

## Description

*sbt-s3* is a simple sbt plugin that can manipulate objects on Amazon S3.

## Usage

* add to your project/plugin.sbt:
   `resolvers += Resolver.sonatypeRepo("public")`
   `addSbtPlugin("io.iftech" % "sbt-s3" % "1.0.0")`
* then add to your build.sbt the line:
   `enablePlugins(S3Plugin)`

You will then be able to use the tasks `s3-upload`, `s3-download`, `s3-delete`, and `s3-generate-links`, defined
in the object `io.iftech.S3Keys` as `s3Upload`, `s3Download`, `s3Delete`, and `s3GenerateLinks` respectively.
All these operations will use HTTPS as a transport protocol.

Please check the Scaladoc API of the `S3Plugin` object, and the `S3Keys` object,
to get additional documentation on the available sbt tasks, and their parameters.

## Example 1

Here is a complete example:

project/plugin.sbt:

    resolvers += Resolver.sonatypeRepo("public")

    addSbtPlugin("io.iftech" % "sbt-s3" % "1.0.0")

build.sbt:

    enablePlugins(S3Plugin)

    mappings in s3Upload := Seq((new java.io.File("a"),"zipa.txt"),(new java.io.File("b"),"pongo/zipb.jar"))

    s3Bucket in s3Upload := "s3sbt-test"

    s3Region in s3Upload := "cn-northwest-1"

Just create two sample files called "a" and "b" in the same directory that contains build.sbt, then try:

    $ sbt s3Upload

You can also see progress while uploading:

    $ sbt
    > set s3Progress in s3Upload := true
    > s3Upload
    [==================================================]   100%   zipa.txt
    [=====================================>            ]    74%   zipb.jar

Unless explicitly provided as described above, credentials will be obtained via (in order):

1. `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables
2. `aws.accessKeyId` and `aws.secretKey` Java system properties
3. Default aws cli credentials file (`~/.aws/credentials`)
4. IAM instance profile if running under EC2.

## Example 2

As well as simply uploading a file to s3 you can also set some s3 ObjectMetadata.
For example, you may want to gzip a CSS file for quicker download but still have its content type as css,
In which case you need to set the Content-Type and Content-Encoding, a small change to
build.sbt is all that is needed.

build.sbt:

    enablePlugins(S3Plugin)

    mappings in s3Upload := Seq((target.value / "web" / "stage" / "css" / "style-group2.css.gz" ,"css/style-group2.css"))

    def md = {
      import com.amazonaws.services.s3.model.ObjectMetadata
      var omd = new ObjectMetadata()
      omd.setContentEncoding("gzip")
      omd.setContentType("text/css")
      omd
    }

    s3Metadata in s3Upload := Map("css/style-group2.css" -> md)

    s3Bucket in s3Upload := "s3sbt-test"

    s3Region in s3Upload := "cn-northwest-1"


## License

This code is open source software licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0.html">Apache 2.0 License</a>.

## Publishing a New Release

Change the version to the next non-snapshot version by modifying the version var on `build.sbt`.

Then, publish a signed artifact to Sonatype

```
sbt publishSigned
```

Once that succeeds, perform a sonatype release:

```
sbt sonatypeRelease
```

Update the version to the next snapshot and commit.