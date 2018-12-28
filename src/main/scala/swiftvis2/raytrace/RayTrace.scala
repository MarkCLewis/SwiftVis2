package raytrace

import java.awt.{Color,Font,Graphics2D}
import java.awt.image.BufferedImage
import scala.swing._
import javax.imageio.ImageIO
import java.io.File

object RayTrace {
  def main(args : Array[String]) : Unit = {
    val ray=new Ray(new Point(0,-2,0),new Point(0,-1,0))
    val img=new BufferedImage(3000,4000,BufferedImage.TYPE_INT_ARGB)  // 3000, 4000
    val lights=List(new AmbientLight(new FColor(.1f,.1f,.1f,1)), //new PointLight(new FColor(0.9f,0.9f,0.9f,1),new Point(0,-4,5)),
      new DirectionLight(new FColor(0.75f,0.75f,0.75f,1),new Vect(1,1,-1)))
    val scene=new OctreeScene(Point(0,0,0), 10)
//      buildTestScene(scene)
      buildTextbookCoverE2V2(scene)
      render(new Point(0,-4,0),new Point(-0.5,-3,-0.7),new Vect(1,0,0),new Vect(0,0,1.4),img,scene,lights,10)
      ImageIO.write(img,"png",new File("OOADSUSBig.png"))
//      for(i <- 0 until pnts.length) {
//          pnts(i)+=vel(i)
//          if(pnts(i).z < -2.0) vel(i)=new Vect(vel(i).x,vel(i).y,-vel(i).z)
//          else if(pnts(i).x.abs > 2.0) vel(i)=new Vect(-vel(i).x,vel(i).y,vel(i).z)
//          else if(pnts(i).y.abs > 1.0) vel(i)=new Vect(vel(i).x,-vel(i).y,vel(i).z)
//          else vel(i)-=new Vect(0,0,0.02)
//      }
//    }
    val frame=new MainFrame {
      title = "Trace Frame"
      contents = new Label("",Swing.Icon(img),Alignment.Center)
    }
    frame.visible = true
  }
  
  def buildTestScene(scene:Scene) {
    val sphere=new GeomSphere(new Point(0.6,0.5,-0.5),0.25,(x:Point)=>new Color(100,100,100,255),(x:Point)=>0.3)
    val ellip=new GeomEllipsoid(new Point(0.5,0,0.2),new Vect(0.1,0,0),new Vect(0,0.2,0),new Vect(0,0,0.3),(x:Point)=>new Color(0,0,100,55),(x:Point)=>0.3)
    val triPnts=Array(new Point(0,2,1),new Point(-1,2,-1),new Point(1,2,-1))
    val triCols=Array(Color.red,Color.green,Color.blue)
    val cf=new PointColorFunc(triPnts,triCols)
    val tri=new GeomPolygon(triPnts,Array(new Vect(0,-1,0),new Vect(0,-1,0),new Vect(0,-1,0)),
                             (x)=>cf(x),Array(0.0,1,0.5))
    val quadPnts=Array(new Point(-2,20,-1),new Point(-2,0,-1),new Point(2,0,-1),new Point(2,20,-1))
    val quadTexture=new TextureColorFunc(ImageIO.read(new File("SatBacklit2.jpg")),new Point(-1.5,-0.5,-1),new Vect(3,0,0),new Vect(0,2,0))
    val quad=new GeomPolygon(quadPnts,Array(new Vect(0,0,1),new Vect(0,0,1),new Vect(0,0,1),new Vect(0,0,1)),
                            (x)=>quadTexture(x),Array(0.5,0.5,0.5,0.5))
    val cyl=new GeomCylinder(new Point(-0.7,0.5,0.0),new Point(-0.3,0.5,0.0),0.2,(x:Point)=>new Color(200,200,200,255),(x:Point)=>0.5)
    scene.addGeom(sphere)
    scene.addGeom(ellip)
    scene.addGeom(tri)
    scene.addGeom(quad)
    scene.addGeom(cyl)
    scene.addGeom(cylinder(new Point(-0.6,0.6,-0.9),new Vect(0,0,0.5),new Vect(-0.2,0,0),6,(x)=>new Color(100,100,100,0),0))
    triangulateGrid(scene,gridFromFunc((x,y)=>Math.cos(12*((x-1)*(x-1)+(y-1)*(y-1)))/20,new Point(1,2,-1.001),new Vect(0,-2,0),new Vect(0,0,2),60),(x)=>new Color(100,100,100,255),0.6)
    val gridTexture=new TextureColorFunc(ImageIO.read(new File("NoHair.jpg")),new Point(-1,0,1),new Vect(2,0,0),new Vect(0,2,0))
    triangulateGrid(scene,gridFromFunc((x,y)=>Math.sin(4*x)/20,new Point(-1,2,1),new Vect(2.001,0,0),new Vect(0,-2,0),40),(x)=>gridTexture(x),0.6)    
  }
  
