package io.transwarp.maintenance.koalas.worker.tdhenv

import java.io.{File, FileOutputStream}

import io.transwarp.maintenance.koalas.KoalasContext
import io.transwarp.maintenance.koalas.common.{CommandExecutor, OutputHelper}
import io.transwarp.maintenance.koalas.utils.{ExternalResourcesLoadUtils, ZipCompressingUtils}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Created by Suheng on 7/20/15.
 */

/**
 * 需要检查的服务
 * serviceName eg. Hadoop,Hive,Zookeeper etc
 * directories为Hadoop需要检查的目录或etc需要检查的目录
 */
class Service(serviceName: String, directories: ListBuffer[String]) {
  override def toString: String = "[" + serviceName + "]"

  //获取需要检查的目录ListBuffer
  def getDirectories = {
    directories
  }

  def getServiceName = {
    serviceName
  }
}


/**
 * THD环境变量检查
 */
class TDHEnv(kc: KoalasContext) {
  //日志输出目录
  val logFile = new File(kc.workingDir, "TDHEnvCheck.log")
  //打包日志
  val zipLogFile = new File(kc.workingDir, "zipLog.log")
  //添加需要检查当前环境的目录
  val checkedService: mutable.HashSet[Service] = new mutable.HashSet[Service]()


  //没有安装的服务
  val unInstalledService: mutable.HashSet[String] = new mutable.HashSet[String]()

  //加载服务名
  for (serviceName <- ExternalResourcesLoadUtils.loadTDHSCanServiceName()) {
    //需要koalas扫描的服务目录
    checkedService.add(ExternalResourcesLoadUtils.loadTDHScanLibXML(serviceName))
  }


  /**
   * 多版本存储媒介，用于存储版本号和版本匹配后的权值
   * 因为要求版本匹配是有顺序的
   * 普通的HashMap是没有顺序
   * 所以使用LinkedHashMap存储有顺序的值
   */
  val versionAndWeight: mutable.LinkedHashMap[String, ListBuffer[Int]] = new mutable.LinkedHashMap[String, ListBuffer[Int]]()

  /**
   * 读取配置环境中的所有xml的数量（版本数量）
   * 根据读取的值初始化versionAndWeight
   */
  def mulVersionCheck(): Unit = {
    try {
      val versionArray = ExternalResourcesLoadUtils.loadHowManyVersionXML()
      versionArray.reverse.foreach(versionAndWeight.put(_, ListBuffer(0, 0, 0, 0)))
    }
    catch {
      case t: Throwable => t.printStackTrace()
    }

  }


  /**
   * 该函数在MainPage中被调用
   */
  def action(): Unit = {
    val logOutput = new FileOutputStream(logFile)
    val outputHelper = OutputHelper(kc.output, logOutput, kc)



    //初始化versionAndWeight
    mulVersionCheck()
    var current = 0
    println("\nProgress...")
    versionAndWeight.foreach(kv => {
      TDHEnvCheck(outputHelper, kv._1, kv._2, versionAndWeight.size, current)
      current += 1
    })
    print("\r100%\n\n")


    //输出权值
    //println("Each versionXML's weight:\n[VersionXML,(notLinkJarsLessThanStandard,notLinkJarsMoreThanStandard,linkJarsLessThanStandard,linkJarsMoreThanStandard)]:")
    matchVersion(outputHelper, versionAndWeight)

    val logPath = ExternalResourcesLoadUtils.loadTDHLogXML()
    //    logPath.foreach(println)
    logPath.foreach(kv => ZipCompressingUtils("log/" + kc.workingDir.getName + "/" + kv.split("\\:")(0).substring(9) + ".zip",
      new File(kv.split("\\:")(0)), kv.split("\\:")(1)).zip())
    //压缩文件
    //    ZipCompressingUtils("log/" + kc.workingDir.getName + "/" + kc.workingDir.getName + ".zip",
    //      new File("/Users/gesuheng/WorkSpace/scalaWorkSpace/zookeeper.log.1")).zip()
  }

