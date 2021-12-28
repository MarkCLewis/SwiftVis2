package swiftvis2

import scalajs.js
import slinky.core._
import slinky.core.annotations.react
import slinky.web.ReactDOM
import swiftvis2.plotting._
import swiftvis2.plotting.renderer.ReactRenderer

import slinky.web.svg._

@react class SwiftVis2Component extends StatelessComponent {
  case class Props(plot: Plot, width: Double, height: Double)

  
  def render() = {
    val renderer = new ReactRenderer(props.width, props.height)
    props.plot.render(renderer, Bounds(0.0, 0.0, props.width, props.height))
    svg ( width:=s"${props.width}px", height:=s"${props.height}px", renderer.elements )
  }

}