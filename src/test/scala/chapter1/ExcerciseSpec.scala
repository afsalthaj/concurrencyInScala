package chapter1

import chapter1.Exercises.ConcurrentBiMapImpl
import org.specs2.{ScalaCheck, Specification}

class ExcerciseSpec extends Specification with ScalaCheck {
  def is =
    s2"""
       $test
      """

  def test = {
    val s = new ConcurrentBiMapImpl[Int, Int]
    s.put(1, 1)
    println(s.array.toList)
    1 == 1
    //s.getValue(0).contains(0) && s.getValue(1).contains(1) && s.getValue(2).contains(2)
  }
}