  def matchVersion(outputHelper: OutputHelper, versionAndWeight: mutable.LinkedHashMap[String, ListBuffer[Int]]): Unit = {
    var nameOfJars = ListBuffer[Int]()
    var max = 0
    var versionXML = ""
    for (kv <- versionAndWeight) {
      val weight = calWeight(kv._2)
      if (weight > max) {
        max = weight
        versionXML = kv._1
        nameOfJars = kv._2
      }
    }

    outputHelper println ("TDH Environment Check Result:\n\n[1]The most match versionXML is " + versionXML + " \n\tHere are the information:")
    outputHelper println ("\t1.notLinkJarsLessThanStandard: " + nameOfJars(0))
    outputHelper println ("\t2.notLinkJarsMoreThanStandard: " + nameOfJars(1))
    outputHelper println ("\t3.linkJarsLessThanStandard: " + nameOfJars(2))
    outputHelper println ("\t4.linkJarsMoreThanStandard: " + nameOfJars(3) + "\n\n[2]The Services:")
    if (unInstalledService.size != 0) {
      for (value <- unInstalledService) {
        outputHelper.println("\t" + value)
      }
    } else {
      outputHelper.println("\tAll the Service installed")
    }
  }

  /**
   * 计算权值
   */
  def calWeight(nums: ListBuffer[Int]) = {
    nums(0) * 1 + nums(1) * 2 + nums(2) * 1 + nums(3) * 1
  }


  /**
   * 传入versionWeight每一项对每一项进行检查
   * 设置权值ListBuffer
   * eg.(version4_4.xml,ListBuffer(0, 0, 0, 0))
   */
  def TDHEnvCheck(outputHelper: OutputHelper, versionNo: String, weight: ListBuffer[Int], size: Int, current: Int) = {
    //    outputHelper.println("1.Checking versionXML of " + versionNo)
    print("\r" + (current * 1.0 / size) * 100 + "%")
    checkedService.foreach(service => mainEnviromentCheck(outputHelper, service, versionNo, weight))
  }

