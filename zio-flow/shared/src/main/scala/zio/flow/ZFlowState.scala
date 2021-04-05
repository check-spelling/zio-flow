package zio.flow

sealed trait ZFlowState[+A] { self =>
  final def flatMap[B](f: Remote[A] => ZFlowState[B]): ZFlowState[B] =
    ZFlowState.FlatMap(self, f)

  final def map[B](f: Remote[A] => Remote[B]): ZFlowState[B] =
    ZFlowState.Map(self, f)

  final def zip[B](that: ZFlowState[B]): ZFlowState[(A, B)] =
    self.flatMap(a => that.map(b => a -> b))
}

object ZFlowState {
  // TODO: Change `value` to be an `A` and the `Schema[A]`.
  final case class Return[A: Schema](value: A)                                        extends ZFlowState[A]
  final case class NewVar[A](name: String, defaultValue: Remote[A])                   extends ZFlowState[Variable[A]]
  final case class FlatMap[A, B](value: ZFlowState[A], k: Remote[A] => ZFlowState[B]) extends ZFlowState[B]
  final case class Map[A, B](value: ZFlowState[A], f: Remote[A] => Remote[B])         extends ZFlowState[B]

  def apply[A: Schema](a: A): ZFlowState[A] = Return(a)

  def newVar[A](name: String, value: Remote[A]): ZFlowState[Variable[A]] = NewVar(name, value)
}