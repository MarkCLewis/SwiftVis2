package polynote.swiftvis2

import java.io.ByteArrayOutputStream
import java.util.Base64

import javax.imageio.ImageIO
import polynote.runtime.{MIMERepr, ReprsOf}
import swiftvis2.plotting.Plot
import swiftvis2.plotting.renderer.SwingRenderer

trait SwiftVisReprsOf[T] extends ReprsOf[T]

object SwiftVisReprsOf {
  implicit val plotRep: SwiftVisReprsOf[Plot] = {
    (value: Plot) => {
      val out = new ByteArrayOutputStream()
      val bi = SwingRenderer.renderToImage(value, 800, 600)
      ImageIO.write(bi, "png", out)
      val bytes = out.toByteArray
      val enc = Base64.getEncoder
      Array(
        MIMERepr(
          "image/png", enc.encodeToString(bytes)
        )
      )
    }
  }
}