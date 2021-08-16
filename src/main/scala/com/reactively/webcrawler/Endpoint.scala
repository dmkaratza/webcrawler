package com.reactively.webcrawler

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.reactively.webcrawler.model.{CrawlerConfig, Url}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Endpoint extends App {
  implicit val system: ActorSystem = ActorSystem("Crawler")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config: CrawlerConfig = ConfigSource.default.loadOrThrow[CrawlerConfig]

  val seedUrl =
    Url(url = System.getProperty("start-url"))
  val result = CrawlerPipeline(seedUrl, config).run

  result.onComplete {
    case Success(_) =>
      println(s"Successfully downloaded the web pages the from the seed url")
      system.terminate()
    case Failure(error) =>
      println(s"Error occurred while crawling pages, reason: $error")
      system.terminate()
  }
}
