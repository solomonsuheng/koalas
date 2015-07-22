package io.transwarp.maintenance.koalas.page

import java.io.File

import io.transwarp.maintenance.koalas.KoalasContext
import io.transwarp.maintenance.koalas.layout.SequenceLayout
import io.transwarp.maintenance.koalas.widgets.{OptionDialog, Window}
import io.transwarp.maintenance.koalas.worker.system.SystemEnv
import io.transwarp.maintenance.koalas.worker.tdhenv.TDHEnv

/**
 * Created by Suheng on 7/18/15.
 */
class MainPage(kc: KoalasContext) {
  //操作选项和操作对应的执行函数
  val operationMap = Seq[(String, (KoalasContext) => Unit)](
    //系统环境检查
    ("System Environment Check", (kc: KoalasContext) => SystemEnv(kc).action()),
    //TDH环境检查
    ("TDH Environment Check", (kc: KoalasContext) => TDHEnv(kc).action())
  ).toMap //seq可以通过.toMap函数获取到对应的Map


  val layout = new SequenceLayout(kc) //kc里面存放着对于向用户交互模式输出的I/O


  /**
   * cli UI交互界面 interactiveMode模式，如果是交互模式才进行输出标题
   */
  //存放组建的layout
  //创建欢迎交互页面的windows
  if (kc.interactiveMode) {
    val welcomeWindow = new Window(title = "Transwarp Maintenance Tool Suite")
    //添加欢迎界面组件
    layout.addItem(welcomeWindow)
  }


  //选项目录交互界面组件
  /**
   * 如果是非交互模式必须在conf中设置koalas.menu参数
   */
  val menu = new OptionDialog(id = "koalas.menu",
    title = "Menu",
    options = operationMap.map(_._1).toSeq,
    callback = (ans: String, kc: KoalasContext) => {
      operationMap(ans)(kc)
    })

  //添加组件
  layout.addItem(menu)

  //调用layout中的每一个action函数，产生Item效果
  layout.action(kc)


}
