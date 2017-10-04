package swiftvis2.plotting.renderer

import scalafx.Includes._
import scalafx.scene.canvas.GraphicsContext
import swiftvis2.plotting.Bounds
import scalafx.scene.paint.Color
import swiftvis2.plotting.Plot
import scalafx.stage.Stage
import scalafx.stage.StageStyle
import scalafx.scene.canvas.Canvas
import scalafx.scene.Scene
import scalafx.scene.text.Font
import scalafx.scene.text.Text
import scalafx.scene.SceneAntialiasing
import scalafx.scene.layout.BorderPane
import scalafx.scene.control.MenuBar
import scalafx.scene.control.Menu
import scalafx.scene.layout.Pane
import scalafx.scene.control.MenuItem
import scalafx.event.ActionEvent
import javax.imageio.ImageIO
import scalafx.embed.swing.SwingFXUtils
import java.io.FileOutputStream
import scalafx.stage.FileChooser
import scalafx.application.Platform
import scalafx.application.JFXApp
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.File
import java.awt.image.BufferedImage

object FXRenderer {
  def shellStart(args: Array[String] = Array()): Future[JFXApp] = {
    Future {
      object ShellFX extends JFXApp {}
      ShellFX.main(args)
      ShellFX
    }
  }

  def apply(plot: Plot, pwidth: Double = 1000, pheight: Double = 1000): FXRenderer = {
    val canvas = new Canvas(pwidth, pheight)
    val gc = canvas.graphicsContext2D
    val renderer = new FXRenderer(gc)
    Platform.runLater {
      val stage = new Stage(StageStyle.Decorated)
      stage.title = "Plotting Test"
      stage.scene = new Scene(pwidth, pheight + 30, false, SceneAntialiasing.Balanced) {
        val border = new BorderPane
        val menuBar = new MenuBar
        val menu = new Menu("File")
        val menuItem = new MenuItem("Save Image")
        menu.items = Seq(menuItem)
        menuBar.menus = Seq(menu)
        border.top = menuBar
        val pane = new Pane
        pane.children = canvas
        border.center = pane
        root = border

        menuItem.onAction = (ae: ActionEvent) => {
          val img = canvas.snapshot(null, null)
          val chooser = new FileChooser()
          val file = chooser.showSaveDialog(stage)
          if (file != null) ImageIO.write(SwingFXUtils.fromFXImage(img, null), "PNG", new FileOutputStream(file))
        }

        import swiftvis2.plotting

        plot.render(renderer, Bounds(0, 0, pwidth, pheight))
        pane.width.onChange {
          if (pane.width() != canvas.width() && pane.width() > 1.0 && pane.height() > 1) {
            canvas.width = pane.width()
            plot.render(renderer, Bounds(0, 0, pane.width(), pane.height()))
          }
        }
        pane.height.onChange {
          if (pane.height() != canvas.height() && pane.width() > 1 && pane.height() > 1.0) {
            canvas.height = pane.height()
            plot.render(renderer, Bounds(0, 0, pane.width(), pane.height()))
          }
        }
      }
      stage.showing = true
    }
    renderer
  }

