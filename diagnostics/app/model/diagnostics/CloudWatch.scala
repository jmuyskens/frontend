package model.diagnostics  

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient
import com.amazonaws.handlers.AsyncHandler
import conf.Configuration
import com.amazonaws.services.cloudwatch.model._
import scala.collection.JavaConversions._
import common.Logging
import model.diagnostics._

trait CloudWatch extends Logging {

  lazy val cloudwatch = {
    val client = new AmazonCloudWatchAsyncClient(Configuration.aws.credentials)
    client.setEndpoint("monitoring.eu-west-1.amazonaws.com")
    client
  }

  object asyncHandler extends AsyncHandler[PutMetricDataRequest, Void] with Logging
  {
    def onError(exception: Exception)
    {
      log.info(s"CloudWatch PutMetricDataRequest error: ${exception.getMessage}}")
    }
    def onSuccess(request: PutMetricDataRequest, result: Void )
    {
      log.info("CloudWatch PutMetricDataRequest - sucess")
      Metric.reset // TODO shouldn't be here
    }
  }

  def put(namespace: String, metrics: scala.collection.mutable.Map[String, Int]) {

      val request = new PutMetricDataRequest().
        withNamespace(namespace).
        withMetricData(metrics.map{ case (name, count) => 
          new MetricDatum()
            .withValue(count.toDouble)
            .withMetricName(name)
            .withUnit("Count")
        })

      cloudwatch.putMetricDataAsync(request, asyncHandler)
  }
}

object CloudWatch extends CloudWatch