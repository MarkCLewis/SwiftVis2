package raytrace

import java.awt.Color
import java.awt.image.BufferedImage

class TextureColorFunc(bi:BufferedImage,tl:Point,r:Vect,d:Vect) extends (Point => Color) {
    val img=bi
    val topLeft=tl
    val right=r.normalize
    val down=d.normalize
    val rInvStep=img.getWidth/r.magnitude
    val dInvStep=img.getHeight/d.magnitude
    
    def apply(p:Point):Color = {
      val rpix=((right dot (p-topLeft))*rInvStep).toInt
      val dpix=((down dot (p-topLeft))*dInvStep).toInt
      if(rpix<0 || rpix>=img.getWidth || dpix<0 || dpix>=img.getHeight) new Color(0,0,0,0)
      else new Color(img.getRGB(rpix,dpix),true)
    }
}
