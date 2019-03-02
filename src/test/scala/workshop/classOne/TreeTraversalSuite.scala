package workshop.classOne

import org.scalatest.{FlatSpec, Matchers}

class TreeTraversalSuite extends FlatSpec with Matchers {

  behavior of "TreeTraversal"

  val tree: Tree[Int] = Node(1,
    Node(2, Node(4, NilNode, NilNode), Node(5, NilNode, NilNode)),
    Node(3, Node(6, NilNode, NilNode), Node(7, NilNode, NilNode)))

  it should "produce inorder output" in {
    val lst = TreeTraversal.inorder(tree)
    lst should contain theSameElementsInOrderAs List(4,2,5,1,6,3,7)
  }

  it should "produce preorder output" in {
    val lst = TreeTraversal.preorder(tree)
    lst should contain theSameElementsInOrderAs List(1,2,4,5,3,6,7)
  }

  it should "produce postorder output" in {
    val lst = TreeTraversal.postorder(tree)
    lst should contain theSameElementsInOrderAs List(4,5,2,6,7,3,1)
  }

  it should "produce bfsorder output" in {
    val lst = TreeTraversal.bfsorder(tree)
    lst should contain theSameElementsInOrderAs List(1,2,3,4,5,6,7)
  }

  it should "produce dfsorder output" in {
    val lst = TreeTraversal.dfsorder(tree)
    lst should contain theSameElementsInOrderAs List(1,2,4,5,3,6,7)
  }

}
