package com.pigeonnest.domain.usecase.base

abstract class NoParamUseCase<out R> {
    abstract suspend operator fun invoke(): R
}
