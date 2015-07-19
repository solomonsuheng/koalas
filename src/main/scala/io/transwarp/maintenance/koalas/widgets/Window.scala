package io.transwarp.maintenance.koalas.widgets

import java.io.PrintStream

import io.transwarp.maintenance.koalas.KoalasContext

/**
 * Created by Suheng on 7/18/15.
 */
class Window(val width: Int = 30,
             val sectionHeight: Int = 1,
             val title: String = "",
             val msg: Seq[String] = Seq()) extends Item {


  //进行装饰
  private def newSection(ps: PrintStream): Unit = {
    var i = 0
    while (i < sectionHeight) {
      ps.println("")
      i += 1
    }
  }

  /**
   * Window组件会被加入到SequenceLayout的ArrayBuffer中
   * Sequence会根据action调用每一个ArrayBuffer中的Item的action
   */
  override def action(kc: KoalasContext): Unit = {
    val ps = kc.output //ps = System.out
    ps.println(title)
    newSection(ps)
    msg.foreach(msg => {
      ps.println(msg)
      newSection(ps)
    })
  }
}
