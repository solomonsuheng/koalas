package io.transwarp.maintenance.koalas.utils

import io.transwarp.maintenance.koalas.worker.tdhenv.Service

import scala.collection.mutable
import scala.collection.mutable.{LinkedHashMap, ListBuffer}

/**
 * Created by Suheng on 7/19/15.
 */
/**
 * 用于加载各种外部资源文件的工具类，如xml等
 */
class ExternalResourcesLoadUtils {

}


object ExternalResourcesLoadUtils {
  def apply() = {
    new ExternalResourcesLoadUtils
  }

  /**
   * 获取日志文件xml
   */
  def loadTDHLogXML() = {
    val tdhLogXML = loadXMLByScalaWithPath("tdhlog/tdhlog.xml")
    val libPath = tdhLogXML \\ "@value"
    libPath.toArray.map(_.toString)
  }


  /**
   * 读取有多少种versionxml
   */
  def loadHowManyVersionXML() = {
    val howManyVersionXML = loadXMLByScalaWithPath("tdhversion/version.xml")
    val versionArray = (howManyVersionXML \\ "@name").toArray.map(_.toString)
    versionArray
  }

  /**
   * 加载tdhversion指定版本标准TDH环境
   */
  def loadTDHVersion(version: String) = {
    val tdhVersionXMLPath = "tdhversion/" + version
    val tdhTDHVersionXML = loadXMLByScalaWithPath(tdhVersionXMLPath)
    //return Array(Hadoop-hdfs, Hadoop-mapreduce, Hadoop-yarn)
    val l = (tdhTDHVersionXML \\ "@component_name").toArray.map(_.toString)
    def getLibByComponentName(component_name: String) = {
      (tdhTDHVersionXML \ component_name \ "lib" \\ "@value").toArray.map(_.toString)
    }
    val resulthashMap = new mutable.HashMap[String, Array[String]]()
    val libList: ListBuffer[String] = ListBuffer[String]()
    l.map(component_name => (component_name, getLibByComponentName(component_name))).map(kv => resulthashMap.put(kv._1, kv._2))
    resulthashMap
  }


  /**
   * 获取TDHScanLib.xml中所有要检查的serviceName
   */
  def loadTDHSCanServiceName() = {
    val tdhScanLibXMLPath: String = "tdhenv/TDHScanLib.xml"
    val tdhScanLibXML = loadXMLByScalaWithPath(tdhScanLibXMLPath)
    (tdhScanLibXML \ "_").toString.split("\\>").map(getServiceName(_)).toSet
  }

  def getServiceName(service: String) = {
    val k = service.split(" ")(0).substring(1)
    k
  }

  /**
   * 从TDHScanLib.xml中获取需要koalas执行command扫描的本地系统目录
   */
  def loadTDHScanLibXML(serviceName: String) = {
    //存放Koalas调用shell需要检查的TDHLib目录
    val resultScanLib = ListBuffer[String]()
    val tdhScanLibXMLPath: String = "tdhenv/TDHScanLib.xml"
    val tdhScanLibXML = loadXMLByScalaWithPath(tdhScanLibXMLPath)
    //从XML中获取的数据进行封装
    (tdhScanLibXML \ serviceName \ "@value").toString.split(" ").map(_.trim).foreach(resultScanLib += _)
    //根据serviceName查找路径后，将serviceName和查找到的路径进行封装为一个Service返回
    new Service(serviceName, resultScanLib)
  }


  /**
   * 从KoalasConfig.xml中获取默认配置
   * eg.是否为交互模式 etc
   * requireArgs为所需要读取的内容,eg iteractiveMode
   */

  def loadKoalasConfigXML(requireArgs: String): String = {
    //获取xml文件
    val koalasConfigXMLPath: String = "koalas/KoalasConfig.xml"
    val koalasxml = loadXMLByScalaWithPath(koalasConfigXMLPath)
    //根据需要获取需要的xml中的内容返回为字符串类型
    val value = (koalasxml \ requireArgs \ "@value").toString
    value
  }

  /**
   * 从SystemEnv.xml中获取需要检查的内容存储到Map，返回
   */
  def loadSystemEnvXML(envOrCmd: String) = {
    /**
     * 为了避免从xml读入到Map中的内容没有按照xml中的顺序排列所以使用LinkedHashMap
     * 这样可以使从xml中读取的内容按照xml中的顺序排列
     */
    var push: LinkedHashMap[String, String] = LinkedHashMap[String, String]()
    //xml文件路径
    val systemEnvXMLPath: String = "systemenv/SystemEnv.xml"
    //scala function load xml file
    val sysxml = loadXMLByScalaWithPath(systemEnvXMLPath)
    //根据参数的不同加载不同xml，返回匹配xml
    if (envOrCmd.equals("Env")) {
      val env = (sysxml \ "System_Enviroment" \ "Env")
      env.map(n => (push.put((n \ "@name").toString, (n \ "@desc").toString)))
    } else if (envOrCmd.equals("Cmd")) {
      val env = (sysxml \ "System_Command" \ "Cmd")
      env.map(n => (push.put((n \ "@name").toString, (n \ "@args").toString)))
    }
    push
  }

  //辅助函数，用于调用Scala内置函数加载xml资源
  def loadXMLByScalaWithPath(xmlPath: String) = {
    scala.xml.XML.load(this.getClass.getClassLoader.getResource(xmlPath))
  }

  def main(args: Array[String]) {
    println(loadKoalasConfigXML("koalas.datadir"))
  }
}