  // TODO - Quick fix for 3D issue on Pandoras
  def aliased(plot: Plot, pwidth: Double = 1000, pheight: Double = 1000): FXRenderer = {
    val canvas = new Canvas(pwidth, pheight)
    val gc = canvas.graphicsContext2D
    val renderer = new FXRenderer(gc)
    Platform.runLater {
      val stage = new Stage(StageStyle.Decorated)
      stage.title = "Plotting Test"
      stage.scene = new Scene(pwidth, pheight + 30) {
        val border = new BorderPane
        val menuBar = new MenuBar
        val menu = new Menu("File")
        val menuItem = new MenuItem("Save Image")
        menu.items = Seq(menuItem)
        menuBar.menus = Seq(menu)
        border.top = menuBar
        val pane = new Pane
        pane.children = canvas
        border.center = pane
        root = border

        menuItem.onAction = (ae: ActionEvent) => {
          val img = canvas.snapshot(null, null)
          val chooser = new FileChooser()
          val file = chooser.showSaveDialog(stage)
          if (file != null) ImageIO.write(SwingFXUtils.fromFXImage(img, null), "PNG", new FileOutputStream(file))
        }

        import swiftvis2.plotting

        plot.render(renderer, Bounds(0, 0, pwidth, pheight))
        pane.width.onChange {
          if (pane.width() != canvas.width() && pane.width() > 1.0 && pane.height() > 1) {
            canvas.width = pane.width()
            plot.render(renderer, Bounds(0, 0, pane.width(), pane.height()))
          }
        }
        pane.height.onChange {
          if (pane.height() != canvas.height() && pane.width() > 1 && pane.height() > 1.0) {
            canvas.height = pane.height()
            plot.render(renderer, Bounds(0, 0, pane.width(), pane.height()))
          }
        }
      }
      stage.showing = true
    }
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
      import swiftvis2.plotting
      plot.render(renderer, Bounds(0, 0, pwidth, pheight))
      stage.showing = true

      val img = SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null)
      ImageIO.write(img, "PNG", new FileOutputStream(file))
      stage.showing = false
    }
  }
}

class FXRenderer(gc: GraphicsContext) extends Renderer {
  private val text = new Text("")

  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    gc.strokeOval(cx - width / 2, cy - height / 2, width, height)
  }

  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    gc.strokeRect(x, y, width, height)
  }

  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {
    gc.strokePolygon(xs.toArray, ys.toArray, xs.length min ys.length)
  }

  def drawPolygon(pnts: Seq[(Double, Double)]): Unit = {
    gc.strokePolygon(pnts)
  }

  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    gc.fillOval(cx - width / 2, cy - height / 2, width, height)
  }

  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    gc.fillRect(x, y, width, height)
  }

  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {
    gc.fillPolygon(xs.toArray, ys.toArray, xs.length min ys.length)
  }

  def fillPolygon(pnts: Seq[(Double, Double)]): Unit = {
    gc.fillPolygon(pnts)
  }

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    gc.strokeLine(x1, y1, x2, y2)
  }

  def drawLinePath(x: Seq[Double], y: Seq[Double]): Unit = {
    gc.strokePolyline(x.toArray, y.toArray, x.length min y.length)
  }

  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit = {
    gc.save
    text.text = s
    text.font = gc.font
    val fb = text.boundsInLocal()
    align match {
      case Renderer.HorizontalAlign.Left =>
        gc.translate(x, y)
        gc.rotate(angle)
        gc.fillText(s, 0, -(fb.minY + fb.maxY) / 2)
      case Renderer.HorizontalAlign.Center =>
        gc.translate(x, y)
        gc.rotate(angle)
        gc.fillText(s, -fb.width / 2, -(fb.minY + fb.maxY) / 2)
      case Renderer.HorizontalAlign.Right =>
        gc.translate(x, y)
        gc.rotate(angle)
        gc.fillText(s, -fb.width, -(fb.minY + fb.maxY) / 2)
    }
    gc.restore
  }

  def save(): Unit = gc.save

  def restore(): Unit = gc.restore

  def setColor(argb: Int): Unit = {
    val color = Color.rgb((argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, ((argb >> 24) & 0xff) / 255.0)
    gc.fill = color
    gc.stroke = color
  }

  def setStroke(stroke: Renderer.StrokeData): Unit = {
    gc.lineWidth = stroke.width
    if (stroke.dashing.nonEmpty) {
      gc.delegate.setLineDashes(stroke.dashing: _*)
    }
  }

  def setFont(fd: Renderer.FontData, size: Double): Unit = {
    gc.font = Font(fd.font, size)
    // TODO - styles not implemented
  }

  def setClip(bounds: Bounds): Unit = {
    gc.beginPath()
    gc.rect(bounds.x, bounds.y, bounds.width, bounds.height)
    gc.clip()
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

}