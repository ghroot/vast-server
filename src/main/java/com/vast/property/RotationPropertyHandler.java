package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.Transform;
import com.vast.network.Properties;

public class RotationPropertyHandler extends AbstractPropertyHandler<Float, Float> {
	private ComponentMapper<Transform> transformMapper;

	private float rotationThreshold;

	public RotationPropertyHandler(float rotationThreshold) {
		super(Properties.ROTATION);

		this.rotationThreshold = rotationThreshold;
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return transformMapper.has(entity);
	}

	@Override
	protected Float getPropertyData(int entity) {
		return transformMapper.get(entity).rotation;
	}

	@Override
	protected boolean passedThresholdForSync(int entity, Float lastSyncedRotation) {
		Transform transform = transformMapper.get(entity);
		return getAngleDifference(lastSyncedRotation, transform.rotation) >= rotationThreshold;
	}

	private float getAngleDifference(float alpha, float beta) {
		float phi = Math.abs(beta - alpha) % 360;
		return phi > 180.0f ? 360.0f - phi : phi;
	}
}
