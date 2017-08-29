package swiftvis2

import scala.tools.nsc.Settings
import scala.tools.nsc.Global
import scala.tools.nsc.reporters.ConsoleReporter
import java.io.File
import scala.io.Source
import scala.reflect.io.AbstractFile
import scala.reflect.internal.util.SourceFile
import java.nio.file.Files
import java.nio.file.Paths
import scala.reflect.io.VirtualDirectory
import scala.reflect.internal.util.BatchSourceFile
import scala.reflect.internal.util.AbstractFileClassLoader
import java.math.BigInteger
import java.security.MessageDigest

object CompilerParsing {
  trait FormTrait[@specialized(Int, Double) T] {
    def apply(di: swiftvis2.DataInput, i: Int, j: Seq[Int]): T
    def specialsAndRange(di: DataInput, i: Int): (Int, Int, Seq[Int])
  }
  case class FormulaBoundsInfo(
    inputs: Set[Int], // Which inputs are used in this formula.
    indices: Seq[(Set[Int], Set[Int], Set[Int])], // The indices used with each input for x, s, and k.
    minOffset: Int, // Min offset used for any index from any input
    maxOffset: Int) // Max offset used for any index from any input

  // Setups variables for doing parsing and compiling
  val settings = new Settings
  settings.deprecation.value = true // enable detailed deprecation warnings
  settings.unchecked.value = true // enable detailed unchecked warnings
  settings.usejavacp.value = true
  val outputDirName = "myOutputDir"
  val outputDir = new VirtualDirectory(outputDirName, None)
  settings.outputDirs.setSingleOutput(outputDir)
  val global = Global(settings, new ConsoleReporter(settings))
  val run = new global.Run

  val classCache = collection.mutable.Map[String, Class[_]]()
  val formulaCache = collection.mutable.Map[(String, String, Seq[String]), (FormTrait[_], FormulaBoundsInfo)]()
  val classLoader = new AbstractFileClassLoader(outputDir, this.getClass.getClassLoader)

  def decomposeFormula(formula: String): (String, String, Seq[String]) = {
    // TODO - pull out the group formula and selectors
    (formula, "", Nil)
  }

  def parse[T](f: String, groupFormula: String, selectors: Seq[String], typeName: String): (FormTrait[T], FormulaBoundsInfo) = {
    val fKey = (f, groupFormula, selectors)
    if (formulaCache.contains(fKey)) {
      formulaCache(fKey).asInstanceOf[(FormTrait[T], FormulaBoundsInfo)]
    } else {
      val className = classNameForCode(f)
      val classString = wrapCodeInClass(className, f, typeName, groupFormula, selectors)
      println(className+"\n"+classString)
      val inputFile = new BatchSourceFile(className+".scala", classString)
      val fParser = new global.syntaxAnalyzer.SourceFileParser(inputFile)
      val fTree = fParser.parse
      val bounds = findIndices(fTree)

      run.compileSources(List(inputFile))

      val cls = classLoader.loadClass(className)
      val ft = cls.getConstructor().newInstance().asInstanceOf[FormTrait[T]]

      // TODO - Add to cache
      formulaCache(fKey) = (ft, bounds)
      (ft, bounds)
    }
  }

  private val accessNames = Set("x", "s", "k")
  
//[info] select = Set(name, localName, qualifier, getterName, setterName)
//[info] apply = Set(formatted, ensuring, fun, args, ##)
//[info] def = Set(name, localName, tparams, rhs, getterName, tpt, setterName, mods, keyword, vparamss)
//[info] literal = Set(value)


  private def findIndices(tree: global.Tree): FormulaBoundsInfo = {
    def parseApply(code: List[global.Tree], tab: String): Seq[(Int, String, ExprNode, ExprNode)] = {
      val s = (for (n <- code) yield {
        println(tab + n.toString.split("\n")(0))
        println(tab + n.getClass)
//        println(tab + n.productArity+"; "+n.children.size+"; "+n.productPrefix)
//        for ((p, i) <- n.productIterator.zipWithIndex) println(tab + i+" : "+p.toString.split("\n")(0)+" : "+p.getClass)
//        val name = n.productElement(0).toString
        n match {
          case global.Apply(global.Ident(name), args) if accessNames(name.toString) =>
            println("Matched raw accessor: "+name)
            val accessor = (0, name.toString, globalTreeToExprNode(args(0)), globalTreeToExprNode(args(1)))
            Seq(accessor)
          case global.Apply(global.Select(global.Apply(dname, dindex), aname), args) if accessNames(aname.toString) && dname.toString=="d" =>
            println(s"Matched accessor for $dname $dindex with $aname, args = $args")
            val accessor = (globalTreeToExprNode(dindex.head).eval(Map()), aname.toString, globalTreeToExprNode(args(0)), globalTreeToExprNode(args(1)))
            Seq(accessor)
          case _ => 
            parseApply(n.children, tab+"  ")
        }
//        if (n.productPrefix == "Apply" && accessNames(name)) {
//          val args = n.productElement(1).asInstanceOf[List[global.Tree]]
//          val accessor = (0, n.productElement(0).toString, globalTreeToExprNode(args(0)), globalTreeToExprNode(args(1)))
//          Seq(accessor)
//        } else if (n.productPrefix == "Apply" && n.productElement(0).isInstanceOf[scala.reflect.internal.Trees$Select] && true) {
//          // TODO - handle d nodes
//          parseApply(n.children, tab+"  ")
//        } else 
      }).flatten
      s
    }
    
    def descend(node: global.Tree): Seq[(Int, String, ExprNode, ExprNode)] = {
      if (node.productPrefix == "DefDef" && node.productElement(1).toString == "apply") parseApply(node.productElement(5).asInstanceOf[global.Tree].children.tail, "")
      else node.children.flatMap(n => descend(n))
    }

    val allBounds = descend(tree)
    println(allBounds)
    // TODO
    FormulaBoundsInfo(Set.empty, Seq.empty, 0, 0)
  }

