package com.leonteq.webcrawler

import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import akka.{Done, NotUsed}
import cats.implicits._
import com.leonteq.webcrawler.CrawlerPipeline._
import com.leonteq.webcrawler.model._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.net.URL
import java.nio.file.Paths
import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Try

class CrawlerPipeline[T <: SeedEndpoint](seedEndpoint: T,
                                         config: CrawlerConfig)
                                        (implicit executionContext: ExecutionContextExecutor, materializer: ActorMaterializer) {

  def run: Future[Done] =
    crawlingFlow
      .via(fileFlow)
      .runWith(Sink.ignore)

  def crawlingFlow: SourceType =
    Source.fromFuture(downloadDocument(seedEndpoint.url))
      .flatMapMerge(1, doc =>
        downloadPages(seedEndpoint.depth, Source.single(Page(seedEndpoint, doc)), Source.empty)
      )

  @tailrec
  final def downloadPages(depth: Long, seedPages: SourceType, downloadedPages: SourceType): SourceType = {
    if (depth == 0) seedPages.merge(downloadedPages)
    else {
      val updatedSeedPages =
        seedPages
          .mapAsync(3) { page =>
            parseExternalLinksOnly(page)
              .map { extUrl =>
                downloadDocumentOpt(extUrl.url).map(_.toList)
              }.sequence
          }
          .flatMapConcat { listOfOptDocs =>
            Source.fromIterator(() => listOfOptDocs.flatten.toIterator)
          }
      downloadPages(depth - 1, updatedSeedPages, seedPages.merge(downloadedPages))
    }
  }

  /**
   * @param page Web page to parse the external links from
   * @return A list of urls which do not have the same domain with the seed url - external links
   */
  def parseExternalLinksOnly[T <: SeedEndpoint](page: Page[T]): List[ExternalUrl[SeedEndpoint]] =
    extractUrlDomain(page.url.url).toList
      .flatMap(urlDomain =>
        page.document.getElementsByAttributeStarting("href").asScala
          .toList
          .map(_.attr("abs:href"))
          .filter(_.startsWith("http"))
          .flatMap(link => extractLinkDomain(link).map(linkDomain =>
            ExternalUrl(Url(link, page.url.depth, page.url.externalLinksLimit): SeedEndpoint, linkDomain, urlDomain)
          ))
      ).filterNot(externalUrl => externalUrl.linkDomain == externalUrl.urlDomain)
      .take(page.url.externalLinksLimit)

  private def fileFlow: Flow[Page[SeedEndpoint], IOResult, NotUsed] =
    Flow[Page[SeedEndpoint]].mapAsync(4) { page ⇒
      Source.single(ByteString(s"${page.url.url} \n ${page.document.html()}")).runWith(FileIO.toPath(Paths.get(CrawlerConfig.getFileName(config.outputPath))))
    }
}

object CrawlerPipeline {
  type SourceType = Source[Page[SeedEndpoint], NotUsed]

  def apply(source: Url, config: CrawlerConfig)
           (implicit executionContext: ExecutionContextExecutor, materializer: ActorMaterializer) =
    new CrawlerPipeline(source, config)

  def downloadDocumentOpt[T <: SeedEndpoint](url: T): Future[Option[Page[T]]] = Future.successful(
    Try {
      val document = Jsoup.connect(url.url).get()
      Page(url, document)
    }.toOption
  )

  def downloadDocument(url: String): Future[Document] = Future.fromTry(
    Try(Jsoup.connect(url).get())
      .recoverWith {
        case error: Throwable => throw new Exception(s"Could not download from url='$url', got response code='${error.getMessage}'")
      })

  def extractLinkDomain(url: String): Option[String] = Try(new URL(url)).toOption.map(_.getHost)

  def extractUrlDomain(url: String): Option[String] =
    Try {
      val urlObj = new URL(url)
      val domain = s"${urlObj.getProtocol}://${urlObj.getHost}"
      new URL(domain).getHost
    }.toOption
}
