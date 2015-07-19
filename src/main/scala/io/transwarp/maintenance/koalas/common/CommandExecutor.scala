package io.transwarp.maintenance.koalas.common

import java.io.PrintStream

import io.transwarp.maintenance.koalas.KoalasContext

import scala.collection.JavaConversions._

/**
 * Created by Suheng on 7/19/15.
 */

/**
 * 与shell进行交互的工具
 */
class CommandExecutor(val cmd: String, val args: Seq[String] = Seq()) extends Executor {


  //分别向文件系统和控制台输出文件
  override def execute(kc: KoalasContext, consoleOutput: PrintStream, fileOutput: PrintStream): Unit = {
    /**
     * cmdStr = ls
     * args = -la
     * mkString后 "ls -la"
     */
    val cmdStr = (Seq(cmd) ++ args).mkString(" ")


    consoleOutput.print("Execution command '%s'...".format(cmdStr))
    fileOutput.println("Execution command '%s'...".format(cmdStr))
    //List("ls","-la)
    /**
     * ProcessBuilder.start()和Runtime.exec()方法都被用来创建一个操作系统进程(执行命令行操作)
     * 并返回Process子类的一个实例
     * Process类提供执行从进程输入，执行输出到进程，等待进程完成，检查进程推出状态以及杀死进程的方法
     * Runtime.exec()和ProcessBuilder.start()传递的参数有所不同,Runtime.exec()可接受一个单独的字符串
     * 这个字符串是通过空格分隔可执行命令程序的参数，也可以接受字符串数组参数
     * 而ProcessBuilder的构造函数是一个字符串列表或数组，第一个参数是可执行命令，其他是命令行所需要的参数
     * *Runntime.exec最终是通过调用ProcessBuilder来真正实现操作的
     */
    val pb = (new ProcessBuilder()).command(Seq(cmd) ++ args) //需要调用collection.JavaConversions._
    val proc = pb.redirectErrorStream(true).start()
    if (proc.waitFor() == 0) {
      consoleOutput.println("... Success")
    } else {
      consoleOutput.println("... Failed. Return value is %d".format(proc.exitValue()))
    }

    val readBuffer = new Array[Byte](1024)
    var len = proc.getInputStream.read(readBuffer, 0, 1024)
    while (len >= 0) {
      fileOutput.write(readBuffer, 0, len)
      len = proc.getInputStream.read(readBuffer, 0, 1024)
    }

    consoleOutput.println()
    fileOutput.println()

  }
}

object CommandExecutor {
  def apply(cmd: String, args: Seq[String] = Seq()): CommandExecutor = {
    new CommandExecutor(cmd, args)
  }

  def main(args: Array[String]) {
    val s = CommandExecutor("ls", Seq("-la"))
    s.execute(new KoalasContext, System.out, System.err)
  }
}