  private def classNameForCode(code: String): String = {
    val digest = MessageDigest.getInstance("SHA-1").digest(code.getBytes)
    "sha"+new BigInteger(1, digest).toString(16)
  }

  private def wrapCodeInClass(className: String, f: String, outType: String, groupFormula: String, selectors: Seq[String]) = {
    s"""
class $className extends swiftvis2.CompilerParsing.FormTrait[$outType] {
  def apply(di: swiftvis2.DataInput, i: Int, j: Seq[Int]): $outType = {
    import di._
    $f
  }
  def specialsAndRange(di: swiftvis2.DataInput, i: Int): (Int, Int, Seq[Int]) = {
    ???
  }
}"""
  }

  private val ops = Map[String, (ExprNode, ExprNode) => ExprNode](
      "$plus" -> ((l,r) => AddNode(l,r)),
      "$times" -> ((l,r) => MultNode(l,r)),
      "$minus" -> ((l,r) => AddNode(l,r)),  // TODO
      "$div" -> ((l,r) => AddNode(l,r))     // TODO
  )

  def globalTreeToExprNode(node: global.Tree): ExprNode = node match {
    case global.Select(obj, method) =>
      globalTreeToExprNode(node.productElement(0).asInstanceOf[global.Tree])
    case global.Apply(global.Select(obj, method), args) if ops.contains(method.toString) =>
      val left = globalTreeToExprNode(obj)
      val right = globalTreeToExprNode(args.head)
      ops(method.toString)(left, right)
//    case global.DefDef(mods, tname, tlist, llvals, t1, t2) =>
    case global.Literal(const) =>
      NumberNode(const.value.toString.toInt)
    case global.Ident(name) =>
      VarNode(name.toString)
    case _ =>
      UnknownNode
  }

  sealed trait ExprNode {
    def simplify(): ExprNode
    def usesVariable: Boolean
    def eval(vars: Map[String, Int]): Int
    def valid: Boolean
  }

  case class NumberNode(n: Int) extends ExprNode {
    def simplify(): ExprNode = this
    def usesVariable: Boolean = false
    def eval(vars: Map[String, Int]): Int = n
    def valid: Boolean = true
  }

  case class VarNode(name: String) extends ExprNode {
    def simplify(): ExprNode = this
    def usesVariable: Boolean = true
    def eval(vars: Map[String, Int]): Int = vars(name)
    def valid: Boolean = true
  }

  case class AddNode(left: ExprNode, right: ExprNode) extends ExprNode {
    def simplify(): ExprNode = {
      if (!valid) UnknownNode
      else if (!usesVariable) NumberNode(eval(null))
      else (left, right) match {
        case (AddNode(ll, lr), _) => ???
        case _ => this
      }
    }
    def usesVariable: Boolean = left.usesVariable || right.usesVariable
    def eval(vars: Map[String, Int]): Int = left.eval(vars) + right.eval(vars)
    def valid: Boolean = left.valid && right.valid
  }

  case class MultNode(left: ExprNode, right: ExprNode) extends ExprNode {
    def simplify(): ExprNode = {
      if (!valid) UnknownNode
      else if (!usesVariable) NumberNode(eval(null))
      else (left, right) match {
        case (AddNode(ll, lr), _) => ???
        case _ => this
      }
    }
    def usesVariable: Boolean = left.usesVariable || right.usesVariable
    def eval(vars: Map[String, Int]): Int = left.eval(vars) + right.eval(vars)
    def valid: Boolean = left.valid && right.valid
  }

  case object UnknownNode extends ExprNode {
    def simplify(): ExprNode = this
    def usesVariable: Boolean = false
    def eval(vars: Map[String, Int]): Int = 0
    def valid = false
  }

