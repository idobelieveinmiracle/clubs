package com.rose.clubs.viewmodels.notifications

import com.rose.clubs.data.Club

interface NotificationsModel {
    suspend fun fetchNotifications(): List<NotificationData>
    suspend fun getClubInfo(): Club?
    suspend fun agreeNotification(id: String)
    suspend fun disagreeNotification(id: String)
}