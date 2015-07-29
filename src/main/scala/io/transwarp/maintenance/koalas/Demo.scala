package io.transwarp.maintenance.koalas

import java.io._
import java.util.zip.{ZipEntry, ZipOutputStream}

/**
 * Created by Suheng on 7/27/15.
 */
class Demo(zipFileName: String, inputFile: File, size: String) {

  //压缩
  def zip(): Unit = {
    //    println("Compressing log...")
    //  println(inputFile.getAbsolutePath)
    try {
      val f = new File(zipFileName)

      if (!inputFile.exists()) {
        //        println("There is no compressing log path")
        return
      }
      val out = new ZipOutputStream(new FileOutputStream(f))
      val bo = new BufferedOutputStream(out)
      zip(out, inputFile, inputFile.getName, bo)
      bo.close()
      out.close()
    }
    catch {
      case e: FileNotFoundException => e.printStackTrace()
    }

    //    println("Compressed...")
  }

  //解压
  def zip(out: ZipOutputStream, f: File, base: String, bo: BufferedOutputStream): Unit = {

    if (f.isDirectory) {
      val fl = f.listFiles()
      if (fl.length == 0) {
        out.putNextEntry(new ZipEntry(base + "/"))
        //        println(base + "/")
      }
      for (i <- 0 to fl.length - 1) {
        zip(out, fl(i), base + "/" + fl(i).getName, bo)
      }
    } else {
      out.putNextEntry(new ZipEntry(base))
      //println(base)
      val in = new FileInputStream(f)
      val bi = new BufferedInputStream(in)
      var b = 0
      var total = 0

      val bytes = new Array[Byte](1024)
      b = bi.read(bytes, 0, 1024)
      //此处不能使用(b = bi.read())!=-1,Scala会表达式返回Unit
      while (b != -1) {
        total += b
        if (total > (size.toInt * 1048576)) {
          //获取指定大小的字节数进行压缩
          bi.close()
          in.close()
          return
        }

        bo.write(bytes, 0, b)
        b = bi.read(bytes, 0, 1024)
      }
      //      println(total / 1048576.0 + " size")
      bi.close()
      in.close()
    }
  }
}

object Demo {
  def main(args: Array[String]): Unit = {
        val r = new RandomAccessFile("/Users/gesuheng/WorkSpace/scalaWorkSpace/testInfo", "r")
        val len = r.length()
        var pos = len - 1
        while (pos > -1) {
          r.seek(pos)
          if (r.readByte() == '\n') {
            println(r.readLine())
          }
          pos -= 1
        }

        r.close()
//    try {
//      Demo("/Users/gesuheng/WorkSpace/scalaWorkSpace/zookeeper.log.1.zip",
//        new File("/Users/gesuheng/WorkSpace/scalaWorkSpace/test/zookeeper.log.1"), "70").zip()
//    } catch {
//      case e => e.printStackTrace()
//    }
  }

  def apply(zipFileName: String, inputFile: File, size: String) = {
    new Demo(zipFileName, inputFile, size)
  }
}
