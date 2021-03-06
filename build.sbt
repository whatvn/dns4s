/*
 * Copyright 2014-2016 Michael Krolikowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import ReleaseTransformations._

lazy val scalaVersions = "2.12.0" :: "2.11.8" :: "2.10.6" :: Nil
lazy val guavaVersion = "[15.0,21.0["
lazy val akkaVersion = "[2.3.0,2.5.0["
lazy val nettyVersion = "[4.0.0,4.2.0["
lazy val scalaTestVersion = "3.0.0"
lazy val scalaCheckVersion = "1.13.4"

def projectSettings(n: String, d: String) = Seq(
  name := n,
  description := d,
  organization := "com.github.mkroli",
  scalaVersion := scalaVersions.head,
  scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation") ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 10 | 11)) => Seq("-target:jvm-1.6")
    case Some((2, 12)) => Seq("-target:jvm-1.8")
    case x => Seq.empty
  }),
  crossScalaVersions := scalaVersions,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  autoAPIMappings := true,
  licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://github.com/mkroli/dns4s")),
  pomExtra := (
    <scm>
      <url>git@github.com:mkroli/dns4s.git</url>
      <connection>scm:git:git@github.com:mkroli/dns4s.git</connection>
    </scm>))

def projectOsgiSettings(bundleName: String, packagesPrefix: String, packages: String*) = osgiSettings ++ Seq(
  OsgiKeys.exportPackage := packages.map(pkg => packagesPrefix :: (if (pkg.isEmpty) Nil else pkg :: "*" :: Nil) mkString "."),
  OsgiKeys.privatePackage := Nil,
  OsgiKeys.additionalHeaders += "Bundle-Name" -> bundleName)

lazy val dns4sProjectSettings = Seq(
  libraryDependencies ++= Seq(
    "com.google.guava" % "guava" % guavaVersion,
    "com.google.code.findbugs" % "jsr305" % "+" % "provided",
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test"))

lazy val dns4sAkkaProjectSettings = Seq(
  libraryDependencies ++= Seq(
    "com.google.guava" % "guava" % guavaVersion,
    "com.google.code.findbugs" % "jsr305" % "+" % "provided",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"))

lazy val dns4sNettyProjectSettings = Seq(
  libraryDependencies ++= Seq(
    "io.netty" % "netty-handler" % nettyVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"))

lazy val projectReleaseSettings = Seq(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    setNextVersion,
    commitNextVersion))

lazy val parentSettings = Seq(
  publishArtifact := false)

lazy val siteSettings = Seq(
  scalacOptions ++= Seq("-skip-packages", "akka.pattern", "-doc-title", name.value, "-doc-version", version.value),
  git.remoteRepo := "git@github.com:mkroli/dns4s.git",
  siteMappings ++= (mappings in(ScalaUnidoc, packageDoc)).value)

lazy val dns4sRoot = Project(
  id = "dns4s",
  base = file("."),
  settings = Defaults.coreDefaultSettings ++
    projectSettings("dns4s", "Scala DNS implementation") ++
    projectReleaseSettings ++
    parentSettings ++
    unidocSettings ++
    ghpages.settings ++
    siteSettings)
  .aggregate(dns4sCore, dns4sAkka, dns4sNetty)

lazy val dns4sCore = Project(
  id = "dns4s-core",
  base = file("core"),
  settings = Defaults.coreDefaultSettings ++
    projectOsgiSettings("dns4s-core", "com.github.mkroli.dns4s", "", "dsl", "section") ++
    projectSettings("dns4s-core", "Scala DNS implementation") ++
    dns4sProjectSettings)
  .enablePlugins(SbtOsgi)

lazy val dns4sAkka = Project(
  id = "dns4s-akka",
  base = file("akka"),
  settings = Defaults.coreDefaultSettings ++
    projectOsgiSettings("dns4s-akka", "com.github.mkroli.dns4s", "akka") ++
    projectSettings("dns4s-akka", "Scala DNS implementation - Akka extension") ++
    dns4sAkkaProjectSettings)
  .enablePlugins(SbtOsgi)
  .dependsOn(dns4sCore)

lazy val dns4sNetty = Project(
  id = "dns4s-netty",
  base = file("netty"),
  settings = Defaults.coreDefaultSettings ++
    projectOsgiSettings("dns4s-netty", "com.github.mkroli.dns4s", "netty") ++
    projectSettings("dns4s-netty", "Scala DNS implementation - Netty extension") ++
    dns4sNettyProjectSettings)
  .enablePlugins(SbtOsgi)
  .dependsOn(dns4sCore)
