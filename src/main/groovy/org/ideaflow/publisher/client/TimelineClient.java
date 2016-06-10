package org.ideaflow.publisher.client;

import org.ideaflow.publisher.api.timeline.TreeTimeline;
import org.openmastery.rest.client.CrudClient;
import org.ideaflow.publisher.api.ResourcePaths;
import org.ideaflow.publisher.api.timeline.BandTimeline;

public class TimelineClient extends CrudClient<Object, TimelineClient> {

	public TimelineClient(String baseUrl) {
		super(baseUrl, ResourcePaths.TIMELINE_PATH, Object.class);
	}

	public BandTimeline getBandTimelineForTask(long taskId) {
		return (BandTimeline) getUntypedCrudClientRequest()
				.path(ResourcePaths.TIMELINE_BAND_PATH)
				.queryParam("taskId", taskId)
				.entity(BandTimeline.class)
				.find();
	}

	public TreeTimeline getTreeTimelineForTask(long taskId) {
		return (TreeTimeline) getUntypedCrudClientRequest()
				.path(ResourcePaths.TIMELINE_TREE_PATH)
				.queryParam("taskId", taskId)
				.entity(TreeTimeline.class)
				.find();
	}

}
