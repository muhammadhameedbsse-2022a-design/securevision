package com.securevision.core.domain.usecase

import com.securevision.core.domain.model.DetectionEvent
import com.securevision.core.domain.repository.DetectionRepository

class SaveDetectionEventUseCase(private val detectionRepository: DetectionRepository) {

    /** Persists a single detection event and returns its generated ID. */
    suspend operator fun invoke(event: DetectionEvent): Long =
        detectionRepository.insertDetectionEvent(event)
}
