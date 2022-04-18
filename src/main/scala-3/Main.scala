package example

import java.io.File
import scala.quoted.Quotes
import scala.tasty.inspector.Inspector
import scala.tasty.inspector.Tasty
import scala.tasty.inspector.TastyInspector
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.{Symbol => DottySymbol}
import dotty.tools.dotc.core.Types.{Type => DottyType}
import dotty.tools.dotc.core.TypeOps.asSeenFrom

object Main {

  @annotation.experimental // typeRef is experimental
  def main(args: Array[String]): Unit = withQuotes {
    val q: Quotes = summon[Quotes]
    import q.reflect._

    extension (self: TypeRepr) {
      def asDottyType: DottyType = self.asInstanceOf[DottyType]
    }

    extension (self: Symbol) {
      def asDottySymbol: DottySymbol = self.asInstanceOf[DottySymbol]
    }

    extension (self: DottyType) {
      def fromDottyType: TypeRepr = self.asInstanceOf[TypeRepr]
    }

    implicit val context: Context = q.asInstanceOf[scala.quoted.runtime.impl.QuotesImpl].ctx

    val c = Symbol.requiredClass("example.C")
    val a = Symbol.classSymbol("example.A")
    val List(x: Symbol) = c.methodMember("x")
    val Apply(_, NamedArg("clazz", TypeApply(Ident("classOf"), clazz :: Nil)) :: Nil) = x.annotations.head
    val result = asSeenFrom(clazz.tpe.asDottyType, c.typeRef.asDottyType, a.asDottySymbol).fromDottyType
    println(result.show)
    assert(result =:= TypeRepr.of[D[Int]], result)

    val memberTypeResult1 = clazz.tpe.memberType(c)
    println(memberTypeResult1)
    try {
      println(memberTypeResult1.show)
    } catch {
      case e =>
        e.printStackTrace() // MatchError ???
    }
    val memberTypeResult2 = clazz.tpe.memberType(a)
    println(memberTypeResult2)
    try {
      println(memberTypeResult2.show)
    } catch {
      case e =>
        e.printStackTrace() // MatchError ???
    }
  }

  def getTastyFileOrJar(clazz: Class[?]): Either[File, File] = {
    val base = new File(clazz.getProtectionDomain.getCodeSource.getLocation.getFile)
    if (base.isDirectory) {
      val name = clazz.getName.replace('.', '/') + ".tasty"
      Right(new File(base, name))
    } else if (base.isFile && base.getName.endsWith(".jar")) {
      Left(base)
    } else {
      sys.error("not found " + clazz)
    }
  }

  def runInspector(clazz: Class[?], inspector: Inspector): Unit = {
    getTastyFileOrJar(clazz) match {
      case Right(tasty) =>
        TastyInspector.inspectAllTastyFiles(
          tastyFiles = tasty.getAbsolutePath :: Nil,
          jars = Nil,
          dependenciesClasspath = Nil
        )(inspector)
      case Left(jar) =>
        TastyInspector.inspectAllTastyFiles(
          tastyFiles = Nil,
          jars = jar.getAbsolutePath :: Nil,
          dependenciesClasspath = Nil
        )(inspector)
    }
  }

  def withQuotes[A](f: Quotes ?=> A): Unit = {
    val inspector = new Inspector {
      def inspect(using q: Quotes)(tastys: List[Tasty[q.type]]): Unit = {
        import q.reflect.*
        f
      }
    }
    runInspector(classOf[C], inspector)
  }
}
