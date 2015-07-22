package io.transwarp.maintenance.koalas.worker.system

import java.io.{File, FileOutputStream}

import io.transwarp.maintenance.koalas.KoalasContext
import io.transwarp.maintenance.koalas.common.OutputHelper
import io.transwarp.maintenance.koalas.utils.ExternalResourcesLoadUtils

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
      case e: Throwable => e.printStackTrace()
    } finally {
      //关闭输出文件流
      logOutput.close()
    }


    /**
     * 检查环境变量和系统属性
     */
    def os(): Unit = {
      //从xml中获取数据，返回为map结果
      val prop = ExternalResourcesLoadUtils.loadSystemEnvXML("Env")
      //处理环境变量
      for ((k, v) <- prop) {
        outputHelper.dumpProperty(k.toString, v.toString)
      }

      outputHelper.println()

      //处理命令
      val cmd = ExternalResourcesLoadUtils.loadSystemEnvXML("Cmd")
      for ((k, v) <- cmd) {
        outputHelper.dumpCommand(k.toString + " " + v.toString)
      }
    }
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
  def apply(kc: KoalasContext) = {
    new SystemEnv(kc)
  }
}

