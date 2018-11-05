package workshop

import scala.annotation.tailrec
import scala.collection.immutable.Queue

object TreeTraversal {

  def inorder[A](tree: Tree[A]): List[A] = tree match {
    case NilNode => Nil
    case Node(v, l, r) => inorder(l) ::: v :: inorder(r)
  }

  def preorder[A](tree: Tree[A]): List[A] = {
    @tailrec
    def go(tl: List[Tree[A]], acc: List[A]): List[A] = tl match {
      case Nil => acc.reverse
      case NilNode :: tail => go(tail, acc)
      case Node(v, l, r) :: tail => go(l :: r :: tail, v :: acc)
    }

    go(List(tree), Nil)
  }

  def postorder[A](tree: Tree[A]): List[A] = tree match {
    case NilNode       => Nil
    case Node(v, l, r) => (postorder(l) ::: postorder(r)) :+ v
  }

  def bfsorder[A](tree: Tree[A]): List[A] = {
    @tailrec
    def go(q: Queue[Tree[A]], acc: List[A]): List[A] = q.dequeueOption match {
      case None => acc.reverse
      case Some((NilNode, rest)) => go(rest, acc)
      case Some((Node(v, l, r), rest)) => go(rest.enqueue(l).enqueue(r), v :: acc)
    }

    go(Queue(tree), Nil)
  }

  def dfsorder[A](tree: Tree[A]): List[A] = preorder(tree)

}
