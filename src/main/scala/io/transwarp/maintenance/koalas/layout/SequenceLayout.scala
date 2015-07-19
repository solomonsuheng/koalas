package io.transwarp.maintenance.koalas.layout

import io.transwarp.maintenance.koalas.KoalasContext
import io.transwarp.maintenance.koalas.widgets.Item

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Suheng on 7/18/15.
 */
/**
 * layout用于存储cli交互界面的所有组件，如果菜单标题等
 * 所有的组件（菜单，标题等）都是实现了trait的类
 * 所有的组件都被存放于ArrayBuffer中
 * trait有一个action方法，action对应各自组件的输出形式
 */
class SequenceLayout(kc: KoalasContext) {


  //所有的组件都存在于该ArrayBuffer中，菜单标题等
  val items: ArrayBuffer[Item] = new ArrayBuffer[Item]()

  //增加组件
  def addItem(it: Item) = {
    this.items += it
  }

  //移除组件
  def removeItem(idx: Int): Unit = {
    this.items.remove(idx)
  }

  //存放组件的layout，通过调用存储组建的ArrayBuffer中的每一个action来调用每一个组件的action
  def action(kc: KoalasContext): Unit = {
    this.items.foreach(_.action(kc))
  }
}
