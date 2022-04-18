package example

import scala.annotation.StaticAnnotation

class MyAnnotation(clazz: Class[?]) extends StaticAnnotation

trait D[E]

class A[B] {
  @MyAnnotation(clazz = classOf[D[B]])
  def x: B = ???
}

class C extends A[Int]
