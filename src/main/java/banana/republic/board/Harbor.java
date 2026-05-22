package banana.republic.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import banana.republic.resource.ResourceType;

public class Harbor {

	private static final int DEFAULT_TRADE_RATIO = 4;

	private final int id;
	private final HarborType harborType;
	private final List<Intersection> adjacentIntersections;

	public Harbor(int id, HarborType harborType, List<Intersection> adjacentIntersections) {
		if (harborType == null) {
			throw new IllegalArgumentException("Harbor type cannot be null");
		}
		this.id = id;
		this.harborType = harborType;
		this.adjacentIntersections = copyList(adjacentIntersections);
	}

	public int getId() {
		return id;
	}

	public HarborType getHarborType() {
		return harborType;
	}

	public List<Intersection> getAdjacentIntersections() {
		return Collections.unmodifiableList(adjacentIntersections);
	}

	public int getTradeRatio(ResourceType resource) {
		if (isApplicableFor(resource)) {
			return harborType.getDefaultRatio();
		}
		return DEFAULT_TRADE_RATIO;
	}

	public boolean isApplicableFor(ResourceType resource) {
		if (harborType == HarborType.GENERIC_3TO1) {
			return true;
		}
		if (resource == null) {
			return false;
		}
		return resource.equals(harborType.getSpecificResource());
	}

	private <T> List<T> copyList(List<T> source) {
		if (source == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(source);
	}
}
