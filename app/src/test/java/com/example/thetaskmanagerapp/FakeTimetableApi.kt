package com.example.thetaskmanagerapp

import com.example.thetaskmanagerapp.data.*

class FakeTimetableApi : TimetableApi {
    override suspend fun getTimetable(
        authHeader: String,
        request: TimetableRequest
    ): TimetableResponse {
        return TimetableResponse(
            reservationsList = listOf(
                Reservation(
                    id = "1",
                    subject = "Android Development",
                    description = "Mobile Dev Class",
                    startDate = "2026-04-21T09:00:00",
                    endDate = "2026-04-21T11:00:00"
                )
            )
        )
    }
}