  def main(args: Array[String]): Unit = {
    val sel = """
attachments            hasAttachment       productElement      
canEqual               hasExistingSymbol   productIterator     
canHaveAttrs           hasSymbol           productPrefix       
changeOwner            hasSymbolField      qualifier           
children               hasSymbolWhich      removeAttachment    
clearType              hashCode            setAttachments      
collect                id                  setPos              
copy                   isDef               setSymbol           
correspondsStructure   isEmpty             setType             
defineType             isErroneous         setterName          
duplicate              isErrorTyped        shallowDuplicate    
equals                 isTerm              shortClass          
equalsStructure        isType              substituteSymbols   
exists                 isTyped             substituteThis      
filter                 localName           substituteTypes     
find                   modifyType          summaryString       
forAll                 name                symbol              
foreach                nonEmpty            toString            
foreachPartial         orElse              tpe                 
freeTerms              pos                 tpe_=               
freeTypes              pos_=               updateAttachment    
getterName             productArity        withFilter          
""".trim.split(" +").map(_.trim).toSet

    def app = """
!=                     foreachPartial      pos                 
##                     formatted           pos_=               
+                      freeTerms           productArity        
->                     freeTypes           productElement      
==                     fun                 productIterator     
args                   getClass            productPrefix       
asInstanceOf           hasAttachment       removeAttachment    
attachments            hasExistingSymbol   setAttachments      
canEqual               hasSymbol           setPos              
canHaveAttrs           hasSymbolField      setSymbol           
changeOwner            hasSymbolWhich      setType             
children               hashCode            shallowDuplicate    
clearType              id                  shortClass          
collect                isDef               substituteSymbols   
copy                   isEmpty             substituteThis      
correspondsStructure   isErroneous         substituteTypes     
defineType             isErrorTyped        summaryString       
duplicate              isInstanceOf        symbol              
ensuring               isTerm              symbol_=            
eq                     isType              synchronized        
equals                 isTyped             toString            
equalsStructure        modifyType          tpe                 
exists                 ne                  tpe_=               
filter                 nonEmpty            updateAttachment    
find                   notify              wait                
forAll                 notifyAll           withFilter          
foreach                orElse              →                   
""".trim.split(" +").map(_.trim).toSet

    val defdef = """
attachments            hasSymbol         productPrefix       
canEqual               hasSymbolField    removeAttachment    
canHaveAttrs           hasSymbolWhich    rhs                 
changeOwner            hashCode          setAttachments      
children               id                setPos              
clearType              isDef             setSymbol           
collect                isEmpty           setType             
copy                   isErroneous       setterName          
correspondsStructure   isErrorTyped      shallowDuplicate    
defineType             isTerm            shortClass          
duplicate              isType            substituteSymbols   
equals                 isTyped           substituteThis      
equalsStructure        keyword           substituteTypes     
exists                 localName         summaryString       
filter                 modifyType        symbol              
find                   mods              toString            
forAll                 name              tparams             
foreach                nonEmpty          tpe                 
foreachPartial         orElse            tpe_=               
freeTerms              pos               tpt                 
freeTypes              pos_=             updateAttachment    
getterName             productArity      vparamss            
hasAttachment          productElement    withFilter          
hasExistingSymbol      productIterator                       
""".trim.split(" +").map(_.trim).toSet

    val lit = """
attachments            hasAttachment       productIterator     
canEqual               hasExistingSymbol   productPrefix       
canHaveAttrs           hasSymbol           removeAttachment    
changeOwner            hasSymbolField      setAttachments      
children               hasSymbolWhich      setPos              
clearType              hashCode            setSymbol           
collect                id                  setType             
copy                   isDef               shallowDuplicate    
correspondsStructure   isEmpty             shortClass          
defineType             isErroneous         substituteSymbols   
duplicate              isErrorTyped        substituteThis      
equals                 isTerm              substituteTypes     
equalsStructure        isType              summaryString       
exists                 isTyped             symbol              
filter                 modifyType          symbol_=            
find                   nonEmpty            toString            
forAll                 orElse              tpe                 
foreach                pos                 tpe_=               
foreachPartial         pos_=               updateAttachment    
freeTerms              productArity        value               
freeTypes              productElement      withFilter          
""".trim.split(" +").map(_.trim).toSet

    val tree = """
attachments            hasExistingSymbol   productPrefix       
canEqual               hasSymbol           removeAttachment    
canHaveAttrs           hasSymbolField      setAttachments      
changeOwner            hasSymbolWhich      setPos              
children               hashCode            setSymbol           
clearType              id                  setType             
collect                isDef               shallowDuplicate    
correspondsStructure   isEmpty             shortClass          
defineType             isErroneous         substituteSymbols   
duplicate              isErrorTyped        substituteThis      
equals                 isTerm              substituteTypes     
equalsStructure        isType              summaryString       
exists                 isTyped             symbol              
filter                 modifyType          symbol_=            
find                   nonEmpty            toString            
forAll                 orElse              tpe                 
foreach                pos                 tpe_=               
foreachPartial         pos_=               updateAttachment    
freeTerms              productArity        withFilter          
freeTypes              productElement                          
hasAttachment          productIterator   
""".trim.split(" +").map(_.trim).toSet

    println("select = "+(sel diff tree))
    println("apply = "+(app diff tree))
    println("def = "+(defdef diff tree))
    println("literal = "+(lit diff tree))
  }
}