  def mainEnviromentCheck(outputHelper: OutputHelper, service: Service, versionNo: String, weight: ListBuffer[Int]) = {
    var isDirectoryFine = true

    //链接的Jars少于TDH标准
    val linkJarsLessThanStandard = new mutable.HashSet[LinkJarProperty]()
    //链接的Jars多于TDH标准
    val linkJarsMoreThanStandard = new mutable.HashSet[LinkJarProperty]()
    //非链接的Jars少于TDH标准
    val notLinkJarsLessThanStandard = new mutable.HashSet[NotLinkJarProperty]()
    //非链接的Jars多于TDH标准
    val notLinkJarsMoreThanStandard = new mutable.HashSet[NotLinkJarProperty]()

    //被扫描环境缺少的目录
    val lackedDirectories = new mutable.HashSet[String]()

    //获取要检查的service要扫描的路径
    val directories: ListBuffer[String] = service.getDirectories

    //    outputHelper.println(("\t" * 1) + "2.Checking service of " + service.getServiceName)

    def checkForEachFile(filename: String): Unit = {
      //传入需要检查的目录,对该目录进行检查
      //      outputHelper.println(("\t" * 3) + "4.Checking dir: " + filename)
      //获取该检查目录中的所有jar文件
      val currentJars = getJars(filename)

      val currentNotLinkJars = currentJars._1 //没有链接的Jar
      val currentLinkJars = currentJars._2 //链接的Jar

      val standardJars = EnvLoad(versionNo) getFileToJarsMap (filename) //标准TDH Jar文件

      val standardNotLinkJars = standardJars._1 //标准TDH 非链接Jar文件
      val standardLinkJars = standardJars._2 //标准TDH 链接Jar文件

      notLinkJarsLessThanStandard ++= (standardNotLinkJars -- currentNotLinkJars)
      notLinkJarsMoreThanStandard ++= (currentNotLinkJars -- standardNotLinkJars)
      linkJarsLessThanStandard ++= (standardLinkJars -- currentLinkJars)
      linkJarsMoreThanStandard ++= (currentLinkJars -- standardLinkJars)

    }


    def getJars(filename: String) = {
      val notLinkFile = new mutable.HashSet[NotLinkJarProperty]()
      val linkFile = new mutable.HashSet[LinkJarProperty]()

      def auxFun(file: String): Unit = {
        //调用util工具执行shell语句
        val ls_res: (String, Int) = CommandExecutor.executeShellWithArguments("ls -l ", file)
        if (ls_res._2 == 0) {
          //执行File命令,检测检测文件类型
          val file_res: (String, Int) = CommandExecutor.executeShellWithArguments("file ", file)
          val splits: Array[String] = ls_res._1.split("\\s+")
          val perm: (String, String, String) = getPerm(splits(0))
          //根据是否为链接文件进行分类处理
          if (file_res._2 == 0 && file_res._1.contains("symbolic link")) {
            linkFile += LinkJarProperty(BaseProperty(Permission(perm._1, perm._2, perm._3),
              splits(2), splits(3)), splits(splits.length - 3), splits(splits.length - 1))
          }
          else if (file_res._2 == 0 && (!file_res._1.contains("symbolic link"))) {
            notLinkFile += NotLinkJarProperty(BaseProperty(Permission(perm._1, perm._2, perm._3),
              splits(2), splits(3)), splits(splits.length - 1))
          }
        }
      }

      val files = getAllFile(filename)
      files.filter(_.endsWith(".jar")).foreach(auxFun)
      //      outputHelper.println(("\t" * 4) + "5.All the jar files of : " + filename)
      //      outputHelper.println(("\t" * 6) + "not link file")
      //      notLinkFile.foreach(kv => outputHelper.println(("\t" * 7) + kv))
      //      outputHelper.println(("\t" * 6) + "link file")
      //      linkFile.foreach(kv => outputHelper.println(("\t" * 7) + kv))
      (notLinkFile.toSet, linkFile.toSet)
    }

    def getAllFile(path: String) = {
      val allfiles: mutable.HashSet[String] = new mutable.HashSet[String]()
      def getfile(path: String): Unit = {
        val file = new File(path)
        val listFiles: Array[File] = file.listFiles()
        if (listFiles != null) {
          listFiles.foreach(file => {
            file.isDirectory match {
              case true => getfile(file.toString)
              case false => allfiles += file.toString
            }
          })
        }
      }
      getfile(path)
      allfiles.toSet
    }

    def getPerm(str: String) = {
      //a 3 tuple (user permission ,group permission, other permission)
      (str.substring(1, 4), str.substring(4, 7), str.substring(7, 10))
    }

    //依次获取目录
    for (dir <- directories) {
      val file = new File(dir)
      if (file.exists()) {
        //        outputHelper.println(("\t" * 2) + "3.The dir exsist")
        //对每一项进行检查
        checkForEachFile(dir)
      } else {
        //        outputHelper.println(("\t" * 2) + "3.The dir doesn't exsist ")
        //将所需要环境不存在扫描环境的目录加入到lackDir中
        lackedDirectories.add(dir)
        if (isDirectoryFine)
          isDirectoryFine = false
      }
    }
    var isJarsConsisitent = true
    if (notLinkJarsLessThanStandard.size != 0 || notLinkJarsMoreThanStandard.size != 0 ||
      linkJarsLessThanStandard.size != 0 || linkJarsMoreThanStandard.size != 0) {
      //      println("notLinkJarsLessThanStandard: " + notLinkJarsLessThanStandard.size)
      //      println("notLinkJarsMoreThanStandard: " + notLinkJarsMoreThanStandard.size)
      //      println("linkJarsLessThanStandard: " + linkJarsLessThanStandard.size)
      //      println("linkJarsMoreThanStandard: " + linkJarsMoreThanStandard.size)
      weight(0) += notLinkJarsLessThanStandard.size
      weight(1) += notLinkJarsMoreThanStandard.size
      weight(2) += linkJarsLessThanStandard.size
      weight(3) += linkJarsMoreThanStandard.size
      isJarsConsisitent = false
    }
    if (!isDirectoryFine && lackedDirectories.size == 1 && directories.length == 1) {
      unInstalledService.add("The Service: " + service.toString +
        " is not installed in Current Environment!")
    }
    unInstalledService
  }
}


object TDHEnv {
  def apply(kc: KoalasContext) = {
    new TDHEnv(kc)
  }

  def main(args: Array[String]) {
    val versionArray = ExternalResourcesLoadUtils.loadHowManyVersionXML()
    versionArray.foreach(println)
  }
}
