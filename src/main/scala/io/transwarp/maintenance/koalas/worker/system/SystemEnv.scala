package io.transwarp.maintenance.koalas.worker.system

import java.io.{File, FileOutputStream}

import io.transwarp.maintenance.koalas.KoalasContext
import io.transwarp.maintenance.koalas.common.OutputHelper

import scala.collection.mutable

/**
 * Created by Suheng on 7/19/15.
 */
class SystemEnv(kc: KoalasContext) {
  //File(String parent,String child)根据parent路径名和child路径名字字符串创建新的File实例
  val logFile = new File(kc.workingDir, "system.log")


  def action(): Unit = {
    //日志输出目录
    val logOutput = new FileOutputStream(logFile)
    //创建输出辅助类
    val outputHelper = OutputHelper(kc.output, logOutput, kc)


    try {
      os()
    } catch {
      case e: Throwable =>
    } finally {
      //关闭输出文件流
      logOutput.close()
    }


    /**
     * 检查环境变量和系统属性
     */
    def os(): Unit = {
      //从xml中获取数据，返回为map结果
      val prop = loadSystemEnvXML("Env")
      //处理环境变量
      for ((k, v) <- prop) {
        outputHelper.dumpProperty(k.toString, v.toString)
      }

      outputHelper.println()

      //处理命令
      val cmd = loadSystemEnvXML("Cmd")
      for ((k, v) <- cmd) {
        outputHelper.dumpCommand(k.toString + " " + v.toString)
      }
    }
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
    val sysxml = scala.xml.XML.load(this.getClass.getClassLoader.getResource(systemEnvXMLPath))
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

}

/**
 * Scala没有静态方法，不过有个类似的特性叫做单例对象
 * 通常一个类有一个伴生对象
 * 类和它的伴生对象可以互相访问私有特性
 * 但是必须存在于同一个源文件中
 */
object SystemEnv {

  /**
   * apply方法返回的是伴生类的对象
   * 这样可以省去new，这样做的好处就是在嵌套表达式中很方便
   */
  def apply(kc: KoalasContext): SystemEnv = {
    new SystemEnv(kc)
  }
}

