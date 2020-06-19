/*
 * Copyright (C) 2018 Taktik SA
 *
 * This file is part of iCureBackend.
 *
 * iCureBackend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * iCureBackend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iCureBackend.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.taktik.icure.asynclogic.impl.filter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.taktik.icure.utils.distinctById
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.taktik.icure.entities.base.Identifiable
import org.taktik.icure.utils.distinct
import java.io.Serializable
import java.util.*

@ExperimentalCoroutinesApi
class Filters : ApplicationContextAware {
    private var applicationContext: ApplicationContext? = null
    private val filters: MutableMap<String, Filter<*, *, *>> = HashMap()

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    fun <T : Serializable, O : Identifiable<T>> resolve(filter: org.taktik.icure.dto.filter.Filter<T, O>) = flow<T> {
        val truncatedFullClassName = filter.javaClass.name.replace(".+?filter\\.".toRegex(), "")
        val filterToBeResolved =
                filters[truncatedFullClassName] as Filter<T, O, org.taktik.icure.dto.filter.Filter<T, O>>?
                        ?: try {
                            ((applicationContext!!.autowireCapableBeanFactory.createBean(
                                    Class.forName("org.taktik.icure.asynclogic.impl.filter.$truncatedFullClassName"),
                                    AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                    false
                            )) as? Filter<T, O, org.taktik.icure.dto.filter.Filter<T, O>>)?.also { filters[truncatedFullClassName] = it }
                        } catch (e: ClassNotFoundException) {
                            throw IllegalStateException(e)
                        }
        val ids = hashSetOf<Serializable>()
        (filterToBeResolved?.resolve(filter, this@Filters)?: throw IllegalStateException("Invalid filter")).collect {
            if (!ids.contains(it)) {
                emit(it)
                ids.add(it)
            }
        }
    }

    class ConstantFilter<T : Serializable, O : Identifiable<T>> : Filter<T, O, org.taktik.icure.dto.filter.Filters.ConstantFilter<T, O>> {
        override fun resolve(filter: org.taktik.icure.dto.filter.Filters.ConstantFilter<T, O>, context: Filters): Flow<T> {
            return filter.getConstant().asFlow()
        }
    }

    @FlowPreview
    class UnionFilter<T : Serializable, O : Identifiable<T>> : Filter<T, O, org.taktik.icure.dto.filter.Filters.UnionFilter<T, O>> {
        override fun resolve(filter: org.taktik.icure.dto.filter.Filters.UnionFilter<T, O>, context: Filters): Flow<T> {
            return filter.getFilters().asFlow().flatMapConcat { context.resolve(it) }
        }
    }

    class IntersectionFilter<T : Serializable, O : Identifiable<T>> : Filter<T, O, org.taktik.icure.dto.filter.Filters.IntersectionFilter<T, O>> {
        override fun resolve(filter: org.taktik.icure.dto.filter.Filters.IntersectionFilter<T, O>, context: Filters): Flow<T> = flow {
            val filters = filter.getFilters()
            val result = mutableSetOf<T>()
            for (i in filters.indices) {
                if (i == 0) {
                    result.addAll(context.resolve(filters[i]).toList())
                } else {
                    result.retainAll(context.resolve(filters[i]).toList())
                }
                result.forEach { emit(it) } // TODO SH MB: not reactive... can be optimized?
            }
        }
    }

    class ComplementFilter<T : Serializable, O : Identifiable<T>> : Filter<T, O, org.taktik.icure.dto.filter.Filters.ComplementFilter<T, O>> {
        override fun resolve(filter: org.taktik.icure.dto.filter.Filters.ComplementFilter<T, O>, context: Filters): Flow<T> = flow {
            val superFlow: Flow<T> = context.resolve(filter.getSuperSet())
            val subList: List<T> = context.resolve(filter.getSubSet()).toList()
            superFlow.collect {
                if (!subList.contains(it)) emit(it)
            }
        }
    }
}