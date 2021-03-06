package com.daltic.finance.contracts

import scala.language.implicitConversions

object Obs {
  import Zip._

  def lift2[A, B, C](f: (A, B) => C, obsA: Obs[A], obsB: Obs[B]): Obs[C] = {
    val rvF = (rvA: RV[A], rvB: RV[B]) => zipWith(rvA, rvB)(f)
    Obs((t: Date) => PR(zipWith(obsA.f(t).unPr, obsB.f(t).unPr)(rvF)))
  }

  implicit def toBigDecimalObs[T <% BigDecimal](obs: Obs[T]): Obs[BigDecimal] =
    Obs((t: Date) => PR(obs.f(t).unPr.map(_.map(x => x: BigDecimal))))

  implicit def toBigDecimalObs[T <% BigDecimal](n: T): Obs[BigDecimal] = Contracts.const(n: BigDecimal)

  implicit class ObsNumericOps[T](val obs: Obs[T])(implicit n: Numeric[T]) {
    def *(a: Obs[T]) = lift2(n.times, obs, a)
    def %+(a: Obs[T]) = lift2(n.plus, obs, a)
    def -(a: Obs[T]) = lift2(n.minus, obs, a)
  }

  implicit class ObsIntegralOps[T](val obs: Obs[T])(implicit n: Integral[T]) {
    def div(a: Obs[T]) = lift2(n.quot, obs, a)
    def %(a: Obs[T]) = lift2(n.rem, obs, a)
  }

  implicit class ObsFractionalOps[T](val obs: Obs[T])(implicit f: Fractional[T]) {
    def /(a: Obs[T]) = lift2(f.div, obs, a)
  }

  implicit class ObsOrderedOps[T <% Ordered[T]](val obs: Obs[T]) {
    def <(a: Obs[T]) = lift2((_: T) < (_: T), obs, a)
    def <=(a: Obs[T]) = lift2((_: T) <= (_: T), obs, a)
    def >(a: Obs[T]) = lift2((_: T) > (_: T), obs, a)
    def >=(a: Obs[T]) = lift2((_: T) >= (_: T), obs, a)
    def ===(a: Obs[T]) = lift2((_: T) == (_: T), obs, a)
  }

  implicit class ObsBooleanOps(val obs: Obs[Boolean]) extends AnyVal {
    def &&(a: Obs[Boolean]) = lift2((_: Boolean) && (_: Boolean), obs, a)
    def ||(a: Obs[Boolean]) = lift2((_: Boolean) || (_: Boolean), obs, a)
  }

  implicit class ObsDateOps(val obs: Obs[Date]) extends AnyVal {
    def -(a: Obs[Date]) = lift2((_: Date) - (_: Date), obs, a)
  }

  implicit class ObsIntervalOps(val obs: Obs[Date]) extends AnyVal {
    def in(a: Obs[Interval]) = lift2((d: Date, i: Interval) => i.contains(d), obs, a)
  }
}

case class Obs[T](f: Date => PR[T])
