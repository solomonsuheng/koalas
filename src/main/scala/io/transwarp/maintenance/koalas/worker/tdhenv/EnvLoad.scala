package io.transwarp.maintenance.koalas.worker.tdhenv

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

class EnvLoad(versionName: String) {

  def getFileToJarsMap = {
    val hadoop_LinkJars = dispatcherLink("hadoop")
    val hadoop_NotLinkJars = dispatcherNotLink("hadoop")
    val hadoop_Hdfs_LinkJars = dispatcherLink("hadoop-hdfs")
    val hadoop_Hdfs_NotLinkJars = dispatcherNotLink("hadoop-hdfs")
    val hadoop_Mapreduce_LinkJars = dispatcherLink("hadoop-mapreduce")
    val hadoop_Mapreduce_NotLinkJars = dispatcherNotLink("hadoop-mapreduce")
    val hadoop_Yarn_LinkJars = dispatcherLink("hadoop-yarn")
    val hadoop_Yarn_NotLinkJars = dispatcherNotLink("hadoop-yarn")
    val hbase_LinkJars = dispatcherLink("hbase")
    val hbase_NotLinkJars = dispatcherNotLink("hbase")
    val hive_LinkJars = dispatcherLink("hive")
    val hive_NotLinkJars = dispatcherNotLink("hive")
    val ngmr_NotLinkJars = dispatcherNotLink("ngmr")
    val ngmr_LinkJars = dispatcherLink("ngmr")
    val ngmr_Shell_NotLinkJars = dispatcherNotLink("ngmr-shell")
    val ngmr_Shell_LinkJars = dispatcherLink("ngmr-shell")
    val scala_NotLinkJars = dispatcherNotLink("scala")
    val scala_LinkJars = dispatcherLink("scala")
    val elasticsearch_NotLinkJars = dispatcherNotLink("elasticsearch")
    val elasticsearch_LinkJars = dispatcherLink("elasticsearch")
    val transwarp_jobserver_NotLinkJars = dispatcherNotLink("transwarp-jobserver")
    val transwarp_jobserver_LinkJars = dispatcherLink("transwarp-jobserver")
    val pig_NotLinkJars = dispatcherNotLink("pig")
    val pig_LinkJars = dispatcherLink("pig")
    val sqoop_NotLinkJars = dispatcherNotLink("sqoop")
    val sqoop_LinkJars = dispatcherLink("sqoop")
    val zookeeper_NotLinkJars = dispatcherNotLink("zookeeper")
    val zookeeper_LinkJars = dispatcherLink("zookeeper")
    val oozie_NotLinkJars = dispatcherNotLink("oozie")
    val oozie_LinkJars = dispatcherLink("oozie")
    val flume_NotLinkJars = dispatcherNotLink("flume")
    val flume_LinkJars = dispatcherLink("flume")
    val kafka_NotLinkJars = dispatcherNotLink("kafka")
    val kafka_LinkJars = dispatcherLink("kafka")
    val sparkR_NotLinkJars = dispatcherNotLink("sparkR")
    val sparkR_LinkJars = dispatcherLink("sparkR")
    val mahout_NotLinkJars = dispatcherNotLink("mahout")
    val mahout_LinkJars = dispatcherLink("mahout")
    val bigtop_tomcat_NotLinkJars = dispatcherNotLink("bigtop-tomcat")
    val bigtop_tomcat_LinkJars = dispatcherLink("bigtop-tomcat")
    val bigtop_util_NotLinkJars = dispatcherNotLink("bigtop-util")
    val bigtop_util_LinkJars = dispatcherLink("bigtop-util")

    val returnValue = immutable.HashMap[String, (immutable.Set[NotLinkJarProperty], immutable.Set[LinkJarProperty])](
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
    returnValue
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

    val ss = standardLib._2.getOrElse(kind, null)

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

    val hashMap = ExternalResourcesLoadUtils.loadTDHVersion(versionName)
    //    hashMap.foreach(kv=>kv._2.foreach(println))
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

}

object EnvLoad {
  def apply(versionName: String): EnvLoad = {
    new EnvLoad(versionName)
  }
}

