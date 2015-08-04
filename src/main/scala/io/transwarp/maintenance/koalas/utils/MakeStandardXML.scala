package io.transwarp.maintenance.koalas.utils

import java.io.{File, FileNotFoundException, PrintWriter}
import java.util.Scanner

import io.transwarp.maintenance.koalas.common.CommandExecutor

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Created by Suheng on 7/31/15.
 */
/**
 * 当该工具处于标准的TDH环境中
 * 调用该功能扫描TDH标准环境的XML
 * 生成标准XML
 */
class MakeStandardXML(fileName: String) {
  /**
   * 获取需要生成jar文件的标准路径xml->TDHScanLib.xml
   * 返回每一个路径用于扫描
   */
  def getFilePathByTDHSCanLibXML() = {
    val allLibPath = ExternalResourcesLoadUtils.loadTDHScanFilePath()
    allLibPath
  }

  /**
   * 根据给定的目录
   * 扫描出所有该目录下面所还有的文件
   */
  def scanFileListByGiveFilePath(filePath: String, libPathListBuffer: ListBuffer[String]): Unit = {
    if (!(new File(filePath).exists())) {
      //file or directory doesn't exists
      return
    } else {
      //file exists
      val resultFile = CommandExecutor.executeShellWithArguments("ls -l ", filePath)._1.split("\n")
      //对数据进行拆分成指定的形式
      //      println(filePath.split("\\/")(3))
      //      resultFile.foreach(println)
      val resultArray = new ListBuffer[String]()
      for (i <- 1 to resultFile.length - 1) {
        resultArray += resultFile(i)
      }
      //对获取的数据进行拼接
      //      resultArray.foreach(spliceJoin(_))
      resultArray.foreach(kv => libPathListBuffer += spliceJoin(kv, filePath))
    }
  }

  def makeXML(component_name: String, libArray: ListBuffer[String]) = {
    val xmlPrint = new StringBuffer()
    xmlPrint.append("<" + component_name + " component_name=\"" + component_name + "\">")
    libArray.foreach(kv => xmlPrint.append("<lib value=\"" + kv + "\"/>"))
    xmlPrint.append("</" + component_name + ">")
    xmlPrint.toString
  }

  /**
   * 将扫描的数据进行拼接
   */
  def spliceJoin(stringFile: String, filePath: String) = {
    val sb = new StringBuffer()
    val s = stringFile.split(" ").filterNot(_.equals(""))
    sb.append(s(0).substring(1, 4) + " ")
    sb.append(s(0).substring(4, 7) + " ")
    sb.append(s(0).substring(7, 10) + " ")
    sb.append(s(2) + " ")
    sb.append(s(3) + " ")
    if (s.length > 10) {
      //链接文件
      sb.append(filePath + "/" + s(8) + " ")
      sb.append(filePath + "/" + s(10))
    } else {
      //非链接文件
      sb.append(filePath + "/" + s(8))
    }
    sb.toString()
  }

  def getStandardTDHXML(): Unit = {
    val libAndItsLibPath = mutable.HashMap[String, ListBuffer[String]]()
    getFilePathByTDHSCanLibXML().
      foreach(kv => {
      libAndItsLibPath.put(kv.split("\\/")(3), new ListBuffer[String]())
      scanFileListByGiveFilePath(kv, libAndItsLibPath.getOrElse(kv.split("\\/")(3), new ListBuffer[String]()))
    })
    val sb = new StringBuffer()
    sb.append("<Version>")
    //    libAndItsLibPath.foreach(println)
    libAndItsLibPath.foreach(kv => {
      sb.append(makeXML(kv._1.capitalize, kv._2))
    })
    sb.append("</Version>")
    //    println(sb.toString)
    val writer = new PrintWriter(new File(fileName))
    writer.write(sb.toString)
    writer.close
  }
}

object MakeStandardXML {
  def apply(fileName: String) = {
    new MakeStandardXML(fileName)
  }

  def main(args: Array[String]): Unit = {
    var flag = true
    while (flag) {
      try {
        outPut()
        flag = false
      } catch {
        case ex: FileNotFoundException => {
          println("Input file not found try again")
        }
      }
    }
  }

  def outPut(): Unit = {
    val sc = new Scanner(System.in)
    println("Please Input File Name (eg: 4_3, 5_1): ")
    val filePath = sc.nextLine()
    MakeStandardXML("version"+filePath+".xml").getStandardTDHXML()
    println("StandardTDHXML Output Done")
  }
}
