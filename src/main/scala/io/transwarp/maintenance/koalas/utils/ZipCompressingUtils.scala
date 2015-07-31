package io.transwarp.maintenance.koalas

import java.io._
import java.util.zip.{ZipEntry, ZipOutputStream}

/**
 * Created by Suheng on 7/27/15.
 */

class ZipCompressingUtils(zipFileName: String, file: File, maxSize: String, tempFilePath: String) {
  //zipFileName传入进来的是压缩后目录的名字和路径
  //inputFile存放的是要压缩的路径如/var/log/zookeeper1
  /**
   * inputFile中获取文件先进行.log然后是.log.1进行扫描
   */
  def zipFile(inputFileName: String) = {
    //input file
    val file = new File(inputFileName)

    val in = new FileInputStream(file)

    //out put file
    val out = new ZipOutputStream(new FileOutputStream(zipFileName))

    out.putNextEntry(new ZipEntry(file.getName))

    val bytes = new Array[Byte](1024)

    var len = in.read(bytes, 0, 1024)
    while (len > 0) {
      out.write(bytes, 0, len)
      len = in.read(bytes, 0, 1024)
    }

    out.close
    in.close
  }


  def tail(): Unit = {
    val fileListTotal = file.list()
    val tempFile = new File(tempFilePath)
    val tempFileOutput = new DataOutputStream(new FileOutputStream(tempFile))
    if (!file.exists()) {
      //文件不存在
      return
    }
    var isNotCLose = true
    fileListTotal.foreach(fileList => {
      if (isNotCLose) {
        aux(fileList)
      }
    })

    if (tempFile.exists()) {
      tempFile.delete()
    }

    def aux(fileList: String) {
      val fileHander = new RandomAccessFile(file.getAbsoluteFile + "/" + fileList, "r")
      val fileLength = fileHander.length()
      var point = fileLength - 1
      var sb = new StringBuffer()
      var s: Long = 0
      while (point > 0) {
        fileHander.seek(point)
        val c = fileHander.readByte().toChar
        s += 1
        if (c == 0xA || c == 0xD) {
          val stringBuilder = sb.reverse().toString

          //        println(stringBuilder)
          try {
            tempFileOutput.writeBytes(stringBuilder + "\n")
          } catch {
            case ex => {
              tempFile.delete()
            }
          }

          sb = new StringBuffer()

          if (s > (maxSize.toInt * 1048576)) {
            tempFileOutput.close()
            zipFile(tempFile.getAbsolutePath)
            tempFile.delete()
            //          println(total)
            isNotCLose = false
            return
          }
        } else {
          sb.append(c)
        }
        point -= 1
      }
    }
  }
}

object ZipCompressingUtils {
  def apply(zipFileName: String, file: File, maxSize: String, tempFilePath: String) = {
    new ZipCompressingUtils(zipFileName: String, file: File, maxSize: String, tempFilePath: String)
  }

  def main(args: Array[String]) {
    val path = "/var/log/zookeeper1:10"
    val outputZipPath = "/Users/gesuheng/WorkSpace/scalaWorkSpace/" + "pp" + ".zip"
    val d = new Demo(outputZipPath, new File(path.split("\\:")(0) + "/"), path.split("\\:")(1), "/Users/gesuheng/WorkSpace/scalaWorkSpace/tempFile")
    d.tail()
    //    val f = new File(path)

    //    val fileList = f.list()

    //    d.tail(new File("/var/log/zookeeper1/" + fileList(0)))
  }
}

