package com.reactively.webcrawler

import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import akka.{Done, NotUsed}
import cats.implicits._
import CrawlerPipeline._
import com.reactively.webcrawler.model.{CrawlerConfig, Page, SeedEndpoint, Url}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.nio.file.Paths
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Try

class CrawlerPipeline(seedEndpoint: SeedEndpoint,
                      downloadSeedDocument: SeedEndpoint => Future[Document],
                      downloadPageOpt: SeedEndpoint => DownloadedPageType,
                      config: CrawlerConfig)
                     (implicit executionContext: ExecutionContextExecutor, materializer: ActorMaterializer) {

  def run: Future[Done] =
    crawlingFlow
      .via(fileFlow)
      .runWith(Sink.ignore)

  def crawlingFlow: SourceType =
    Source.fromFuture(downloadSeedDocument(seedEndpoint))
      .flatMapMerge(1, doc =>
        downloadPages(config.depth, Source.single(Page(seedEndpoint, doc)), Source.empty)
      )

  @tailrec
  final def downloadPages(depth: Long, seedPages: SourceType, downloadedPages: SourceType): SourceType = {
    if (depth == 0) seedPages.merge(downloadedPages)
    else {
      val updatedSeedPages =
        seedPages
          .mapAsync(3) { page =>
            parseExternalLinksOnly(page, config.linksLimit)
              .map { extUrl =>
                downloadPageOpt(extUrl).map(_.toList)
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
  def parseExternalLinksOnly(page: Page[SeedEndpoint], linksLimit: Int): List[SeedEndpoint] =
    page.extractLinks
      .filterNot(url => url.extractLinkDomain == page.extractUrlDomain)
      .take(linksLimit)

  private def fileFlow: Flow[Page[SeedEndpoint], IOResult, NotUsed] =
    Flow[Page[SeedEndpoint]].mapAsync(4) { page ⇒
      Source.single(ByteString(s"${page.url.url} \n ${page.document.html()}"))
        .runWith(FileIO.toPath(Paths.get(config.getFileName)))
    }
}

object CrawlerPipeline {
  type SourceType = Source[Page[SeedEndpoint], NotUsed]
  type DownloadedPageType = Future[Option[Page[SeedEndpoint]]]

  def apply(seedUrl: Url, config: CrawlerConfig)
           (implicit executionContext: ExecutionContextExecutor, materializer: ActorMaterializer) =
    new CrawlerPipeline(seedUrl, downloadSeedDocument, downloadPageOpt, config)

  def downloadPageOpt(url: SeedEndpoint)(implicit ec: ExecutionContextExecutor): DownloadedPageType = Future {
    Try {
      val document = Jsoup.connect(url.url).get()
      Page(url, document)
    }.toOption
  }

  def downloadSeedDocument(url: SeedEndpoint): Future[Document] = Future.fromTry(
    Try(Jsoup.connect(url.url).get())
      .recoverWith {
        case error: Throwable =>
          throw new Exception(s"Could not download from url='$url', got response code='${error.getMessage}'")
      })
}
