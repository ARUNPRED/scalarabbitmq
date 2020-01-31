import com.google.gson.Gson
import com.typesafe.config.ConfigFactory
import io.shaka.http.ContentType.APPLICATION_FORM_URLENCODED
import io.shaka.http.FormParameter
import io.shaka.http.Https.{HttpsConfig, TrustAllSslCertificates, TrustAnyServer}
import io.shaka.http.Http.http
import io.shaka.http.HttpHeader.AUTHORIZATION
import io.shaka.http.Request.{GET, POST}
import io.shaka.http.Status.OK
import org.apache.commons.codec.binary.Base64



class AccessToken  {

  def accesstoken (): String = {


    implicit val https: Option[HttpsConfig] = Some(HttpsConfig(TrustAnyServer))
    val errormessage = Error("401", "Unauthorized")
    val errorreturn = new Gson()

    val conf = ConfigFactory.load()
    val url = conf.getString("gluuurl")
    val grant = conf.getString("grant_type")
    val ClientID = conf.getString("ClientID")
    val ClientSecret = conf.getString("ClientSecret")
    val username = "testing"
    val password = "testing"
    val bytesFromString = (ClientID ++ ":" ++ ClientSecret).getBytes("UTF-8")

    val urlAuth = "Basic " ++ Base64.encodeBase64String(bytesFromString)
    val response = http(POST(url).contentType(APPLICATION_FORM_URLENCODED).basicAuth(ClientID, ClientSecret).formParameters(FormParameter("grant_type", grant),FormParameter("username", username), FormParameter("password",password),FormParameter("scope","openid+profile")))
    if (response.status == OK) {
      return response.entityAsString;
    }
    else {
      return errorreturn.toJson(errormessage)
    }
  }
}
