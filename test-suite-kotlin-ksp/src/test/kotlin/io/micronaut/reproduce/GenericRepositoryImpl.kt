package io.micronaut.reproduce

import jakarta.inject.Singleton

/**
 * A generic concrete implementation. Leaving this generic (open type parameter) in the bean graph
 * is intended to reproduce candidate selection issues where type variables appear among candidates.
 */
@Singleton
open class GenericRepositoryImpl<T> : GenericRepository<T>
