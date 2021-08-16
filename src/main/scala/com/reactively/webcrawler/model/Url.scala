package com.reactively.webcrawler.model

import org.jsoup.nodes.Document

import java.net.URL
import scala.util.{Random, Try}
import scala.collection.JavaConverters._

final case class CrawlerConfig(depth: Long, linksLimit: Int, outputPath: String) {
  def getFileName: String = s"$outputPath/${Random.alphanumeric.take(8).mkString}"
}

trait SeedEndpoint {
  def url: String
}

final case class Url(url: String) extends SeedEndpoint {
  def extractLinkDomain: Option[String] = Try(new URL(url)).toOption.map(_.getHost)
}

final case class Page[T <: SeedEndpoint](url: T, document: Document) {
  def extractUrlDomain: Option[String] =
    Try {
      val urlObj = new URL(url.url)
      val domain = s"${urlObj.getProtocol}://${urlObj.getHost}"
      new URL(domain).getHost
    }.toOption

  def extractLinks: List[Url] =
    document.getElementsByAttributeStarting("href").asScala
      .toList
      .map(_.attr("abs:href"))
      .filter(_.startsWith("http"))
      .map(Url)
}
