package com.knoldus

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import com.knoldus.ConfigurationManager._
import com.knoldus.Constants._
import com.knoldus.model.{AwsCredentials, VaultFailureResponse, VaultResponse, VaultSuccessResponse}
import org.apache.log4j.Logger

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.util.{Failure, Success}

object VaultAPIMain extends App {

  implicit val system: ActorSystem = ActorSystem("SampleVault")

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val logger = Logger.getLogger(this.getClass)
  val http: HttpExt = Http.apply()
  val vault = new VaultAPI(vaultRootToken, http)

  vault.getCredentials.onComplete {

    case _@Success(Some(awsCredentials)) => logger.info(s"Access Key ID: ${awsCredentials.accessKeyId} \nSecret Access Key: ${awsCredentials.secretAccessKey}")

    case _@Success(None) =>
      logger.error("Either AWS Access Key ID or AWS Secret Access Key or both is missing.")
      throw new Exception("Either AWS Access Key ID or AWS Secret Access Key or both is missing.")

    case Failure(exception) =>
      exception.printStackTrace
      throw exception
  }
}

class VaultAPI(clientToken: String, http: HttpExt)(implicit
                                                   val system: ActorSystem,
                                                   val materializer: ActorMaterializer,
                                                   val executionContext: ExecutionContextExecutor) {

  val logger = Logger.getLogger(this.getClass)

  def getCredentials: Future[Option[AwsCredentials]] = {

    val httpRequest = HttpRequest(HttpMethods.GET, Uri(url + uri + awsStore))
      .withHeaders(RawHeader(VAULT_CLIENT_TOKEN, clientToken))
    val vaultResponse = getVaultResponse(httpRequest)

    vaultResponse.map {
      case successResponse: VaultSuccessResponse =>
        val awsAccessKeyIDOpt = successResponse.data.data.get(AWS_ACCESS_KEY_ID)
        val awsSecretAccessKeyOpt = successResponse.data.data.get(AWS_SECRET_ACCESS_KEY)

        (awsAccessKeyIDOpt, awsSecretAccessKeyOpt) match {
          case (Some(awsAccessKeyID), Some(awsSecretAccessKey)) => Some(AwsCredentials(awsAccessKeyID, awsSecretAccessKey))
          case _ => None
        }
      case failureResponse: VaultFailureResponse =>
        logger.error(s"HttpRequest $httpRequest failed with Errors: ${failureResponse.errors}")
        None
    }
  }

  /**
    * *
    * To get the VaultResponse for the specified HttpRequest.
    *
    * @param httpRequest The HTTP request for which the the Vault API request needs to be executed.
    * @return a `VaultSuccessResponse` if the HttpRequest is successful, or `VaultFailureResponse` if unsuccessful.
    */
  private def getVaultResponse(httpRequest: HttpRequest): Future[VaultResponse] = {

    import com.knoldus.model.JsonSupport._

    http.singleRequest(httpRequest)
      .flatMap {
        /* Note - This case handles the success response from the vault, as indicated in their API.
        https://www.vaultproject.io/api/overview.html#http-status-codes*/
        case response: HttpResponse if response.status == StatusCodes.OK || response.status == StatusCodes.NoContent =>
          response.toStrict(FiniteDuration(REQUEST_TIMEOUT_IN_MS, MILLISECONDS)).flatMap {
            strictEntity =>
              Unmarshal(strictEntity).to[VaultSuccessResponse]
          }
        case response: HttpResponse if response.status == StatusCodes.NotFound =>
          response.toStrict(FiniteDuration(REQUEST_TIMEOUT_IN_MS, MILLISECONDS)).map {
            httpReponse =>
              VaultFailureResponse(List("Credentials not found"))
          }
        case response: HttpResponse =>
          logger.error(s"HttpRequest $httpRequest failed with Status code: ${response.status}")
          response.toStrict(FiniteDuration(REQUEST_TIMEOUT_IN_MS, MILLISECONDS)).flatMap {
            strictEntity =>
              Unmarshal(strictEntity).to[VaultFailureResponse]
          }
      }.recoverWith {
      case exception =>
        logger.error(s"Received exception $exception while fetching credentials from Vault for ${httpRequest.uri}")
        throw exception
    }
  }
}
