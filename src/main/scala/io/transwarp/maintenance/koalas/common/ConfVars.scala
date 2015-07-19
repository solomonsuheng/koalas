package io.transwarp.maintenance.koalas.common

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap

/**
 * Created by Suheng on 7/18/15.
 */


class ConfVars {
  val prop: HashMap[String, Any] = new HashMap[String, Any]() //存储属性

  /**
   * System.getenv()是javaHashMap类型，调用collection.JavaConversions进行转换成Scala类型
   */
  System.getenv().foreach(kv => {
    if (kv._1.startsWith("koalas")) {
      //如果环境变量中存在以koalas开头的变量，存入到ConfVars prop中
      this.set(kv._1, kv._2)
    }
  })

  System.getProperties().foreach(kv => {
    //获取系统属性,加载到prop HashMap中
    if (kv._1.startsWith("koalas")) {
      this.set(kv._1, kv._2)
    }
  })

  /**
   * 从HashMap prop获取参数
   */
  def get(key: String, default: Any): String = {
    //从prop HashMap中读取数据,如果不存在用Any进行替换
    this.prop.getOrElse(key, default).toString
  }

  /**
   * 设置prop HashMap中的参数
   */
  def set(key: String, value: String) = {
    this.prop.put(key, value)
  }

  //获取Int值
  def getInt(key: String, default: Int): Int = {
    val value = this.prop.getOrElse(key, default) //如果从中无法获取需要的值用default进行替换
    if (!value.isInstanceOf[Int]) {
      //检查从prop HashMap中获取的类型是否为Int
      value.toString.toInt //如果不是Int类型toString后返回Int类型
    } else {
      value.asInstanceOf[Int] //如果不是强制转换成Int返回
    }
  }

  //设置Int值
  def setInt(key: String, value: Int) = {
    this.prop.put(key, value)
  }

  //根据key获取boolean类型的值
  def getBoolean(key: String, default: Boolean): Boolean = {
    val value = this.prop.getOrElse(key, default) //从HashMap中获取值，如果没有获取到用default代替
    if (!value.isInstanceOf[Boolean]) {
      value.toString.toBoolean
    } else {
      value.asInstanceOf[Boolean] //如果不进行强制类型转换为Boolean,value的值为Any
    }
  }

  /**
   * 设置boolean类型的参数
   */
  def setBoolean(key: String, value: Boolean) = {
    this.prop.put(key, value)
  }
}

object ConfVars {
  val iteractiveMode = "koalas.interactive"
  //交互模式
  val workDir = "koalas.workdir"
  val dataDir = "koalas.datadir"
}

