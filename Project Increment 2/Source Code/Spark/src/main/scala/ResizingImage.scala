import java.io.File

import org.bytedeco.javacpp.opencv_core.{Size, Mat}
import org.bytedeco.javacpp.opencv_highgui._

/**
  * Created by Mayanka on 16-Mar-16.
  */
object ResizingImage{

  def main(args: Array[String]) {
    val IMAGE_CATEGORIES = List("donut", "dumplins", "frenchfries", "hotdogs", "sushis")
    val PATH = "data/train/"
    val PATH2 = "data/train3/"
    IMAGE_CATEGORIES.foreach(f => {
      val file = new File(PATH + f)
      val listOffiles = file.listFiles()
      val file2 = new File(PATH2 + f)
      file2.mkdirs()
      var count = 1
      listOffiles.foreach(fi => {
        println(fi.getPath)
        val img = imread(fi.getPath, CV_LOAD_IMAGE_UNCHANGED)
        var src = new Mat
        org.bytedeco.javacpp.opencv_imgproc.resize(img,src,new Size(256, 192))


        imwrite(file2.getPath + "/" + count + ".jpg", src)
        count = count + 1
      })
    })
  }
}
