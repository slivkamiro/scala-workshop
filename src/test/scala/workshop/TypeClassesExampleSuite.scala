package workshop

import org.scalatest.{FlatSpec, Matchers}

class TypeClassesExampleSuite extends FlatSpec with Matchers {

  behavior of "TypeClasses"

  val tree: Tree[Int] = Node(1,
    Node(2, Node(4, NilNode, NilNode), Node(5, NilNode, NilNode)),
    Node(3, Node(6, NilNode, NilNode), Node(7, NilNode, NilNode)))


  it should "combine all the elements" in {
    TypeClassesExample.combineAll(tree) shouldBe 28
  }

}
