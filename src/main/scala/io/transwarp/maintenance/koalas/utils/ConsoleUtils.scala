package io.transwarp.maintenance.koalas.utils

import java.io.PrintStream
import java.util.Properties

import io.transwarp.maintenance.koalas.KoalasContext
import io.transwarp.maintenance.koalas.common.CommandExecutor

/**
 * Created by Suheng on 7/19/15.
 */
/**
 * 用于调用shell命令和shell交互的工具
 */
object ConsoleUtils {
  /**
   * ps是组合输出流分别输出到控制台和file system
   * k是要显示的名字，desc是要替换显示的名字，别名
   */
  def dumpProperties(ps: Array[PrintStream], prop: Properties, k: String, desc: String = null, fixWidth: Int = 10): Unit = {
    var v = prop.getProperty(k)
    if (v == null) {
      v = ""
    }
    //判断是否需要使用别名
    val d = if (desc == null) {
      k //别名为空，使用原来的名字
    } else {
      desc //使用别名
    }
    ps.foreach(_.println(("%" + fixWidth + "s :%s").format(d, v)))
  }

  def newSection(ps: Array[PrintStream]): Unit = {
    ps.foreach(_.println)
  }

  /**
   * 执行命令与shell进行交互
   */

  def dumpCommand(ps: Array[PrintStream], cmd: String, args: Seq[String] = Seq(), kc: KoalasContext): Unit = {
    CommandExecutor(cmd, args).execute(kc, ps(0), ps(1))
  }
}
