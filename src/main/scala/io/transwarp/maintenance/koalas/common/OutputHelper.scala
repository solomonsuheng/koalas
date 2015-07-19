package io.transwarp.maintenance.koalas.common

import java.io.{FileOutputStream, PrintStream}

import io.transwarp.maintenance.koalas.KoalasContext
import io.transwarp.maintenance.koalas.utils.ConsoleUtils

/**
 * Created by Suheng on 7/19/15.
 */
/**
 * case class使得对对象进行模式匹配非常方便
 * 当普通的类定义前加了case，可以对这些类进行模式匹配
 * 1.编译器为case class生成一个同名的对象构造器
 * 可以不使用new
 * 2.Scala编译器为case class的构造函数的参数创建以参数名为名称的属性
 * 3.编译器为case class构造了更自然的toString，hashCode和equals实现
 * 它们会递归打印，比较case class的参数属性
 * 4.Scala编译器为case class添加了一个Copy方法，这个Copy方法可以用来构造类对象的一个可以修改的拷贝
 */
case class OutputHelper(val stdout: PrintStream, logFileOutput: FileOutputStream, kc: KoalasContext) {
  val logOut = new PrintStream(logFileOutput)

  //组合输出,分别用于输出到控制台和文件目录
  val combineOut = if (stdout != null) {
    Array(stdout, logOut)
  } else {
    Array(logOut)
  }


  //OutputHelper的println函数，分别调用组合参数中的每一个输出流FileSyste和控制台输出流
  def println(s: String = ""): Unit = {
    combineOut.foreach(_.println(s))
  }

  //OutputHelper的print函数，分别调用组合参数中的每一个输出流FileSyste和控制台输出流
  def print(s: String = ""): Unit = {
    combineOut.foreach(_.print(s))
  }

  /**
   * 调用底层与shell的命令交互
   */

  /**
   * 获取系统属性
   */
  def dumpProperty(k: String, desc: String) = ConsoleUtils.dumpProperties(combineOut, System.getProperties, k, desc, fixWidth = 20)


  /**
   * 处理命令
   */
  def dumpCommand(cmd: String) = {
    //过滤为空的字符
    val tokens = cmd.split(' ').filter(!_.trim.isEmpty)

    /**
     * 与shell交互的内容所需要的参数是String,Seq()这里做拼装
     * eg.ls -la
     * Array[(String, Int)] = Array((ls,0), (-la,1))
     * Array[(String, Int)] = Array((-la,1))
     * Array[String] = Array(-la)
     */
    ConsoleUtils.dumpCommand(combineOut, tokens(0), tokens.zipWithIndex.filter(_._2 != 0).map(_._1), kc)
  }
}