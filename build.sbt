name := "clean-acme-inc"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val sprayClientV     = "1.3.2"
  val akkaV            = "2.4.7"
  val ficusV           = "1.2.4"
  val scalaTestV       = "3.0.0-M15"
  val slf4sV           = "1.7.10"
  val logbackV         = "1.1.3"
  val scalaCommonV     = "1.6.0"
  val casbahV          = "3.1.1"
  val spec2V           = "3.7.3"

  Seq(
    "com.typesafe.akka"   %% "akka-http-core"                    % akkaV,
    "com.typesafe.akka"   %% "akka-http-experimental"            % akkaV,
    "com.typesafe.akka"   %% "akka-http-spray-json-experimental" % akkaV,
    "io.spray"            %% "spray-client"                      % sprayClientV,
    "com.iheart"          %% "ficus"                             % ficusV,
    "org.mongodb"         %% "casbah"                            % casbahV,
    "com.typesafe.akka"   %% "akka-testkit"                      % akkaV          % Test,
    "org.scalatest"       %% "scalatest"                         % scalaTestV     % Test,
    "com.typesafe.akka"   %% "akka-http-testkit"                 % akkaV          % Test,
    "com.typesafe.akka"   %% "akka-stream-testkit"               % akkaV          % Test,
    "org.specs2"          %% "specs2-core"                       % spec2V       % Test
    )
}

scalacOptions := Seq(
  "-encoding", "utf8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-target:jvm-1.7",
  "-Xlog-reflective-calls",
  "-Ypatmat-exhaust-depth", "40",
  "-Xmax-classfile-name", "240", // for docker container
  "-Yrangepos",
  "-optimise"
)

mainClass in Compile := Some("acme.inc.Boot")
updateOptions := updateOptions.value.withCachedResolution(true)
