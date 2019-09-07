package examples

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, SocketChannel}
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ArrayBlockingQueue, Executors}

import com.typesafe.scalalogging.LazyLogging
import mess.ast.MsgPack
import org.msgpack.core.MessagePack

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}

object NonBlockingIOMain extends LazyLogging {

  private[this] val selector = Selector.open()

  private[this] val pending = new ArrayBlockingQueue[WriteTask](1000)
  private[this] val running = new AtomicBoolean(false)

  @volatile private[this] var stopped = false

  private[this] def emitter(c: SocketChannel): Runnable =
    () => {
      while (!stopped) {
        if (running.get)
          NANOSECONDS.sleep(5)
        else
          Option(pending.poll(250, MILLISECONDS)) match {
            case Some(task) =>
              running.set(true)
              logger.info(s"emit task: $task")
              c.register(selector, SelectionKey.OP_WRITE, task)
              selector.wakeup()
            case _ => ()
          }
      }

      logger.info("stopped emitter")
    }

  private[this] val sender = new Runnable {

    override def run(): Unit = {
      val rest = ByteBuffer.allocate(128)
      logger.info("started")
      while (!stopped) try {
        if (selector.select(100) > 0) {
          val keys = selector.selectedKeys()
          logger.info(s"selected keys: ${keys.size()}")
          val i = keys.iterator()
          while (i.hasNext) {
            val key        = i.next()
            val sc         = key.channel().asInstanceOf[SocketChannel]
            val attachment = key.attachment()
            i.remove()

            logger.info(
              "got the key is: valid: {}, writable: {}, readable: {}, acceptable: {}, connectable: {}, attachment: {}.",
              key.isValid,
              key.isWritable,
              key.isReadable,
              key.isAcceptable,
              key.isConnectable,
              attachment
            )

            if (key.isConnectable) {
              logger.info(s"connect: ${key.toString}")

              val address = attachment.asInstanceOf[InetSocketAddress]
              sc.connect(address)
              while (!sc.finishConnect()) MILLISECONDS.sleep(1)

              if (running.compareAndSet(true, false)) {
                logger.info(s"connect: done")
              }
            } else if (key.isWritable) {
              logger.info(s"write: got the attachment: $attachment")

              val chunk  = UUID.randomUUID().toString
              val option = MsgPack.fromPairs(MsgPack.fromString("chunk") -> MsgPack.fromString(chunk))
              val task   = attachment.asInstanceOf[WriteTask]
              val msg    = MsgPack.fromVector(task.event :+ option)
              logger.info(s"write: message: {}", msg)
              val encoded = MsgPack.pack(msg, MessagePack.DEFAULT_PACKER_CONFIG)
              val bytes   = ByteBuffer.wrap(encoded)

              while (bytes.hasRemaining) sc.write(bytes)

              sc.register(key.selector(), SelectionKey.OP_READ, ReadTask(chunk, task.promise))
            } else if (key.isReadable) {
              logger.info(s"read: got the attachment: $attachment")
              val task = attachment.asInstanceOf[ReadTask]
              rest.flip()
              rest.clear()

              val sc = key.channel().asInstanceOf[SocketChannel]
              while (sc.read(rest) != 0) MILLISECONDS.sleep(1)
              rest.rewind()

              logger.info(s"read: got the chunk: ${rest.duplicate().array()}")

              val unpacked = MsgPack.unpack(MessagePack.DEFAULT_UNPACKER_CONFIG.newUnpacker(rest))
              logger.info(s"received ack: $unpacked")

              val ack = unpacked.asMap.get(MsgPack.fromString("ack")).asString.get
              if (ack != task.chunk)
                task.promise.failure(new Exception(s"got the unexpected ack: $ack, chunk: ${task.chunk}"))
              else
                task.promise.success(())

              if (running.compareAndSet(true, false)) {
                logger.info(s"read: done")
              }
            } else {
              logger.info("skipped.")
              if (running.compareAndSet(true, false)) {}
            }
          }
          logger.info("running: {}", running.get)
        }

        MILLISECONDS.sleep(1)
      } catch {
        case _: InterruptedException =>
          logger.info("interrupted")
          stopped = true
          Thread.currentThread().interrupt()
      }
      logger.info("stopped sender")
    }

  }

  def send(c: SocketChannel, event: Vector[MsgPack]): Future[Unit] = {
    val p = Promise[Unit]

    if (!pending.offer(WriteTask(event, p)))
      p.failure(new Exception("full"))

    p.future
  }

  def main(args: Array[String]): Unit = {
    logger.info("start sender")
    val senderEx = Executors.newSingleThreadExecutor(r => {
      val t = new Thread(r)
      t.setName("sender")
      t
    })
    senderEx.execute(sender)

    logger.info("start connect")
    val c = SocketChannel.open()
    c.configureBlocking(false)
    c.register(selector, SelectionKey.OP_CONNECT, new InetSocketAddress("localhost", 24224))
    running.set(true)

    val emitterEx = Executors.newSingleThreadExecutor(r => {
      val t = new Thread(r)
      t.setName("emitter")
      t
    })
    emitterEx.execute(emitter(c))

    logger.info("start send")
    val tasks = (1 to 100).map { i =>
      val event =
        Vector(
          MsgPack.fromString("tag.name"),
          MsgPack.fromLong(System.currentTimeMillis() / 1000L),
          MsgPack.fromPairs(MsgPack.fromString("num") -> MsgPack.fromInt(i))
        )

      send(c, event)
    }

    val executor                      = Executors.newSingleThreadExecutor()
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executor)

    Await.result(Future.sequence(tasks), Duration.Inf).foreach { check =>
      logger.info(check.toString)
    }

    stopped = true

    c.close()

    logger.info("done")

    MILLISECONDS.sleep(1000)

    senderEx.shutdown()
    emitterEx.shutdown()
    executor.shutdown()
  }
}

final case class WriteTask(event: Vector[MsgPack], promise: Promise[Unit])
final case class ReadTask(chunk: String, promise: Promise[Unit])
