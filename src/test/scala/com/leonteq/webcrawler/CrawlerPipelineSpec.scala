package com.leonteq.webcrawler

import akka.stream.scaladsl.{Sink, Source}
import com.leonteq.webcrawler.base.AkkaStreamSpecBase
import com.leonteq.webcrawler.model.{Page, SeedEndpoint, Url}
import com.leonteq.webcrawler.samples.WikiHtml
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}

class CrawlerPipelineSpec extends AnyFreeSpec with AkkaStreamSpecBase with Matchers with ScalaFutures with TypeCheckedTripleEquals {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(4, Seconds), Span(10, Millis))
  private val crawlerPipeline = CrawlerPipeline(url, config)

  "CrawlerPipeline" - {
    "throws suitable error if seed url does not exist" in {
      val crawlerPipeline = CrawlerPipeline(Url("wiki", 2, 3), config)
      val result = crawlerPipeline.crawlingFlow.runWith(Sink.seq).failed.futureValue.getMessage
      result should ===("Could not download from url='wiki', got response code='Malformed URL: wiki'")
    }

    "parses no external pages when seed url is malformed" in {
      val result = crawlerPipeline.parseExternalLinksOnly(
        page = Page(Url("wiki", 2, 3), WikiHtml.sample())
      )
      result should ===(List.empty)
    }

    "returns the seed page when depth is 0" in {
      val urlWithDepth = url.copy(depth = 0)
      val pageWithDepth: Page[SeedEndpoint] = Page(url, WikiHtml.sample())
      val result = CrawlerPipeline(urlWithDepth, config)
        .downloadPages(urlWithDepth.depth, Source.single(pageWithDepth), Source.empty).runWith(Sink.seq).futureValue

      result should ===(Seq(pageWithDepth))
    }

    "returns 4 pages when seed url exists depth is 1, given that externalLinks is 3" in {
      val urlWithDepth = url.copy(depth = 1)
      val pageWithDepth: Page[SeedEndpoint] = Page(url, WikiHtml.sample())
      val result = CrawlerPipeline(urlWithDepth, config)
        .downloadPages(urlWithDepth.depth, Source.single(pageWithDepth), Source.empty).runWith(Sink.seq).futureValue

      result.size should ===(4)
    }

    "parses the configured number of valid external links per page" in {
      val result = crawlerPipeline.parseExternalLinksOnly(
        page = Page(url, WikiHtml.sample())
      )

      result.map(_.url.url) should ===(List(
        "https://fr.wikipedia.org/wiki/Akka_(logiciel)",
        "https://ko.wikipedia.org/wiki/Akka",
        "https://zh.wikipedia.org/wiki/Akka"))
    }
  }
}
