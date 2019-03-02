package workshop.classOne

import simulacrum._

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.language.higherKinds

@typeclass
trait Foldable[F[_]] {
  @op("fold") def fold[A, B](fa: F[A])(z: B)(f: (A, B) => B): B
}

object Foldable {
  implicit val treeFoldable = new Foldable[Tree] {
    override def fold[A, B](fa: Tree[A])(z: B)(f: (A, B) => B): B = {
      @tailrec
      def go(q: Queue[Tree[A]], acc: B): B = q.dequeueOption match {
        case None => acc
        case Some((NilNode, rest)) => go(rest, acc)
        case Some((Node(v, l, r), rest)) => go(rest.enqueue(l).enqueue(r), f(v, acc))
      }

      go(Queue(fa), z)
    }
  }
}

@typeclass
trait Monoid[A] {
  def zero: A
  @op("|+|") def combine(x: A, y: A): A
}

object Monoid {
  implicit val intMonoid = new Monoid[Int] {
    override val zero: Int = 0

    override def combine(x: Int, y: Int): Int = x + y
  }
}

object TypeClassesExample {
  import Foldable.ops._
  import Monoid.ops._

  def combine[A: Monoid](a: A, b: A): A = a |+| b

  def combineAll[F[_]: Foldable, A: Monoid](x: F[A]): A = x.fold(Monoid[A].zero)(_ |+| _)
}
