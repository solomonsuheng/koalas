package io.transwarp.maintenance.koalas.widgets

import io.transwarp.maintenance.koalas.KoalasContext

/**
 * Created by Suheng on 7/18/15.
 */
class OptionDialog(val id: String, val title: String, val vertical: Boolean = true, val options: Seq[String] = Seq()
                   , val default: String = null, val callback: (String, KoalasContext) => Unit = null) extends Item {
  assert(default == null || options.contains(default))

  override def action(kc: KoalasContext): Unit = {
    val ps = kc.output

    //从交互界面获取用户的输入结果传给ans
//    val ans = if (kc.interactiveMode) {
    //      ps.print(title)
    //      var ans: String = null //临时变量作为返回结果给调用
    //      while (ans == null) {
    //        if (!vertical) {
    //
    //        }
    //      }
    //
    //    }
  }
}
