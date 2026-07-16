package com.abrarshakhi.mishti.domain.use_case

import com.abrarshakhi.mishti.domain.model.Conversation
import com.abrarshakhi.mishti.domain.repository.ConversationRepository
import com.abrarshakhi.mishti.presentation.chat.ConversationGroup
import com.abrarshakhi.mishti.presentation.chat.ConversationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

/**
 * Converts a flat list of [Conversation]s into time-bucketed
 * [ConversationGroup]s ready for the drawer UI.
 *
 * Buckets (matching Claude / ChatGPT / Gemini conventions):
 *   • Today
 *   • Yesterday
 *   • Previous 7 days
 *   • Previous 30 days
 *   • Older
 *
 * Empty buckets are omitted so the drawer stays clean.
 */
class GetConversationGroupsUseCase(
    private val repository: ConversationRepository,
) {
    operator fun invoke(): Flow<List<ConversationGroup>> =
        repository.observeAll().map { conversations ->
            bucket(conversations)
        }

    private fun bucket(conversations: List<Conversation>): List<ConversationGroup> {
        val now = Calendar.getInstance()

        val today = now.startOfDay()
        val yesterday = (now.clone() as Calendar).also { it.add(Calendar.DAY_OF_YEAR, -1) }.startOfDay()
        val sevenDaysAgo = (now.clone() as Calendar).also { it.add(Calendar.DAY_OF_YEAR, -7) }.startOfDay()
        val thirtyDaysAgo = (now.clone() as Calendar).also { it.add(Calendar.DAY_OF_YEAR, -30) }.startOfDay()

        val todayItems = mutableListOf<ConversationItem>()
        val yesterdayItems = mutableListOf<ConversationItem>()
        val last7Items = mutableListOf<ConversationItem>()
        val last30Items = mutableListOf<ConversationItem>()
        val olderItems = mutableListOf<ConversationItem>()

        conversations.forEach { conv ->
            val item = conv.toItem()
            when {
                conv.createdAt >= today.timeInMillis       -> todayItems
                conv.createdAt >= yesterday.timeInMillis   -> yesterdayItems
                conv.createdAt >= sevenDaysAgo.timeInMillis  -> last7Items
                conv.createdAt >= thirtyDaysAgo.timeInMillis -> last30Items
                else                                         -> olderItems
            }.add(item)
        }

        return buildList {
            if (todayItems.isNotEmpty())   add(ConversationGroup("Today",            todayItems))
            if (yesterdayItems.isNotEmpty()) add(ConversationGroup("Yesterday",       yesterdayItems))
            if (last7Items.isNotEmpty())   add(ConversationGroup("Previous 7 days",  last7Items))
            if (last30Items.isNotEmpty())  add(ConversationGroup("Previous 30 days", last30Items))
            if (olderItems.isNotEmpty())   add(ConversationGroup("Older",            olderItems))
        }
    }

    private fun Calendar.startOfDay(): Calendar = (clone() as Calendar).also {
        it.set(Calendar.HOUR_OF_DAY, 0)
        it.set(Calendar.MINUTE, 0)
        it.set(Calendar.SECOND, 0)
        it.set(Calendar.MILLISECOND, 0)
    }

    private fun Conversation.toItem() = ConversationItem(id = id, title = title)
}
