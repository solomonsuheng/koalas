package io.transwarp.maintenance.koalas.widgets

import io.transwarp.maintenance.koalas.KoalasContext

/**
 * Created by Suheng on 7/18/15.
 */

trait Item {
  def action(kc: KoalasContext)
}
