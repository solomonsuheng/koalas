package io.transwarp.maintenance.koalas.widgets

import io.transwarp.maintenance.koalas.KoalasContext

/**
 * Created by Suheng on 7/18/15.
 */
class OptionDialog(val id: String,
                   val title: String,
                   val vertical: Boolean = true,
                   val options: Seq[String],
                   val default: String = null,
                   val callback: (String, KoalasContext) => Unit) extends Item {

  /**
   * default为默认选项
   * 如果函数传递进来的default没有在options选项中
   * 则不能认为是默认
   */
  assert(default == null || options.contains(default))

  override def action(kc: KoalasContext): Unit = {
    val ps = kc.output

    //从交互界面获取用户的输入结果传给ans
    val ans = if (kc.interactiveMode) {
      ps.println(title)
      var ans: String = null //临时变量作为返回结果给调用
      while (ans == null) {
        if (!vertical) {
          //eg.Menu (0.System Environment Check, 1.TDH Environment Check, 2.TDH Update Server, 3.TDH Update Client)
          if (default != null) {
            ps.print("%s (%s,default:%s) ".format(title, options.zipWithIndex.map(kv => kv._2 + "." + kv._1).mkString(","), default))
          } else {
            //default没有设定为空
            ps.print("%s (%s)".format(title, options.zipWithIndex.map(kv => kv._2 + "." + kv._1).mkString(",")))
          }
        } else {
          options.zipWithIndex.map(kv => kv._2 + "." + kv._1).foreach(s => {
            ps.println(s)
          })

          //查看是否给定了默认值
          if (default != null) {
            ps.print("Your choice is (Default:%s".format(default))
          } else {
            ps.print("Your choice is: ")
          }
        }

        val input = kc.input.readLine().trim //获取用户输入，并且去除空字符

        try {
          val idx = input.toInt
          if (idx >= 0 && idx < options.length) {
            ans = options(idx) //获取option中的选项内容
          } else {
            ans = null
          }
        } catch {
          case _: Throwable =>
        }


        if (ans == null && default != null) {
          //如果获取用户输入没有获取到，同时默认值存在，自动设置用户输入为默认值
          ans = default
        }
      }
      kc.conf.set(id, ans) //用户输入，和选项
      ans //返回用户输入
    } else {
      //非交互模式，从设置的kc.conf中获取数据
      val ans = kc.conf.get(id, default)
      
      if (ans == null) {
        //没有选择选项
        throw new IllegalArgumentException("Missing value for option %s".format(id))
      }

      /**
       * 用来处理非交互模式，在设置的时候可能存在不正确的设置
       */
      if (!options.contains(ans)) {
        //选择的内容并非存在于选项中则不能执行函数
        throw new IllegalArgumentException("%s is not is option list %s".format(ans, options.mkString(",")))
      }
      ans
    }

    callback(ans, kc) //调用对应选项的函数
  }

}
