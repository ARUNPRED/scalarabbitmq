import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.lang.reflect.{ParameterizedType, Type}

import Publisher.{QUEUE_NAME, serialize}
import com.google.gson.Gson
import com.rabbitmq.client._
import org.json4s._
import org.json4s.jackson.JsonMethods

object Subscriber extends App
{


  var channel = getAMQPChannel("inbound", "/")
  var consumer: QueueingConsumer = null
  var accesstoken = getaccesstoken()
  private val QUEUE_NAME = "outbound"

  while (true) {
    var task: QueueingConsumer.Delivery = null
    try { task = consumer.nextDelivery() }
    catch {
      case ex: Exception => {
        println("Error in AMQP connection: reconnecting.")//, ex)
        channel = getAMQPChannel("inbound", "/")
      }
    }

    if (task != null && task.getBody() != null) {
      val output = deserialize(task.getBody)
    //  val gson = new Gson
      //val jsonString = gson.toJson(output)
      val pgresponse = invokepgrestapi(output)
      if(pgresponse)
        {
          channel.basicPublish("", QUEUE_NAME, null, serialize(pgresponse))

        }
      try { channel.basicAck(task.getEnvelope().getDeliveryTag(), false) }
      catch {
        case ex: Exception => { println("Error ack'ing message.", ex) }
      }
    }
  }



  // Opens up a connection to RabbitMQ, retrying every five seconds
  // if the queue server is unavailable.
  def getAMQPChannel(queue: String, vhost: String) : Channel = {
    var attempts = 0
    var channel: Channel = null
    var connection: Connection = null

    println("Opening connection to AMQP " + vhost + " "  + queue + "...")
    try {
      connection = getConnection(queue, "localhost", 5672, "guest", "guest",vhost)
      channel = connection.createChannel()
      consumer = new QueueingConsumer(channel)
      channel.exchangeDeclare(queue, "direct", true)
      channel.queueDeclare(queue, true, false, false, null)
      channel.queueBind(queue, queue, queue)
      channel.basicConsume(queue, false, consumer)
      println("Connected to RabbitMQ")
    } catch {
      case ex: Exception => {
        println(".....cannot connect to AMQP. ")//, ex)
      }
    }
    channel
  }

  def getaccesstoken(): String ={
    val auth = new AccessToken
    val jsonresponse = auth.accesstoken()
    implicit val formats = org.json4s.DefaultFormats
    val jsonMap = JsonMethods.parse(jsonresponse).values.asInstanceOf[Map[String, String]]
    println(jsonMap.getOrElse("id_token", "401"))
    return jsonMap.getOrElse("id_token", "401")
  }

  def invokepgrestapi(dataobject:Manifesto): Boolean ={
    val invokeapi = new PGRestAPIService
    val trans = invokeapi.pgrestpost(accesstoken,dataobject)
    return trans
  }
  // Returns a new connection to an AMQP queue.
  def getConnection(queue: String, host: String, port: Int, username: String, password: String, vhost: String): Connection = {
    val factory = new ConnectionFactory()
    factory.setHost(host)
    factory.setPort(port)
    factory.setUsername(username)
    factory.setPassword(password)
    factory.setVirtualHost(vhost)
    factory.newConnection()
  }





  def deserialize(bytes: Array[Byte]): Manifesto = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val value = ois.readObject
    ois.close
    value.asInstanceOf[Manifesto]
  }

  def serialize(value: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close
    stream.toByteArray()
  }
  Subscriber.main(args)

}