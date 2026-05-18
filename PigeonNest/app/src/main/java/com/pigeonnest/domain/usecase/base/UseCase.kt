package com.pigeonnest.domain.usecase.base

abstract class UseCase<in P, out R> {
    abstract suspend operator fun invoke(params: P): R
}
