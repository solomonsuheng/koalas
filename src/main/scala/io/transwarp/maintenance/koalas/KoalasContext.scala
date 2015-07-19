package io.transwarp.maintenance.koalas

import java.io.{BufferedReader, File, InputStreamReader, PrintStream}
import java.util.Calendar

import io.transwarp.maintenance.koalas.common.ConfVars
import io.transwarp.maintenance.koalas.exception.KoalasException

/**
 * Created by Suheng on 7/18/15.
 */
class KoalasContext {
  //new ConfVars和new ConfVars()是一样的
  var conf = new ConfVars

  //获取是否为交互模式，默认是交互模式
  val interactiveMode = this.conf.getBoolean(ConfVars.iteractiveMode, true)

  val input = if (interactiveMode) {
    //如果是交互模式，设置获取用户输入的输入流
    new BufferedReader(new InputStreamReader((System.in)))
  } else {
    //如果不是交互模式，就不需要设置向用户的I/O
    null
  }

  val output: PrintStream = if (interactiveMode) {
    //如果是交互模式，设置向用户控制台输出的输出流
    System.out
  } else {
    //如果不是交互模式，就不需要设置向用户的I/O
    null
  }

  //获取时间，设置时间，用户日志和文件的输出
  private val currTime = Calendar.getInstance()
  currTime.setTimeInMillis(System.currentTimeMillis())

  //设置日志输入文件，根据时间进行设置
  /**
   * conf.get(ConfVars.workDir,".")获取文件输出位置，如果没有设置，默认为当前目录
   * Calendar.MONTH默认获取的月份是少1的
   */
  private val logDirStr = conf.get(ConfVars.workDir, ".") + "/log/" + "koalas_%02d%02d%02d%02d%02d".format(
    currTime.get(Calendar.MONTH) + 1, currTime.get(Calendar.DAY_OF_MONTH),
    currTime.get(Calendar.HOUR), currTime.get(Calendar.MINUTE), currTime.get(Calendar.SECOND)
  )

  /**
   * 设置文件输出目录
   * lazy初始化被推迟，直到首次取值，在进行调用
   */
  lazy val workingDir = {
    val dir = new File(logDirStr)
    if (dir.exists()) {
      //日志文件之前不能够存在，如果存在不进行输出
      throw new KoalasException("%s has been exists.".format(this.logDirStr))
    } else {
      //如果不存在工作目录，根据系统时间戳进行目录的创建
      dir.mkdir()
    }
    //返回创建后的目录
    dir
  }

  private val dataDirStr = {
    //从conf中获取数据输出目录，如果不存在则设置当前目录下的data文件夹
    conf.get(ConfVars.dataDir, ".") + "/data"
  }

  /**
   * lazy初始化被推迟，直到第一次调用才加载
   */
  lazy val dataDir = {
    val dir = new File(dataDirStr)
    if (!dir.exists()) {
      //如果数据输出目录不存在创建，如果存在仍使用原来的存在的
      dir.mkdir()
    }

    dir
  }

}
