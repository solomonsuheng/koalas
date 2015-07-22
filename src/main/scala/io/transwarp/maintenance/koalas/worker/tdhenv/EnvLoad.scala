package io.transwarp.maintenance.koalas.worker.tdhenv

import java.io.{BufferedReader, InputStream, InputStreamReader}

import io.transwarp.maintenance.koalas.utils.ExternalResourcesLoadUtils

import scala.collection.mutable.ListBuffer
import scala.collection.{immutable, mutable}

/**
 * Created by Suheng on 7/20/15.
 */

/**
 * 自定义的类型用于匹配如下结构
 * -rw------- 1 root root 20823 Jul 19 06:33 .bash_history
 * -rw-r--r-- 1 root root   101 Jul 19 02:54 .bash_profie
 */
//Unix中的权限,userPerm文件所有者权限,groupPerm文件所属组权限,otherPerm其他用户对于文件的权限
case class Permission(userPerm: String, groupPerm: String, otherPerm: String) {
  override def toString: String = "[" + userPerm + "," + groupPerm + "," + otherPerm + "]"
}

// 用户权限,用户,组
case class BaseProperty(perm: Permission, user: String, group: String) {
  override def toString: String = perm.toString + " user=[" + user + "] group=[" + group + "]"
}


//不带链接的文件
case class NotLinkJarProperty(base: BaseProperty, file: String) {
  override def toString: String = base.toString + " file=[" + file + "]"
}

//带链接的文件
case class LinkJarProperty(base: BaseProperty, srcFile: String, destFile: String) {
  override def toString: String = base.toString + " srcFile=[" + srcFile + "] destFile=[" + destFile + "]"
}


object EnvLoad {
  val versionNo = Array("ASD") //设置版本号

  /*  Carefully: Here can add other properties which should be checked! */

  lazy val hadoop_LinkJars = dispatcherLink("hadoop")
  lazy val hadoop_NotLinkJars = dispatcherNotLink("hadoop")
  lazy val hadoop_Hdfs_LinkJars = dispatcherLink("hadoop-hdfs")
  lazy val hadoop_Hdfs_NotLinkJars = dispatcherNotLink("hadoop-hdfs")
  lazy val hadoop_Mapreduce_LinkJars = dispatcherLink("hadoop-mapreduce")
  lazy val hadoop_Mapreduce_NotLinkJars = dispatcherNotLink("hadoop-mapreduce")
  lazy val hadoop_Yarn_LinkJars = dispatcherLink("hadoop-yarn")
  lazy val hadoop_Yarn_NotLinkJars = dispatcherNotLink("hadoop-yarn")
  lazy val hbase_LinkJars = dispatcherLink("hbase")
  lazy val hbase_NotLinkJars = dispatcherNotLink("hbase")
  lazy val hive_LinkJars = dispatcherLink("hive")
  lazy val hive_NotLinkJars = dispatcherNotLink("hive")
  lazy val ngmr_NotLinkJars = dispatcherNotLink("ngmr")
  lazy val ngmr_LinkJars = dispatcherLink("ngmr")
  lazy val ngmr_Shell_NotLinkJars = dispatcherNotLink("ngmr-shell")
  lazy val ngmr_Shell_LinkJars = dispatcherLink("ngmr-shell")
  lazy val scala_NotLinkJars = dispatcherNotLink("scala")
  lazy val scala_LinkJars = dispatcherLink("scala")
  lazy val elasticsearch_NotLinkJars = dispatcherNotLink("elasticsearch")
  lazy val elasticsearch_LinkJars = dispatcherLink("elasticsearch")
  lazy val transwarp_jobserver_NotLinkJars = dispatcherNotLink("transwarp-jobserver")
  lazy val transwarp_jobserver_LinkJars = dispatcherLink("transwarp-jobserver")
  lazy val pig_NotLinkJars = dispatcherNotLink("pig")
  lazy val pig_LinkJars = dispatcherLink("pig")
  lazy val sqoop_NotLinkJars = dispatcherNotLink("sqoop")
  lazy val sqoop_LinkJars = dispatcherLink("sqoop")
  lazy val zookeeper_NotLinkJars = dispatcherNotLink("zookeeper")
  lazy val zookeeper_LinkJars = dispatcherLink("zookeeper")
  lazy val oozie_NotLinkJars = dispatcherNotLink("oozie")
  lazy val oozie_LinkJars = dispatcherLink("oozie")
  lazy val flume_NotLinkJars = dispatcherNotLink("flume")
  lazy val flume_LinkJars = dispatcherLink("flume")
  lazy val kafka_NotLinkJars = dispatcherNotLink("kafka")
  lazy val kafka_LinkJars = dispatcherLink("kafka")
  lazy val sparkR_NotLinkJars = dispatcherNotLink("sparkR")
  lazy val sparkR_LinkJars = dispatcherLink("sparkR")
  lazy val mahout_NotLinkJars = dispatcherNotLink("mahout")
  lazy val mahout_LinkJars = dispatcherLink("mahout")
  lazy val bigtop_tomcat_NotLinkJars = dispatcherNotLink("bigtop-tomcat")
  lazy val bigtop_tomcat_LinkJars = dispatcherLink("bigtop-tomcat")
  lazy val bigtop_util_NotLinkJars = dispatcherNotLink("bigtop-util")
  lazy val bigtop_util_LinkJars = dispatcherLink("bigtop-util")


