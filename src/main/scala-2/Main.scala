package example

import scala.reflect.runtime.universe._

object Main {
  def main(args: Array[String]): Unit = {
    val c = rootMirror.staticClass("example.C")
    val x = c.info.member(TermName("x")).asMethod
    val Apply(Select(New(t), _), Literal(Constant(arg: Type)) :: Nil) = x.annotations.head.tree
    val a = rootMirror.staticClass("example.A")
    val result = arg.asSeenFrom(typeOf[C], a)
    println(result)
    assert(result =:= typeOf[D[Int]])
  }
}
