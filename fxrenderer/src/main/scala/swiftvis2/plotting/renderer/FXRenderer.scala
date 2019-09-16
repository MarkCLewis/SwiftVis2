package swiftvis2.plotting.renderer

import java.io.{File, FileOutputStream}
import java.util.concurrent.{CountDownLatch, Executors}

import javax.imageio.ImageIO
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.embed.swing.SwingFXUtils
import scalafx.event.ActionEvent
import scalafx.geometry.VPos
import scalafx.scene.canvas.{Canvas, GraphicsContext}
import scalafx.scene.control.{ChoiceDialog, Menu, MenuBar, MenuItem}
import scalafx.scene.layout.{BorderPane, Pane}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, Text, TextAlignment}
import scalafx.scene.{Scene, SceneAntialiasing}
import scalafx.stage.{FileChooser, Stage, StageStyle, WindowEvent}
import swiftvis2.plotting.{Bounds, Plot}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object FXRenderer {
  def shellStart(args: Array[String] = Array()): Future[JFXApp] = {
    Future {
      object ShellFX extends JFXApp
      ShellFX.main(args)
      ShellFX
    }
  }

  def apply(plot: Plot, pwidth: Double = 1000, pheight: Double = 1000): FXRenderer = {
    val ec = ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())
    val canvas = new Canvas(pwidth, pheight)
    val gc = canvas.graphicsContext2D
    val renderer = new FXRenderer(gc, 10000)
    Platform.runLater {
      try {
        val stage = new Stage(StageStyle.Decorated)
        stage.onCloseRequest = (e: WindowEvent) => ec.shutdown()
        stage.title = "Plotting Test"
        stage.scene = new Scene(pwidth, pheight + 30, false, SceneAntialiasing.Balanced) {
          val border = new BorderPane
          val menuBar = new MenuBar
          val menu = new Menu("File")
          val menuItem = new MenuItem("Save Image")
          val svgMenuItem = new MenuItem("Save as SVG")
          menu.items = Seq(menuItem, svgMenuItem)
          menuBar.menus = Seq(menu)
          border.top = menuBar
          val pane = new Pane
          pane.children = canvas
          border.center = pane
          root = border

          val choices = Seq("1000,1000", "1280,720", "1280,800", "1000,750")
          val svgDialog = new ChoiceDialog(defaultChoice = "1000x1000", choices = choices) {
            title = "SVG Size"
            headerText = "Select prefered width and height for the SVG"
            contentText = "Choose your resolution:"
          }

          menuItem.onAction = (ae: ActionEvent) => {
            val img = canvas.snapshot(null, null)
            val chooser = new FileChooser()
            val file = chooser.showSaveDialog(stage)
            if (file != null) ImageIO.write(SwingFXUtils.fromFXImage(img, null), "PNG", new FileOutputStream(file))
          }

          svgMenuItem.onAction = (ae: ActionEvent) => {
            val chooser = new FileChooser()
            val file = chooser.showSaveDialog(stage)
            if (file != null) {}
            val result = svgDialog.showAndWait()
            result match {
              case Some(choice) => JVMSVGInterface(plot, file.getPath(), choice.split(",")(0).toDouble, choice.split(",")(1).toDouble)
              case None => println("Save as SVG cancelled")
            }
          }
          var lastWidth = 0.0
          var lastHeight = 0.0

          pane.width.onChange {
            if (pane.width() != canvas.width() && pane.width() > 1.0 && pane.height() > 1) {
              canvas.width = pane.width()
              if ((lastWidth - pane.width()).abs > 5 || (lastHeight - pane.height()).abs > 5) {
                // TODO - put in a proper queueing system
                Future {
                  lastWidth = pane.width()
                  lastHeight = pane.height()
                  println(s"Render for width: $lastWidth $lastHeight")
                  plot.render(renderer, Bounds(0, 0, pane.width(), pane.height()))
                }(ec)
              }
              ()
            }
          }
          pane.height.onChange {
            if (pane.height() != canvas.height() && pane.width() > 1 && pane.height() > 1.0) {
              canvas.height = pane.height()
              if ((lastWidth - pane.width()).abs > 5 || (lastHeight - pane.height()).abs > 5) {
                Future {
                  lastWidth = pane.width()
                  lastHeight = pane.height()
                  println(s"Render for height: $lastWidth $lastHeight")
                  plot.render(renderer, Bounds(0, 0, pane.width(), pane.height()))
                }(ec)
              }
              ()
            }
          }
        }
        stage.showing = true
      } catch {
        case ex: Exception => ex.printStackTrace
      }
    }
    //    plot.render(renderer, Bounds(0, 0, pwidth, pheight))
    renderer
  }

  /**
   * Pops up a stage with a Canvas and renders the plot to it, then saves the resulting snapshot if applicable and closes the window.
   */
  def saveToImage(plot: Plot, pwidth: Double, pheight: Double, file: File): Unit = {
    val canvas = new Canvas(pwidth, pheight)
    val gc = canvas.graphicsContext2D
    val renderer = new FXRenderer(gc)
    Platform.runLater {
      val stage = new Stage(StageStyle.Decorated)
      stage.scene = new Scene(pwidth, pheight + 30, false, SceneAntialiasing.Balanced)
      stage.scene().content = canvas
      plot.render(renderer, Bounds(0, 0, pwidth, pheight))
      stage.showing = true

      val img = SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null)
      ImageIO.write(img, "PNG", new FileOutputStream(file))
      stage.showing = false
    }
  }
}

