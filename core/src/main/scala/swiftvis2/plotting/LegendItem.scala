package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles.{BarStyle, PlotStyle, ScatterStyle}

case class LegendItem (
                       desc: String,
                       illustrations: Seq[Illustration],
                       subItems: Seq[LegendItem] = List.empty,
                     ) extends Product  with Serializable
// Internal struct containing all data required for an illustration
// Should seriously consider a refactor to make LegendItems and section headers separate,
// as well as making gradient illustrations separate. This would get rid of the options
// in Illustration and make the render method much cleaner.


// TODO: Defaults

case class Illustration (
                            color: Int,
                            symbol: Option[PlotSymbol],
                            width: Double,
                            height: Double,
                            gradient: Option[ColorGradient] = None
                        )

object Illustration {
  val small: Double = 5
  val med: Double = 15
  val big: Double = 30
}