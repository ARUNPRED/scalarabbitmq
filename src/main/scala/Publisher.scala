import com.rabbitmq.client.ConnectionFactory
import java.io.{ByteArrayOutputStream, ObjectOutputStream}

object Publisher {

  private val QUEUE_NAME = "inbound"

  def main(argv: Array[String]) {
    val factory = new ConnectionFactory()
    factory.setHost("localhost")
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(QUEUE_NAME, true, false, false, null)
    val message1 = Manifesto("Informed", true)
    channel.basicPublish("", QUEUE_NAME, null, serialize(message1))
    println(" [x] Sent '" + message1 + "'")
    channel.close()
    connection.close()
  }
  def serialize(value: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close
    stream.toByteArray()
  }
}