package workshop.classOne

import org.scalatest.prop.PropertyChecks
import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

class SortingSuite extends FunSpec with PropertyChecks with Matchers with GivenWhenThen {

  describe("A list sorting") {

    it("should not change size of supplied list") {

      forAll { lst: List[Int] =>
        val sortedLst = Sorting.sort(lst)

        sortedLst.size shouldBe lst.size
        sortedLst shouldBe sorted
      }
    }
  }
}
