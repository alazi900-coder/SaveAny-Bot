package com.krau.saveany.ui.nav

object Routes {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val STORAGES = "storages"
    const val SETTINGS = "settings"

    const val CREATE_TASK_PATTERN = "create?type={type}"
    fun create(type: String?): String = if (type.isNullOrBlank()) "create?type=" else "create?type=$type"

    const val TASK_DETAIL_PATTERN = "task/{id}"
    fun task(id: String): String = "task/$id"
}
