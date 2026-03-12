package com.securevision.feature.live.di

import com.securevision.feature.live.analyzer.FrameAnalyzer
import com.securevision.ml.attributes.AttributeClassifier
import com.securevision.ml.face.FaceDetector
import com.securevision.ml.weapon.WeaponDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object LiveModule {

    @Provides
    @ViewModelScoped
    fun provideFaceDetector(): FaceDetector = FaceDetector(confidenceThreshold = 0.7f)

    @Provides
    @ViewModelScoped
    fun provideWeaponDetector(): WeaponDetector = WeaponDetector()

    @Provides
    @ViewModelScoped
    fun provideAttributeClassifier(): AttributeClassifier = AttributeClassifier()

    @Provides
    @ViewModelScoped
    fun provideFrameAnalyzer(
        faceDetector: FaceDetector,
        weaponDetector: WeaponDetector,
        attributeClassifier: AttributeClassifier
    ): FrameAnalyzer = FrameAnalyzer(faceDetector, weaponDetector, attributeClassifier)
}
