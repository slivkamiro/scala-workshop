package workshop

import scala.collection.immutable.Queue

object TreeTraversal {

  def inorder[A](tree: Tree[A]): List[A] = tree match {
    case NilNode       => Nil
    case Node(v, l, r) => inorder(l) ::: v :: inorder(r)
  }

  def preorder[A](tree: Tree[A]): List[A] = tree match {
    case NilNode       => Nil
    case Node(v, l, r) => v :: preorder(l) ::: preorder(r)
  }

  def postorder[A](tree: Tree[A]): List[A] = tree match {
    case NilNode       => Nil
    case Node(v, l, r) => (postorder(l) ::: postorder(r)) :+ v
  }

  def bfsorder[A](tree: Tree[A]): List[A] = {
    def go(q: Queue[Tree[A]]): List[A] = q.dequeueOption match {
      case None                        => Nil
      case Some((NilNode, Queue()))    => Nil
      case Some((NilNode, rest))       => go(rest)
      case Some((Node(v, l, r), rest)) => v :: go(rest.enqueue(l).enqueue(r))
    }
    go(Queue(tree))
  }

  def dfsorder[A](tree: Tree[A]): List[A] = preorder(tree)

}
