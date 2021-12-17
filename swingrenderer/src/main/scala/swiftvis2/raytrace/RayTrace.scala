package swiftvis2.raytrace

import java.awt.{ Color, Font, Graphics2D }
import java.awt.image.BufferedImage
//import scala.swing._
import javax.imageio.ImageIO
import java.io.File
import scala.collection.parallel.CollectionConverters._

object RayTrace {
  def texturePanel(p: Point, r: Vect, d: Vect, n: Vect, ref: Double, fname: String): Geometry = {
    val pnts = Array(p - r * 0.5, p + d - r * 0.5, p + d + r * 0.5, p + r * 0.5)
    val texture = new TextureColorFunc(ImageIO.read(new File(fname)), p - r * 0.5, r, d)
    new GeomPolygon(pnts, Array(n, n, n, n), (x) => texture(x), Array(ref, ref, ref, ref))
  }
  
  def render(view: LinearViewPath.View, img: RTImage, geom: Geometry, lights: List[Light], numRays: Int): Unit = {
    val aspect = img.width.toDouble/img.height
    val right = view.dir cross view.up
    render(view.loc, view.loc+view.dir+view.up/2-right*(aspect/2), right, -view.up, img, geom, lights, numRays)
  }

  def render(eye: Point, topLeft: Point, right: Vect, down: Vect, img: RTImage, geom: Geometry, lights: List[Light], numRays: Int): Unit = {
    val aspect = img.width.toDouble/img.height
    for (i <- (0 until img.width).par; j <- (0 until img.height).par) {
      img.setColor(i, j, (((0 until numRays).map(index => {
        val ray = Ray(eye, topLeft + right * (aspect * (i + (if (index > 0) math.random() * 0.75 else 0)) / img.width) + down * (j + (if (index > 0) math.random() * 0.75 else 0)) / img.height)
        castRay(ray, geom, lights, 0)
      }).reduceLeft(_ + _)) / numRays))
    }
  }

  def castRay(ray: Ray, geom: Geometry, lights: List[Light], cnt: Int): RTColor = {
    if (cnt > 5) new RTColor(0, 0, 0, 1)
    else {
      val oid = geom intersect ray
      oid match {
        case None => RTColor.Black
        case Some(id) => {
        	val geomSize = id.geom.boundingSphere.radius
          val lightColors = for (light <- lights) yield light.color(id, geom)
          val refColor = if (id.reflect > 0) {
            val refRay = new Ray(id.point + id.norm * 0.0001 * geomSize, ray.dir - id.norm * 2 * (id.norm dot ray.dir))
            castRay(refRay, geom, lights, cnt + 1)
          } else new RTColor(0, 0, 0, 1)
          val alphaColor = if (id.color.a >= 1.0) new RTColor(0, 0, 0, 1)
            else {
              val ndd = id.norm dot ray.dir
              //            val transRay=if(ndd<0) new Ray(id.point-id.norm*0.0001,(ray.dirVect+id.norm*ndd*2).normalize)
              //              else new Ray(id.point+id.norm*0.0001,(ray.dirVect+id.norm*ndd*0.5).normalize)
              val transRay = if (ndd < 0) new Ray(id.point - id.norm * 0.0001, ray.dir.normalize) // non-refracting
              else new Ray(id.point + id.norm * 0.0001 * geomSize, ray.dir.normalize)
              castRay(transRay, geom, lights, cnt + 1) * ((255 - id.color.a) / 255.0).toFloat
            }
          id.color * (lightColors.foldLeft(new RTColor(0, 0, 0, 1))(_ + _)) + refColor * id.reflect.toFloat + alphaColor
        }
      }
    }
  }

  private def cylinder(bottom: Point, up: Vect, left: Vect, faces: Int, col: (Point) => RTColor, ref: Double): Geometry = {
    val back = (up.normalize cross left)
    val pnts = (for (i <- 0 to faces) yield {
      val ang = 2 * math.Pi * i / faces
      val c = Math.cos(ang)
      val s = Math.sin(ang)
      (bottom + left * c + back * s, bottom + left * c + back * s + up)
    }).toArray
    val ret = new ListScene()
    val refs = Array.tabulate(faces)((x) => ref)
    println(pnts.length)
    for (i <- 0 until faces) {
      ret.addGeom(new GeomPolygon(Array(pnts(i)._2, pnts(i)._1, pnts(i + 1)._1, pnts(i + 1)._2), col, refs))
    }
    ret.addGeom(new GeomPolygon(pnts.take(faces).map(_._1).toArray, col, refs))
    ret.addGeom(new GeomPolygon(pnts.take(faces).map(_._2).toArray, col, refs))
    ret
  }

  private def gridFromFunc(func: (Double, Double) => Double, corner: Point, right: Vect, up: Vect, num: Int): Array[Array[Point]] = {
    val rmag = right.magnitude
    val umag = up.magnitude
    val rvec = right.normalize
    val uvec = up.normalize
    val out = (up cross right).normalize
    for (i: Int <- (1 until num).toArray) yield {
      for (j: Int <- (1 until num).toArray) yield {
        val x = i * rmag / num
        val y = j * umag / num
        corner + rvec * x + uvec * y + out * (func(x, y))
      }
    }
  }

