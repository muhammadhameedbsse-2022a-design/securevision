package com.securevision.core.domain.usecase

import com.securevision.core.domain.model.Alert
import com.securevision.core.domain.repository.AlertRepository

class SaveAlertUseCase(private val alertRepository: AlertRepository) {

    /** Persists a single alert and returns its generated ID. */
    suspend operator fun invoke(alert: Alert): Long =
        alertRepository.insertAlert(alert)
}
