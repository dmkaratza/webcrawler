package com.leonteq.webcrawler.base

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{TestKit, TestKitBase}
import com.leonteq.webcrawler.model.{CrawlerConfig, Url}
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

trait AkkaStreamSpecBase extends TestKitBase with Suite with BeforeAndAfterAll {
  override implicit lazy val system: ActorSystem = ActorSystem("test-Actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config: CrawlerConfig = CrawlerConfig(outputPath = "src/test/scala/com/leonteq/webcrawler")

  val url: Url = Url("https://en.wikipedia.org/wiki/Akka_(toolkit)", 2, 3)

  val timeout: FiniteDuration = 15 seconds

  override def afterAll(): Unit = {
    super.afterAll()
    materializer.shutdown()
    TestKit.shutdownActorSystem(system, 30 seconds)
  }
}