class FXRenderer(gc: GraphicsContext, maxQueue: Int = 1000) extends Renderer {
  private val text = new Text("")
  private var queue = mutable.Queue[() => Unit]()

  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    enqueue(() => gc.strokeOval(cx - width / 2, cy - height / 2, width, height))
  }

  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    enqueue(() => gc.strokeRect(x, y, width, height))
  }

  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {
    enqueue(() => gc.strokePolygon(xs.toArray, ys.toArray, xs.length min ys.length))
  }

  def drawPolygon(pnts: Seq[(Double, Double)]): Unit = {
    enqueue(() => gc.strokePolygon(pnts))
  }

  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    enqueue(() => gc.fillOval(cx - width / 2, cy - height / 2, width, height))
  }

  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    enqueue(() => gc.fillRect(x, y, width, height))
  }

  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {
    enqueue(() => gc.fillPolygon(xs.toArray, ys.toArray, xs.length min ys.length))
  }

  def fillPolygon(pnts: Seq[(Double, Double)]): Unit = {
    enqueue(() => gc.fillPolygon(pnts))
  }

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    enqueue(() => gc.strokeLine(x1, y1, x2, y2))
  }

  def drawLinePath(x: Seq[Double], y: Seq[Double]): Unit = {
    enqueue(() => gc.strokePolyline(x.toArray, y.toArray, x.length min y.length))
  }

  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit = {
    enqueue(() => {
      gc.save
      text.text = s
      text.font = gc.font
      gc.textBaseline = VPos.Center
      gc.translate(x, y)
      gc.rotate(angle)
      gc.textAlign = align match {
        case Renderer.HorizontalAlign.Left => TextAlignment.Left
        case Renderer.HorizontalAlign.Center => TextAlignment.Center
        case Renderer.HorizontalAlign.Right => TextAlignment.Right
      }
      gc.fillText(s, 0, 0)
      gc.restore
    })
  }

  def save(): Unit = enqueue(() => gc.save)

  def restore(): Unit = enqueue(() => gc.restore)

  def setColor(argb: Int): Unit = {
    enqueue(() => {
      val color = Color.rgb((argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, ((argb >> 24) & 0xff) / 255.0)
      gc.fill = color
      gc.stroke = color
    })
  }

  def setStroke(stroke: Renderer.StrokeData): Unit = {
    enqueue(() => {
      gc.lineWidth = stroke.width
      if (stroke.dashing.nonEmpty) {
        gc.delegate.setLineDashes(stroke.dashing: _*)
      }
    })
  }

  def setFont(fd: Renderer.FontData, size: Double): Unit = {
    enqueue(() => {
      gc.font = Font(fd.font, size)
      // TODO - styles not implemented
    })
  }

  def setClip(bounds: Bounds): Unit = {
    enqueue(() => {
      gc.beginPath()
      gc.rect(bounds.x, bounds.y, bounds.width, bounds.height)
      gc.clip()
    })
  }

  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double = {
    val text = new Text("")
    text.font = Font(fd.font, 10)
    var maxWidth = 0.0
    var maxHeight = 0.0
    for (s <- strings) {
      text.text = s
      val bounds = text.boundsInLocal()
      if (bounds.width > maxWidth) maxWidth = bounds.width
      if (bounds.height > maxHeight) maxHeight = bounds.height
    }
    10 * (allowedWidth / maxWidth min allowedHeight / maxHeight)
  }

  def finish(): Unit = {
    clearQueue()
  }

  private def enqueue(op: () => Unit): Unit = {
    //    println("adding "+queue.size)
    queue += op
    //    if (queue.size % 100 == 0) println(queue.size)
    if (queue.size >= maxQueue) clearQueue()
  }

  case class Runner(lq: mutable.Queue[() => Unit], latch: CountDownLatch) {
    def run(): Unit = {
      while (!lq.isEmpty) {
        val f = lq.dequeue()
        f()
        if(latch.getCount > 0 && lq.size < maxQueue/4) latch.countDown()
      }
      if(latch.getCount > 0) latch.countDown
      //      println("Done dequeueing")
    }
  }

  private def clearQueue(): Unit = {
    println("Clearing "+queue.size)
    println(Thread.currentThread)
    val latch = new CountDownLatch(1)
    val runner = Runner(queue, latch)
    queue = mutable.Queue[() => Unit]()
    if (gc.canvas.parent != null) {
      Platform.runLater {
        runner.run()
      }
    } else {
      runner.run()
    }
    latch.await
  }
}
