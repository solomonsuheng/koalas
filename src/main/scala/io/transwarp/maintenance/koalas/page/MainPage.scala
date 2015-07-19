package io.transwarp.maintenance.koalas.page

import io.transwarp.maintenance.koalas.KoalasContext
import io.transwarp.maintenance.koalas.layout.SequenceLayout
import io.transwarp.maintenance.koalas.widgets.{OptionDialog, Window}
import io.transwarp.maintenance.koalas.worker.system.SystemEnv

/**
 * Created by Suheng on 7/18/15.
 */
class MainPage(kc: KoalasContext) {
  //存放组建的layout
  val layout = new SequenceLayout(kc) //kc里面存放着对于向用户交互模式输出的I/O


  //操作选项和操作对应的执行函数
  val operationMap = Seq[(String, (KoalasContext) => Unit)](
    ("System Environment Check", (kc: KoalasContext) => SystemEnv(kc).action())
  )

  //创建欢迎交互页面的windows
  val welcomeWindow = new Window(title = "Transwarp Maintenance Tool Suite")
  //添加欢迎界面
  layout.addItem(welcomeWindow)

  SystemEnv(kc).action()

  //选项目录交互界面
  val menu = new OptionDialog(id = "koalas.menu", title = "Menu")
  //添加菜单交互界面
  layout.addItem(menu)

  //调用layout中的每一个action函数，产生Item效果
  layout.action(kc)


}
