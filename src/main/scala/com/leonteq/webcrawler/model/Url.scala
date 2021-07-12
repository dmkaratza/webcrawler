package com.leonteq.webcrawler.model

import org.jsoup.nodes.Document
import scala.util.Random

sealed trait SeedEndpoint {
  def url: String

  def depth: Long

  def externalLinksLimit: Int
}

final case class Url(url: String, depth: Long, externalLinksLimit: Int) extends SeedEndpoint

final case class Page[T<: SeedEndpoint](url: T, document: Document)

final case class ExternalUrl[T<: SeedEndpoint](url: T, linkDomain: String, urlDomain: String)

case class CrawlerConfig(outputPath: String)

object CrawlerConfig {
  def getFileName(outputPath: String): String =
    s"$outputPath/${Random.alphanumeric.take(8).mkString}"
}
