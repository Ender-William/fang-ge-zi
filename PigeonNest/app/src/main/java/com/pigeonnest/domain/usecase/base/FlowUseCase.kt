package com.pigeonnest.domain.usecase.base

import kotlinx.coroutines.flow.Flow

abstract class FlowUseCase<in P, out R> {
    abstract operator fun invoke(params: P): Flow<R>
}