  def buildTextbookCoverE2V1(scene:Scene) {
    val clear = new Color(0,0,0,0)
    val stairs = new GeomCylinder(Point(0,0,-0.98),Point(0,0,1.02),0.5,
        p => {
          val ang = math.Pi-math.atan2(p.x,p.y)
          if((-4 to 4 by 2).exists(i => ((p.z*2.9+i)*math.Pi-ang).abs<2)) Color.red else clear  
        },
        p => {
          val ang = math.Pi-math.atan2(p.x,p.y)
          if((-4 to 4 by 2).exists(i => ((p.z*2.9+i)*math.Pi-ang).abs<2)) 0.4 else 0.0  
        })
    scene addGeom stairs
    scene addGeom new GeomPolyFunc(Array(Point(-300,300,-1),Point(-300,-3,-1),Point(300,-3,-1),Point(300,300,-1)),
        //p => Vect(0,0,1),
        p => Vect(0,0.05*(math.cos(p.y*6)),1),
        p => Color.black,
        p => 0.4)
//    scene addGeom fileTextPoly(
//        "quicksort.scala",
//        Point(0.1,-0.7,-0.6),2,clear)
    scene addGeom fileTextPoly(
        "RayTrace.scala",
        Point(-2.5,1,2.5),2,clear)
//    scene addGeom fileTextPoly(
//        "/home/mlewis/Documents/Teaching/ScalaBook/LatexVersion/Chapters/Functions/Code/Factorial.scala",
//        Point(-0.7,-2,-0.75),2,clear)
    scene addGeom fileTextPoly(
        "repl.txt",
        Point(-0.6,-0.5,1.85),2,clear)
    scene addGeom fileTextPoly(
        "dot.scala",
        Point(-0.2,-1.5,0),2,clear)
    scene addGeom new GeomSphere(Point(-2, 4.5, 0), 1.9, p => Color.lightGray, p => 0.9)
    scene addGeom new GeomEllipsoid(Point(0.8, -1, -0.3), Vect(0,0,0.6), Vect(0.4,0,0), Vect(0,0.3,0), p => new Color(0,50,255), p => 0.7)
  }

  def buildTextbookCoverE2V2(scene:Scene) {
    val clear = new Color(0,0,0,0)
    val stairs = new GeomCylinder(Point(0,0,-0.98),Point(0,0,1.02),0.5,
        p => {
          val ang = math.Pi-math.atan2(p.x,p.y)
          if((-4 to 4 by 2).exists(i => ((p.z*2.9+i)*math.Pi-ang).abs<2)) Color.red else clear  
        },
        p => {
          val ang = math.Pi-math.atan2(p.x,p.y)
          if((-4 to 4 by 2).exists(i => ((p.z*2.9+i)*math.Pi-ang).abs<2)) 0.4 else 0.0  
        })
    scene addGeom stairs
//    scene addGeom fileTextPoly(
//        "quicksort.scala",
//        Point(0.1,-0.7,-0.6),2,clear)
    scene addGeom fileTextPoly(
        "RayTrace.scala",
        Point(-2.5,1,2.5),2,clear)
//    scene addGeom fileTextPoly(
//        "/home/mlewis/Documents/Teaching/ScalaBook/LatexVersion/Chapters/Functions/Code/Factorial.scala",
//        Point(-0.7,-2,-0.75),2,clear)
//    scene addGeom fileTextPoly(
//        "repl.txt",
//        Point(-0.6,-0.5,1.85),2,clear)
    scene addGeom fileTextPoly(
        "Futures.scala",
        Point(-0.2,-1.5,0),3,clear)
    scene addGeom new GeomSphere(Point(-2, 4.5, 0), 1.9, p => Color.lightGray, p => 0.9)
    fractalSurface(scene, Point(0, -5, -3), Point(5, 5, -1), Point(-5, 5, -1), p => Color.green, 0.2, 8)
  }
  
  def texturePanel(p:Point,r:Vect,d:Vect,n:Vect,ref:Double,fname:String):Geometry = {
    val pnts=Array(p-r*0.5,p+d-r*0.5,p+d+r*0.5,p+r*0.5)
    val texture=new TextureColorFunc(ImageIO.read(new File(fname)),p-r*0.5,r,d)
    new GeomPolygon(pnts,Array(n,n,n,n),(x)=>texture(x),Array(ref,ref,ref,ref))
  }  
  
