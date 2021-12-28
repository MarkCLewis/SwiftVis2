package swiftvis2.raytrace

class ListScene(g:Geometry*) extends Geometry with Scene {
  private var geom : List[Geometry] = g.toList
  
  def addGeom(g:Geometry): Unit = {
    geom = g::geom
  }
  
  override def intersect(r:Ray) : Option[IntersectData] = {
    def recur(g:List[Geometry]) : Option[IntersectData] = {
      g match {
        case Nil => None
        case h :: t => {
        	val thisID=h.intersect(r)
          val tailID=recur(t)
          (tailID,thisID) match {
            case (None,_) => thisID
            case (_,None) => tailID
            case (Some(tailData),Some(thisData)) => {
              if(tailData.time<thisData.time) tailID
              else thisID
            }
          }
        }
      }
    }
    recur(geom)
  }
  
  def boundingSphere : Sphere = {
    geom.foldLeft(geom.head.boundingSphere)((s,g)=>BoundingSphere.mutualSphere(s,g.boundingSphere))
  }

  def boundingBox : Box = {
    geom.foldLeft(geom.head.boundingBox)((s,g)=>BoundingBox.mutualBox(s,g.boundingBox))
  }
}
