package io.transwarp.maintenance.koalas

import io.transwarp.maintenance.koalas.page.MainPage

/**
 * Created by Suheng on 7/18/15.
 */
object Koalas {
  def main(args: Array[String]) {
    val kc = new KoalasContext
    new MainPage(kc)

  }
}