  def render(eye:Point,topLeft:Point,right:Vect,down:Vect,img:BufferedImage,geom:Geometry,lights:List[Light],numRays:Int) {
    for(i <- 0 until img.getWidth par; j <- 0 until img.getHeight) {
      img.setRGB(i,img.getHeight-j-1,(((1 to numRays).map(index=>{
          val ray=new Ray(eye,topLeft+right*(i+(if(index>0) math.random*0.75 else 0))/img.getWidth+down*(j+(if(index>0) math.random*0.75 else 0))/img.getHeight)
          castRay(ray,geom,lights,0)
      }).reduceLeft(_+_))/numRays).toColor.getRGB)
    }
  }
  
  private def castRay(ray:Ray,geom:Geometry,lights:List[Light],cnt:Int):FColor = {
    if(cnt>5) new FColor(0,0,0,1)
    else {
      val oid=geom intersect ray
      oid match {
        case None => new FColor(0,0,0,1)
        case Some(id) => {
          val lightColors=for(light <- lights) yield light.color(id,geom)
          val refColor=if(id.reflect>0) {
            val refRay=new Ray(id.point+id.norm*0.0001,ray.dirVect-id.norm*2*(id.norm dot ray.dirVect))
            castRay(refRay,geom,lights,cnt+1) 
          } else new FColor(0,0,0,1)
          val alphaColor=if(id.color.getAlpha==255) new FColor(0,0,0,1)
          else {
            val ndd=id.norm dot ray.dirVect
//            val transRay=if(ndd<0) new Ray(id.point-id.norm*0.0001,(ray.dirVect+id.norm*ndd*2).normalize)
//              else new Ray(id.point+id.norm*0.0001,(ray.dirVect+id.norm*ndd*0.5).normalize)
            val transRay = if(ndd<0) new Ray(id.point-id.norm*0.0001,ray.dirVect.normalize) // non-refracting
              else new Ray(id.point+id.norm*0.0001,ray.dirVect.normalize)
            castRay(transRay,geom,lights,cnt+1)*((255-id.color.getAlpha)/255.0).toFloat 
          }
          new FColor(id.color)*((new FColor(0,0,0,1) /: lightColors)(_+_))+refColor*id.reflect.toFloat+alphaColor
        }  
      } 
    }
  }
  
  private def cylinder(bottom:Point,up:Vect,left:Vect,faces:Int,col:(Point)=>Color,ref:Double):Geometry = {
    val back=(up.normalize cross left)
    val pnts=(for(i <- 0 to faces) 
      yield {
        val ang=2*math.Pi*i/faces
        val c=Math.cos(ang)
        val s=Math.sin(ang)
        (bottom+left*c+back*s,bottom+left*c+back*s+up)
      }).toArray
    val ret=new ListScene()
    val refs=Array.tabulate(faces)((x)=>ref)
    println(pnts.length)
    for(i<-0 until faces) {
      ret.addGeom(new GeomPolygon(Array(pnts(i)_2,pnts(i)_1,pnts(i+1)_1,pnts(i+1)_2),col,refs))
    }
    ret.addGeom(new GeomPolygon(pnts.take(faces).map(_._1).toArray,col,refs))
    ret.addGeom(new GeomPolygon(pnts.take(faces).map(_._2).toArray,col,refs))
    ret
  }
  
  private def gridFromFunc(func:(Double,Double)=>Double,corner:Point,right:Vect,up:Vect,num:Int) = {
    val rmag=right.magnitude
    val umag=up.magnitude
    val rvec=right.normalize
    val uvec=up.normalize
    val out=(up cross right).normalize
    for(i:Int <- (1 until num).toArray) yield {
      for(j:Int <- (1 until num).toArray) yield {
        val x=i*rmag/num
        val y=j*umag/num
        corner+rvec*x+uvec*y+out*(func(x,y))
      }
    }
  }
    
  private def triangulateGrid(scene:Scene,grid:Array[Array[Point]],col:(Point)=>Color,ref:Double) {
    val refs=Array(ref,ref,ref)
    val norm=for(i <- 0 until grid.length) yield for(j <- 0 until grid(0).length)
      yield calcNorm(grid,i,j).normalize
    for(i <- 0 until grid.length-1; j <- 0 until grid(0).length-1) {
      scene.addGeom(new GeomPolygon(Array(grid(i)(j),grid(i+1)(j),grid(i)(j+1)),
                                    Array(norm(i)(j),norm(i+1)(j),norm(i)(j+1)),col,refs))
      scene.addGeom(new GeomPolygon(Array(grid(i)(j+1),grid(i+1)(j),grid(i+1)(j+1)),
                                    Array(norm(i)(j+1),norm(i+1)(j),norm(i+1)(j+1)),col,refs))
    }
  }
  
