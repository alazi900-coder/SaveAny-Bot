package com.krau.saveany.ui.util

import com.krau.saveany.R
import com.krau.saveany.data.api.TaskInfo
import com.krau.saveany.data.api.TaskStatus
import com.krau.saveany.data.api.TaskType

fun TaskStatus.labelRes(): Int = when (this) {
    TaskStatus.QUEUED -> R.string.tasks_status_queued
    TaskStatus.RUNNING -> R.string.tasks_status_running
    TaskStatus.COMPLETED -> R.string.tasks_status_completed
    TaskStatus.FAILED -> R.string.tasks_status_failed
    TaskStatus.CANCELLED -> R.string.tasks_status_cancelled
}

fun TaskType.labelRes(): Int = when (this) {
    TaskType.DIRECTLINKS -> R.string.tt_directlinks
    TaskType.YTDLP -> R.string.tt_ytdlp
    TaskType.ARIA2 -> R.string.tt_aria2
    TaskType.PARSED -> R.string.tt_parseditem
    TaskType.TGFILES -> R.string.tt_tgfiles
    TaskType.TPHPICS -> R.string.tt_tphpics
    TaskType.TRANSFER -> R.string.tt_transfer
}

fun TaskType.descriptionRes(): Int = when (this) {
    TaskType.DIRECTLINKS -> R.string.tt_directlinks_desc
    TaskType.YTDLP -> R.string.tt_ytdlp_desc
    TaskType.ARIA2 -> R.string.tt_aria2_desc
    TaskType.PARSED -> R.string.tt_parseditem_desc
    TaskType.TGFILES -> R.string.tt_tgfiles_desc
    TaskType.TPHPICS -> R.string.tt_tphpics_desc
    TaskType.TRANSFER -> R.string.tt_transfer_desc
}

fun TaskInfo.percentInt(): Int =
    progress?.percent?.let { (it.coerceIn(0.0, 100.0)).toInt() } ?: 0

fun TaskInfo.fraction(): Float =
    progress?.percent?.let { (it / 100.0).toFloat().coerceIn(0f, 1f) } ?: 0f

fun guessTaskTypeFromUrl(url: String): TaskType {
    val u = url.trim().lowercase()
    return when {
        u.startsWith("magnet:") -> TaskType.ARIA2
        u.contains("youtube.com") || u.contains("youtu.be") ||
            u.contains("twitter.com") || u.contains("x.com") ||
            u.contains("tiktok.com") || u.contains("vimeo.com") ||
            u.contains("bilibili.com") || u.contains("instagram.com") ||
            u.contains("twitch.tv") || u.contains("facebook.com") -> TaskType.YTDLP
        u.contains("t.me/") -> TaskType.TGFILES
        u.contains("telegra.ph") -> TaskType.TPHPICS
        else -> TaskType.DIRECTLINKS
    }
}
