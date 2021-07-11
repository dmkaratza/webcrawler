package com.leonteq.webcrawler

import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import akka.{Done, NotUsed}
import cats.implicits._
import com.leonteq.webcrawler.CrawlerPipeline._
import com.leonteq.webcrawler.model.{CrawlerConfig, ExternalUrl, Page, SeedEndpoint, Url}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.net.URL
import java.nio.file.Paths
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Try
import scala.annotation.tailrec

class CrawlerPipeline[T <: SeedEndpoint](seedEndpoint: T,
                                         config: CrawlerConfig)
                                        (implicit executionContext: ExecutionContextExecutor, materializer: ActorMaterializer) {

  def run: Future[Done] =
    crawlingFlow
      .via(fileFlow)
      .runWith(Sink.ignore)

  def crawlingFlow: Source[Page, NotUsed] =
    Source.fromFuture(downloadDocument(seedEndpoint.url))
      .flatMapMerge(1, doc =>
        downloadPages(seedEndpoint, Source.single(Page(seedEndpoint.url, seedEndpoint.depth, seedEndpoint.externalLinksLimit, doc))))

  @tailrec
  final def downloadPages[T <: SeedEndpoint](url: T, accumulatedPages: Source[Page, NotUsed]): Source[Page, NotUsed] = {
    if (url.depth == 0) accumulatedPages
    else {
      val downloadedPages =
        accumulatedPages
          .mapAsync(3) { page =>
            parseExternalLinksOnly(page)
              .map{extUrl =>
//                println(s">>>downloading ${extUrl.url.url}, depth = ${url.depth}")
                downloadDocumentOpt(extUrl.url).map(_.toList)}.sequence
          }
          .flatMapConcat { listOfOptDocs =>
            Source.fromIterator(() => listOfOptDocs.flatten.toIterator)
          }
      downloadPages(Url(url.url, url.depth - 1, url.externalLinksLimit), downloadedPages.merge(accumulatedPages))
    }
  }

  /**
   * @param page Web page to parse the external links from
   * @return A list of urls which do not have the same domain with the seed url - external links
   */
  def parseExternalLinksOnly(page: Page): List[ExternalUrl] =
    extractUrlDomain(page.url).toList
      .flatMap(urlDomain =>
        page.document.getElementsByAttributeStarting("href").asScala
          .toList
          .map(_.attr("abs:href"))
          .filter(_.startsWith("http"))
          .flatMap(link => extractLinkDomain(link).map(linkDomain =>
            ExternalUrl(Url(link, page.depth, page.externalLinksLimit), linkDomain, urlDomain)
          ))
      ).filterNot(externalUrl => externalUrl.linkDomain == externalUrl.urlDomain)
      .take(page.externalLinksLimit)

  private def fileFlow: Flow[Page, IOResult, NotUsed] =
    Flow[Page].mapAsync(4) { page ⇒
//      println(s">>>>> ${page.url}")
      Source.single(ByteString(s"${page.url} \n ${page.document.html()}")).runWith(FileIO.toPath(Paths.get(CrawlerConfig.getFileName(config.outputPath))))
    }
}

object CrawlerPipeline {
  def apply(source: Url, config: CrawlerConfig)
           (implicit executionContext: ExecutionContextExecutor, materializer: ActorMaterializer) =
    new CrawlerPipeline(source, config)

  def downloadDocumentOpt(url: Url): Future[Option[Page]] = Future.successful(
    Try {
      val document = Jsoup.connect(url.url).get()
      Page(url.url, url.depth, url.externalLinksLimit, document)
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
