package com.leonteq.webcrawler.model

import org.jsoup.nodes.Document
import scala.util.Random

trait SeedEndpoint {
  def url: String

  def depth: Long

  def externalLinksLimit: Int
}

final case class Url(url: String, depth: Long, externalLinksLimit: Int) extends SeedEndpoint

final case class Page(url: String, depth: Long, externalLinksLimit: Int, document: Document) extends SeedEndpoint

final case class ExternalUrl(url: Url, linkDomain: String, urlDomain: String)

case class CrawlerConfig(outputPath: String)

object CrawlerConfig {
  def getFileName(outputPath: String): String =
    s"$outputPath/${Random.alphanumeric.take(8).mkString}"
}
