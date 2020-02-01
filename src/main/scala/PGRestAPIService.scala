import com.google.gson.Gson
import com.typesafe.config.ConfigFactory
import io.shaka.http.ContentType.{APPLICATION_FORM_URLENCODED, APPLICATION_JSON}
import io.shaka.http.{Entity, FormParameter}
import io.shaka.http.Http.http
import io.shaka.http.HttpHeader.AUTHORIZATION
import io.shaka.http.Https.{HttpsConfig, TrustAnyServer}
import io.shaka.http.Request.{GET, POST}
import io.shaka.http.Status.OK
import org.apache.commons.codec.binary.Base64


class PGRestAPIService {

  def pgrestget(accesstoken: String): List[Manifesto] = {


    implicit val https: Option[HttpsConfig] = Some(HttpsConfig(TrustAnyServer))
    val response = http(GET("http://localhost:3000/todos"))
    if (response.status == OK) {
      val data = new Gson()
      return data.fromJson(response.entityAsString, classOf[List[Manifesto]])
    }
    else
      return null
  }

  def pgrestpost(accesstoken: String, data: Manifesto): Boolean = {

    val jsonval = new Gson()
    val taskdata = jsonval.toJson(data)
    println(taskdata)

    implicit val https: Option[HttpsConfig] = Some(HttpsConfig(TrustAnyServer))
    val token = "Bearer " ++ accesstoken;
    val response = http(POST("http://localhost:3000/todos").contentType(APPLICATION_JSON).entity(taskdata).header(AUTHORIZATION, token))
    println(response.status)
    if (response.status == OK) {
      return true
    }
    else {
      return false
    }
  }
}