  private def calcNorm(grid:Array[Array[Point]],i:Int,j:Int):Vect = {
    val zero=new Vect(0,0,0)
    (if(i>0 && j>0) (grid(i-1)(j)-grid(i)(j)) cross (grid(i)(j-1)-grid(i)(j)) else zero)+
    (if(i<grid.length-1 && j>0) (grid(i)(j-1)-grid(i)(j)) cross (grid(i+1)(j-1)-grid(i)(j)) else zero)+
    (if(i<grid.length-1 && j<grid.length-1) (grid(i+1)(j)-grid(i)(j)) cross (grid(i)(j+1)-grid(i)(j)) else zero)+
    (if(i>0 && j<grid.length-1) (grid(i)(j+1)-grid(i)(j)) cross (grid(i-1)(j)-grid(i)(j)) else zero)
  }
  
  private def fileTextPoly(filename:String,topLeft:Point,sm:Double,bg:Color):GeomPolyFunc = {
    val source = io.Source.fromFile(filename)
    val lines = source.getLines().toArray
    source.close
    val img = new BufferedImage(10+15*lines.map(_.length).max,25+22*lines.length,
        BufferedImage.TYPE_INT_ARGB)
    val height = img.getHeight/img.getWidth.toDouble
    val g=img.createGraphics()
    import java.awt.RenderingHints
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON)
    g.setColor(bg)
    g.fillRect(0,0,img.getWidth(),img.getHeight())
    g.setColor(Color.white)
    g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,20))
    var y = 25
    for(line <- lines) {
      g.drawString(line,5,y)
      y += 22
    }
    val texture = new TextureColorFunc(img,topLeft,Vect(sm,0,0),Vect(0,0,-sm*height))
    new GeomPolyFunc(Array(topLeft,topLeft+Vect(0,0,-sm*height),topLeft+Vect(sm,0,-sm*height),topLeft+Vect(sm,0,0)),
        p => Vect(0,-1,0),
        texture,
        p => 0.0)
  }
  
  private def pointRandom(p: Point): Double = {
    val xl = java.lang.Double.doubleToRawLongBits(p.x)
    val yl = java.lang.Double.doubleToRawLongBits(p.y)
    val zl = java.lang.Double.doubleToRawLongBits(p.z)
    ((xl << 16) ^ yl ^ (zl >> 16)).toDouble / Long.MaxValue
  }
  
  private def fractalSurface(scene: Scene, tp1: Point, tp2: Point, tp3: Point, color: Point => Color, reflect: Double, maxLevel: Int): Unit = {
    val refArray = Array(reflect, reflect, reflect)
    def helper(p1: Point, p2: Point, p3: Point, n1: Vect, n2: Vect, n3: Vect, mag: Double, lvl: Int): Unit = {
      if(lvl==maxLevel) {
        scene addGeom new GeomPolygon(Array(p1, p2, p3),Array(n1, n2, n3),color, refArray)
      } else {
        val cn1 = ((p2-p1) cross ((n1+n2) cross (p2-p1))).normalize
        val cn2 = ((p3-p2) cross ((n2+n3) cross (p3-p2))).normalize
        val cn3 = ((p1-p3) cross ((n3+n1) cross (p1-p3))).normalize
        val cp1 = p1+(p2-p1)/2+cn1*mag*pointRandom(p1+(p2-p1)/2)
        val cp2 = p2+(p3-p2)/2+cn2*mag*pointRandom(p2+(p3-p2)/2)
        val cp3 = p3+(p1-p3)/2+cn3*mag*pointRandom(p3+(p1-p3)/2)
        helper(p1,cp1,cp3, n1, cn1, cn3, mag/2, lvl+1)
        helper(cp1,p2,cp2, cn1, n2, cn2, mag/2, lvl+1)
        helper(cp2,p3,cp3, cn2, n3, cn3, mag/2, lvl+1)
        helper(cp1, cp2, cp3, cn1, cn2, cn3, mag/2, lvl+1)
      }
    }
    val tn1 = (tp2-tp1) cross (tp3-tp1)
    val tn2 = (tp3-tp2) cross (tp1-tp2)
    val tn3 = (tp1-tp3) cross (tp2-tp3)
    val mag = ((tp1-tp2).magnitude+(tp2-tp3).magnitude+(tp3-tp1).magnitude)/3
    helper(tp1, tp2, tp3, tn1.normalize, tn2.normalize, tn3.normalize, mag/4, 1)
  }
}