  //读取非链接Jar
  def loadNotLinkJars(path: String) = {
    val res = new mutable.HashSet[NotLinkJarProperty]()
    try {
      val asStream: InputStream = this.getClass.getClassLoader.getResourceAsStream(path)
      val reader: BufferedReader = new BufferedReader(new InputStreamReader(asStream))
      var continue: Boolean = true
      while (continue) {
        val line = reader.readLine()
        val option: Option[String] = Option(line)
        option match {
          case Some(x) => {
            val splits: Array[String] = x.split(" ")
            val permission = Permission(splits(0), splits(1), splits(2))
            res += NotLinkJarProperty(BaseProperty(permission, splits(3), splits(4)), splits(5))
          }
          case _ => continue = false
        }
      }
    } catch {
      case e: Exception => new RuntimeException("Can't read file successfully!!! ")
    }
    res.toSet
  }

  //读取链接Jar
  def loadLinkJars(path: String) = {
    val res = new mutable.HashSet[LinkJarProperty]()
    try {
      val asStream: InputStream = this.getClass.getClassLoader.getResourceAsStream(path)
      val reader: BufferedReader = new BufferedReader(new InputStreamReader(asStream))
      var continue: Boolean = true
      while (continue) {
        val line = reader.readLine()
        val option: Option[String] = Option(line)
        option match {
          case Some(x) => {
            val splits: Array[String] = x.split(" ")
            val permission = Permission(splits(0), splits(1), splits(2))
            res += LinkJarProperty(BaseProperty(permission, splits(3), splits(4)), splits(5), splits(6))
          }
          case _ => continue = false
        }
      }
    } catch {
      case e: Exception => new RuntimeException("Can't read file successfully!!! ")
    }
    res.toSet
  }


  /**
   * kind为具体的组件名字(如hadoop,zookeeper)，该方法是导入带link的文件
   */
  def dispatcherLink(kind: String) = {
    val res = new mutable.HashSet[LinkJarProperty]()
    val standardLib = loadStandardLib()
    val ss = standardLib._1.getOrElse(kind, null)

    if (ss != null) {
      val s = ss.toSet
      for (v <- s) {
        val splits: Array[String] = v.split(" ")
        val permission = Permission(splits(0), splits(1), splits(2))
        res += LinkJarProperty(BaseProperty(permission, splits(3), splits(4)), splits(5), splits(6))
      }
    }
    res.toSet

  }

  /**
   * kind为具体的组件名字(如hadoop,zookeeper)，该方法是导入不带link的文件
   */
  def dispatcherNotLink(kind: String) = {
    val res = new mutable.HashSet[NotLinkJarProperty]()
    val standardLib = loadStandardLib()
    val ss = standardLib._1.getOrElse(kind, null)
    if (ss != null) {
      val s = ss.toSet
      for (v <- s) {
        val splits: Array[String] = v.split(" ")
        val permission = Permission(splits(0), splits(1), splits(2))
        res += NotLinkJarProperty(BaseProperty(permission, splits(3), splits(4)), splits(5))
      }
    }
    res.toSet
  }