  private def triangulateGrid(scene: Scene, grid: Array[Array[Point]], col: (Point) => RTColor, ref: Double): Unit = {
    val refs = Array(ref, ref, ref)
    val norm = for (i <- 0 until grid.length) yield for (j <- 0 until grid(0).length)
      yield calcNorm(grid, i, j).normalize
    for (i <- 0 until grid.length - 1; j <- 0 until grid(0).length - 1) {
      scene.addGeom(new GeomPolygon(
        Array(grid(i)(j), grid(i + 1)(j), grid(i)(j + 1)),
        Array(norm(i)(j), norm(i + 1)(j), norm(i)(j + 1)), col, refs))
      scene.addGeom(new GeomPolygon(
        Array(grid(i)(j + 1), grid(i + 1)(j), grid(i + 1)(j + 1)),
        Array(norm(i)(j + 1), norm(i + 1)(j), norm(i + 1)(j + 1)), col, refs))
    }
  }

  private def calcNorm(grid: Array[Array[Point]], i: Int, j: Int): Vect = {
    val zero = new Vect(0, 0, 0)
    (if (i > 0 && j > 0) (grid(i - 1)(j) - grid(i)(j)) cross (grid(i)(j - 1) - grid(i)(j)) else zero) +
      (if (i < grid.length - 1 && j > 0) (grid(i)(j - 1) - grid(i)(j)) cross (grid(i + 1)(j - 1) - grid(i)(j)) else zero) +
      (if (i < grid.length - 1 && j < grid.length - 1) (grid(i + 1)(j) - grid(i)(j)) cross (grid(i)(j + 1) - grid(i)(j)) else zero) +
      (if (i > 0 && j < grid.length - 1) (grid(i)(j + 1) - grid(i)(j)) cross (grid(i - 1)(j) - grid(i)(j)) else zero)
  }

  private def fileTextPoly(filename: String, topLeft: Point, sm: Double, bg: RTColor): GeomPolyFunc = {
    val source = io.Source.fromFile(filename)
    val lines = source.getLines().toArray
    source.close
    val img = new BufferedImage(10 + 15 * lines.map(_.length).max, 25 + 22 * lines.length,
      BufferedImage.TYPE_INT_ARGB)
    val height = img.getHeight / img.getWidth.toDouble
    val g = img.createGraphics()
    import java.awt.RenderingHints
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setColor(bg.toAWTColor)
    g.fillRect(0, 0, img.getWidth(), img.getHeight())
    g.setColor(Color.white)
    g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20))
    var y = 25
    for (line <- lines) {
      g.drawString(line, 5, y)
      y += 22
    }
    val texture = new TextureColorFunc(img, topLeft, Vect(sm, 0, 0), Vect(0, 0, -sm * height))
    new GeomPolyFunc(
      Array(topLeft, topLeft + Vect(0, 0, -sm * height), topLeft + Vect(sm, 0, -sm * height), topLeft + Vect(sm, 0, 0)),
      p => Vect(0, -1, 0),
      texture,
      p => 0.0)
  }

  private def pointRandom(p: Point): Double = {
    val xl = java.lang.Double.doubleToRawLongBits(p.x)
    val yl = java.lang.Double.doubleToRawLongBits(p.y)
    val zl = java.lang.Double.doubleToRawLongBits(p.z)
    ((xl << 16) ^ yl ^ (zl >> 16)).toDouble / Long.MaxValue
  }

  private def fractalSurface(scene: Scene, tp1: Point, tp2: Point, tp3: Point, color: Point => RTColor, reflect: Double, maxLevel: Int): Unit = {
    val refArray = Array(reflect, reflect, reflect)
    def helper(p1: Point, p2: Point, p3: Point, n1: Vect, n2: Vect, n3: Vect, mag: Double, lvl: Int): Unit = {
      if (lvl == maxLevel) {
        scene addGeom new GeomPolygon(Array(p1, p2, p3), Array(n1, n2, n3), color, refArray)
      } else {
        val cn1 = ((p2 - p1) cross ((n1 + n2) cross (p2 - p1))).normalize
        val cn2 = ((p3 - p2) cross ((n2 + n3) cross (p3 - p2))).normalize
        val cn3 = ((p1 - p3) cross ((n3 + n1) cross (p1 - p3))).normalize
        val cp1 = p1 + (p2 - p1) / 2 + cn1 * mag * pointRandom(p1 + (p2 - p1) / 2)
        val cp2 = p2 + (p3 - p2) / 2 + cn2 * mag * pointRandom(p2 + (p3 - p2) / 2)
        val cp3 = p3 + (p1 - p3) / 2 + cn3 * mag * pointRandom(p3 + (p1 - p3) / 2)
        helper(p1, cp1, cp3, n1, cn1, cn3, mag / 2, lvl + 1)
        helper(cp1, p2, cp2, cn1, n2, cn2, mag / 2, lvl + 1)
        helper(cp2, p3, cp3, cn2, n3, cn3, mag / 2, lvl + 1)
        helper(cp1, cp2, cp3, cn1, cn2, cn3, mag / 2, lvl + 1)
      }
    }
    val tn1 = (tp2 - tp1) cross (tp3 - tp1)
    val tn2 = (tp3 - tp2) cross (tp1 - tp2)
    val tn3 = (tp1 - tp3) cross (tp2 - tp3)
    val mag = ((tp1 - tp2).magnitude + (tp2 - tp3).magnitude + (tp3 - tp1).magnitude) / 3
    helper(tp1, tp2, tp3, tn1.normalize, tn2.normalize, tn3.normalize, mag / 4, 1)
  }
}
