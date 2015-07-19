package io.transwarp.maintenance.koalas.common

import java.io.PrintStream

import io.transwarp.maintenance.koalas.KoalasContext

/**
 * Created by Suheng on 7/19/15.
 */
trait Executor {
  //分别向文件系统和控制台输出文件
  def execute(kc: KoalasContext, consoleOutput: PrintStream, fileOutput: PrintStream)
}