  /**
   * 从文件中获取TDH标准的lib放入到Map中进行返回
   */
  def loadStandardLib() = {

    var tDHStandardLibLoadMapLink = mutable.HashMap[String, ListBuffer[String]]()
    var tDHStandardLibLoadMapNotLink = new mutable.HashMap[String, ListBuffer[String]]()

    val hashMap = ExternalResourcesLoadUtils.loadTDHVersion(versionNo(0))

    /**
     * l的结果如下(String,Array())类型
     * rw- r-- r-- root root /usr/lib/zookeeper/lib/slf4j-log4j12-1.6.1.jar
     * rw- r-- r-- root root /usr/lib/zookeeper/lib/jline-0.9.94.jar
     * (Zookeeper,())
     * rw- r-- r-- root root /usr/lib/hadoop-yarn/lib/protobuf-java-2.5.0.jar
     * rw- r-- r-- root root /usr/lib/hadoop-yarn/lib/commons-cli-1.2.jar
     * (Hadoop-yarn,())
     */
     // hashMap.foreach(kv => println(kv._1, kv._2.foreach(println)))


    hashMap.foreach(kv => kv._2.foreach(load(_)))

    def load(option: String) = {
      val link: (String, String) = getKindAndLink(option)
      if (link._2.equals("notLink")) {
        val ll = tDHStandardLibLoadMapNotLink.getOrElse(link._1, ListBuffer())
        ll += option
        tDHStandardLibLoadMapNotLink.put(link._1, ll)
      } else if (link._2.equals("link")) {
        val ll = tDHStandardLibLoadMapLink.getOrElse(link._1, ListBuffer())
        ll += option
        tDHStandardLibLoadMapLink.put(link._1, ll)
      }
    }
    (tDHStandardLibLoadMapLink, tDHStandardLibLoadMapNotLink)
  }


  /**
   * 从文件中读取的每一条line传入该方法，根据link还是非link类型进行返回
   */
  def getKindAndLink(line: String) = {
    val splits: Array[String] = line.split("\\s+")
    val kind: String = splits(5).split("\\/")(3)
    if (splits.length == 7) {
      //是链接类型
      (kind, "link")
    } else {
      //非链接类型
      (kind, "notLink")
    }

  }


  lazy val FileToJarsMap =
    immutable.HashMap[String, (immutable.Set[NotLinkJarProperty], immutable.Set[LinkJarProperty])](
      ("/usr/lib/hadoop", (hadoop_NotLinkJars, hadoop_LinkJars)),
      ("/usr/lib/hadoop-hdfs", (hadoop_Hdfs_NotLinkJars, hadoop_Hdfs_LinkJars)),
      ("/usr/lib/hadoop-mapreduce", (hadoop_Mapreduce_NotLinkJars, hadoop_Mapreduce_LinkJars)),
      ("/usr/lib/hadoop-yarn", (hadoop_Yarn_NotLinkJars, hadoop_Yarn_LinkJars)),
      ("/usr/lib/hive", (hive_NotLinkJars, hive_LinkJars)),
      ("/usr/lib/hbase", (hbase_NotLinkJars, hbase_LinkJars)),
      ("/usr/lib/ngmr", (ngmr_NotLinkJars, ngmr_LinkJars)),
      ("/usr/lib/ngmr-shell", (ngmr_Shell_NotLinkJars, ngmr_Shell_LinkJars)),
      ("/usr/lib/scala", (scala_NotLinkJars, scala_LinkJars)),
      ("/usr/lib/elasticsearch", (elasticsearch_NotLinkJars, elasticsearch_LinkJars)),
      ("/usr/lib/transwarp-jobserver", (transwarp_jobserver_NotLinkJars, transwarp_jobserver_LinkJars)),
      ("/usr/lib/pig", (pig_NotLinkJars, pig_LinkJars)),
      ("/usr/lib/sqoop", (sqoop_NotLinkJars, sqoop_LinkJars)),
      ("/usr/lib/zookeeper", (zookeeper_NotLinkJars, zookeeper_LinkJars)),
      ("/usr/lib/oozie", (oozie_NotLinkJars, oozie_LinkJars)),
      ("/usr/lib/flume", (flume_NotLinkJars, flume_LinkJars)),
      ("/usr/lib/kafka", (kafka_NotLinkJars, kafka_LinkJars)),
      ("/usr/lib/sparkR", (sparkR_NotLinkJars, sparkR_LinkJars)),
      ("/usr/lib/mahout", (mahout_NotLinkJars, mahout_LinkJars)),
      ("/usr/lib/bigtop-tomcat", (bigtop_tomcat_NotLinkJars, bigtop_tomcat_LinkJars)),
      ("/usr/lib/bigtop-util", (bigtop_util_NotLinkJars, bigtop_util_LinkJars))
    )


}

