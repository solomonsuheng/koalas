package io.transwarp.maintenance.koalas

import io.transwarp.maintenance.koalas.page.MainPage

/**
 * Created by Suheng on 7/18/15.
 */

object Koalas {
  def main(args: Array[String]) {
        val kc = new KoalasContext //初始化设备上下文
        new MainPage(kc) //Go it!!!
  }
}
