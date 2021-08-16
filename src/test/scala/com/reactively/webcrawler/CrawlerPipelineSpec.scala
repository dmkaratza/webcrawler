package com.reactively.webcrawler

import akka.stream.scaladsl.{Sink, Source}
import com.reactively.webcrawler.CrawlerPipeline.DownloadedPageType
import com.reactively.webcrawler.base.AkkaStreamSpecBase
import com.reactively.webcrawler.model.{CrawlerConfig, Page, SeedEndpoint, Url}
import com.reactively.webcrawler.samples.WikiHtml
import org.jsoup.nodes.Document
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.Future

class CrawlerPipelineSpec extends AnyFreeSpec with AkkaStreamSpecBase with Matchers with ScalaFutures with TypeCheckedTripleEquals {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(4, Seconds), Span(10, Millis))

  private def initCrawler(seedUrl: Url,
                          downloadSeedDocument: SeedEndpoint => Future[Document],
                          downloadPageOpt: SeedEndpoint => DownloadedPageType,
                          crawlerConfig: CrawlerConfig = config): CrawlerPipeline = {
    new CrawlerPipeline(seedUrl, downloadSeedDocument, downloadPageOpt, crawlerConfig)
  }

  "CrawlerPipeline" - {
    "throws suitable error if seed url does not exist" in {
      val crawlerPipeline = initCrawler(
        seedUrl = Url("wiki"),
        downloadSeedDocument = _ => Future.failed(new Exception("Could not download from url='wiki', got response code='Malformed URL: wiki'")),
        downloadPageOpt = _ => Future.successful(None)
      )
      val result = crawlerPipeline.crawlingFlow.runWith(Sink.seq).failed.futureValue.getMessage
      result should ===("Could not download from url='wiki', got response code='Malformed URL: wiki'")
    }

    "returns 2 pages" in {
      val crawlerPipeline = initCrawler(
        seedUrl = url,
        downloadSeedDocument = _ => Future.successful(WikiHtml.sample()),
        downloadPageOpt = _ => Future.successful(Some(Page(url, WikiHtml.sample()))),
      )
      val result = crawlerPipeline.crawlingFlow.runWith(Sink.seq).futureValue
      result.size should ===(13)
    }

    "parses the configured number of valid external links per page" in {
      val crawlerPipeline = initCrawler(
        seedUrl = url,
        downloadSeedDocument = _ => Future.successful(WikiHtml.sample()),
        downloadPageOpt = _ => Future.successful(Some(Page(url, WikiHtml.sample()))),
      )
      val result = crawlerPipeline.parseExternalLinksOnly(
        page = Page(url, WikiHtml.sample()),
        linksLimit = 3
      )

      result.map(_.url) should ===(List(
        "https://fr.wikipedia.org/wiki/Akka_(logiciel)",
        "https://ko.wikipedia.org/wiki/Akka",
        "https://zh.wikipedia.org/wiki/Akka"))
    }

    "returns the seed page when depth is 0" in {
      val crawlerPipeline = initCrawler(
        seedUrl = url,
        downloadSeedDocument = _ => Future.successful(WikiHtml.sample()),
        downloadPageOpt = _ => Future.successful(Some(Page(url, WikiHtml.sample()))),
        crawlerConfig = config.copy(depth = 0)
      )
      val pageWithDepth: Page[SeedEndpoint] = Page(url, WikiHtml.sample())
      val result = crawlerPipeline.downloadPages(0, Source.single(pageWithDepth), Source.empty).runWith(Sink.seq).futureValue

      result should ===(Seq(pageWithDepth))
    }


    "returns 13 pages when seed url exists depth is 1, given that externalLinks is 3" in {
      val crawlerPipeline = initCrawler(
        seedUrl = url,
        downloadSeedDocument = _ => Future.successful(WikiHtml.sample()),
        downloadPageOpt = _ => Future.successful(Some(Page(url, WikiHtml.sample()))),
        crawlerConfig = config.copy(depth = 1)
      )

      val result = crawlerPipeline.downloadPages(config.depth,
        Source.single(Page(url, WikiHtml.sample())), Source.empty).runWith(Sink.seq).futureValue

      result.size should ===(13)
    }
  }
}
