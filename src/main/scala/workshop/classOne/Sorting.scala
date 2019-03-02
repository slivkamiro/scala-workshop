package workshop.classOne

object Sorting {

  def sort(l: List[Int]): List[Int] = l match {
    case Nil            => Nil
    case head :: tail   => sort(tail.filter(_ <= head)) ::: head :: sort(tail.filter(_ > head))
  }

}
