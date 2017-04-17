package fluflu

import java.io.IOException
import java.lang.{ Boolean => JBool }
import java.net.{ InetSocketAddress, StandardSocketOptions }
import java.nio.ByteBuffer
import java.nio.channels.{ NotYetConnectedException, SocketChannel }
import java.time.{ Clock, Duration, Instant }
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.atomic.AtomicReference

import cats.instances.either._
import cats.syntax.option._
import cats.syntax.either._
import cats.syntax.flatMap._
import scala.annotation.tailrec
import scala.compat.java8.FunctionConverters._
import scala.concurrent.blocking

final case class Connection(
    remote: InetSocketAddress,
    reconnectionTimeout: Duration,
    reconnectionBackoff: Backoff
)(implicit clock: Clock) {
  import StandardSocketOptions._

  private[this] val channel: AtomicReference[Either[Throwable, Option[SocketChannel]]] =
    new AtomicReference(go(open, 0, Instant.now(clock)))

  private[this] def open = {
    val ch = SocketChannel.open()
    ch.setOption[JBool](TCP_NODELAY, true)
    ch
  }

  @tailrec private[this] def go(x: SocketChannel, retries: Int, start: Instant): Either[Throwable, Option[SocketChannel]] = {
    try {
      if (x.connect(remote)) Either.right(x.some) else Either.left(new IOException("Failed to connect"))
    } catch {
      case e: IOException =>
        if (Instant.now(clock).minusNanos(reconnectionTimeout.toNanos).compareTo(start) <= 0) {
          blocking(NANOSECONDS.sleep(reconnectionBackoff.nextDelay(retries).toNanos))
          x.close()
          go(open, retries + 1, start)
        } else {
          if (x.isOpen) x.close()
          Either.left(e)
        }
    }
  }

  def connect(): Either[Throwable, Option[SocketChannel]] =
    channel.updateAndGet(asJavaUnaryOperator {
      case t @ Right(Some(ch)) if ch.isConnected || ch.isConnectionPending => t
      case _ => go(open, 0, Instant.now(clock))
    })

  def isClosed: Boolean = channel.get.fold(
    Function.const(false),
    _.fold(false)(ch => !(ch.isConnected || ch.isConnectionPending))
  )

  def write(message: ByteBuffer): Either[Throwable, Unit] = {
    val r = connect() >>= (ch => Either.catchNonFatal(ch.map(_.write(message))))
    r.fold({
      case e: NotYetConnectedException =>
        channel.get >>= {
          case Some(ch) => if (ch.finishConnect()) Either.right(()) else Either.left(e)
          case None => Either.right(())
        }
      case e: IOException =>
        close(); Either.left(e)
      case e: Throwable =>
        Either.left(e)
    }, _ => Either.right(()))
  }

  def close(): Unit =
    channel.updateAndGet(asJavaUnaryOperator {
      case Right(ch) => Either.catchNonFatal { ch.foreach(_.close()); none }
      case _ => Either.right(none)
    })
}

