package io.transwarp.maintenance.koalas.utils

import scala.collection.mutable

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
   * 从KoalasConfig.xml中获取默认配置
   * eg.是否为交互模式 etc
   * requireArgs为所需要读取的内容,eg iteractiveMode
   */

  def loadKoalasConfigXML(requireArgs: String): String = {
    //获取xml文件
    val koalasConfigXMLPath: String = "Koalas/KoalasConfig.xml"
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
    var push: mutable.LinkedHashMap[String, String] = mutable.LinkedHashMap[String, String]()
    //xml文件路径
    val systemEnvXMLPath: String = "SystemEnv/SystemEnv.xml"
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

