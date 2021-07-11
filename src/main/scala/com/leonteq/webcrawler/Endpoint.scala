package com.leonteq.webcrawler

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.leonteq.webcrawler.model.{CrawlerConfig, Url}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Endpoint extends App {
  implicit val system: ActorSystem = ActorSystem("Crawler")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config: CrawlerConfig = ConfigSource.default.loadOrThrow[CrawlerConfig]

  val source =
    Url("https://en.wikipedia.org/wiki/Akka_(toolkit)", 2, 3)
  //    Source
  //    .single(Url(
  //    System.getProperty("start-url"),
  //    System.getProperty("depth").toLong,
  //    System.getProperty("links-limit").toInt))
  val result = CrawlerPipeline(source, config).run

  result.onComplete {
    case Success(_) =>
      println(s"Successfully downloaded the web pages the from the seed url")
      system.terminate()
    case Failure(error) =>
      println(s"Error occurred while crawling pages, reason: $error")
      system.terminate()
  }
}
