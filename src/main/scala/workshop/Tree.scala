package workshop

sealed trait Tree[+A]
final case class Node[A](value: A, left: Tree[A], right: Tree[A]) extends Tree[A]
case object NilNode extends Tree[Nothing]
